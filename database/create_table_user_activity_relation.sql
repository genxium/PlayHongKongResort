DROP TABLE IF EXISTS `user_activity_relation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_activity_relation` (
  `id` int(32) NOT NULL AUTO_INCREMENT,
  `user_id` int(32) NOT NULL,
  `activity_id` int(32) NOT NULL,
  `relation` int(3) NOT NULL,
  `generated_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_applying_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_accepted_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_rejected_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UA_UNI_ID` (`user_id`,`activity_id`),
  KEY `user_activity_relation_ibfk_1` (`user_id`),
  KEY `user_activity_relation_ibfk_2` (`activity_id`),
  KEY `relation_index` (`relation`) USING BTREE,
  CONSTRAINT `user_activity_relation_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `user_activity_relation_ibfk_2` FOREIGN KEY (`activity_id`) REFERENCES `activity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
