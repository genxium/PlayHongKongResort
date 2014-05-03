-- MySQL dump 10.13  Distrib 5.6.17, for Linux (i686)
--
-- Host: localhost    Database: hongkongweb
-- ------------------------------------------------------
-- Server version	5.6.17

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
  `ActivityBeginTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `ActivityApplicationDeadline` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `ActivityCapacity` int(32) NOT NULL DEFAULT '0',
  `ActivityStatus` int(3) NOT NULL DEFAULT '0',
  PRIMARY KEY (`ActivityId`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Activity`
--

LOCK TABLES `Activity` WRITE;
/*!40000 ALTER TABLE `Activity` DISABLE KEYS */;
INSERT INTO `Activity` VALUES (12,'Test image in product mode','','2014-04-29 16:07:32','2014-01-01 00:00:00','2014-01-01 00:00:00',0,0),(13,'测试1','内容1','2014-04-29 16:23:07','2014-05-02 00:00:00','2014-05-01 00:00:00',0,1);
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
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ActivityImageRelationTable`
--

LOCK TABLES `ActivityImageRelationTable` WRITE;
/*!40000 ALTER TABLE `ActivityImageRelationTable` DISABLE KEYS */;
INSERT INTO `ActivityImageRelationTable` VALUES (13,12,13,'2014-04-29 16:07:32'),(14,12,14,'2014-04-29 16:17:06'),(15,13,15,'2014-04-29 16:23:07');
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
  `PredecessorId` int(32) NOT NULL DEFAULT '-1',
  `CommentType` int(3) NOT NULL DEFAULT '0',
  `GeneratedTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `ParentID` int(32) NOT NULL DEFAULT '-1',
  PRIMARY KEY (`CommentAId`),
  KEY `FK_CommenterId` (`CommenterId`),
  KEY `FK_ActivityId` (`ActivityId`),
  CONSTRAINT `CommentOnActivity_ibfk_1` FOREIGN KEY (`CommenterId`) REFERENCES `User` (`UserId`),
  CONSTRAINT `CommentOnActivity_ibfk_2` FOREIGN KEY (`ActivityId`) REFERENCES `Activity` (`ActivityId`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `CommentOnActivity`
--

LOCK TABLES `CommentOnActivity` WRITE;
/*!40000 ALTER TABLE `CommentOnActivity` DISABLE KEYS */;
INSERT INTO `CommentOnActivity` VALUES (1,'lalala',4,12,-1,0,'2014-04-29 16:17:21',-1);
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
  `ImageURL` varchar(64) NOT NULL,
  PRIMARY KEY (`ImageId`),
  UNIQUE KEY `ImageURL` (`ImageURL`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Image`
--

LOCK TABLES `Image` WRITE;
/*!40000 ALTER TABLE `Image` DISABLE KEYS */;
INSERT INTO `Image` VALUES (13,'/images/UID4_1398787652871_pikachu02.jpeg'),(14,'/images/UID4_1398788226481_MarvelousSeed.jpg'),(20,'/images/UID4_1398788840657_MarvelousSeed.jpg'),(22,'/images/UID4_1398789231932_C37604F0EA82650413CE69A5BC1892D0.jpg'),(15,'/images/UID6_1398788587698_72CEB057D9703BD869FCB1152DCF9DF6.jpg'),(16,'/images/UID6_1398788726841_肉丸05.jpg'),(17,'/images/UID6_1398788729009_肉丸05.jpg'),(18,'/images/UID6_1398788729144_肉丸05.jpg'),(19,'/images/UID6_1398788839871_QQ图片20140220221819.jpg'),(21,'/images/UID6_1398788974973_images.jpg'),(23,'/images/UID6_1398789351662_QQ图片20140224003925.jpg');
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
  `DisplayName` varchar(32) DEFAULT NULL,
  `UserPassword` varchar(32) NOT NULL,
  `UserEmail` varchar(32) NOT NULL,
  `UserGroupId` int(2) NOT NULL DEFAULT '0',
  `UserAuthenticationStatus` int(2) NOT NULL DEFAULT '0',
  `UserGender` int(1) NOT NULL DEFAULT '0',
  `UserLastLoggedInTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `UserAvatar` int(32) NOT NULL DEFAULT '0',
  `UserLastLoggedOutTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `UserLastExitTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `UserCreatedTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `Username` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`UserId`),
  UNIQUE KEY `UserEmail` (`UserEmail`),
  UNIQUE KEY `UserName` (`Username`),
  UNIQUE KEY `Username_2` (`Username`),
  KEY `UserGroupId` (`UserGroupId`),
  CONSTRAINT `FK_UserGroupId` FOREIGN KEY (`UserGroupId`) REFERENCES `UserGroup` (`GroupId`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `User`
--

LOCK TABLES `User` WRITE;
/*!40000 ALTER TABLE `User` DISABLE KEYS */;
INSERT INTO `User` VALUES (1,NULL,'e10adc3949ba59abbe56e057f20f883e','genxium@gmail.com',1,0,0,'2014-04-28 17:37:38',0,'2014-04-28 17:37:38','2014-04-28 17:37:38','2014-04-28 17:37:38','genxium'),(4,NULL,'e10adc3949ba59abbe56e057f20f883e','genxium@hotmail.com',1,0,0,'2014-04-28 17:59:35',22,'2014-04-28 17:59:35','2014-04-28 17:59:35','2014-04-28 17:59:35','genxium1988'),(5,NULL,'c33367701511b4f6020ec61ded352059','admin@hongkongresort.com',3,0,0,'2014-04-28 18:14:22',0,'2014-04-28 18:14:22','2014-04-28 18:14:22','2014-04-28 18:14:22','admin'),(6,NULL,'e10adc3949ba59abbe56e057f20f883e','ray@gmail.com',1,0,0,'2014-04-29 16:20:56',23,'2014-04-29 16:20:56','2014-04-29 16:20:56','2014-04-29 16:20:56','ray');
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
  `LastApplyingTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `LastAcceptedTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `LastRejectedTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
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
INSERT INTO `UserActivityRelationTable` VALUES (12,4,12,0,'2014-04-29 16:07:32','2014-04-29 16:07:32','2014-04-29 16:07:32','2014-04-29 16:07:32'),(13,6,13,0,'2014-04-29 16:23:07','2014-04-29 16:23:07','2014-04-29 16:23:07','2014-04-29 16:23:07');
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

-- Dump completed on 2014-04-29 16:41:20
