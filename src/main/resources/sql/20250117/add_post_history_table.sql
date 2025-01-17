USE db_heredium;

CREATE TABLE post_history (
    id BIGINT NOT NULL AUTO_INCREMENT,
    post_content TEXT,
    modify_email VARCHAR(255)
);
