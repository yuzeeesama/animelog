package com.animelog.animelogserver.service.impl;

import com.animelog.animelogserver.config.BangumiApiProperties;
import com.animelog.animelogserver.service.BangumiApiService;
import com.animelog.animelogserver.vo.AnimeVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BangumiApiServiceImpl implements BangumiApiService {
    private static final int SUBJECT_TYPE_ANIME = 2;

    private final ObjectMapper objectMapper;
    private final BangumiApiProperties bangumiApiProperties;

    @Override
    public List<AnimeVO> search(String keyword) {
        JsonNode body = post("/v0/search/subjects", """
                {
                  "keyword": %s,
                  "sort": "rank"
                }
                """.formatted(objectMapper.valueToTree(keyword).toString()));

        List<AnimeVO> results = new ArrayList<>();
        for (JsonNode item : readArray(body, "data", "list")) {
            if (readInt(item, "type") != SUBJECT_TYPE_ANIME) {
                continue;
            }
            results.add(toAnimeVO(item, true));
        }
        return results;
    }

    @Override
    public AnimeVO getDetail(Long subjectId) {
        JsonNode body = get("/v0/subjects/" + subjectId);
        if (readInt(body, "type") != null && readInt(body, "type") != SUBJECT_TYPE_ANIME) {
            throw new IllegalStateException("Bangumi 返回的条目不是动画 subjectId=" + subjectId);
        }
        return toAnimeVO(body, true);
    }

    private JsonNode post(String path, String payload) {
        HttpRequest request = HttpRequest.newBuilder(buildUri(path))
                .timeout(Duration.ofSeconds(20))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("User-Agent", bangumiApiProperties.getUserAgent())
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();
        return send(request);
    }

    private JsonNode get(String path) {
        HttpRequest request = HttpRequest.newBuilder(buildUri(path))
                .timeout(Duration.ofSeconds(20))
                .header("Accept", "application/json")
                .header("User-Agent", bangumiApiProperties.getUserAgent())
                .GET()
                .build();
        return send(request);
    }

    private JsonNode send(HttpRequest request) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Bangumi API 请求失败, status=" + response.statusCode());
            }
            return objectMapper.readTree(response.body());
        } catch (IOException | InterruptedException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new IllegalStateException("Bangumi API 请求失败", ex);
        }
    }

    private URI buildUri(String path) {
        return URI.create(bangumiApiProperties.getBaseUrl() + path);
    }

    private AnimeVO toAnimeVO(JsonNode item, boolean external) {
        AnimeVO vo = new AnimeVO();
        String originalName = readText(item, "name");
        String cnName = readText(item, "name_cn");
        LocalDate airDate = parseDate(readText(item, "date"));

        vo.setName(StringUtils.hasText(cnName) ? cnName : originalName);
        vo.setOriginalName(StringUtils.hasText(originalName) && !originalName.equals(cnName) ? originalName : null);
        vo.setCoverUrl(readText(item.path("images"), "large", "common", "medium", "small"));
        vo.setTotalEpisodes(defaultIfNull(readInt(item, "eps"), 0));
        vo.setType("动画");
        vo.setSourceType("Bangumi API");
        vo.setSourceProvider("bangumi");
        vo.setSourceSubjectId(readLong(item, "id"));
        vo.setExternal(external);
        vo.setAirDate(airDate);
        vo.setReleaseYear(airDate != null ? airDate.getYear() : null);
        vo.setSeason(parseSeason(airDate));
        vo.setSynopsis(readText(item, "summary"));
        vo.setRatingScore(readDecimal(item.path("rating"), "score"));
        return vo;
    }

    private Iterable<JsonNode> readArray(JsonNode root, String... names) {
        for (String name : names) {
            JsonNode node = root.get(name);
            if (node != null && node.isArray()) {
                List<JsonNode> list = new ArrayList<>();
                node.forEach(list::add);
                return list;
            }
        }
        if (root.isArray()) {
            List<JsonNode> list = new ArrayList<>();
            root.forEach(list::add);
            return list;
        }
        return List.of();
    }

    private String readText(JsonNode node, String... names) {
        if (node == null || node.isNull()) {
            return null;
        }
        for (String name : names) {
            JsonNode field = node.get(name);
            if (field != null && field.isValueNode() && StringUtils.hasText(field.asText())) {
                return field.asText().trim();
            }
        }
        return null;
    }

    private Integer readInt(JsonNode node, String... names) {
        if (node == null || node.isNull()) {
            return null;
        }
        for (String name : names) {
            JsonNode field = node.get(name);
            if (field == null || field.isNull()) {
                continue;
            }
            if (field.isInt() || field.isLong()) {
                return field.asInt();
            }
            if (field.isTextual() && StringUtils.hasText(field.asText())) {
                try {
                    return Integer.parseInt(field.asText().trim());
                } catch (NumberFormatException ignored) {
                    // ignore
                }
            }
        }
        return null;
    }

    private Long readLong(JsonNode node, String... names) {
        if (node == null || node.isNull()) {
            return null;
        }
        for (String name : names) {
            JsonNode field = node.get(name);
            if (field == null || field.isNull()) {
                continue;
            }
            if (field.isIntegralNumber()) {
                return field.asLong();
            }
            if (field.isTextual() && StringUtils.hasText(field.asText())) {
                try {
                    return Long.parseLong(field.asText().trim());
                } catch (NumberFormatException ignored) {
                    // ignore
                }
            }
        }
        return null;
    }

    private BigDecimal readDecimal(JsonNode node, String... names) {
        if (node == null || node.isNull()) {
            return null;
        }
        for (String name : names) {
            JsonNode field = node.get(name);
            if (field == null || field.isNull()) {
                continue;
            }
            if (field.isNumber()) {
                return field.decimalValue();
            }
            if (field.isTextual() && StringUtils.hasText(field.asText())) {
                try {
                    return new BigDecimal(field.asText().trim());
                } catch (NumberFormatException ignored) {
                    // ignore
                }
            }
        }
        return null;
    }

    private LocalDate parseDate(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (Exception ex) {
            return null;
        }
    }

    private String parseSeason(LocalDate date) {
        if (date == null) {
            return null;
        }
        int month = date.getMonthValue();
        if (month <= 3) {
            return "冬";
        }
        if (month <= 6) {
            return "春";
        }
        if (month <= 9) {
            return "夏";
        }
        return "秋";
    }

    private Integer defaultIfNull(Integer value, Integer defaultValue) {
        return value == null ? defaultValue : value;
    }
}
