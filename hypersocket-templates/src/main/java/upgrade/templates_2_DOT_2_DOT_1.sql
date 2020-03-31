EXIT IF FRESH;
ALTER TABLE templates ADD CONSTRAINT templates_cascade_1 FOREIGN KEY (realm_id) REFERENCES realms (resource_id) ON DELETE CASCADE;


