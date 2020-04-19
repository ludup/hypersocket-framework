/* The cascade in this case is a @JoinTable, so we always want these to run. */
ALTER TABLE role_realms ADD CONSTRAINT role_realms_cascade_1 FOREIGN KEY (role_id) REFERENCES roles (resource_id) ON DELETE CASCADE;
ALTER TABLE role_realms ADD CONSTRAINT role_realms_cascade_2 FOREIGN KEY (principal_id) REFERENCES realms (resource_id) ON DELETE CASCADE;
ALTER TABLE role_principals ADD CONSTRAINT role_principals_cascade_1 FOREIGN KEY (role_id) REFERENCES roles (resource_id) ON DELETE CASCADE;
ALTER TABLE role_principals ADD CONSTRAINT role_principals_cascade_2 FOREIGN KEY (principal_id) REFERENCES principals (resource_id) ON DELETE CASCADE;

/* These already have foreign keys but with the wrong trigger types. Because we don't know */
/* exactly what the FK names will be, we use this special 'DROP ALL FOREIGN KEYS FOR ..' statement */
/* that the UpgradeService provides and then re-add the ones we want. This is always done */
/* regardless of if a fresh database or not. */

DROP ALL FOREIGN KEYS FOR principals;
ALTER TABLE principals ADD CONSTRAINT principals_cascade_1 FOREIGN KEY (realm_id) REFERENCES realms (resource_id) ON DELETE CASCADE;
ALTER TABLE principals ADD CONSTRAINT principals_cascade_2 FOREIGN KEY (parent_principal) REFERENCES principals (resource_id) ON DELETE CASCADE;
ALTER TABLE principals ADD CONSTRAINT principals_cascade_3 FOREIGN KEY (realm_id) REFERENCES realms (resource_id) ON DELETE CASCADE;
DROP ALL FOREIGN KEYS FOR remote_users;
ALTER TABLE remote_users ADD CONSTRAINT remote_users_cascade_1 FOREIGN KEY (resource_id) REFERENCES principals (resource_id) ON DELETE CASCADE;
DROP ALL FOREIGN KEYS FOR remote_user_groups;
ALTER TABLE remote_user_groups ADD CONSTRAINT remote_user_groups_cascade_1 FOREIGN KEY (uuid) REFERENCES remote_users (resource_id) ON DELETE CASCADE;
ALTER TABLE remote_user_groups ADD CONSTRAINT remote_user_groups_cascade_2 FOREIGN KEY (guid) REFERENCES remote_groups (resource_id) ON DELETE CASCADE;
DROP ALL FOREIGN KEYS FOR remote_groups;
ALTER TABLE remote_groups ADD CONSTRAINT remote_groups_cascade_1 FOREIGN KEY (resource_id) REFERENCES principals (resource_id) ON DELETE CASCADE;
DROP ALL FOREIGN KEYS FOR remote_group_groups;
ALTER TABLE remote_group_groups ADD CONSTRAINT remote_group_groups_cascade_1 FOREIGN KEY (gguid) REFERENCES remote_groups (resource_id) ON DELETE CASCADE;
ALTER TABLE remote_group_groups ADD CONSTRAINT remote_group_groups_cascade_2 FOREIGN KEY (guid) REFERENCES remote_groups (resource_id) ON DELETE CASCADE;
DROP ALL FOREIGN KEYS FOR resource_roles;
ALTER TABLE resource_roles ADD CONSTRAINT resource_roles_cascade_1 FOREIGN KEY (role_id) REFERENCES roles (resource_id) ON DELETE CASCADE ON UPDATE RESTRICT;
ALTER TABLE resource_roles ADD CONSTRAINT resource_roles_cascade_2 FOREIGN KEY (resource_id) REFERENCES assignable_resources (resource_id) ON DELETE CASCADE ON UPDATE RESTRICT;

EXIT IF FRESH;

/* Finally only do these on upgrades (Hibernate will have added the keys on creation, but won't upgrade them */

