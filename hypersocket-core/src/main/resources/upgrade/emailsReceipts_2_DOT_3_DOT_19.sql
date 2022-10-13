ALTER TABLE email_receipts DROP FOREIGN KEY FKfuc9enuer5v2k5je6dmgpa5ub;
ALTER TABLE email_receipts DROP FOREIGN KEY FKm4u09hevfs28c8y3x1e9rf372;

ALTER TABLE email_receipts ADD CONSTRAINT FKfuc9enuer5v2k5je6dmgpa5ub FOREIGN KEY(principal_resource_id) REFERENCES principals(resource_id) ON DELETE CASCADE ON UPDATE RESTRICT;
ALTER TABLE email_receipts ADD CONSTRAINT FKm4u09hevfs28c8y3x1e9rf372 FOREIGN KEY(tracker_resource_id) REFERENCES email_tracking(resource_id) ON DELETE CASCADE ON UPDATE RESTRICT;