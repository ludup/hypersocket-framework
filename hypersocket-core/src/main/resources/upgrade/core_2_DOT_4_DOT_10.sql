alter table role_realms drop foreign key FK2o9ofbo9uk1u85flhq4ioj7k7;
ALTER TABLE role_realms ADD CONSTRAINT FK2o9ofbo9uk1u85flhq4ioj7k7 FOREIGN KEY (`role_id`) REFERENCES `roles` (`resource_id`) ON DELETE CASCADE;

alter table delegate_roles drop foreign key FK9yqsfnybcn5ty7cvvpnr2r8bo;
ALTER TABLE delegate_roles ADD CONSTRAINT FK9yqsfnybcn5ty7cvvpnr2r8bo FOREIGN KEY (`role_id`) REFERENCES `roles` (`resource_id`) ON DELETE CASCADE;

alter table role_permissions drop foreign key FKn5fotdgk8d1xvo8nav9uv3muc;
ALTER TABLE role_permissions ADD CONSTRAINT FKn5fotdgk8d1xvo8nav9uv3muc FOREIGN KEY (`role_id`) REFERENCES `roles` (`resource_id`) ON DELETE CASCADE;

alter table scheme_denied_roles drop foreign key FKd1vn7v26s0v2ko1gshffsifvb;
ALTER TABLE scheme_denied_roles ADD CONSTRAINT FKd1vn7v26s0v2ko1gshffsifvb FOREIGN KEY (`role_id`) REFERENCES `roles` (`resource_id`) ON DELETE CASCADE;

alter table scheme_allowed_roles drop foreign key FKkpyxgqonkmxbc8v9jt0wga1v2;
ALTER TABLE scheme_allowed_roles ADD CONSTRAINT FKkpyxgqonkmxbc8v9jt0wga1v2 FOREIGN KEY (`role_id`) REFERENCES `roles` (`resource_id`) ON DELETE CASCADE;

alter table resource_roles drop foreign key FK29l6ger5o0n72vfrxf89n6da4;
ALTER TABLE resource_roles ADD CONSTRAINT FK29l6ger5o0n72vfrxf89n6da4 FOREIGN KEY (`role_id`) REFERENCES `roles` (`resource_id`) ON DELETE CASCADE;