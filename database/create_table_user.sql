-- MySQL dump 10.13  Distrib 5.6.22, for Linux (i686)
--
-- Host: localhost    Database: hongkongresort
-- ------------------------------------------------------
-- Server version	5.6.22

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user` (
  `id` int(32) NOT NULL AUTO_INCREMENT,
  `name` varchar(32) DEFAULT NULL,
  `display_name` varchar(32) DEFAULT NULL,
  `password` varchar(32) NOT NULL,
  `email` varchar(32) NOT NULL,
  `group_id` int(2) NOT NULL DEFAULT '0',
  `authentication_status` int(2) NOT NULL DEFAULT '0',
  `gender` int(1) NOT NULL DEFAULT '0',
  `avatar` int(32) NOT NULL DEFAULT '0',
  `created_time` bigint(20) DEFAULT NULL,
  `verification_code` varchar(32) DEFAULT NULL,
  `unread_count` int(32) NOT NULL DEFAULT '0',
  `password_reset_code` varchar(32) DEFAULT NULL,
  `salt` varchar(16) DEFAULT NULL,
  `unassessed_count` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (10,'genxium',NULL,'e10adc3949ba59abbe56e057f20f883e','genxium@gmail.com',1,0,0,0,2147483647,'',1,'3ad92a25ea707b34',NULL,0),(11,'adminsam',NULL,'c33367701511b4f6020ec61ded352059','admin@hongkongresort.com',3,0,0,0,2147483647,'3d17adadbcb2beff',0,NULL,NULL,0),(12,'genxium1988',NULL,'e10adc3949ba59abbe56e057f20f883e','genxium@hotmail.com',0,0,0,0,2147483647,'59b6bf402cbee126',0,NULL,NULL,0),(13,'genxium0430',NULL,'e10adc3949ba59abbe56e057f20f883e','genxium@yahoo.com',0,0,0,0,2147483647,'523b59ae1c79ce50',0,NULL,NULL,0),(14,'raychen',NULL,'e10adc3949ba59abbe56e057f20f883e','saichanjiawei@126.com',0,0,0,0,2147483647,'e39df49da99cbea3',0,NULL,NULL,0),(15,'rayc33',NULL,'e10adc3949ba59abbe56e057f20f883e','saichanjiawei@gmail.com',0,0,0,0,2147483647,'33d6b0100208e1f5',0,NULL,NULL,0),(16,'lafaxiu123',NULL,'e922cfdac77e24bf76c46708b5940195','lafaxiu@126.com',1,0,0,0,2147483647,'',0,NULL,NULL,0),(17,'raychan',NULL,'e10adc3949ba59abbe56e057f20f883e','523027315@qq.com',0,0,0,0,2147483647,'6e6e234913378390',0,NULL,NULL,0),(19,'genxium126',NULL,'b5c936dc0838baeff6db21bc6cd49388','genxium@126.com',1,0,0,0,NULL,'',0,'','c576b41286e9f0e8',0),(20,'iroowe',NULL,'73af5a17f6edc48ce06b4bef2571fdda','a@gmail.com',0,0,0,0,NULL,'99215148d43052b4',0,NULL,'5905f1946f8c09bb',0),(21,'ToTorz',NULL,'9ceb311f4f5e79f5014f9db71fdf3230','xtan124@gmail.com',0,0,0,5,NULL,'1aa2207b46536cfe',0,NULL,'adcd26aade82dfe1',0);
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2015-01-14  4:28:51