ALTER TABLE principal_links ADD CONSTRAINT principals_links_cascade_1 FOREIGN KEY (principals_resource_id) REFERENCES principals (resource_id) ON DELETE CASCADE;
ALTER TABLE principal_links ADD CONSTRAINT principals_links_cascade_2 FOREIGN KEY (linkedPrincipals_resource_id) REFERENCES principals (resource_id) ON DELETE CASCADE;
ALTER TABLE password_history ADD CONSTRAINT password_history_cascade_1 FOREIGN KEY (principal_resource_id) REFERENCES principals (resource_id) ON DELETE CASCADE;
ALTER TABLE sessions ADD CONSTRAINT sessions_cascade_1 FOREIGN KEY (principal_id) REFERENCES principals (resource_id) ON DELETE CASCADE;
ALTER TABLE sessions ADD CONSTRAINT sessions_cascade_2 FOREIGN KEY (impersonating_principal_id) REFERENCES principals (resource_id) ON DELETE CASCADE;
ALTER TABLE sessions ADD CONSTRAINT sessions_cascade_3 FOREIGN KEY (current_realm_id) REFERENCES realms (resource_id) ON DELETE CASCADE;
ALTER TABLE sessions ADD CONSTRAINT sessions_cascade_4 FOREIGN KEY (realm_id) REFERENCES realms (resource_id) ON DELETE CASCADE;
ALTER TABLE sessions ADD CONSTRAINT sessions_cascade_5 FOREIGN KEY (authentication_scheme) REFERENCES auth_schemes (resource_id) ON DELETE CASCADE;
ALTER TABLE assignable_resources ADD CONSTRAINT assignable_resources_cascade_1 FOREIGN KEY (realm_id) REFERENCES realms (resource_id) ON DELETE CASCADE;
ALTER TABLE auth_schemes ADD CONSTRAINT auth_schemes_cascade_1 FOREIGN KEY (realm_id) REFERENCES realms (resource_id) ON DELETE CASCADE;
ALTER TABLE batch_processing_items ADD CONSTRAINT batch_processing_items_cascade_1 FOREIGN KEY (realm_id) REFERENCES realms (resource_id) ON DELETE CASCADE;
ALTER TABLE ssl_certificates ADD CONSTRAINT ssl_certificates_cascade_1 FOREIGN KEY (realm_id) REFERENCES realms (resource_id) ON DELETE CASCADE;
ALTER TABLE count_keys ADD CONSTRAINT count_keys_cascade_1 FOREIGN KEY (realm_id) REFERENCES realms (resource_id) ON DELETE CASCADE;
ALTER TABLE email_batch_items ADD CONSTRAINT email_batch_items_cascade_1 FOREIGN KEY (realm_id) REFERENCES realms (resource_id) ON DELETE CASCADE;
ALTER TABLE uploaded_files ADD CONSTRAINT uploaded_files_cascade_1 FOREIGN KEY (realm_id) REFERENCES realms (resource_id) ON DELETE CASCADE;
ALTER TABLE html_templates ADD CONSTRAINT html_templates_cascade_1 FOREIGN KEY (realm_id) REFERENCES realms (resource_id) ON DELETE CASCADE;
ALTER TABLE jobs ADD CONSTRAINT jobs_cascade_1 FOREIGN KEY (realm_id) REFERENCES realms (resource_id) ON DELETE CASCADE;
ALTER TABLE message_resource ADD CONSTRAINT message_resource_cascade_1 FOREIGN KEY (realm_id) REFERENCES realms (resource_id) ON DELETE CASCADE;
ALTER TABLE ous ADD CONSTRAINT ous_cascade_1 FOREIGN KEY (realm_id) REFERENCES realms (resource_id) ON DELETE CASCADE;
ALTER TABLE realm_attribute_categories ADD CONSTRAINT realm_attribute_categories_cascade_1 FOREIGN KEY (realm_id) REFERENCES realms (resource_id) ON DELETE CASCADE;
ALTER TABLE roles ADD CONSTRAINT roles_cascade_1 FOREIGN KEY (realm_id) REFERENCES realms (resource_id) ON DELETE CASCADE;
ALTER TABLE secret_keys ADD CONSTRAINT secret_keys_cascade_1 FOREIGN KEY (realm_id) REFERENCES realms (resource_id) ON DELETE CASCADE;
ALTER TABLE tasks ADD CONSTRAINT tasks_cascade_1 FOREIGN KEY (realm_id) REFERENCES realms (resource_id) ON DELETE CASCADE;
ALTER TABLE interface_state ADD CONSTRAINT interface_state_cascade_1 FOREIGN KEY (realm_id) REFERENCES realms (resource_id) ON DELETE CASCADE;
