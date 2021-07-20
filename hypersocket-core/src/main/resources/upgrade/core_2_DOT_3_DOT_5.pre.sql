EXIT IF FRESH;
ALTER TABLE `properties` CHANGE `resourceKey` `resourceKey` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL;
ALTER TABLE `properties` CHANGE `value` `value` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL;
ALTER TABLE `properties` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `resources` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `resources` CHANGE `name` `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL;
ALTER TABLE `resources` CHANGE `reference` `reference` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL;
ALTER TABLE `resources` CHANGE `resource_category` `resource_category` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL;