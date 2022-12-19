ALTER TABLE email_tracking DROP FOREIGN KEY email_tracking_cascade_1;
ALTER TABLE email_tracking DROP FOREIGN KEY email_tracking_cascade_2;

ALTER TABLE email_tracking ADD CONSTRAINT email_tracking_cascade_1 FOREIGN KEY(realm_resource_id) REFERENCES realms(resource_id) ON DELETE CASCADE ON UPDATE RESTRICT;
ALTER TABLE email_tracking ADD CONSTRAINT email_tracking_cascade_2 FOREIGN KEY(principal_resource_id) REFERENCES principals(resource_id) ON DELETE CASCADE ON UPDATE RESTRICT;