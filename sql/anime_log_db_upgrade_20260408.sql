USE anime_log_db;

ALTER TABLE anime
    ADD COLUMN IF NOT EXISTS source_provider VARCHAR(50) DEFAULT NULL COMMENT '外部来源提供方: bangumi' AFTER synopsis,
    ADD COLUMN IF NOT EXISTS source_subject_id BIGINT DEFAULT NULL COMMENT '外部来源条目ID' AFTER source_provider;

ALTER TABLE anime
    ADD UNIQUE KEY uk_anime_source_subject (source_provider, source_subject_id);