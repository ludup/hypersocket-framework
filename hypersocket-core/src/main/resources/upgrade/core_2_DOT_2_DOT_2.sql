EXIT IF FRESH;
ALTER TABLE session RENAME COLUMN 'system' TO 'system_session';
ALTER TABLE resources RENAME COLUMN 'system' TO 'system_resource';
ALTER TABLE permissions RENAME COLUMN 'system' TO 'system_permission';
