-- MySQL dump 10.13  Distrib 5.6.14, for osx10.7 (x86_64)
--
-- Host: localhost    Database: hongkongresort
-- ------------------------------------------------------
-- Server version	5.6.14

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
  `ActivityId` int(20) NOT NULL AUTO_INCREMENT,
  `ActivityTitle` varchar(32) NOT NULL,
  `ActivityContent` varchar(1024) NOT NULL,
  `ActivityCreatedTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `ActivityBeginDate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `ActivityEndDate` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `ActivityCapacity` int(32) NOT NULL DEFAULT '0',
  `ActivityStatus` int(3) NOT NULL DEFAULT '0',
  PRIMARY KEY (`ActivityId`)
) ENGINE=InnoDB AUTO_INCREMENT=46 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Activity`
--

LOCK TABLES `Activity` WRITE;
/*!40000 ALTER TABLE `Activity` DISABLE KEYS */;
INSERT INTO `Activity` VALUES (32,'Testing Edit Accepted Activities','lalala','2013-12-26 11:14:42','2013-12-26 11:14:42','2013-12-28 17:15:32',0,3),(33,'Title 33 updating','Another test case.','2013-12-26 11:54:21','2013-12-26 11:54:21','2013-12-26 16:59:45',0,3),(34,'Edit Test','Tom and Jerry','2013-12-26 11:55:58','2013-12-26 11:55:58','2013-12-28 13:18:27',0,3),(35,'uet','sfdfd','2013-12-26 12:08:17','2013-12-26 12:08:17','2013-12-27 13:50:12',0,3),(36,'Testing','lallalalla','2013-12-26 15:43:20','2013-12-26 15:43:20','2013-12-26 16:40:31',0,3),(38,'mytitle ','lalalal','2013-12-27 14:13:48','2013-12-27 14:13:48','2013-12-27 17:25:45',0,0),(44,'Hey Hey ','Miao Miao','2013-12-29 08:16:50','2013-12-29 08:16:50','2013-12-29 08:17:00',0,0),(45,'Miao','?','2013-12-29 08:18:39','2013-12-29 08:18:39','2013-12-29 08:18:48',0,0);
/*!40000 ALTER TABLE `Activity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `User`
--

DROP TABLE IF EXISTS `User`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `User` (
  `UserId` int(20) NOT NULL AUTO_INCREMENT,
  `UserName` varchar(32) NOT NULL,
  `UserPassword` varchar(32) NOT NULL,
  `UserEmail` varchar(32) NOT NULL,
  `UserGroupId` int(2) NOT NULL DEFAULT '0',
  `UserAuthenticationStatus` int(2) NOT NULL DEFAULT '0',
  `UserGender` int(1) NOT NULL DEFAULT '0',
  `UserLastLoggedInTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`UserId`),
  UNIQUE KEY `UserEmail` (`UserEmail`),
  KEY `UserGroupId` (`UserGroupId`),
  CONSTRAINT `User_ibfk_1` FOREIGN KEY (`UserGroupId`) REFERENCES `UserGroup` (`GroupId`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `User`
--

LOCK TABLES `User` WRITE;
/*!40000 ALTER TABLE `User` DISABLE KEYS */;
INSERT INTO `User` VALUES (9,'genxium','e10adc3949ba59abbe56e057f20f883e','genxium@hotmail.com',0,0,0,'2013-12-23 14:13:30'),(10,'genxium','e10adc3949ba59abbe56e057f20f883e','genxium@126.com',0,0,0,'2013-12-23 14:45:19'),(11,'kk','e10adc3949ba59abbe56e057f20f883e','kk@gmail.com',0,0,0,'2013-12-27 14:13:30'),(12,'genxium','e10adc3949ba59abbe56e057f20f883e','genxium@gmail.com',0,0,0,'2013-12-28 04:21:15'),(13,'genxium','e10adc3949ba59abbe56e057f20f883e','genxium@yahoo.com',0,0,0,'2013-12-29 08:16:26');
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
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
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
  `UserActivityRelationTableId` int(20) NOT NULL AUTO_INCREMENT,
  `UserId` int(20) NOT NULL,
  `ActivityId` int(20) NOT NULL,
  `UserActivityRelationId` int(3) NOT NULL,
  `GeneratedTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`UserActivityRelationTableId`),
  UNIQUE KEY `UA_UNI_ID` (`UserId`,`ActivityId`),
  KEY `UserId` (`UserId`),
  KEY `ActivityId` (`ActivityId`),
  KEY `UserActivityRelationId` (`UserActivityRelationId`),
  CONSTRAINT `UserActivityRelationTable_ibfk_1` FOREIGN KEY (`UserId`) REFERENCES `User` (`UserId`),
  CONSTRAINT `UserActivityRelationTable_ibfk_2` FOREIGN KEY (`ActivityId`) REFERENCES `Activity` (`ActivityId`),
  CONSTRAINT `UserActivityRelationTable_ibfk_3` FOREIGN KEY (`UserActivityRelationId`) REFERENCES `UserActivityRelation` (`UserActivityRelationId`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `UserActivityRelationTable`
--

LOCK TABLES `UserActivityRelationTable` WRITE;
/*!40000 ALTER TABLE `UserActivityRelationTable` DISABLE KEYS */;
INSERT INTO `UserActivityRelationTable` VALUES (10,9,32,0,'2013-12-26 11:14:42'),(11,9,33,0,'2013-12-26 11:54:21'),(12,9,34,0,'2013-12-26 11:55:58'),(13,9,35,0,'2013-12-26 12:08:17'),(14,9,36,0,'2013-12-26 15:43:20'),(16,11,38,0,'2013-12-27 14:13:48'),(22,13,44,0,'2013-12-29 08:16:50'),(23,9,45,0,'2013-12-29 08:18:39');
/*!40000 ALTER TABLE `UserActivityRelationTable` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `UserGroup`
--

DROP TABLE IF EXISTS `UserGroup`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `UserGroup` (
  `GroupId` int(20) NOT NULL,
  `GroupName` varchar(32) NOT NULL,
  PRIMARY KEY (`GroupId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
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
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
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

-- Dump completed on 2013-12-29 16:27:47
