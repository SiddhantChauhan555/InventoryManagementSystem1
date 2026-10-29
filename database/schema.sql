CREATE DATABASE  IF NOT EXISTS `inventory_management` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `inventory_management`;
-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: inventory_management
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Temporary view structure for view `low_stock_products`
--

DROP TABLE IF EXISTS `low_stock_products`;
/*!50001 DROP VIEW IF EXISTS `low_stock_products`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `low_stock_products` AS SELECT 
 1 AS `product_id`,
 1 AS `product_name`,
 1 AS `category`,
 1 AS `stock_quantity`,
 1 AS `low_stock_limit`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `product_sales_summary`
--

DROP TABLE IF EXISTS `product_sales_summary`;
/*!50001 DROP VIEW IF EXISTS `product_sales_summary`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `product_sales_summary` AS SELECT 
 1 AS `product_id`,
 1 AS `product_name`,
 1 AS `units_sold`,
 1 AS `total_revenue`,
 1 AS `sale_transactions`,
 1 AS `average_unit_price`,
 1 AS `largest_sale`,
 1 AS `smallest_sale`*/;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `products`
--

DROP TABLE IF EXISTS `products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `products` (
  `product_id` int NOT NULL AUTO_INCREMENT,
  `product_name` varchar(100) NOT NULL,
  `category` varchar(50) NOT NULL,
  `purchase_price` decimal(10,2) NOT NULL,
  `selling_price` decimal(10,2) NOT NULL,
  `stock_quantity` int NOT NULL DEFAULT '0',
  `low_stock_limit` int NOT NULL DEFAULT '5',
  PRIMARY KEY (`product_id`),
  KEY `idx_product_name` (`product_name`),
  CONSTRAINT `products_chk_1` CHECK ((`purchase_price` >= 0)),
  CONSTRAINT `products_chk_2` CHECK ((`selling_price` >= 0)),
  CONSTRAINT `products_chk_3` CHECK ((`stock_quantity` >= 0)),
  CONSTRAINT `products_chk_4` CHECK ((`low_stock_limit` >= 0))
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `products`
--

LOCK TABLES `products` WRITE;
/*!40000 ALTER TABLE `products` DISABLE KEYS */;
INSERT INTO `products` VALUES (1,'Keyboard','Electronics',400.00,600.00,20,5),(2,'Mouse','Electronics',200.00,350.00,15,5),(3,'Notebook','Stationery',30.00,50.00,70,10),(4,'USB Cable','Accessories',80.00,150.00,13,5),(6,'MousePad','Accessories',500.00,750.00,20,5),(7,'Laptop','Electronics',42000.00,48000.00,12,5),(8,'Wireless Mouse','Accessories',350.00,599.00,40,10),(9,'Mechanical Keyboard','Accessories',1800.00,2499.00,15,5),(10,'USB-C Cable','Accessories',150.00,249.00,30,8),(11,'Monitor 24 Inch','Electronics',8500.00,9999.00,8,3),(12,'Headphones','Electronics',1200.00,1799.00,20,5),(13,'Bluetooth Speaker','Electronics',950.00,1499.00,18,5),(14,'Webcam','Electronics',1600.00,2199.00,10,4),(15,'USB Flash Drive 32GB','Storage',250.00,399.00,50,10),(16,'External SSD 1TB','Storage',5500.00,6999.00,7,3),(17,'Power Bank','Accessories',900.00,1299.00,25,5),(18,'Laptop Stand','Accessories',650.00,999.00,16,5),(19,'Ethernet Cable','Networking',120.00,199.00,35,10),(20,'WiFi Router','Networking',1800.00,2499.00,9,4),(21,'HDMI Cable','Accessories',180.00,299.00,28,8),(22,'Printer','Electronics',6500.00,7999.00,6,3),(23,'Printer Paper Pack','Stationery',220.00,299.00,45,10),(24,'Ink Cartridge','Stationery',700.00,999.00,14,5),(25,'Office Chair','Furniture',4200.00,5499.00,5,3),(26,'Study Table','Furniture',3500.00,4499.00,4,2),(27,'Desk Lamp','Electronics',450.00,699.00,22,5),(28,'Extension Board','Electrical',400.00,649.00,17,5),(29,'Smartphone','Electronics',15000.00,17999.00,8,3),(30,'Tablet','Electronics',11000.00,13499.00,6,3),(31,'Laptop Charger','Accessories',900.00,1399.00,11,4),(32,'Screen Protector','Accessories',80.00,149.00,60,15),(33,'Mobile Cover','Accessories',100.00,199.00,45,10),(34,'Thermal Printer','Electronics',3800.00,4999.00,3,5),(35,'Barcode Scanner','Electronics',2200.00,2999.00,7,3),(36,'UPS 600VA','Electrical',2500.00,3299.00,9,4);
/*!40000 ALTER TABLE `products` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `purchases`
--

DROP TABLE IF EXISTS `purchases`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `purchases` (
  `purchase_id` int NOT NULL AUTO_INCREMENT,
  `product_id` int NOT NULL,
  `supplier_id` int DEFAULT NULL,
  `quantity` int NOT NULL,
  `unit_cost` decimal(10,2) NOT NULL,
  `purchase_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`purchase_id`),
  KEY `product_id` (`product_id`),
  KEY `supplier_id` (`supplier_id`),
  KEY `idx_purchases_purchase_date` (`purchase_date`),
  CONSTRAINT `purchases_ibfk_1` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`),
  CONSTRAINT `purchases_ibfk_2` FOREIGN KEY (`supplier_id`) REFERENCES `suppliers` (`supplier_id`) ON DELETE SET NULL,
  CONSTRAINT `purchases_chk_1` CHECK ((`quantity` > 0)),
  CONSTRAINT `purchases_chk_2` CHECK ((`unit_cost` >= 0))
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `purchases`
--

LOCK TABLES `purchases` WRITE;
/*!40000 ALTER TABLE `purchases` DISABLE KEYS */;
INSERT INTO `purchases` VALUES (1,4,1,5,80.00,'2026-10-26 06:19:59'),(2,4,1,5,80.00,'2026-10-26 20:24:20'),(3,3,2,76,459.00,'2026-10-28 16:39:45');
/*!40000 ALTER TABLE `purchases` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sales`
--

