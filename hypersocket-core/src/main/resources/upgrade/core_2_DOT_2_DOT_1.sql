DROP ALL FOREIGN KEYS FOR resource_roles;
ALTER TABLE resource_roles ADD CONSTRAINT FK29l6ger5o0n72vfrxf89n6da4 FOREIGN KEY (role_id) REFERENCES roles (resource_id) ON DELETE CASCADE ON UPDATE RESTRICT;
ALTER TABLE resource_roles ADD CONSTRAINT FKgjl6h4ulfnmsjv4ci9r0tuup5 FOREIGN KEY (resource_id) REFERENCES assignable_resources (resource_id) ON DELETE CASCADE ON UPDATE RESTRICT;
