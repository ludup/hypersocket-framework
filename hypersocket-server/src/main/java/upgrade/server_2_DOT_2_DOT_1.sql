EXIT IF FRESH;
ALTER TABLE http_interfaces ADD CONSTRAINT http_interfaces_cascade_1 FOREIGN KEY (realm_id) REFERENCES realms (resource_id) ON DELETE CASCADE;
