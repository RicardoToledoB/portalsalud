CREATE TABLE user_portal_assignments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    support_portal_id BIGINT NOT NULL,
    portal_topic_id BIGINT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_portal_assignments_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_portal_assignments_portal FOREIGN KEY (support_portal_id) REFERENCES support_portals(id),
    CONSTRAINT fk_user_portal_assignments_topic FOREIGN KEY (portal_topic_id) REFERENCES portal_topics(id)
);

CREATE INDEX idx_user_portal_assignments_user ON user_portal_assignments(user_id);
CREATE INDEX idx_user_portal_assignments_portal ON user_portal_assignments(support_portal_id);
CREATE INDEX idx_user_portal_assignments_topic ON user_portal_assignments(portal_topic_id);

INSERT INTO user_portal_assignments (user_id, support_portal_id, portal_topic_id)
SELECT id, support_portal_id, portal_topic_id
FROM users
WHERE role = 'REFERENTE_DSSM'
  AND support_portal_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM user_portal_assignments a
      WHERE a.user_id = users.id
        AND a.support_portal_id = users.support_portal_id
        AND ((a.portal_topic_id IS NULL AND users.portal_topic_id IS NULL) OR a.portal_topic_id = users.portal_topic_id)
  );
