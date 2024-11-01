USE db_heredium;

CREATE TABLE company (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_company_name UNIQUE (name)
);

CREATE TABLE post (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    image_original_file_name VARCHAR(255),
    image_url VARCHAR(255),
    thumbnail_urls VARCHAR(255),
    is_enabled BOOLEAN NOT NULL,
    content_detail TEXT,
    future_exhibition_count INTEGER,
    ongoing_exhibition_count INTEGER,
    completed_exhibition_count INTEGER,
    future_program_count INTEGER,
    ongoing_program_count INTEGER,
    completed_program_count INTEGER,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    admin_id BIGINT NOT NULL,
    created_date DATETIME NOT NULL,
    last_modified_date DATETIME NOT NULL,
    created_name VARCHAR(10) NOT NULL DEFAULT '',
    last_modified_name VARCHAR(10) NOT NULL DEFAULT '',
    PRIMARY KEY (id)
);

CREATE TABLE membership (
    id BIGINT NOT NULL AUTO_INCREMENT,
    post_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    membership_period BIGINT NOT NULL,
    price INTEGER NOT NULL,
    is_enabled BOOLEAN NOT NULL,
    image_url VARCHAR(255),
    created_date DATETIME NOT NULL,
    last_modified_date DATETIME NOT NULL,
    created_name VARCHAR(10) NOT NULL DEFAULT '',
    last_modified_name VARCHAR(10) NOT NULL DEFAULT '',
    PRIMARY KEY (id),
    CONSTRAINT fk_membership_post FOREIGN KEY (post_id) REFERENCES post(id)
);

CREATE TABLE membership_registration (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(100),
    uuid VARCHAR(36) NOT NULL,
    account_id BIGINT NOT NULL,
    membership_id BIGINT,
    company_id BIGINT,
    registration_date DATE,
    expiration_date DATE,
    payment_status VARCHAR(50),
    payment_date DATE,
    payment_order_id VARCHAR(36),
    registration_type VARCHAR(50) NOT NULL,
    price BIGINT,
    created_date DATETIME NOT NULL,
    last_modified_date DATETIME NOT NULL,
    created_name VARCHAR(10) NOT NULL DEFAULT '',
    last_modified_name VARCHAR(10) NOT NULL DEFAULT '',
    PRIMARY KEY (id),
    CONSTRAINT uk_membership_reg_uuid UNIQUE (uuid),
    CONSTRAINT uk_membership_reg_payment_order UNIQUE (payment_order_id),
    CONSTRAINT fk_membership_reg_account FOREIGN KEY (account_id) REFERENCES account(id),
    CONSTRAINT fk_membership_reg_membership FOREIGN KEY (membership_id) REFERENCES membership(id),
    CONSTRAINT fk_membership_reg_company FOREIGN KEY (company_id) REFERENCES company(id)
);

CREATE TABLE coupon (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    discount_percent INTEGER,
    period_in_days INTEGER,
    started_date DATETIME,
    ended_date DATETIME,
    image_url VARCHAR(255),
    number_of_uses BIGINT,
    is_permanent BOOLEAN,
    is_non_membership_coupon BOOLEAN,
    membership_id BIGINT,
    company_id BIGINT,
    from_source VARCHAR(50) NOT NULL,
    created_date DATETIME NOT NULL,
    last_modified_date DATETIME NOT NULL,
    created_name VARCHAR(10) NOT NULL DEFAULT '',
    last_modified_name VARCHAR(10) NOT NULL DEFAULT '',
    PRIMARY KEY (id),
    CONSTRAINT fk_coupon_membership FOREIGN KEY (membership_id) REFERENCES membership(id),
    CONSTRAINT fk_coupon_company FOREIGN KEY (company_id) REFERENCES company(id)
);

CREATE TABLE coupon_usage (
    id BIGINT NOT NULL AUTO_INCREMENT,
    coupon_id BIGINT NOT NULL,
    account_id BIGINT NOT NULL,
    is_used BOOLEAN NOT NULL,
    delivered_date DATETIME NOT NULL,
    used_date DATETIME,
    expiration_date DATETIME NOT NULL,
    uuid VARCHAR(36) NOT NULL,
    used_count BIGINT,
    is_permanent BOOLEAN,
    created_date DATETIME NOT NULL,
    last_modified_date DATETIME NOT NULL,
    created_name VARCHAR(10) NOT NULL DEFAULT '',
    last_modified_name VARCHAR(10) NOT NULL DEFAULT '',
    PRIMARY KEY (id),
    CONSTRAINT uk_coupon_usage_uuid UNIQUE (uuid),
    CONSTRAINT fk_coupon_usage_coupon FOREIGN KEY (coupon_id) REFERENCES coupon(id),
    CONSTRAINT fk_coupon_usage_account FOREIGN KEY (account_id) REFERENCES account(id)
);

CREATE TABLE company_membership_registration_history (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(255),
    start_date VARCHAR(255),
    price VARCHAR(255),
    payment_date VARCHAR(255),
    status VARCHAR(50),
    failed_reason VARCHAR(255),
    account_id BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT fk_company_membership_reg_history_account FOREIGN KEY (account_id) REFERENCES account(id)
);

CREATE INDEX idx_post_admin ON post(admin_id);
CREATE INDEX idx_membership_post ON membership(post_id);
CREATE INDEX idx_membership_registration_account ON membership_registration(account_id);
CREATE INDEX idx_coupon_membership ON coupon(membership_id);
CREATE INDEX idx_coupon_company ON coupon(company_id);
CREATE INDEX idx_coupon_usage_coupon ON coupon_usage(coupon_id);
CREATE INDEX idx_coupon_usage_account ON coupon_usage(account_id);