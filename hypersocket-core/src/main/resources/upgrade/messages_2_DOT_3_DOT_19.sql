ALTER TABLE message_resource DROP FOREIGN KEY message_resource_cascade_1;
ALTER TABLE message_resource DROP FOREIGN KEY message_resource_cascade_2;

ALTER TABLE message_resource ADD CONSTRAINT message_resource_cascade_1 FOREIGN KEY(realm_id) REFERENCES realms(resource_id)  ON DELETE CASCADE ON UPDATE RESTRICT;
ALTER TABLE message_resource ADD CONSTRAINT message_resource_cascade_2 FOREIGN KEY(html_template) REFERENCES html_templates(resource_id)  ON DELETE CASCADE ON UPDATE RESTRICT;