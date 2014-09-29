DROP TABLE IF EXISTS `activity_image_relation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `activity_image_relation` (
  `activity_id` int(32) NOT NULL,
  `image_id` int(32) NOT NULL,
  `generated_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY `activity_image_relation_ibfk_1` (`activity_id`),
  KEY `activity_image_relation_ibfk_2` (`image_id`),
  CONSTRAINT `activity_image_relation_ibfk_1` FOREIGN KEY (`activity_id`) REFERENCES `activity` (`id`),
  CONSTRAINT `activity_image_relation_ibfk_2` FOREIGN KEY (`image_id`) REFERENCES `image` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