DROP TABLE IF EXISTS `sales`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sales` (
  `sale_id` int NOT NULL AUTO_INCREMENT,
  `product_id` int NOT NULL,
  `quantity` int NOT NULL,
  `unit_price` decimal(10,2) NOT NULL,
  `total_amount` decimal(12,2) GENERATED ALWAYS AS ((`quantity` * `unit_price`)) STORED,
  `sale_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`sale_id`),
  KEY `product_id` (`product_id`),
  KEY `idx_sales_sale_date` (`sale_date`),
  CONSTRAINT `sales_ibfk_1` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`),
  CONSTRAINT `sales_chk_1` CHECK ((`quantity` > 0)),
  CONSTRAINT `sales_chk_2` CHECK ((`unit_price` >= 0))
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sales`
--

LOCK TABLES `sales` WRITE;
/*!40000 ALTER TABLE `sales` DISABLE KEYS */;
INSERT INTO `sales` (`sale_id`, `product_id`, `quantity`, `unit_price`, `sale_date`) VALUES (1,4,3,150.00,'2026-10-26 19:54:03'),(2,4,5,150.00,'2026-10-26 20:24:45'),(3,3,34,50.00,'2026-10-28 16:40:20'),(4,3,5,50.00,'2026-10-28 16:41:02'),(5,3,2,50.00,'2026-10-28 16:41:42');
/*!40000 ALTER TABLE `sales` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `suppliers`
--

DROP TABLE IF EXISTS `suppliers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `suppliers` (
  `supplier_id` int NOT NULL AUTO_INCREMENT,
  `supplier_name` varchar(100) NOT NULL,
  `phone` varchar(15) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`supplier_id`),
  UNIQUE KEY `uq_supplier_email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `suppliers`
--

LOCK TABLES `suppliers` WRITE;
/*!40000 ALTER TABLE `suppliers` DISABLE KEYS */;
INSERT INTO `suppliers` VALUES (1,'ABC Suppliers','9876543210','abc@example.com'),(2,'XYZ Traders','9123456780','xyz@example.com');
/*!40000 ALTER TABLE `suppliers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'inventory_management'
--

--
-- Dumping routines for database 'inventory_management'
--

--
-- Final view structure for view `low_stock_products`
--

/*!50001 DROP VIEW IF EXISTS `low_stock_products`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `low_stock_products` AS select `products`.`product_id` AS `product_id`,`products`.`product_name` AS `product_name`,`products`.`category` AS `category`,`products`.`stock_quantity` AS `stock_quantity`,`products`.`low_stock_limit` AS `low_stock_limit` from `products` where (`products`.`stock_quantity` <= `products`.`low_stock_limit`) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `product_sales_summary`
--

/*!50001 DROP VIEW IF EXISTS `product_sales_summary`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `product_sales_summary` AS select `p`.`product_id` AS `product_id`,`p`.`product_name` AS `product_name`,coalesce(sum(`s`.`quantity`),0) AS `units_sold`,coalesce(sum(`s`.`total_amount`),0.00) AS `total_revenue`,count(`s`.`sale_id`) AS `sale_transactions`,avg(`s`.`unit_price`) AS `average_unit_price`,max(`s`.`total_amount`) AS `largest_sale`,min(`s`.`total_amount`) AS `smallest_sale` from (`products` `p` left join `sales` `s` on((`s`.`product_id` = `p`.`product_id`))) group by `p`.`product_id`,`p`.`product_name` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-30  0:49:12
