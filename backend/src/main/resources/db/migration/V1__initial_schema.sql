CREATE TABLE users (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, full_name VARCHAR(120) NOT NULL,
 email VARCHAR(254) NOT NULL UNIQUE, password_hash VARCHAR(100) NOT NULL,
 phone VARCHAR(30), role VARCHAR(16) NOT NULL, account_status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 CONSTRAINT chk_users_role CHECK (role IN ('USER','OWNER','ADMIN')),
 CONSTRAINT chk_users_status CHECK (account_status IN ('ACTIVE','SUSPENDED'))
) ENGINE=InnoDB;
CREATE INDEX idx_users_role_status ON users(role,account_status);
CREATE TABLE properties (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, owner_id BIGINT NOT NULL, title VARCHAR(160) NOT NULL,
 description TEXT NOT NULL, property_type VARCHAR(32) NOT NULL, locality VARCHAR(120), city VARCHAR(100) NOT NULL,
 state VARCHAR(100), postal_code VARCHAR(16), monthly_rent DECIMAL(12,2) NOT NULL,
 furnishing VARCHAR(24), occupancy VARCHAR(40), availability_status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
 listing_status VARCHAR(20) NOT NULL DEFAULT 'PENDING', rejection_reason VARCHAR(500),
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 CONSTRAINT fk_properties_owner FOREIGN KEY(owner_id) REFERENCES users(id) ON DELETE RESTRICT,
 CONSTRAINT chk_properties_rent CHECK(monthly_rent >= 0),
 CONSTRAINT chk_properties_listing CHECK(listing_status IN ('PENDING','APPROVED','REJECTED','INACTIVE')),
 CONSTRAINT chk_properties_availability CHECK(availability_status IN ('AVAILABLE','UNAVAILABLE'))
) ENGINE=InnoDB;
CREATE INDEX idx_properties_moderation ON properties(listing_status,availability_status,created_at);
CREATE INDEX idx_properties_city_type ON properties(city,property_type);
CREATE TABLE amenities (id BIGINT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(80) NOT NULL UNIQUE) ENGINE=InnoDB;
CREATE TABLE property_amenities (
 property_id BIGINT NOT NULL, amenity_id BIGINT NOT NULL, PRIMARY KEY(property_id,amenity_id),
 FOREIGN KEY(property_id) REFERENCES properties(id) ON DELETE CASCADE,
 FOREIGN KEY(amenity_id) REFERENCES amenities(id) ON DELETE RESTRICT
) ENGINE=InnoDB;
CREATE TABLE property_images (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, property_id BIGINT NOT NULL, storage_ref VARCHAR(500) NOT NULL,
 original_name VARCHAR(255) NOT NULL, content_type VARCHAR(100) NOT NULL, size_bytes BIGINT NOT NULL,
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 FOREIGN KEY(property_id) REFERENCES properties(id) ON DELETE CASCADE
) ENGINE=InnoDB;
CREATE TABLE bookmarks (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, user_id BIGINT NOT NULL, property_id BIGINT NOT NULL,
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, UNIQUE KEY uq_bookmark_user_property(user_id,property_id),
 FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE,
 FOREIGN KEY(property_id) REFERENCES properties(id) ON DELETE RESTRICT
) ENGINE=InnoDB;
CREATE TABLE inquiries (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, property_id BIGINT NOT NULL, seeker_id BIGINT NOT NULL,
 message TEXT NOT NULL, inquiry_status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 FOREIGN KEY(property_id) REFERENCES properties(id) ON DELETE RESTRICT,
 FOREIGN KEY(seeker_id) REFERENCES users(id) ON DELETE RESTRICT
) ENGINE=InnoDB;
CREATE INDEX idx_inquiries_status_created ON inquiries(inquiry_status,created_at);
CREATE TABLE reviews (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, property_id BIGINT NOT NULL, reviewer_id BIGINT NOT NULL,
 rating TINYINT NOT NULL, body TEXT, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 UNIQUE KEY uq_review_property_reviewer(property_id,reviewer_id),
 CONSTRAINT chk_review_rating CHECK(rating BETWEEN 1 AND 5),
 FOREIGN KEY(property_id) REFERENCES properties(id) ON DELETE RESTRICT,
 FOREIGN KEY(reviewer_id) REFERENCES users(id) ON DELETE RESTRICT
) ENGINE=InnoDB;
CREATE TABLE audit_logs (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, actor_id BIGINT NOT NULL, action VARCHAR(60) NOT NULL,
 entity_type VARCHAR(40) NOT NULL, entity_id BIGINT NOT NULL, metadata_json JSON,
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 FOREIGN KEY(actor_id) REFERENCES users(id) ON DELETE RESTRICT
) ENGINE=InnoDB;
CREATE INDEX idx_audit_created ON audit_logs(created_at);
