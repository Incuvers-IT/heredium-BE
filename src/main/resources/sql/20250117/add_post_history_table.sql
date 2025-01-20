USE db_heredium;

CREATE TABLE post_history (
    id BIGINT NOT NULL AUTO_INCREMENT,
    PRIMARY KEY (id),
    post_content TEXT,
    modify_user_email VARCHAR(255)
);
