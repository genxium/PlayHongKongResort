-- MySQL dump 10.13  Distrib 5.5.33, for Linux (x86_64)
--
-- Host: localhost    Database: hongkongresort
-- ------------------------------------------------------
-- Server version	5.5.33

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
-- Table structure for table `Activity`
--

DROP TABLE IF EXISTS `Activity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Activity` (
  `ActivityId` int(32) NOT NULL AUTO_INCREMENT,
  `ActivityTitle` varchar(32) NOT NULL,
  `ActivityContent` mediumtext,
  `ActivityCreatedTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `ActivityBeginTime` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `ActivityApplicationDeadline` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `ActivityCapacity` int(32) NOT NULL DEFAULT '0',
  `ActivityStatus` int(3) NOT NULL DEFAULT '0',
  PRIMARY KEY (`ActivityId`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Activity`
--

LOCK TABLES `Activity` WRITE;
/*!40000 ALTER TABLE `Activity` DISABLE KEYS */;
INSERT INTO `Activity` VALUES (5,'標題 pikachu','內容','2014-02-18 16:25:46','2013-12-31 16:00:00','2013-12-31 16:00:00',0,3),(6,'我是標題','應該看到很多pikachu！','2014-02-22 16:45:51','2013-12-31 16:00:00','2013-12-31 16:00:00',0,3),(7,'順豐','拉拉啦','2014-04-01 17:15:19','2013-12-31 16:00:00','2013-12-31 16:00:00',0,3),(8,'trial','lalala','2014-04-08 17:23:34','2013-12-31 16:00:00','2013-12-31 16:00:00',0,3),(9,'喵','賣個萌？','2014-04-09 17:34:10','2013-12-31 16:00:00','2013-12-31 16:00:00',0,1);
/*!40000 ALTER TABLE `Activity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ActivityImageRelationTable`
--

DROP TABLE IF EXISTS `ActivityImageRelationTable`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ActivityImageRelationTable` (
  `ActivityImageRelationTableId` int(32) NOT NULL AUTO_INCREMENT,
  `ActivityId` int(32) NOT NULL,
  `ImageId` int(32) NOT NULL,
  `GeneratedTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`ActivityImageRelationTableId`),
  KEY `FK_ActivityId` (`ActivityId`),
  KEY `FK_ImageId` (`ImageId`),
  CONSTRAINT `FK_ActivityId` FOREIGN KEY (`ActivityId`) REFERENCES `Activity` (`ActivityId`),
  CONSTRAINT `FK_ImageId` FOREIGN KEY (`ImageId`) REFERENCES `Image` (`ImageId`)
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ActivityImageRelationTable`
--

LOCK TABLES `ActivityImageRelationTable` WRITE;
/*!40000 ALTER TABLE `ActivityImageRelationTable` DISABLE KEYS */;
INSERT INTO `ActivityImageRelationTable` VALUES (3,5,3,'2014-02-18 16:40:53'),(4,6,4,'2014-02-22 16:46:39'),(5,6,5,'2014-02-22 16:46:39'),(6,6,6,'2014-02-22 16:46:39'),(31,7,31,'2014-04-01 17:15:47'),(32,8,32,'2014-04-08 17:23:55'),(33,9,33,'2014-04-09 17:34:29');
/*!40000 ALTER TABLE `ActivityImageRelationTable` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `CommentOnActivity`
--

DROP TABLE IF EXISTS `CommentOnActivity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `CommentOnActivity` (
  `CommentAId` int(32) NOT NULL AUTO_INCREMENT,
  `CommentAContent` varchar(1024) NOT NULL,
  `CommenterId` int(32) NOT NULL,
  `ActivityId` int(32) NOT NULL,
  `PredecessorId` int(32) NOT NULL,
  `CommentType` int(3) NOT NULL DEFAULT '0',
  `GeneratedTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`CommentAId`),
  KEY `FK_CommenterId` (`CommenterId`),
  KEY `FK_ActivityId` (`ActivityId`),
  CONSTRAINT `CommentOnActivity_ibfk_1` FOREIGN KEY (`CommenterId`) REFERENCES `User` (`UserId`),
  CONSTRAINT `CommentOnActivity_ibfk_2` FOREIGN KEY (`ActivityId`) REFERENCES `Activity` (`ActivityId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `CommentOnActivity`
--

LOCK TABLES `CommentOnActivity` WRITE;
/*!40000 ALTER TABLE `CommentOnActivity` DISABLE KEYS */;
/*!40000 ALTER TABLE `CommentOnActivity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Image`
--

DROP TABLE IF EXISTS `Image`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Image` (
  `ImageId` int(32) NOT NULL AUTO_INCREMENT,
  `ImageAbsolutePath` varchar(128) NOT NULL,
  `ImageURL` varchar(64) NOT NULL,
  PRIMARY KEY (`ImageId`),
  UNIQUE KEY `ImageAbsolutePath` (`ImageAbsolutePath`),
  UNIQUE KEY `ImageURL` (`ImageURL`)
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Image`
--

LOCK TABLES `Image` WRITE;
/*!40000 ALTER TABLE `Image` DISABLE KEYS */;
INSERT INTO `Image` VALUES (3,'/Users/user/play-2.2.0/PlayHongKongResort/public/images/UID12_1392741653975_pikachu03.png','/assets/images/UID12_1392741653975_pikachu03.png'),(4,'/Users/user/play-2.2.0/PlayHongKongResort/public/images/UID12_1393087599431_pikachu01.png','/assets/images/UID12_1393087599431_pikachu01.png'),(5,'/Users/user/play-2.2.0/PlayHongKongResort/public/images/UID12_1393087599476_pikachu02.jpeg','/assets/images/UID12_1393087599476_pikachu02.jpeg'),(6,'/Users/user/play-2.2.0/PlayHongKongResort/public/images/UID12_1393087599480_pikachu03.png','/assets/images/UID12_1393087599480_pikachu03.png'),(31,'/Users/user/play-2.2.2/PlayHongKongResort/public/images/UID12_1396372547817_20120423101701829.jpg','/assets/images/UID12_1396372547817_20120423101701829.jpg'),(32,'/Users/user/play-2.2.2/PlayHongKongResort/public/images/UID21_1396977835541_pikachu04.gif','/assets/images/UID21_1396977835541_pikachu04.gif'),(33,'/Users/user/play-2.2.2/PlayHongKongResort/public/images/UID10_1397064869930_MarvelousSeed.jpg','/assets/images/UID10_1397064869930_MarvelousSeed.jpg');
/*!40000 ALTER TABLE `Image` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `User`
--

DROP TABLE IF EXISTS `User`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `User` (
  `UserId` int(32) NOT NULL AUTO_INCREMENT,
  `UserName` varchar(32) NOT NULL,
  `UserPassword` varchar(32) NOT NULL,
  `UserEmail` varchar(32) NOT NULL,
  `UserGroupId` int(2) NOT NULL DEFAULT '0',
  `UserAuthenticationStatus` int(2) NOT NULL DEFAULT '0',
  `UserGender` int(1) NOT NULL DEFAULT '0',
  `UserLastLoggedInTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `UserAvatar` int(32) NOT NULL DEFAULT '0',
  `UserLastLoggedOutTime` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `UserLastExitTime` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `UserCreatedTime` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`UserId`),
  UNIQUE KEY `UserEmail` (`UserEmail`),
  KEY `UserGroupId` (`UserGroupId`),
  CONSTRAINT `FK_UserGroupId` FOREIGN KEY (`UserGroupId`) REFERENCES `UserGroup` (`GroupId`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `User`
--

LOCK TABLES `User` WRITE;
/*!40000 ALTER TABLE `User` DISABLE KEYS */;
INSERT INTO `User` VALUES (9,'genxium','e10adc3949ba59abbe56e057f20f883e','genxium@hotmail.com',1,0,0,'2013-12-23 14:13:30',0,'0000-00-00 00:00:00','0000-00-00 00:00:00','0000-00-00 00:00:00'),(10,'genxium','e10adc3949ba59abbe56e057f20f883e','genxium@126.com',1,0,0,'2013-12-23 14:45:19',0,'0000-00-00 00:00:00','0000-00-00 00:00:00','0000-00-00 00:00:00'),(11,'kk','e10adc3949ba59abbe56e057f20f883e','kk@gmail.com',1,0,0,'2013-12-27 14:13:30',0,'0000-00-00 00:00:00','0000-00-00 00:00:00','0000-00-00 00:00:00'),(12,'genxium','e10adc3949ba59abbe56e057f20f883e','genxium@gmail.com',1,0,0,'2013-12-28 04:21:15',0,'0000-00-00 00:00:00','0000-00-00 00:00:00','0000-00-00 00:00:00'),(13,'genxium','e10adc3949ba59abbe56e057f20f883e','genxium@yahoo.com',1,0,0,'2013-12-29 08:16:26',0,'0000-00-00 00:00:00','0000-00-00 00:00:00','0000-00-00 00:00:00'),(18,'kk','e10adc3949ba59abbe56e057f20f883e','kk@hotmail.com',1,0,0,'2014-01-03 08:01:30',0,'0000-00-00 00:00:00','0000-00-00 00:00:00','0000-00-00 00:00:00'),(19,'sai','e10adc3949ba59abbe56e057f20f883e','sai@126.com',1,0,0,'2014-01-03 08:12:15',0,'0000-00-00 00:00:00','0000-00-00 00:00:00','0000-00-00 00:00:00'),(20,'yukping','e10adc3949ba59abbe56e057f20f883e','yukping@gmail.com',1,0,0,'2014-01-03 16:42:09',0,'0000-00-00 00:00:00','0000-00-00 00:00:00','0000-00-00 00:00:00'),(21,'admin','c33367701511b4f6020ec61ded352059','admin@hongkongresort.com',3,0,0,'2014-01-07 16:12:42',0,'0000-00-00 00:00:00','0000-00-00 00:00:00','0000-00-00 00:00:00'),(22,'ray','fcea920f7412b5da7be0cf42b8c93759','ray@gmail.com',1,0,0,'2014-01-12 09:11:48',0,'0000-00-00 00:00:00','0000-00-00 00:00:00','0000-00-00 00:00:00');
/*!40000 ALTER TABLE `User` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `UserActivityRelation`
--

DROP TABLE IF EXISTS `UserActivityRelation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `UserActivityRelation` (
  `UserActivityRelationId` int(3) NOT NULL,
  `UserActivityRelationName` varchar(32) NOT NULL,
  PRIMARY KEY (`UserActivityRelationId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `UserActivityRelation`
--

LOCK TABLES `UserActivityRelation` WRITE;
/*!40000 ALTER TABLE `UserActivityRelation` DISABLE KEYS */;
INSERT INTO `UserActivityRelation` VALUES (0,'host'),(1,'applied'),(2,'selected'),(3,'present'),(4,'absent');
/*!40000 ALTER TABLE `UserActivityRelation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `UserActivityRelationTable`
--

DROP TABLE IF EXISTS `UserActivityRelationTable`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `UserActivityRelationTable` (
  `UserActivityRelationTableId` int(32) NOT NULL AUTO_INCREMENT,
  `UserId` int(32) NOT NULL,
  `ActivityId` int(32) NOT NULL,
  `UserActivityRelationId` int(3) NOT NULL,
  `GeneratedTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `LastApplyingTime` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `LastAcceptedTime` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `LastRejectedTime` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`UserActivityRelationTableId`),
  UNIQUE KEY `UA_UNI_ID` (`UserId`,`ActivityId`),
  KEY `UserId` (`UserId`),
  KEY `ActivityId` (`ActivityId`),
  KEY `UserActivityRelationId` (`UserActivityRelationId`),
  CONSTRAINT `UserActivityRelationTable_ibfk_1` FOREIGN KEY (`UserId`) REFERENCES `User` (`UserId`),
  CONSTRAINT `UserActivityRelationTable_ibfk_2` FOREIGN KEY (`ActivityId`) REFERENCES `Activity` (`ActivityId`),
  CONSTRAINT `UserActivityRelationTable_ibfk_3` FOREIGN KEY (`UserActivityRelationId`) REFERENCES `UserActivityRelation` (`UserActivityRelationId`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `UserActivityRelationTable`
--

LOCK TABLES `UserActivityRelationTable` WRITE;
/*!40000 ALTER TABLE `UserActivityRelationTable` DISABLE KEYS */;
INSERT INTO `UserActivityRelationTable` VALUES (5,12,5,0,'2014-02-18 16:25:46','0000-00-00 00:00:00','0000-00-00 00:00:00','0000-00-00 00:00:00'),(6,12,6,0,'2014-02-22 16:45:51','0000-00-00 00:00:00','0000-00-00 00:00:00','0000-00-00 00:00:00'),(7,11,6,1,'2014-03-09 14:40:50','0000-00-00 00:00:00','0000-00-00 00:00:00','0000-00-00 00:00:00'),(8,19,6,2,'2014-03-09 14:40:50','0000-00-00 00:00:00','0000-00-00 00:00:00','0000-00-00 00:00:00'),(9,19,5,2,'2014-03-18 16:20:33','0000-00-00 00:00:00','0000-00-00 00:00:00','0000-00-00 00:00:00'),(10,12,7,0,'2014-04-01 17:15:19','0000-00-00 00:00:00','0000-00-00 00:00:00','0000-00-00 00:00:00'),(11,21,8,0,'2014-04-08 17:23:34','0000-00-00 00:00:00','0000-00-00 00:00:00','0000-00-00 00:00:00'),(12,21,6,1,'2014-04-08 17:24:23','0000-00-00 00:00:00','0000-00-00 00:00:00','0000-00-00 00:00:00'),(13,10,9,0,'2014-04-09 17:34:10','0000-00-00 00:00:00','0000-00-00 00:00:00','0000-00-00 00:00:00');
/*!40000 ALTER TABLE `UserActivityRelationTable` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `UserGroup`
--

DROP TABLE IF EXISTS `UserGroup`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `UserGroup` (
  `GroupId` int(32) NOT NULL,
  `GroupName` varchar(32) NOT NULL,
  PRIMARY KEY (`GroupId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `UserGroup`
--

LOCK TABLES `UserGroup` WRITE;
/*!40000 ALTER TABLE `UserGroup` DISABLE KEYS */;
INSERT INTO `UserGroup` VALUES (0,'visitor'),(1,'user'),(2,'manager'),(3,'admin');
/*!40000 ALTER TABLE `UserGroup` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `UserRelation`
--

DROP TABLE IF EXISTS `UserRelation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `UserRelation` (
  `UserRelationId` int(2) NOT NULL,
  `UserRelationName` varchar(32) NOT NULL,
  PRIMARY KEY (`UserRelationId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `UserRelation`
--

LOCK TABLES `UserRelation` WRITE;
/*!40000 ALTER TABLE `UserRelation` DISABLE KEYS */;
/*!40000 ALTER TABLE `UserRelation` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2014-04-13 20:30:32
