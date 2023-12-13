EXIT IF FRESH;
TRY;
alter table http_interfaces drop foreign key `http_interfaces_cascade_1`;
alter table http_interfaces add CONSTRAINT `http_interfaces_cascade_1` FOREIGN KEY (`realm_id`) REFERENCES `realms` (`resource_id`) ON DELETE CASCADE;
CATCH;
