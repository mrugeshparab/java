# Database design

Flyway migrations are the single schema authority (`backend/src/main/resources/db/migration`). MySQL InnoDB, foreign keys, unique email, unique user/property bookmark, and indexed query/filter columns are used. Hibernate schema auto-update is disabled.

Core tables: `users`, `properties`, `amenities`, `property_amenities`, `property_images`, `bookmarks`, `inquiries`, `reviews`, `audit_logs`. Foreign-key deletes are restricted for records with history; property-amenity associations cascade with property deletion. The admin UI deactivates listings instead of deleting them.

`audit_logs` records actor, action, entity, entity id, timestamp, and a small JSON metadata object. It must not contain passwords, tokens, or inquiry text. No destructive reset or sample data is part of startup. Configure a dedicated development database and preserve it across restarts.

Property image rows store storage references, original names, verified content types, byte sizes, and creation time. Image bytes are kept on the configured local upload volume (`CAMPUSNEST_UPLOAD_DIR`); use shared durable storage before deploying multiple application instances.
