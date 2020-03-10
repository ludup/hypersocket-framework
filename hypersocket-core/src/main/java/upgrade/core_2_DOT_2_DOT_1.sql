EXIT IF FRESH;
ALTER TABLE password_history ADD CONSTRAINT password_history_cascade_1 FOREIGN KEY (principal_resource_id) REFERENCES principals (resource_id) ON DELETE CASCADE;
ALTER TABLE sessions ADD CONSTRAINT sessions_cascade_1 FOREIGN KEY (principal_id) REFERENCES principals (resource_id) ON DELETE CASCADE;
ALTER TABLE sessions ADD CONSTRAINT sessions_cascade_2 FOREIGN KEY (impersonating_principal_id) REFERENCES principals (resource_id) ON DELETE CASCADE;
ALTER TABLE sessions ADD CONSTRAINT sessions_cascade_3 FOREIGN KEY (current_realm_id) REFERENCES realms (resource_id) ON DELETE CASCADE;
ALTER TABLE sessions ADD CONSTRAINT sessions_cascade_4 FOREIGN KEY (realm_id) REFERENCES realms (resource_id) ON DELETE CASCADE;
ALTER TABLE sessions ADD CONSTRAINT sessions_cascade_5 FOREIGN KEY (authentication_scheme) REFERENCES auth_schemes (resource_id) ON DELETE CASCADE;