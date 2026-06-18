ALTER TABLE users
    ADD COLUMN portal_topic_id BIGINT NULL AFTER support_portal_id,
    ADD CONSTRAINT fk_users_portal_topic FOREIGN KEY (portal_topic_id) REFERENCES portal_topics(id);

CREATE INDEX idx_users_portal_topic_id ON users(portal_topic_id);
