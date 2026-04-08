package com.animelog.animelogserver.service.impl;

import com.animelog.animelogserver.config.BangumiDataProperties;
import com.animelog.animelogserver.entity.Anime;
import com.animelog.animelogserver.mapper.AnimeMapper;
import com.animelog.animelogserver.service.BangumiDataSyncService;
import com.animelog.animelogserver.vo.AnimeVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class BangumiDataSyncServiceImpl implements BangumiDataSyncService {
    private final ObjectMapper objectMapper;
    private final AnimeMapper animeMapper;
    private final BangumiDataProperties bangumiDataProperties;

    private final Object cacheLock = new Object();
    private volatile List<JsonNode> cachedItems = List.of();
    private volatile Instant cacheAt = Instant.EPOCH;

    @Override
    public void syncByKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return;
        }

        List<JsonNode> matchedItems = matchItems(keyword);

        for (JsonNode item : matchedItems) {
            Anime anime = toAnime(item);
            if (!StringUtils.hasText(anime.getName())) {
                continue;
            }
            syncAnime(anime);
        }

        log.info("bangumi-data 同步完成, keyword={}, matched={}", keyword, matchedItems.size());
    }

    @Override
    public List<AnimeVO> searchFallback(String keyword) {
        syncByKeyword(keyword);
        return animeMapper.searchByKeyword(keyword).stream()
                .map(this::toVO)
                .peek(item -> item.setFallbackSource("bangumi-data"))
                .toList();
    }

    @Override
    public Anime ensureLocalAnimeByKeyword(String keyword) {
        syncByKeyword(keyword);
        List<Anime> animeList = animeMapper.searchByKeyword(keyword);
        return animeList.isEmpty() ? null : animeList.get(0);
    }

    private List<JsonNode> matchItems(String keyword) {
        return loadItems().stream()
                .filter(item -> matchesKeyword(item, keyword))
                .limit(bangumiDataProperties.getSyncLimit())
                .toList();
    }

    private List<JsonNode> loadItems() {
        Instant now = Instant.now();
        if (Duration.between(cacheAt, now).toMinutes() < bangumiDataProperties.getCacheMinutes() && !cachedItems.isEmpty()) {
            return cachedItems;
        }

        synchronized (cacheLock) {
            now = Instant.now();
            if (Duration.between(cacheAt, now).toMinutes() < bangumiDataProperties.getCacheMinutes() && !cachedItems.isEmpty()) {
                return cachedItems;
            }

            cachedItems = extractItems(fetchDataWithFallback());
            cacheAt = now;
            log.info("bangumi-data 缓存刷新成功, count={}", cachedItems.size());
            return cachedItems;
        }
    }

    private JsonNode fetchDataWithFallback() {
        try {
            return fetchRemoteData();
        } catch (Exception ex) {
            log.warn("bangumi-data 远程拉取失败，尝试读取本地兜底文件, url={}", bangumiDataProperties.getUrl(), ex);
            JsonNode localData = readLocalData();
            if (localData != null) {
                log.info("bangumi-data 本地兜底读取成功, resource={}", bangumiDataProperties.getLocalResource());
                return localData;
            }
            throw ex;
        }
    }

    private JsonNode fetchRemoteData() {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        HttpRequest request = HttpRequest.newBuilder(URI.create(bangumiDataProperties.getUrl()))
                .GET()
                .timeout(Duration.ofSeconds(20))
                .header("Accept", "application/json")
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("拉取 bangumi-data 失败, status=" + response.statusCode());
            }
            return objectMapper.readTree(response.body());
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new IllegalStateException("拉取 bangumi-data 失败", e);
        }
    }

    private JsonNode readLocalData() {
        if (!StringUtils.hasText(bangumiDataProperties.getLocalResource())) {
            return null;
        }

        ClassPathResource resource = new ClassPathResource(bangumiDataProperties.getLocalResource());
        if (!resource.exists()) {
            log.warn("bangumi-data 本地兜底文件不存在, resource={}", bangumiDataProperties.getLocalResource());
            return null;
        }

        try (InputStream inputStream = resource.getInputStream()) {
            return objectMapper.readTree(inputStream);
        } catch (IOException ex) {
            throw new IllegalStateException("读取本地 bangumi-data 失败", ex);
        }
    }

    private List<JsonNode> extractItems(JsonNode root) {
        if (root == null || root.isNull()) {
            return List.of();
        }
        if (root.isArray()) {
            List<JsonNode> items = new ArrayList<>();
            root.forEach(items::add);
            return items;
        }

        JsonNode itemsNode = root.get("items");
        if (itemsNode != null && itemsNode.isArray()) {
            List<JsonNode> items = new ArrayList<>();
            itemsNode.forEach(items::add);
            return items;
        }

        JsonNode dataNode = root.get("data");
        if (dataNode != null && dataNode.isArray()) {
            List<JsonNode> items = new ArrayList<>();
            dataNode.forEach(items::add);
            return items;
        }
        return List.of();
    }

    private boolean matchesKeyword(JsonNode item, String keyword) {
        String normalizedKeyword = keyword.trim().toLowerCase(Locale.ROOT);
        return collectSearchTexts(item).stream()
                .map(text -> text.toLowerCase(Locale.ROOT))
                .anyMatch(text -> text.contains(normalizedKeyword));
    }

    private Set<String> collectSearchTexts(JsonNode item) {
        Set<String> texts = new LinkedHashSet<>();
        addText(texts, firstText(item, "title", "name"));
        addText(texts, firstText(item, "summary", "description", "synopsis"));
        collectAllTexts(item.get("titleTranslate"), texts);
        collectAllTexts(item.get("title_translation"), texts);
        return texts;
    }

    private void collectAllTexts(JsonNode node, Set<String> texts) {
        if (node == null || node.isNull()) {
            return;
        }
        if (node.isTextual()) {
            addText(texts, node.asText());
            return;
        }
        if (node.isArray()) {
            node.forEach(child -> collectAllTexts(child, texts));
            return;
        }
        if (node.isObject()) {
            Iterator<JsonNode> elements = node.elements();
            while (elements.hasNext()) {
                collectAllTexts(elements.next(), texts);
            }
        }
    }

    private Anime toAnime(JsonNode item) {
        Anime anime = new Anime();
        String name = firstText(item, "title", "name");
        String begin = firstText(item, "begin", "startDate", "date");

        anime.setName(name);
        anime.setOriginalName(extractOriginalName(item, name));
        anime.setCoverUrl(firstText(item.path("images"), "large", "common", "medium", "small"));
        anime.setTotalEpisodes(parseEpisodeCount(item));
        anime.setType(normalizeType(firstText(item, "type")));
        anime.setSourceType("bangumi-data");
        anime.setReleaseYear(parseYear(begin));
        anime.setSeason(parseSeason(begin));
        anime.setSynopsis(firstText(item, "summary", "description", "synopsis"));
        return anime;
    }

    private void syncAnime(Anime anime) {
        Anime existing = animeMapper.selectByNameAndReleaseYear(anime.getName(), anime.getReleaseYear());
        if (existing == null) {
            animeMapper.insert(anime);
            return;
        }

        anime.setId(existing.getId());
        animeMapper.updateSyncFields(anime);
    }

    private String extractOriginalName(JsonNode item, String name) {
        Set<String> texts = new LinkedHashSet<>();
        collectAllTexts(item.get("titleTranslate"), texts);
        collectAllTexts(item.get("title_translation"), texts);

        return texts.stream()
                .filter(StringUtils::hasText)
                .filter(text -> !text.equals(name))
                .findFirst()
                .orElse(null);
    }

    private Integer parseEpisodeCount(JsonNode item) {
        Integer episodes = firstInt(item, "eps", "episodes", "episodeCount");
        return episodes == null || episodes < 0 ? 0 : episodes;
    }

    private Integer parseYear(String begin) {
        if (!StringUtils.hasText(begin) || begin.length() < 4) {
            return null;
        }
        try {
            return Integer.parseInt(begin.substring(0, 4));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String parseSeason(String begin) {
        if (!StringUtils.hasText(begin) || begin.length() < 7) {
            return null;
        }
        try {
            int month = Integer.parseInt(begin.substring(5, 7));
            if (month >= 1 && month <= 3) {
                return "冬";
            }
            if (month <= 6) {
                return "春";
            }
            if (month <= 9) {
                return "夏";
            }
            return "秋";
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String normalizeType(String type) {
        if (!StringUtils.hasText(type)) {
            return null;
        }
        return switch (type.toLowerCase(Locale.ROOT)) {
            case "tv" -> "TV";
            case "web" -> "WEB";
            case "movie" -> "剧场版";
            case "ova" -> "OVA";
            case "oad" -> "OAD";
            default -> type;
        };
    }

    private String firstText(JsonNode node, String... fieldNames) {
        if (node == null || node.isNull()) {
            return null;
        }
        for (String fieldName : fieldNames) {
            JsonNode fieldNode = node.get(fieldName);
            if (fieldNode != null && fieldNode.isValueNode()) {
                String text = fieldNode.asText();
                if (StringUtils.hasText(text)) {
                    return text.trim();
                }
            }
        }
        return null;
    }

    private Integer firstInt(JsonNode node, String... fieldNames) {
        if (node == null || node.isNull()) {
            return null;
        }
        for (String fieldName : fieldNames) {
            JsonNode fieldNode = node.get(fieldName);
            if (fieldNode == null || fieldNode.isNull()) {
                continue;
            }
            if (fieldNode.isInt() || fieldNode.isLong()) {
                return fieldNode.asInt();
            }
            if (fieldNode.isTextual() && StringUtils.hasText(fieldNode.asText())) {
                try {
                    return Integer.parseInt(fieldNode.asText().trim());
                } catch (NumberFormatException ignored) {
                    // ignore invalid numeric text
                }
            }
        }
        return null;
    }

    private void addText(Set<String> texts, String value) {
        if (StringUtils.hasText(value)) {
            texts.add(value.trim());
        }
    }

    private AnimeVO toVO(Anime anime) {
        AnimeVO vo = new AnimeVO();
        BeanUtils.copyProperties(anime, vo);
        vo.setExternal(false);
        return vo;
    }
}
