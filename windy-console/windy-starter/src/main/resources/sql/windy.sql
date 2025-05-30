/*M!999999\- enable the sandbox mode */
-- ------------------------------------------------------
-- Server version	5.7.44

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*M!100616 SET @OLD_NOTE_VERBOSITY=@@NOTE_VERBOSITY, NOTE_VERBOSITY=0 */;

--
-- Table structure for table `bind_branch`
--

DROP TABLE IF EXISTS `bind_branch`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `bind_branch` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `bind_id` varchar(64) NOT NULL COMMENT '绑定Id',
  `git_branch` varchar(100) NOT NULL COMMENT '绑定分支',
  `git_url` varchar(2000) NOT NULL COMMENT 'git地址',
  `pipeline_id` varchar(64) NOT NULL COMMENT '流水线Id',
  `is_choose` int(2) NOT NULL COMMENT '是否被选中',
  `create_time` bigint(20) NOT NULL COMMENT '创建时间',
  `update_time` bigint(20) DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_pipeline_id` (`pipeline_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bind_branch`
--

LOCK TABLES `bind_branch` WRITE;
/*!40000 ALTER TABLE `bind_branch` DISABLE KEYS */;
INSERT INTO `bind_branch` VALUES
(1926164103127126017,'3e9a9b77d2bf4b05908e0f3ac06bb412','main','https://github.com/languyue/windy-demo.git','e9750851db224383b49a0952d8bf1245',1,1748068283244,1748068285768);
/*!40000 ALTER TABLE `bind_branch` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bug`
--

DROP TABLE IF EXISTS `bug`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `bug` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `bug_id` varchar(100) NOT NULL COMMENT '缺陷Id',
  `bug_name` varchar(100) NOT NULL DEFAULT '' COMMENT '缺陷名称',
  `environment` varchar(100) DEFAULT NULL COMMENT '环境',
  `scene` varchar(1000) NOT NULL DEFAULT '' COMMENT 'bug现象',
  `replay_step` varchar(100) DEFAULT NULL COMMENT '复现步骤',
  `expect_result` varchar(100) DEFAULT NULL COMMENT '预期结果',
  `real_result` varchar(100) DEFAULT NULL COMMENT '实际结果',
  `proposer_name` varchar(100) DEFAULT '' COMMENT '提出的团队',
  `proposer` varchar(100) NOT NULL DEFAULT '' COMMENT '提出人',
  `acceptor_name` varchar(100) DEFAULT NULL COMMENT '接受团队',
  `acceptor` varchar(100) DEFAULT NULL COMMENT '接受人',
  `start_time` bigint(20) DEFAULT NULL COMMENT '开始时间',
  `tags` varchar(100) DEFAULT NULL COMMENT '标签',
  `level` int(11) NOT NULL COMMENT 'bug严重级别',
  `status` int(2) NOT NULL DEFAULT '1' COMMENT 'bug状态',
  `relation_id` varchar(64) DEFAULT NULL COMMENT '需求ID',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(20) DEFAULT NULL COMMENT '更新时间',
  `iteration_id` varchar(64) DEFAULT NULL COMMENT '迭代ID',
  `space_id` varchar(64) NOT NULL DEFAULT '' COMMENT '空间ID',
  `workload` int(4) NOT NULL DEFAULT '0' COMMENT '开发人日',
  PRIMARY KEY (`id`),
  UNIQUE KEY `bug_id` (`bug_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bug`
--

LOCK TABLES `bug` WRITE;
/*!40000 ALTER TABLE `bug` DISABLE KEYS */;
/*!40000 ALTER TABLE `bug` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `build_tool`
--

DROP TABLE IF EXISTS `build_tool`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `build_tool` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `tool_id` varchar(64) NOT NULL DEFAULT '',
  `name` varchar(50) NOT NULL DEFAULT '',
  `type` varchar(10) NOT NULL DEFAULT '',
  `install_path` varchar(100) NOT NULL DEFAULT '',
  `description` varchar(256) DEFAULT NULL,
  `create_time` bigint(20) NOT NULL,
  `update_time` bigint(20) DEFAULT NULL,
  `build_config` varchar(2000) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `build_tool`
--

LOCK TABLES `build_tool` WRITE;
/*!40000 ALTER TABLE `build_tool` DISABLE KEYS */;
INSERT INTO `build_tool` VALUES
(1,'44dd1c32dba94680823838d8475edf49','openjdk-11','Java','/usr/local/openjdk-11',NULL,1731552526809,1731552526809,NULL),
(2,'dbce81f924584ea7a609d739fdd90419','mvn-3.8.8','Maven','/opt/windy-client/maven',NULL,1731649152143,1734933608239,'');
/*!40000 ALTER TABLE `build_tool` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `business_status`
--

DROP TABLE IF EXISTS `business_status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `business_status` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `status_name` varchar(40) DEFAULT NULL COMMENT '状态名称',
  `type` varchar(50) DEFAULT NULL COMMENT '状态类型',
  `sort` int(3) DEFAULT NULL COMMENT '排序',
  `value` int(11) DEFAULT NULL COMMENT '状态值',
  `status_color` varchar(20) DEFAULT NULL COMMENT '状态颜色',
  `operate_type` int(2) DEFAULT '1' COMMENT '状态类型： 1 可变更状态 2 状态不可变更',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `business_status`
--

LOCK TABLES `business_status` WRITE;
/*!40000 ALTER TABLE `business_status` DISABLE KEYS */;
INSERT INTO `business_status` VALUES
(1,'待接受','DEMAND',1,1,NULL,1),
(2,'已接受','DEMAND',2,2,NULL,1),
(3,'研发中','DEMAND',3,3,NULL,1),
(4,'待验收','DEMAND',4,4,NULL,1),
(5,'已发布','DEMAND',5,5,NULL,2),
(6,'已拒绝','DEMAND',6,6,NULL,3),
(7,'待接受','BUG',1,1,'#E4E7ED',1),
(8,'处理中','BUG',2,2,'#409EFF',1),
(9,'测试中','BUG',3,3,'#409EFF',1),
(10,'测试驳回','BUG',4,4,'#E6A23C',1),
(11,'测试通过','BUG',5,5,'#67C23A',1),
(12,'已发布','BUG',6,6,'#67C23A',2),
(13,'未开始','ITERATION',1,1,'#909399',1),
(14,'进行中','ITERATION',2,2,'#409EFF',1),
(15,'已关闭','ITERATION',3,3,'#F56C6C',3),
(16,'已完成','ITERATION',4,4,'#67C23A',2),
(17,'个人定制','DEMAND_TAG',1,1,'#F56C6C',1),
(18,'业务需求','DEMAND_TAG',2,2,'#67C23A',1),
(19,'延期需求','DEMAND_TAG',3,3,'#E6A23C',1),
(20,'待开始','WORK',1,1,NULL,1),
(21,'开发中','WORK',2,2,NULL,1),
(22,'已完成','WORK',3,3,NULL,2),
(23,'暂停','WORK',4,4,NULL,1),
(24,'客户体验','DEMAND_VALUE',1,1,NULL,1),
(25,'功能优化','DEMAND_VALUE',2,2,NULL,1),
(26,'功能修复','DEMAND_VALUE',3,3,NULL,1),
(27,'性能','DEMAND_VALUE',4,4,NULL,1),
(28,'产品稳定性','DEMAND_VALUE',5,5,NULL,1);
/*!40000 ALTER TABLE `business_status` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `code_change`
--

DROP TABLE IF EXISTS `code_change`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `code_change` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `change_id` varchar(64) NOT NULL COMMENT '变更Id',
  `change_name` varchar(50) DEFAULT NULL COMMENT '变更名称',
  `change_desc` varchar(500) DEFAULT NULL COMMENT '变更描述',
  `change_branch` varchar(500) NOT NULL COMMENT '变更分支',
  `relation_id` varchar(100) DEFAULT NULL COMMENT '关联ID 每次的变更触发可以与需求或者是bug或者是一个优化项关联，通过这个关联的ID就可以在后续的代码工作中串联起来 达到观察工作流的作用',
  `service_id` varchar(100) NOT NULL COMMENT '服务Id',
  `relation_type` int(3) DEFAULT NULL COMMENT '关联类型(需求、缺陷、工作项)',
  `relation_name` varchar(100) DEFAULT NULL COMMENT '关联资源名称',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(20) DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_change_id` (`change_id`),
  UNIQUE KEY `uniq_service_id_change_branch` (`service_id`,`change_branch`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `code_change`
--

LOCK TABLES `code_change` WRITE;
/*!40000 ALTER TABLE `code_change` DISABLE KEYS */;
INSERT INTO `code_change` VALUES
(1926152402373754881,'feae92bc8d354446acef26ef32002fd7','测试创建变更','创建的变更的同时会在git上创建对应的开发分支','test_code_branch','bdce59df46254d428ea26d607a051edb','d738e2841bc041a38cab9d3179dd0434',1,'默认演示需求',1748065493562,1748065493562);
/*!40000 ALTER TABLE `code_change` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `comment`
--

DROP TABLE IF EXISTS `comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `comment` (
  `id` bigint(11) unsigned NOT NULL AUTO_INCREMENT,
  `comment_id` varchar(64) DEFAULT NULL COMMENT '评论ID',
  `relative_id` varchar(64) DEFAULT NULL COMMENT '评论关联的ID',
  `comment` varchar(1000) DEFAULT NULL COMMENT '评论的内容',
  `user_id` varchar(64) DEFAULT NULL COMMENT '评论的用户ID',
  `user_name` varchar(40) DEFAULT NULL COMMENT '用户名称',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(20) DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `comment`
--

LOCK TABLES `comment` WRITE;
/*!40000 ALTER TABLE `comment` DISABLE KEYS */;
/*!40000 ALTER TABLE `comment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `demand`
--

DROP TABLE IF EXISTS `demand`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `demand` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `demand_id` varchar(64) DEFAULT NULL COMMENT '需求Id',
  `demand_name` varchar(50) DEFAULT NULL COMMENT '需求名称',
  `demand_content` varchar(1000) DEFAULT NULL COMMENT '需求内容',
  `customer_value` varchar(40) DEFAULT NULL COMMENT '客户价值',
  `proposer_name` varchar(64) NOT NULL DEFAULT '' COMMENT '需求提出人名称',
  `proposer` varchar(64) NOT NULL DEFAULT '' COMMENT '需求提出人ID',
  `acceptor_name` varchar(64) DEFAULT NULL COMMENT '需求接受人名称',
  `acceptor` varchar(64) DEFAULT NULL COMMENT '需求接受人ID',
  `accept_time` bigint(20) DEFAULT NULL COMMENT '接受时间',
  `status` int(3) NOT NULL COMMENT '需求状态',
  `level` int(3) DEFAULT NULL COMMENT '需求级别',
  `expect_time` bigint(20) NOT NULL COMMENT '期待完成时间',
  `start_time` bigint(20) DEFAULT NULL COMMENT '需求开始时间',
  `workload` int(4) DEFAULT '0' COMMENT '工作量(人日)',
  `tag` varchar(40) DEFAULT NULL COMMENT '需求标签',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(20) DEFAULT NULL COMMENT '修改时间',
  `iteration_id` varchar(64) DEFAULT NULL COMMENT '迭代ID',
  `space_id` varchar(64) DEFAULT NULL COMMENT '空间ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_demand_id` (`demand_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `demand`
--

LOCK TABLES `demand` WRITE;
/*!40000 ALTER TABLE `demand` DISABLE KEYS */;
INSERT INTO `demand` VALUES
(1926152347965243393,'bdce59df46254d428ea26d607a051edb','默认演示需求','创建测试需求为满足基本功能演示','客户体验','管理员','d595a3d55b4e47978dd25cb8acb3e753','管理员','d595a3d55b4e47978dd25cb8acb3e753',NULL,1,1,1748448000000,NULL,10,'1',1748065480591,1748065480591,NULL,'b973c0dd53844fc5b241acf79d44e798');
/*!40000 ALTER TABLE `demand` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dispatch_log`
--

DROP TABLE IF EXISTS `dispatch_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `dispatch_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `log_id` varchar(64) NOT NULL COMMENT '日志Id',
  `log_type` int(11) NOT NULL COMMENT '任务类型 1 pipeline  2 feature',
  `source_id` varchar(320) NOT NULL DEFAULT '' COMMENT '任务日志触发源',
  `source_name` varchar(100) DEFAULT NULL COMMENT '触发源名称',
  `node_ip` varchar(100) DEFAULT NULL COMMENT '任务执行master节点Ip',
  `log_status` int(11) NOT NULL DEFAULT '1' COMMENT '任务日志状态',
  `lock_version` int(11) DEFAULT NULL COMMENT '乐观锁版本号',
  `source_record_id` varchar(100) DEFAULT NULL COMMENT '任务来源记录id',
  `create_time` bigint(20) NOT NULL COMMENT '创建时间',
  `update_time` bigint(20) NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_log_id` (`log_id`),
  KEY `idx_source_id` (`source_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dispatch_log`
--

LOCK TABLES `dispatch_log` WRITE;
/*!40000 ALTER TABLE `dispatch_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `dispatch_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `environment`
--

DROP TABLE IF EXISTS `environment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `environment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `env_id` varchar(64) NOT NULL COMMENT '环境Id',
  `env_name` varchar(50) NOT NULL COMMENT '环境名称',
  `env_status` int(11) DEFAULT NULL COMMENT '环境状态 1 正常 2 暂停 3 已删除',
  `env_type` int(11) NOT NULL DEFAULT '1' COMMENT '1 ssh 2 k8s 3 docker',
  `env_params` text COMMENT '环境相关配置',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(20) DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_env_name` (`env_name`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `environment`
--

LOCK TABLES `environment` WRITE;
/*!40000 ALTER TABLE `environment` DISABLE KEYS */;
INSERT INTO `environment` VALUES
(1926190972312612865,'50ec1db3137d43b49fc7a1f3b447475f','本地演示环境-测试时请切换真实IP',1,1,'{\"sshIp\":\"192.168.1.7\",\"sshPort\":\"22\",\"user\":\"guyuelan\",\"password\":\"zj137899\",\"remotePath\":\"/opt/windy\"}',1748074689352,1748074721973);
/*!40000 ALTER TABLE `environment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `execute_point`
--

DROP TABLE IF EXISTS `execute_point`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `execute_point` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `point_id` varchar(64) NOT NULL COMMENT '用例Id',
  `execute_type` int(11) DEFAULT '1' COMMENT '执行类型',
  `feature_id` varchar(64) NOT NULL COMMENT '用例Id',
  `description` varchar(200) DEFAULT NULL COMMENT '执行点描述',
  `feature_info` text NOT NULL COMMENT '特性运行信息',
  `compare_define` text COMMENT '特性执行结果比较',
  `variables` varchar(1000) DEFAULT NULL COMMENT '执行响应结果参数',
  `test_stage` int(11) NOT NULL COMMENT '用例阶段',
  `sort_order` int(11) DEFAULT NULL COMMENT '排序',
  `template_id` varchar(64) DEFAULT NULL COMMENT '模版Id',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(20) DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_point_id` (`point_id`),
  KEY `idx_feature_id` (`feature_id`),
  KEY `idx_template_id` (`template_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `execute_point`
--

LOCK TABLES `execute_point` WRITE;
/*!40000 ALTER TABLE `execute_point` DISABLE KEYS */;
INSERT INTO `execute_point` VALUES
(1926167393361588225,'b968e660f78045a89fa049c138e1b2d8',1,'4f50da32ce58404ca0b9a3ff2e90c5f2','hello接口','{\"description\":\"hello接口\",\"headers\":{},\"invokeType\":2,\"method\":\"GET\",\"name\":\"hello接口\",\"params\":[{\"description\":\"request domain address\",\"initData\":{\"data\":\"${host}\"},\"paramKey\":\"host\",\"position\":\"Path\",\"type\":\"String\",\"value\":\"${host}\"}],\"service\":\"${host}/hello\"}','null','null',2,0,'81c6894170554959800ae5a9e69015f8',1748069067695,1748069067695),
(1926167592834297857,'a50a100c82dc472d8cf2c2ef94b377e2',1,'b8e92c13c9b64952a44e9dc8c88230c1','message测试接口','{\"description\":\"message测试接口\",\"headers\":{},\"invokeType\":2,\"method\":\"POST\",\"name\":\"message接口\",\"params\":[{\"description\":\"用户名称\",\"initData\":{},\"paramKey\":\"name\",\"position\":\"Body\",\"type\":\"String\",\"value\":\"guyuelan\"},{\"description\":\"request domain address\",\"initData\":{\"data\":\"${host}\"},\"paramKey\":\"host\",\"position\":\"Path\",\"type\":\"String\",\"value\":\"${host}\"}],\"service\":\"${host}/message\"}','null','null',2,0,'1488e0c2186545f49350b2b93ee3f493',1748069115267,1748069115267),
(1926167716813729793,'943b9534d8894dac87ef76115ebe645b',1,'068404ba74554f7faa00999f58ab3f4a','message测试接口','{\"description\":\"message测试接口\",\"headers\":{},\"invokeType\":2,\"method\":\"POST\",\"name\":\"message接口\",\"params\":[{\"description\":\"用户名称\",\"initData\":{},\"paramKey\":\"name\",\"position\":\"Body\",\"type\":\"String\"},{\"description\":\"request domain address\",\"initData\":{\"data\":\"${host}\"},\"paramKey\":\"host\",\"position\":\"Path\",\"type\":\"String\",\"value\":\"${host}\"}],\"service\":\"${host}/message\"}','null','null',2,0,'1488e0c2186545f49350b2b93ee3f493',1748069144816,1748069144816);
/*!40000 ALTER TABLE `execute_point` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `execute_record`
--

DROP TABLE IF EXISTS `execute_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `execute_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `execute_record_id` varchar(64) DEFAULT NULL COMMENT '执行点记录',
  `history_id` varchar(64) DEFAULT NULL COMMENT '历史记录ID',
  `status` int(2) DEFAULT NULL COMMENT '执行点运行状态',
  `execute_result` text COMMENT '执行结果',
  `execute_point_name` varchar(100) DEFAULT NULL COMMENT '执行点名称',
  `execute_point_id` varchar(64) DEFAULT NULL COMMENT '执行点ID',
  `execute_type` int(2) DEFAULT NULL COMMENT '执行类型',
  `test_stage` int(2) DEFAULT NULL COMMENT '测试阶段',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(20) DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_history_id` (`history_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `execute_record`
--

LOCK TABLES `execute_record` WRITE;
/*!40000 ALTER TABLE `execute_record` DISABLE KEYS */;
/*!40000 ALTER TABLE `execute_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `execute_template`
--

DROP TABLE IF EXISTS `execute_template`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `execute_template` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `template_id` varchar(64) NOT NULL COMMENT '特性id',
  `template_type` int(2) DEFAULT '1' COMMENT '模版类型',
  `service` varchar(500) DEFAULT NULL COMMENT '特性执行类名',
  `method` varchar(100) DEFAULT NULL COMMENT '特性执行方法名',
  `name` varchar(100) DEFAULT NULL COMMENT '特性执行名称',
  `owner` varchar(64) DEFAULT NULL COMMENT '模版拥有者',
  `description` varchar(100) DEFAULT NULL COMMENT '特性描述',
  `source` varchar(100) DEFAULT NULL COMMENT '特性原路径',
  `param` text COMMENT '模版参数',
  `invoke_type` int(2) DEFAULT NULL COMMENT '执行调用方式 1 本地方法  2 Http',
  `header` text COMMENT 'invoke_type为http时，请求Header',
  `related_id` varchar(64) DEFAULT NULL COMMENT '关联的模版的Id',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(20) DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `execute_template`
--

LOCK TABLES `execute_template` WRITE;
/*!40000 ALTER TABLE `execute_template` DISABLE KEYS */;
INSERT INTO `execute_template` VALUES
(1,'71a26960299f49c3aa26b687ea2fbdb2',2,NULL,'','for循环','','循环查询',NULL,NULL,NULL,NULL,NULL,1670927202853,1670927202853),
(2,'71a26960299f49c3aa26b687ea2fbdb3',3,NULL,'','if判断','','条件执行',NULL,NULL,NULL,NULL,NULL,1670927202853,1670927202853),
(3,'71a26960299f49c3aa26b687ea2fbdba',4,'com.zj.client.handler.feature.ability.mysql.MysqlFeature','executeQuery','mysql查询','','执行mysql语句',NULL,'[{\"defaultValue\":{\"defaultValue\":\"\"},\"description\":\"mysql连接地址\",\"paramKey\":\"connect\",\"type\":\"String\"},{\"defaultValue\":{\"defaultValue\":\"\"},\"description\":\"数据库名称\",\"paramKey\":\"dbName\",\"type\":\"String\"},{\"defaultValue\":{\"defaultValue\":\"\"},\"description\":\"数据库用户\",\"paramKey\":\"user\",\"type\":\"String\"},{\"defaultValue\":{\"defaultValue\":\"\"},\"description\":\"用户密码\",\"paramKey\":\"password\",\"type\":\"String\"},{\"defaultValue\":{\"defaultValue\":\"\"},\"description\":\"执行的sql\",\"paramKey\":\"sql\",\"type\":\"String\"}]',1,'{}',NULL,1670927202853,1692542168926),
(4,'f18a546c54fe4f70bae3a0b529e46908',4,'com.zj.client.handler.feature.ability.http.HttpFeature','startHttp','Http请求','','简单的http请求',NULL,'[{\"defaultValue\":{\"defaultValue\":\"\"},\"description\":\"请求的url\",\"paramKey\":\"url\",\"type\":\"String\"},{\"defaultValue\":{\"defaultValue\":\"\",\"range\":[\"GET\",\"POST\",\"PUT\",\"DELETE\"]},\"description\":\"请求的Http方法\",\"paramKey\":\"method\",\"type\":\"Array\"},{\"defaultValue\":{\"defaultValue\":\"\"},\"description\":\"http请求的header\",\"paramKey\":\"headers\",\"type\":\"Map\"},{\"defaultValue\":{\"defaultValue\":\"\"},\"description\":\"请求的body内容\",\"paramKey\":\"body\",\"type\":\"String\"}]',1,NULL,NULL,1671003944283,1675230014305),
(5,'9dcb1e34d902487fa2e5698a14dbb552',4,'com.zj.client.handler.feature.ability.kafka.KafkaFeature','startConsume','kafka消费','','消费kafka消息',NULL,'[{\"description\":\"kafka地址ip:port格式\",\"paramKey\":\"address\",\"type\":\"String\"},{\"description\":\"kafka消费topic\",\"paramKey\":\"topic\",\"type\":\"String\"},{\"description\":\"kafka消费群组\",\"paramKey\":\"group\",\"type\":\"String\"}]',1,NULL,NULL,1673421517596,1673421517596),
(6,'ef4fa4c63cc8488c801590eca36580d4',4,'com.zj.client.handler.feature.ability.kafka.KafkaFeature','produceMessage','发送Kafak消息','','发送kafka消息',NULL,'[{\"description\":\"发送的topic\",\"paramKey\":\"topic\",\"type\":\"String\"},{\"description\":\"发送消息的key\",\"paramKey\":\"key\",\"type\":\"String\"},{\"description\":\"发送kafka消息内容\",\"paramKey\":\"value\",\"type\":\"String\"},{\"description\":\"发送超时时间，单位秒\",\"paramKey\":\"timeout\",\"type\":\"Integer\"},{\"description\":\"kafka地址格式ip:port\",\"paramKey\":\"address\",\"type\":\"String\"}]',1,NULL,NULL,1673422627042,1673423118348),
(7,'d9329caeea7340f4bec38d5727e521c8',4,'com.zj.client.handler.feature.ability.redis.RedisFeature','setValue','Redis设置Value','','设置redis值',NULL,'[{\"description\":\"redis实例IP\",\"paramKey\":\"ip\",\"type\":\"String\"},{\"description\":\"redis实例端口\",\"paramKey\":\"port\",\"type\":\"Integer\"},{\"description\":\"设置Key\",\"paramKey\":\"key\",\"type\":\"String\"},{\"description\":\"设置value\",\"paramKey\":\"value\",\"type\":\"String\"},{\"description\":\"超时时间\",\"paramKey\":\"timeout\",\"type\":\"Integer\"}]',1,NULL,NULL,1673431790811,1673431790811),
(8,'a145661e75e040d0bb7be75fedf6c60e',4,'com.zj.client.handler.feature.ability.redis.RedisFeature','getValue','获取Redis值','','获取redis值',NULL,'[{\"description\":\"redis实例ip\",\"paramKey\":\"ip\",\"type\":\"String\"},{\"description\":\"redis实例端口\",\"paramKey\":\"port\",\"type\":\"Integer\"},{\"description\":\"key\",\"paramKey\":\"key\",\"type\":\"String\"}]',1,NULL,NULL,1673431883618,1673431883618),
(9,'a145661e75e040d0bb7be75fed678tyh',4,'com.zj.client.handler.feature.ability.wait.WaitInvoker','waitTimeout','等待执行','','等待执行',NULL,'[{\"description\":\"等待时间，单位秒\",\"paramKey\":\"timeout\",\"type\":\"Long\"}]',1,NULL,NULL,1673431883618,1673431883618),
(10,'71a26960299f49c3aa26b687ea226626',6,NULL,'','js代码段','','自定义动态参数',NULL,NULL,NULL,NULL,NULL,1670927202853,1670927202853),
(11,'71a26960299f49c3aa26b687ea2fbdyy',7,NULL,'','异步线程','','异步执行',NULL,NULL,NULL,NULL,NULL,1670927202853,1670927202853),
(1926166724202328065,'81c6894170554959800ae5a9e69015f8',1,'${host}/hello','GET','hello接口','d738e2841bc041a38cab9d3179dd0434','hello接口',NULL,'[{\"description\":\"request domain address\",\"initData\":{\"data\":\"${host}\"},\"paramKey\":\"host\",\"position\":\"Path\",\"type\":\"String\"}]',2,'{}',NULL,1748068908137,1748068908137),
(1926166724202328066,'1488e0c2186545f49350b2b93ee3f493',1,'${host}/message','POST','message接口','d738e2841bc041a38cab9d3179dd0434','message测试接口',NULL,'[{\"description\":\"用户名称\",\"initData\":{},\"paramKey\":\"name\",\"position\":\"Body\",\"type\":\"String\"},{\"description\":\"request domain address\",\"initData\":{\"data\":\"${host}\"},\"paramKey\":\"host\",\"position\":\"Path\",\"type\":\"String\"}]',2,'{}',NULL,1748068908138,1748068908138);
/*!40000 ALTER TABLE `execute_template` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `feature_history`
--

DROP TABLE IF EXISTS `feature_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `feature_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `history_id` varchar(100) NOT NULL COMMENT '历史Id',
  `feature_id` varchar(64) NOT NULL DEFAULT '' COMMENT '用例ID',
  `feature_name` varchar(100) DEFAULT NULL COMMENT '用例名称',
  `record_id` varchar(100) DEFAULT NULL COMMENT '执行记录Id',
  `execute_status` int(2) DEFAULT NULL COMMENT '执行状态',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(20) DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_record_id` (`record_id`),
  KEY `idx_history_id` (`history_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `feature_history`
--

LOCK TABLES `feature_history` WRITE;
/*!40000 ALTER TABLE `feature_history` DISABLE KEYS */;
/*!40000 ALTER TABLE `feature_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `feature_info`
--

DROP TABLE IF EXISTS `feature_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `feature_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `test_case_id` varchar(100) NOT NULL DEFAULT '' COMMENT '测试用例集ID',
  `feature_id` varchar(100) NOT NULL DEFAULT '' COMMENT '用例Id',
  `feature_name` varchar(100) DEFAULT NULL COMMENT '用例名称',
  `test_step` varchar(100) DEFAULT NULL COMMENT '测试步骤',
  `parent_id` varchar(100) DEFAULT NULL COMMENT '父节点Id',
  `feature_type` int(2) NOT NULL DEFAULT '1' COMMENT '用例类型',
  `status` int(2) NOT NULL DEFAULT '1' COMMENT '用例状态',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(20) DEFAULT NULL COMMENT '更新时间',
  `sort_order` int(5) DEFAULT '1' COMMENT '排序',
  `description` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_test_case_id` (`test_case_id`),
  KEY `idx_feature_id` (`feature_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `feature_info`
--

LOCK TABLES `feature_info` WRITE;
/*!40000 ALTER TABLE `feature_info` DISABLE KEYS */;
INSERT INTO `feature_info` VALUES
(1926166927181475841,'876b5f2d0a9e4181b20deac4782b21af','4a44e783fef24d2283ccd6e2f53ec5f9','hello接口测试','',NULL,2,1,1748068956542,1748068956542,1,'演示Get请求接口测试用例'),
(1926166969883684865,'876b5f2d0a9e4181b20deac4782b21af','e889b989179747c288bcaa60620ec015','测试message接口','',NULL,2,1,1748068966740,1748068966740,1,NULL),
(1926167012267126785,'876b5f2d0a9e4181b20deac4782b21af','abcf405b30f843b69b6e934664d4854c','基础功能测试','','4a44e783fef24d2283ccd6e2f53ec5f9',2,1,1748068976836,1748068976836,1,NULL),
(1926167058790346753,'876b5f2d0a9e4181b20deac4782b21af','7eca8d01105842d88792033a83898db6','基础功能测试','','e889b989179747c288bcaa60620ec015',2,1,1748068987937,1748068987937,1,NULL),
(1926167133927108609,'876b5f2d0a9e4181b20deac4782b21af','9120e423049247b1a4aea25998276513','异常场景测试','','e889b989179747c288bcaa60620ec015',2,1,1748069005853,1748069005853,1,NULL),
(1926167211232325633,'876b5f2d0a9e4181b20deac4782b21af','4f50da32ce58404ca0b9a3ff2e90c5f2','hello接口正常访问','','abcf405b30f843b69b6e934664d4854c',1,1,1748069024271,1748069067649,1,NULL),
(1926167463876227073,'876b5f2d0a9e4181b20deac4782b21af','b8e92c13c9b64952a44e9dc8c88230c1','正常接口调用','','7eca8d01105842d88792033a83898db6',1,1,1748069084506,1748069115255,1,NULL),
(1926167662384246785,'876b5f2d0a9e4181b20deac4782b21af','068404ba74554f7faa00999f58ab3f4a','用户名称为空访问','','9120e423049247b1a4aea25998276513',1,1,1748069131839,1748069144810,1,NULL);
/*!40000 ALTER TABLE `feature_info` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `feature_tag`
--

DROP TABLE IF EXISTS `feature_tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `feature_tag` (
  `feature_id` varchar(64) DEFAULT NULL COMMENT '用例Id',
  `tag_value` varchar(100) NOT NULL COMMENT '标签值',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `feature_tag`
--

LOCK TABLES `feature_tag` WRITE;
/*!40000 ALTER TABLE `feature_tag` DISABLE KEYS */;
/*!40000 ALTER TABLE `feature_tag` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `generate_record`
--

DROP TABLE IF EXISTS `generate_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `generate_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `record_id` varchar(64) NOT NULL COMMENT '记录Id',
  `service_id` varchar(64) NOT NULL DEFAULT '' COMMENT '服务Id',
  `execute_params` varchar(256) DEFAULT NULL COMMENT '执行参数',
  `result` text COMMENT '执行记录',
  `status` int(11) DEFAULT NULL COMMENT '执行状态',
  `version` varchar(50) DEFAULT NULL COMMENT '二方包版本',
  `description` varchar(255) DEFAULT NULL COMMENT '版本描述',
  `create_time` bigint(20) NOT NULL COMMENT '创建时间',
  `update_time` bigint(20) DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_service_id_version` (`service_id`,`version`),
  KEY `idx_service_id` (`service_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `generate_record`
--

LOCK TABLES `generate_record` WRITE;
/*!40000 ALTER TABLE `generate_record` DISABLE KEYS */;
/*!40000 ALTER TABLE `generate_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `iteration`
--

DROP TABLE IF EXISTS `iteration`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `iteration` (
  `id` bigint(11) unsigned NOT NULL AUTO_INCREMENT,
  `iteration_id` varchar(64) NOT NULL DEFAULT '' COMMENT '迭代ID',
  `name` varchar(50) NOT NULL DEFAULT '' COMMENT '迭代名称',
  `description` varchar(256) DEFAULT NULL COMMENT '迭代描述',
  `start_time` bigint(20) NOT NULL COMMENT '迭代开始时间',
  `end_time` bigint(20) NOT NULL COMMENT '迭代结束时间',
  `status` int(11) NOT NULL DEFAULT '1' COMMENT '迭代状态',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(20) DEFAULT NULL COMMENT '修改时间',
  `user_id` varchar(64) NOT NULL DEFAULT '' COMMENT '迭代创建人ID',
  `space_id` varchar(64) NOT NULL DEFAULT '' COMMENT '空间ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_iteration_id` (`iteration_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `iteration`
--

LOCK TABLES `iteration` WRITE;
/*!40000 ALTER TABLE `iteration` DISABLE KEYS */;
/*!40000 ALTER TABLE `iteration` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `metric_alert_rule`
--

DROP TABLE IF EXISTS `metric_alert_rule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `metric_alert_rule` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `rule_id` varchar(64) DEFAULT NULL COMMENT '规则ID',
  `metric_id` varchar(64) DEFAULT NULL COMMENT '关联指标ID',
  `condition` varchar(100) DEFAULT NULL COMMENT '触发条件',
  `notify_channel` int(11) DEFAULT NULL COMMENT '通知方式',
  `notify_template` varchar(200) DEFAULT NULL COMMENT '通知模版方式',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(20) DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `metric_alert_rule`
--

LOCK TABLES `metric_alert_rule` WRITE;
/*!40000 ALTER TABLE `metric_alert_rule` DISABLE KEYS */;
/*!40000 ALTER TABLE `metric_alert_rule` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `metric_definition`
--

DROP TABLE IF EXISTS `metric_definition`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `metric_definition` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `metric_id` varchar(64) DEFAULT NULL COMMENT '指标ID',
  `metric_name` varchar(100) DEFAULT NULL COMMENT '指标名称',
  `category` varchar(50) DEFAULT NULL COMMENT '指标分类',
  `calc_type` varchar(100) DEFAULT NULL COMMENT '计算方式',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(20) DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `metric_definition`
--

LOCK TABLES `metric_definition` WRITE;
/*!40000 ALTER TABLE `metric_definition` DISABLE KEYS */;
/*!40000 ALTER TABLE `metric_definition` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `metric_result`
--

DROP TABLE IF EXISTS `metric_result`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `metric_result` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `metric_id` varchar(64) NOT NULL COMMENT '指标ID',
  `value` double DEFAULT NULL COMMENT '统计值',
  `dimension` int(11) DEFAULT NULL COMMENT '统计维度: 1个人 2团队',
  `related_id` varchar(64) DEFAULT NULL COMMENT '关联ID：用户ID或者团队ID',
  `tag` varchar(40) DEFAULT NULL COMMENT '标签计算结果的类型区分',
  `result_name` varchar(50) DEFAULT NULL COMMENT '计算结果的名称，用于表示计算的结果类型',
  `result_type` varchar(30) DEFAULT NULL,
  `create_time` bigint(20) DEFAULT NULL COMMENT '统计时间点',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `metric_result`
--

LOCK TABLES `metric_result` WRITE;
/*!40000 ALTER TABLE `metric_result` DISABLE KEYS */;
/*!40000 ALTER TABLE `metric_result` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `metric_source_data`
--

DROP TABLE IF EXISTS `metric_source_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `metric_source_data` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `source_system` varchar(50) DEFAULT NULL COMMENT '数据来源系统',
  `data_type` varchar(50) DEFAULT NULL COMMENT '数据类型（需求/代码/缺陷）',
  `raw_json` text COMMENT '原始数据（完整Payload）',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `metric_source_data`
--

LOCK TABLES `metric_source_data` WRITE;
/*!40000 ALTER TABLE `metric_source_data` DISABLE KEYS */;
/*!40000 ALTER TABLE `metric_source_data` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `microservice`
--

DROP TABLE IF EXISTS `microservice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `microservice` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `service_id` varchar(64) DEFAULT NULL COMMENT '服务Id',
  `git_url` varchar(1000) DEFAULT NULL COMMENT '服务git地址',
  `service_name` varchar(100) NOT NULL COMMENT '服务名称',
  `description` varchar(300) NOT NULL COMMENT '服务描述',
  `priority` int(4) DEFAULT NULL COMMENT '服务优先级',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(20) DEFAULT NULL COMMENT '修改时间',
  `config` text COMMENT '服务配置(包含服务的上下文、git配置、k8s配置等)',
  `api_coverage` int(4) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_service_name` (`service_name`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `microservice`
--

LOCK TABLES `microservice` WRITE;
/*!40000 ALTER TABLE `microservice` DISABLE KEYS */;
INSERT INTO `microservice` VALUES
(1926134831662616578,'d738e2841bc041a38cab9d3179dd0434','https://github.com/languyue/windy-demo.git','windy-demo','测试服务',3,1748061304359,1748066101360,'{\"appName\":\"windy-demo\",\"envParams\":[{\"related\":false}],\"gitAccessInfo\":{\"accessToken\":\"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\",\"gitDomain\":\"https://api.github.com\",\"gitServiceName\":\"windy-demo\",\"gitType\":\"Github\",\"gitUrl\":\"https://github.com/languyue/windy-demo.git\",\"owner\":\"languyue\"},\"nodeStrategy\":{\"type\":\"\",\"value\":\"\"},\"ports\":[{}],\"replicas\":1,\"serviceContext\":{\"buildVersion\":\"/opt/windy-client/maven\",\"code\":\"Java\",\"deployType\":2,\"mainBranch\":\"main\"},\"strategy\":{\"type\":\"\"},\"volumes\":[{}]}',NULL);
/*!40000 ALTER TABLE `microservice` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `node_bind`
--

DROP TABLE IF EXISTS `node_bind`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `node_bind` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `node_id` varchar(100) NOT NULL COMMENT '执行点ID',
  `node_name` varchar(100) NOT NULL COMMENT '配置名称',
  `user_id` varchar(100) DEFAULT NULL COMMENT '创建人',
  `description` varchar(256) DEFAULT NULL COMMENT '描述',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(20) DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `node_bind`
--

LOCK TABLES `node_bind` WRITE;
/*!40000 ALTER TABLE `node_bind` DISABLE KEYS */;
INSERT INTO `node_bind` VALUES
(1,'07db7de5f7df4b7fa570ef9b14af24e9','代码构建','admin','代码构建',1692540580625,1692540580625),
(2,'a13bf21127284a348ae151e78bbecc0c','执行等待','admin','执行等待',1692540629263,1692540629263),
(3,'3dcd3dc023234abc8892001426d1d3ec','部署','admin','代码部署',1692540643877,1692540643877),
(4,'ce78243933964da78c09c9122c043053','用例任务','admin','用例任务测试',1692540667088,1692540667088),
(5,'5851a43339974fa6b9100c7509502a21','人工卡点','admin','人工卡点',1692540686733,1692540686733),
(6,'3884dbd25e974a508554bf575648fa92','合并代码','admin','合并代码',1692540711592,1692540711592);
/*!40000 ALTER TABLE `node_bind` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `node_config`
--

DROP TABLE IF EXISTS `node_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `node_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `config_id` varchar(64) NOT NULL COMMENT '配置ID',
  `config_name` varchar(100) NOT NULL COMMENT '配置名称',
  `type` int(2) DEFAULT NULL COMMENT '配置类型',
  `config_detail` text COMMENT '配置详情',
  `sort_order` int(2) DEFAULT NULL COMMENT '排序',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(20) DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `node_config`
--

LOCK TABLES `node_config` WRITE;
/*!40000 ALTER TABLE `node_config` DISABLE KEYS */;
/*!40000 ALTER TABLE `node_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `node_record`
--

DROP TABLE IF EXISTS `node_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `node_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `node_id` varchar(64) NOT NULL COMMENT '节点Id',
  `record_id` varchar(64) NOT NULL COMMENT '记录Id',
  `history_id` varchar(64) NOT NULL COMMENT '历史流水线记录Id',
  `code` int(4) DEFAULT NULL COMMENT '处理结果状态码',
  `record_result` text COMMENT '任务处理结果',
  `context` varchar(256) DEFAULT NULL COMMENT '任务执行上下文,作用域整个流水线',
  `status` int(2) DEFAULT NULL COMMENT '任务状态',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(20) DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_record_id` (`record_id`),
  KEY `idx_history_id` (`history_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `node_record`
--

LOCK TABLES `node_record` WRITE;
/*!40000 ALTER TABLE `node_record` DISABLE KEYS */;
/*!40000 ALTER TABLE `node_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `optimistic_lock`
--

DROP TABLE IF EXISTS `optimistic_lock`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `optimistic_lock` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `biz_code` varchar(60) NOT NULL COMMENT '定时业务类型',
  `node_name` varchar(100) DEFAULT NULL COMMENT '实例节点名称',
  `ip` varchar(20) DEFAULT NULL COMMENT '节点ip',
  `start_time` bigint(20) DEFAULT NULL COMMENT '持有锁开始时间',
  `end_time` bigint(20) DEFAULT NULL COMMENT '持有锁结束时间',
  `version` bigint(20) DEFAULT NULL COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_biz_code` (`biz_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分布式乐观锁';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `optimistic_lock`
--

LOCK TABLES `optimistic_lock` WRITE;
/*!40000 ALTER TABLE `optimistic_lock` DISABLE KEYS */;
/*!40000 ALTER TABLE `optimistic_lock` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pipeline`
--

DROP TABLE IF EXISTS `pipeline`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `pipeline` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `pipeline_id` varchar(64) NOT NULL DEFAULT '' COMMENT '流水线ID',
  `pipeline_name` varchar(100) DEFAULT NULL COMMENT '流水线名称',
  `service_id` varchar(100) NOT NULL COMMENT '服务Id',
  `pipeline_type` int(2) DEFAULT NULL COMMENT '流水线类型 1 发布流水线 2 每日构建流水线 3 个人流水线',
  `config` varchar(256) DEFAULT NULL COMMENT '流水线配置信息',
  `execute_type` int(2) DEFAULT NULL COMMENT '执行方式 1 手动执行 2push 3merge',
  `pipeline_status` int(2) DEFAULT '1' COMMENT '流水线状态',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(20) DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_pipeline_id` (`pipeline_id`),
  KEY `idx_service_id` (`service_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pipeline`
--

LOCK TABLES `pipeline` WRITE;
/*!40000 ALTER TABLE `pipeline` DISABLE KEYS */;
INSERT INTO `pipeline` VALUES
(1926164053487538178,'e9750851db224383b49a0952d8bf1245','演示开发流水线','d738e2841bc041a38cab9d3179dd0434',3,NULL,1,1,1748068271385,1748068271385),
(1926191554507173890,'4dd645438ba6496590be022af87e662b','演示流水线关联自动测试','d738e2841bc041a38cab9d3179dd0434',3,NULL,1,1,1748074828156,1748074828156);
/*!40000 ALTER TABLE `pipeline` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pipeline_action`
--

DROP TABLE IF EXISTS `pipeline_action`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `pipeline_action` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `action_id` varchar(100) NOT NULL COMMENT '执行点ID',
  `action_name` varchar(100) NOT NULL COMMENT '配置名称',
  `node_id` varchar(100) DEFAULT NULL COMMENT '执行点类型',
  `action_url` varchar(100) DEFAULT NULL COMMENT '请求地址',
  `param_detail` text COMMENT '配置详情',
  `query_url` varchar(100) DEFAULT NULL COMMENT '请求地址',
  `result` varchar(256) DEFAULT NULL COMMENT '响应结果比较',
  `description` varchar(256) DEFAULT NULL COMMENT '执行点描述',
  `execute_type` varchar(10) NOT NULL COMMENT '执行类型',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(20) DEFAULT NULL COMMENT '修改时间',
  `headers` varchar(256) DEFAULT NULL COMMENT '节点触发的header',
  `query_expression` varchar(256) DEFAULT NULL COMMENT '查询结果比较表达式',
  `body_type` varchar(50) DEFAULT NULL COMMENT '节点触发任务http的body类型',
  PRIMARY KEY (`id`),
  KEY `idx_action_id` (`action_id`),
  KEY `idx_node_id` (`node_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pipeline_action`
--

LOCK TABLES `pipeline_action` WRITE;
/*!40000 ALTER TABLE `pipeline_action` DISABLE KEYS */;
INSERT INTO `pipeline_action` VALUES
(1,'62a0f23ab666417e9a7116d4e78a70a4','代码构建','07db7de5f7df4b7fa570ef9b14af24e9',NULL,'[{\"description\":\"构建文件相对路径\",\"name\":\"pomPath\",\"value\":\"pom.xml\"}]',NULL,'[{\"compareKey\":\"status\",\"description\":\"构建状态\",\"operator\":\"=\",\"showCompare\":false,\"value\":\"1\",\"valueType\":\"Integer\"}]','代码构建','BUILD',1692539956836,1692539956836,NULL,NULL,NULL),
(2,'304d5182969945dda37bf974bc322c83','等待执行','a13bf21127284a348ae151e78bbecc0c',NULL,'[{\"description\":\"节点等待时长\",\"name\":\"waitTime\",\"value\":\"300\"}]',NULL,'[]','这个节点可以用于两个任务之间不期望立即执行的场景，比如部署与测试功能一般可在服务部署之后等待5min然后再开始功能测试。','WAIT',1692540079424,1692540079424,NULL,NULL,NULL),
(3,'24a60706af63443e9721b8653b67fb3a','环境部署','3dcd3dc023234abc8892001426d1d3ec',NULL,'[{\"description\":\"环境Id\",\"name\":\"envId\",\"type\":\"select\",\"value\":\"\"}]',NULL,'[]','当前节点支持将构建好的代码部署到指定的环境，这里说的环境是在系统的环境管理中添加，流水线部署的环境可在流水线中自定义配置。','DEPLOY',1692540178899,1692540178899,NULL,NULL,NULL),
(4,'08fd64be475c49c89daf872a540fe99e','功能测试','ce78243933964da78c09c9122c043053',NULL,'[{\"description\":\"选择任务\",\"name\":\"taskId\",\"type\":\"select\",\"value\":\"\"}]',NULL,'[{\"compareKey\":\"percent\",\"description\":\"执行成功率\",\"operator\":\">=\",\"showCompare\":true,\"value\":\"90\",\"valueType\":\"Integer\"}]','当前节点用于服务功能测试，具体的功能用例在Windy系统的“用例管理”功能中实现。测试任务在流水线中配置选择，成功率是指测试任务的成功百分比只有在测试任务达到设置值之后当前节点运行才算成功。','TEST',1692540382487,1692540382487,NULL,NULL,NULL),
(5,'89de9bdb09a647d2b43c8c386e12d413','审批','5851a43339974fa6b9100c7509502a21',NULL,'[{\"description\":\"审批最大等待时长(秒)\",\"name\":\"maxWait\",\"value\":\"604800\"}]',NULL,'[]','这个节点用于线上发布功能，针对线上代码共在相关功能测试完成之后，由审核人员确认是否发布，这个功能是可选项。','APPROVAL',1692540478118,1692540478118,NULL,NULL,NULL),
(6,'34dabe9de45543fdafd3031875c4d4ca','合入主干','3884dbd25e974a508554bf575648fa92',NULL,'[{\"description\":\"是否删除分支\",\"name\":\"deleteBranch\",\"value\":\"false\"}]',NULL,'[]','当前节点用于线上发布，线上发布流水线功能测试和审批都完成就可以将对应的分支合并到master节点中。','MERGE',1692540555777,1692540555777,NULL,NULL,NULL);
/*!40000 ALTER TABLE `pipeline_action` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pipeline_history`
--

DROP TABLE IF EXISTS `pipeline_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `pipeline_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `history_id` varchar(64) NOT NULL COMMENT '执行历史Id',
  `pipeline_id` varchar(100) NOT NULL COMMENT '流水线id',
  `pipeline_name` varchar(100) DEFAULT NULL COMMENT '流水线名称',
  `branch` varchar(100) NOT NULL COMMENT '流水线运行的分支',
  `config` varchar(256) DEFAULT '' COMMENT '流水线执行的配置',
  `pipeline_status` int(2) NOT NULL COMMENT '流水线执行结果',
  `executor` varchar(50) DEFAULT NULL COMMENT '执行人',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(20) DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_history_id` (`history_id`),
  KEY `idx_pipeline_id` (`pipeline_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流水线执行历史';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pipeline_history`
--

LOCK TABLES `pipeline_history` WRITE;
/*!40000 ALTER TABLE `pipeline_history` DISABLE KEYS */;
/*!40000 ALTER TABLE `pipeline_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pipeline_node`
--

DROP TABLE IF EXISTS `pipeline_node`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `pipeline_node` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `node_id` varchar(64) NOT NULL COMMENT 'nodeId',
  `stage_id` varchar(64) NOT NULL COMMENT '阶段Id',
  `pipeline_id` varchar(64) NOT NULL COMMENT '流水线Id',
  `node_name` varchar(100) NOT NULL COMMENT '节点名称',
  `type` int(2) DEFAULT NULL COMMENT '节点类型',
  `config` varchar(1000) DEFAULT NULL COMMENT '节点配置',
  `create_time` bigint(20) NOT NULL COMMENT '创建时间',
  `update_time` bigint(20) NOT NULL COMMENT '修改时间',
  `sort_order` int(3) DEFAULT NULL COMMENT '排序',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pipeline_node`
--

LOCK TABLES `pipeline_node` WRITE;
/*!40000 ALTER TABLE `pipeline_node` DISABLE KEYS */;
INSERT INTO `pipeline_node` VALUES
(1926164053739196417,'f618079d09de45b6b38158007a58ec6a','b62e3c5459aa44e899d548b93ba3f853','e9750851db224383b49a0952d8bf1245','代码构建',1,'{\"actionId\":\"62a0f23ab666417e9a7116d4e78a70a4\",\"compareInfo\":[{\"compareKey\":\"status\",\"description\":\"构建状态\",\"operator\":\"=\",\"showCompare\":false,\"value\":\"1\",\"valueType\":\"Integer\"}],\"paramList\":{\"pomPath\":\"pom.xml\"}}',1748068271479,1748068271479,3),
(1926191554700111873,'b2171631b61f44aca0c59f46662fb5f4','f1dfecc3dd5444abb72b59773be1b9de','4dd645438ba6496590be022af87e662b','代码构建',1,'{\"actionId\":\"62a0f23ab666417e9a7116d4e78a70a4\",\"compareInfo\":[{\"compareKey\":\"status\",\"description\":\"构建状态\",\"operator\":\"=\",\"showCompare\":false,\"value\":\"1\",\"valueType\":\"Integer\"}],\"paramList\":{\"pomPath\":\"pom.xml\"}}',1748074828216,1748074828216,3),
(1926191554763026435,'6f735ca315e04ba88e5ce07592258609','b5bd718a91234ed19bca6dcb3e58bc2f','4dd645438ba6496590be022af87e662b','环境部署',11,'{\"actionId\":\"24a60706af63443e9721b8653b67fb3a\",\"compareInfo\":[],\"paramList\":{\"envId\":\"50ec1db3137d43b49fc7a1f3b447475f\"}}',1748074828228,1748074828228,5),
(1926191554830135297,'768e9f4a041a4b858cbf7a7515af8e1b','2bb242e5503d4c0699d54b1c5215d466','4dd645438ba6496590be022af87e662b','功能测试',111,'{\"actionId\":\"08fd64be475c49c89daf872a540fe99e\",\"compareInfo\":[{\"compareKey\":\"percent\",\"description\":\"执行成功率\",\"operator\":\">=\",\"showCompare\":true,\"value\":\"90\",\"valueType\":\"Integer\"}],\"paramList\":{\"taskId\":\"65f8105fa5184f0e9c4185cf0e5e3c1c\"}}',1748074828237,1748074828237,7);
/*!40000 ALTER TABLE `pipeline_node` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pipeline_stage`
--

DROP TABLE IF EXISTS `pipeline_stage`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `pipeline_stage` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `stage_id` varchar(64) DEFAULT NULL COMMENT '阶段Id',
  `pipeline_id` varchar(64) NOT NULL COMMENT '流水线Id',
  `config_id` varchar(100) DEFAULT NULL COMMENT '关联的配置Id',
  `stage_name` varchar(100) NOT NULL COMMENT '阶段名称',
  `type` int(2) DEFAULT '1' COMMENT '阶段类型',
  `create_time` bigint(20) NOT NULL COMMENT '创建时间',
  `update_time` bigint(20) NOT NULL COMMENT '修改时间',
  `sort_order` int(3) DEFAULT NULL COMMENT '排序',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pipeline_stage`
--

LOCK TABLES `pipeline_stage` WRITE;
/*!40000 ALTER TABLE `pipeline_stage` DISABLE KEYS */;
INSERT INTO `pipeline_stage` VALUES
(1926164053609172994,'51bc56bedcf844359b087ea95535696a','e9750851db224383b49a0952d8bf1245',NULL,'开始',0,1748068271434,1748068271434,1),
(1926164053609172995,'b62e3c5459aa44e899d548b93ba3f853','e9750851db224383b49a0952d8bf1245','07db7de5f7df4b7fa570ef9b14af24e9','代码构建',1,1748068271448,1748068271448,2),
(1926164053806305281,'6df0715b03084be5a466ff730451b662','e9750851db224383b49a0952d8bf1245',NULL,'结束',11,1748068271486,1748068271486,4),
(1926191554570088450,'086139b2834c47068031ec8ab84ff756','4dd645438ba6496590be022af87e662b',NULL,'开始',0,1748074828183,1748074828183,1),
(1926191554570088451,'f1dfecc3dd5444abb72b59773be1b9de','4dd645438ba6496590be022af87e662b','07db7de5f7df4b7fa570ef9b14af24e9','构建代码',1,1748074828189,1748074828189,2),
(1926191554763026434,'b5bd718a91234ed19bca6dcb3e58bc2f','4dd645438ba6496590be022af87e662b','3dcd3dc023234abc8892001426d1d3ec','部署代码到环境',11,1748074828222,1748074828222,4),
(1926191554763026436,'2bb242e5503d4c0699d54b1c5215d466','4dd645438ba6496590be022af87e662b','ce78243933964da78c09c9122c043053','自动化测试用例执行',111,1748074828232,1748074828232,6),
(1926191554830135298,'a8cb0368a73242f1a2859efb8e3669b6','4dd645438ba6496590be022af87e662b',NULL,'结束',1111,1748074828241,1748074828241,8);
/*!40000 ALTER TABLE `pipeline_stage` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `plugin_info`
--

DROP TABLE IF EXISTS `plugin_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `plugin_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `plugin_name` varchar(64) NOT NULL COMMENT '插件名称',
  `plugin_type` int(11) NOT NULL DEFAULT '1' COMMENT '插件类型 1 模版插件',
  `file_data` mediumblob NOT NULL COMMENT '文件内容',
  `plugin_id` varchar(64) DEFAULT NULL COMMENT '插件Id',
  `status` int(11) NOT NULL DEFAULT '2' COMMENT '是否可用 1可用 2 不可用',
  `create_time` bigint(20) NOT NULL COMMENT '创建时间',
  `update_time` bigint(20) DEFAULT NULL COMMENT '修改时间',
  `hash_value` varchar(64) DEFAULT NULL COMMENT '文件hash值',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `plugin_info`
--

LOCK TABLES `plugin_info` WRITE;
/*!40000 ALTER TABLE `plugin_info` DISABLE KEYS */;
/*!40000 ALTER TABLE `plugin_info` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `publish_bind`
--

DROP TABLE IF EXISTS `publish_bind`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `publish_bind` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `branch` varchar(100) NOT NULL COMMENT '分支名称',
  `service_id` varchar(64) NOT NULL COMMENT '服务id',
  `pipeline_id` varchar(64) NOT NULL COMMENT '流水线id',
  `publish_id` varchar(64) NOT NULL COMMENT '发布Id',
  `publish_line` varchar(64) DEFAULT '' COMMENT '关联的发布流水线id',
  `status` int(2) NOT NULL DEFAULT '1' COMMENT '发布状态 1 待发布  2 发布中',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(20) DEFAULT NULL COMMENT '修改时间',
  `message` varchar(256) DEFAULT NULL COMMENT '分支发布描述信息',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_service_id_branch` (`service_id`,`branch`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `publish_bind`
--

LOCK TABLES `publish_bind` WRITE;
/*!40000 ALTER TABLE `publish_bind` DISABLE KEYS */;
/*!40000 ALTER TABLE `publish_bind` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `resource`
--

DROP TABLE IF EXISTS `resource`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `resource` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Primary key, auto-incremented',
  `resource_id` varchar(64) NOT NULL DEFAULT '' COMMENT '资源ID',
  `resource_name` varchar(255) NOT NULL COMMENT '资源名称',
  `parent_id` varchar(255) DEFAULT NULL COMMENT '父节点ID',
  `sort_order` int(11) DEFAULT NULL COMMENT '排序',
  `content` varchar(2048) DEFAULT NULL COMMENT '资源内容',
  `visible` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否可见',
  `icon` varchar(255) DEFAULT NULL COMMENT '资源icon',
  `create_time` bigint(20) NOT NULL COMMENT '创建时间',
  `update_time` bigint(20) NOT NULL COMMENT '修改时间',
  `operate` varchar(10) DEFAULT NULL COMMENT '资源操作类型',
  `resource_type` int(2) NOT NULL DEFAULT '1' COMMENT '资源类型(1 权限菜单 2 接口资源)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COMMENT='资源表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `resource`
--

LOCK TABLES `resource` WRITE;
/*!40000 ALTER TABLE `resource` DISABLE KEYS */;
INSERT INTO `resource` VALUES
(2,'5ec9cf42c9be42c8bf5fe80becaa0b82','所有接口',NULL,1,'/**',1,NULL,0,1724926335281,'*',1),
(3,'5ec9cf42c9be42c8bf5fe80becaa0b81','个人工作台-菜单',NULL,1,'m10000',0,NULL,1729251038123,1729490156389,'GET',2),
(4,'11f53ad0e5d74271acc3a54ab49fcf8f','空间-菜单',NULL,1,'m10001',0,NULL,1729479075905,1729479159991,'GET',2),
(5,'d614ef76633f430f87d6a106a18c5456','服务-菜单',NULL,NULL,'m10002',0,NULL,1729479150049,1729479150049,'GET',2),
(6,'661134052c5940bfa7352631d111a2b0','api管理-菜单',NULL,NULL,'m10003',0,NULL,1729479192318,1729479192318,'GET',2),
(7,'b154bfe0d1794d45995b43f64736ebeb','变更列表',NULL,NULL,'m10004',0,NULL,1729479235014,1729479235014,'GET',2),
(8,'a2d2cb4268b9464e927ee5dde0f5f6a1','流水线-菜单',NULL,NULL,'m10005',0,NULL,1729479259634,1729479259634,'GET',2),
(9,'3b3fdf57d0144ba2b35ddbb04b205f52','流水线节点管理-菜单',NULL,NULL,'m10006',0,NULL,1729479360833,1729479360833,'GET',2),
(10,'602a55cc3cad46119ecf1cc2b3711e1d','功能测试-菜单',NULL,NULL,'m10007',0,NULL,1729479393377,1729479393377,'GET',2),
(11,'445c003a63e7470ca207a81395237b90','e2e测试-菜单',NULL,NULL,'m10008',0,NULL,1729479416565,1729479416565,'GET',2),
(12,'ddf1ebba0d8d47adb854316a6fd734da','模版管理-菜单',NULL,NULL,'m10009',0,NULL,1729479443148,1729479443148,'GET',2),
(13,'4e0d5e38ab9c49f9aac800d56faaaa62','任务管理-菜单',NULL,NULL,'m10010',0,NULL,1729479466180,1729479466180,'GET',2),
(14,'afcda114c6a74bd0af2d4689bc3e291c','系统配置-菜单',NULL,NULL,'m10011',0,NULL,1729479493325,1729479493325,'GET',2),
(15,'6005aa08f0654ba7a472ed141f0ac2f4','RBAC管理-菜单',NULL,NULL,'m10012',0,NULL,1729479545890,1729479545890,'GET',2),
(16,'f56084e422ad47bd9ec6db947d0103b8','环境管理-菜单',NULL,NULL,'m10013',0,NULL,1729479579035,1729479579035,'GET',2),
(17,'dd93e97155474881acfdcd85e1e82a1d','监控-菜单',NULL,NULL,'m10014',0,NULL,1729479789969,1729479789969,'GET',2),
(18,'bc61847f6157470aa5f43357319620e4','工作台-菜单',NULL,NULL,'m10015',0,NULL,1729490009789,1729490009789,'GET',2),
(19,'14c97e8e22114ed39311c552a458bb99','服务管理-菜单',NULL,NULL,'m10016',0,NULL,1729490033258,1729490033258,'GET',2),
(20,'2143e3e0ea3b4ca8a1363705e76afbbd','流水线管理-菜单',NULL,NULL,'m10017',0,NULL,1729490066505,1729490066505,'GET',2),
(21,'de6fc3b200e146b298afcff2c3e37da5','测试管理-菜单',NULL,NULL,'m10018',0,NULL,1729490084762,1729490084762,'GET',2),
(22,'2604a737963942f6aef116217f7f2e89','系统管理-菜单',NULL,NULL,'m10019',0,NULL,1729490107995,1729490107995,'GET',2),
(23,'7686bfb566964a2b93b1c076f825cca6','发布审批-按钮',NULL,0,'m10020',0,NULL,1729747055432,1734490153951,'GET',2);
/*!40000 ALTER TABLE `resource` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `resource_member`
--

DROP TABLE IF EXISTS `resource_member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `resource_member` (
  `id` bigint(11) unsigned NOT NULL AUTO_INCREMENT,
  `resource_id` varchar(64) DEFAULT NULL COMMENT '资源ID',
  `relation_id` varchar(64) DEFAULT NULL COMMENT '用户、组织ID',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `member_type` varchar(20) DEFAULT NULL COMMENT '关联成员类型',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `resource_member`
--

LOCK TABLES `resource_member` WRITE;
/*!40000 ALTER TABLE `resource_member` DISABLE KEYS */;
INSERT INTO `resource_member` VALUES
(1873556333190492161,'c803c94b2f8f45e8a09b56168d2d14b0','d595a3d55b4e47978dd25cb8acb3e753',1735525613483,'service'),
(1926134831729725442,'d738e2841bc041a38cab9d3179dd0434','d595a3d55b4e47978dd25cb8acb3e753',1748061304398,'service');
/*!40000 ALTER TABLE `resource_member` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `role`
--

DROP TABLE IF EXISTS `role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Primary key, auto-incremented',
  `role_id` varchar(64) NOT NULL DEFAULT '' COMMENT '角色ID',
  `role_name` varchar(255) NOT NULL COMMENT '角色名称',
  `description` varchar(1024) DEFAULT NULL COMMENT '角色描述',
  `sort` int(11) DEFAULT NULL COMMENT '排序',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态',
  `create_time` bigint(20) NOT NULL COMMENT '创建时间',
  `update_time` bigint(20) NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COMMENT='角色表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `role`
--

LOCK TABLES `role` WRITE;
/*!40000 ALTER TABLE `role` DISABLE KEYS */;
INSERT INTO `role` VALUES
(1,'5422e309eb7545279a3378ui706112602','API接口权限','默认情况下用户不允许访问所有API接口，所以默认情况下组织角色需要绑定当前API权限，否则用户无法正常使用',NULL,1,1733742273566,1734489201027),
(2,'5422e309eb7545279a33787706112601','工作台菜单角色','查看和管理需求、缺陷、工作项、迭代，所有人员都使用',NULL,1,1729483007049,1734490101062),
(3,'98322792c9f84e199b75d34ab0b4a92e','服务管理菜单角色','维护代码服务配置信息以及服务的API管理，开发人员使用',NULL,1,1729490179280,1734490060549),
(4,'ea7de5d96d2e4e4992b98baef75d4238','流水线菜单角色','用来服务的构建与流水线管理功能，开发人员使用',NULL,1,1729490218534,1734490035123),
(5,'7ba4833884d54607b2af7267ef09984e','测试管理菜单角色','测试相关的菜单功能，开发与测试人员使用',NULL,1,1729490282204,1734489997140),
(6,'fa16a5f788784e25a280fb6adadfc831','系统管理菜单角色','系统配置和环境管理功能',NULL,1,1729490319683,1734489973527),
(7,'6f1b9ec0df86416186b8c0bf2732b8f0','发布审批','用于服务流水线发布审批审核',NULL,1,1729747405966,1729747405966);
/*!40000 ALTER TABLE `role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `role_resource`
--

DROP TABLE IF EXISTS `role_resource`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `role_resource` (
  `resource_id` varchar(255) NOT NULL COMMENT '资源ID',
  `role_id` varchar(255) NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`resource_id`,`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色资源关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `role_resource`
--

LOCK TABLES `role_resource` WRITE;
/*!40000 ALTER TABLE `role_resource` DISABLE KEYS */;
INSERT INTO `role_resource` VALUES
('11f53ad0e5d74271acc3a54ab49fcf8f','5422e309eb7545279a33787706112601'),
('14c97e8e22114ed39311c552a458bb99','98322792c9f84e199b75d34ab0b4a92e'),
('2143e3e0ea3b4ca8a1363705e76afbbd','ea7de5d96d2e4e4992b98baef75d4238'),
('2604a737963942f6aef116217f7f2e89','fa16a5f788784e25a280fb6adadfc831'),
('3b3fdf57d0144ba2b35ddbb04b205f52','ea7de5d96d2e4e4992b98baef75d4238'),
('445c003a63e7470ca207a81395237b90','7ba4833884d54607b2af7267ef09984e'),
('4e0d5e38ab9c49f9aac800d56faaaa62','7ba4833884d54607b2af7267ef09984e'),
('5ec9cf42c9be42c8bf5fe80becaa0b81','5422e309eb7545279a33787706112601'),
('5ec9cf42c9be42c8bf5fe80becaa0b82','5422e309eb7545279a3378ui706112602'),
('6005aa08f0654ba7a472ed141f0ac2f4','fa16a5f788784e25a280fb6adadfc831'),
('602a55cc3cad46119ecf1cc2b3711e1d','7ba4833884d54607b2af7267ef09984e'),
('661134052c5940bfa7352631d111a2b0','98322792c9f84e199b75d34ab0b4a92e'),
('7686bfb566964a2b93b1c076f825cca6','6f1b9ec0df86416186b8c0bf2732b8f0'),
('a2d2cb4268b9464e927ee5dde0f5f6a1','ea7de5d96d2e4e4992b98baef75d4238'),
('afcda114c6a74bd0af2d4689bc3e291c','fa16a5f788784e25a280fb6adadfc831'),
('b154bfe0d1794d45995b43f64736ebeb','ea7de5d96d2e4e4992b98baef75d4238'),
('bc61847f6157470aa5f43357319620e4','5422e309eb7545279a33787706112601'),
('d614ef76633f430f87d6a106a18c5456','98322792c9f84e199b75d34ab0b4a92e'),
('dd93e97155474881acfdcd85e1e82a1d','fa16a5f788784e25a280fb6adadfc831'),
('ddf1ebba0d8d47adb854316a6fd734da','7ba4833884d54607b2af7267ef09984e'),
('de6fc3b200e146b298afcff2c3e37da5','7ba4833884d54607b2af7267ef09984e'),
('f56084e422ad47bd9ec6db947d0103b8','fa16a5f788784e25a280fb6adadfc831');
/*!40000 ALTER TABLE `role_resource` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `service_api`
--

DROP TABLE IF EXISTS `service_api`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `service_api` (
  `id` bigint(20) NOT NULL,
  `api_id` varchar(64) DEFAULT NULL COMMENT 'apiId',
  `api_name` varchar(100) NOT NULL COMMENT 'api名称',
  `service_id` varchar(64) NOT NULL COMMENT '所属服务Id',
  `parent_id` varchar(64) DEFAULT NULL COMMENT '父节点Id',
  `type` varchar(10) DEFAULT NULL COMMENT 'api类型 http dubbo',
  `method` varchar(100) DEFAULT NULL COMMENT 'api对应的方法 http对应方法名 dubbo对应接口方法名',
  `resource` varchar(256) DEFAULT NULL COMMENT 'api信息http对应的url dubbo对应服务名',
  `description` varchar(256) DEFAULT NULL COMMENT '接口描述',
  `api_type` int(1) DEFAULT '1' COMMENT '0 目录 1 api',
  `request_parameter` text COMMENT '请求参数',
  `response_parameter` text COMMENT '响应参数',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(20) DEFAULT NULL COMMENT '修改时间',
  `body_class` varchar(50) DEFAULT NULL COMMENT '请求体类名',
  `result_class` varchar(50) DEFAULT NULL COMMENT '请求响应结果类名',
  `class_name` varchar(50) DEFAULT NULL COMMENT '文件类名',
  `class_method` varchar(50) DEFAULT NULL COMMENT '文件方法名,如果文件类名相同则方法再同一个类文件中',
  `header` varchar(1000) DEFAULT NULL COMMENT 'http请求的Header',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `service_api`
--

LOCK TABLES `service_api` WRITE;
/*!40000 ALTER TABLE `service_api` DISABLE KEYS */;
INSERT INTO `service_api` VALUES
(1926147112685322241,'81c6894170554959800ae5a9e69015f8','hello接口','d738e2841bc041a38cab9d3179dd0434',NULL,'http','GET','/hello',NULL,1,'[]','[]',1748064232403,1748064686124,NULL,'ResponseMode','DemoController','hello','{}'),
(1926149141428224001,'1488e0c2186545f49350b2b93ee3f493','message接口','d738e2841bc041a38cab9d3179dd0434',NULL,'http','POST','/message','message测试接口',1,'[{\"children\":[],\"description\":\"用户名称\",\"objectName\":\"String\",\"paramKey\":\"name\",\"position\":\"Body\",\"required\":true,\"type\":\"String\"}]','[]',1748064716101,1748064768892,'User','ResponseModel','DemoController','sayName','{}');
/*!40000 ALTER TABLE `service_api` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `service_generate`
--

DROP TABLE IF EXISTS `service_generate`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `service_generate` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `generate_id` varchar(64) NOT NULL COMMENT '配置Id',
  `package_name` varchar(100) NOT NULL COMMENT '生成的包路径',
  `group_id` varchar(50) NOT NULL COMMENT 'maven的groupId',
  `artifact_id` varchar(50) NOT NULL COMMENT 'maven的artifactId',
  `version` varchar(50) NOT NULL COMMENT '包的版本号',
  `service_id` varchar(64) NOT NULL COMMENT '服务Id',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(20) DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `service_generate`
--

LOCK TABLES `service_generate` WRITE;
/*!40000 ALTER TABLE `service_generate` DISABLE KEYS */;
/*!40000 ALTER TABLE `service_generate` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `space`
--

DROP TABLE IF EXISTS `space`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `space` (
  `id` bigint(11) unsigned NOT NULL AUTO_INCREMENT,
  `space_id` varchar(64) DEFAULT NULL COMMENT '空间ID',
  `space_name` varchar(50) DEFAULT NULL COMMENT '空间名称',
  `description` varchar(256) DEFAULT NULL COMMENT '空间描述',
  `user_id` varchar(64) DEFAULT NULL COMMENT '用户ID',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(11) DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `space`
--

LOCK TABLES `space` WRITE;
/*!40000 ALTER TABLE `space` DISABLE KEYS */;
INSERT INTO `space` VALUES
(1,'b973c0dd53844fc5b241acf79d44e798','默认空间','默认空间','d595a3d55b4e47978dd25cb8acb3e753',1723721011500,1733119033033);
/*!40000 ALTER TABLE `space` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sub_dispatch_log`
--

DROP TABLE IF EXISTS `sub_dispatch_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `sub_dispatch_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `sub_task_id` varchar(64) NOT NULL COMMENT '子任务Id',
  `sub_task_name` varchar(100) DEFAULT NULL COMMENT '子任务名称',
  `execute_id` varchar(64) NOT NULL COMMENT '具体任务的Id',
  `execute_param` varchar(500) DEFAULT NULL COMMENT '运行参数',
  `status` int(11) DEFAULT NULL COMMENT '子任务状态',
  `log_id` varchar(100) DEFAULT NULL COMMENT '执行记录Id',
  `client_ip` varchar(20) DEFAULT NULL COMMENT 'client的ip',
  `execute_type` varchar(10) DEFAULT NULL COMMENT '客户端执行 IP',
  `update_time` bigint(20) NOT NULL COMMENT '修改时间',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sub_dispatch_log`
--

LOCK TABLES `sub_dispatch_log` WRITE;
/*!40000 ALTER TABLE `sub_dispatch_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `sub_dispatch_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `system_config`
--

DROP TABLE IF EXISTS `system_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `system_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `config_id` varchar(100) NOT NULL COMMENT '配置ID',
  `config_name` varchar(100) NOT NULL COMMENT '配置名称',
  `parent_id` varchar(100) DEFAULT NULL COMMENT '父节点Id',
  `type` int(2) DEFAULT NULL COMMENT '配置类型',
  `config_detail` text COMMENT '配置详情',
  `sort` int(2) DEFAULT NULL COMMENT '排序',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(20) DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `system_config`
--

LOCK TABLES `system_config` WRITE;
/*!40000 ALTER TABLE `system_config` DISABLE KEYS */;
INSERT INTO `system_config` VALUES
(1,'b973c0dd53844fc5b241acf79d44e798','default_pipeline',NULL,1,'{\"configDetail\":\"[{\\\"id\\\":\\\"0\\\",\\\"name\\\":\\\"开始\\\",\\\"status\\\":\\\"success\\\",\\\"root\\\":true,\\\"group\\\":\\\"0\\\",\\\"disable\\\":true,\\\"next\\\":[{\\\"index\\\":1,\\\"weight\\\":0}]},{\\\"id\\\":\\\"1\\\",\\\"name\\\":\\\"结束\\\",\\\"disable\\\":true,\\\"status\\\":\\\"success\\\",\\\"group\\\":\\\"1\\\",\\\"root\\\":true}]\"}',1,1692543958895,1692543958895);
/*!40000 ALTER TABLE `system_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `task_info`
--

DROP TABLE IF EXISTS `task_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `task_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `task_id` varchar(100) DEFAULT NULL COMMENT '任务ID',
  `user_id` varchar(100) DEFAULT NULL COMMENT '创建者ID',
  `service_id` varchar(100) DEFAULT NULL COMMENT '服务Id',
  `test_case_id` varchar(100) DEFAULT NULL COMMENT '测试集Id',
  `task_name` varchar(100) NOT NULL COMMENT '任务名称',
  `description` varchar(100) DEFAULT NULL COMMENT '任务描述',
  `task_config` varchar(2000) DEFAULT NULL COMMENT '任务执行参数',
  `machines` varchar(100) DEFAULT NULL COMMENT '执行机器列表',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(20) DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_task_id` (`task_id`),
  KEY `idx_service_id` (`service_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `task_info`
--

LOCK TABLES `task_info` WRITE;
/*!40000 ALTER TABLE `task_info` DISABLE KEYS */;
INSERT INTO `task_info` VALUES
(1926167990798249986,'65f8105fa5184f0e9c4185cf0e5e3c1c','d595a3d55b4e47978dd25cb8acb3e753','d738e2841bc041a38cab9d3179dd0434','876b5f2d0a9e4181b20deac4782b21af','演示测试任务',NULL,'{\n   \"host\": \"http://localhost:8888\"\n}','[]',1748069210139,1748069210139);
/*!40000 ALTER TABLE `task_info` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `task_record`
--

DROP TABLE IF EXISTS `task_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `task_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `record_id` varchar(64) NOT NULL DEFAULT '' COMMENT '任务记录ID',
  `task_id` varchar(64) NOT NULL DEFAULT '' COMMENT '任务ID',
  `test_case_id` varchar(64) NOT NULL DEFAULT '' COMMENT '测试集Id',
  `status` int(2) DEFAULT NULL COMMENT '执行状态',
  `task_name` varchar(100) NOT NULL COMMENT '任务名称',
  `task_config` varchar(2000) DEFAULT NULL COMMENT '任务执行参数',
  `machines` varchar(100) DEFAULT NULL COMMENT '执行机器列表',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(20) DEFAULT NULL COMMENT '更新时间',
  `trigger_id` varchar(64) DEFAULT NULL COMMENT '任务触发源ID',
  `percent` int(3) DEFAULT NULL COMMENT '任务执行百分比',
  `type` int(1) DEFAULT '1' COMMENT '记录类型: 1 用例任务  2 临时任务',
  `execute_user` varchar(64) DEFAULT '' COMMENT '任务执行人',
  `user_id` varchar(64) DEFAULT NULL COMMENT '执行人ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_record_id` (`record_id`),
  KEY `idx_task_id` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `task_record`
--

LOCK TABLES `task_record` WRITE;
/*!40000 ALTER TABLE `task_record` DISABLE KEYS */;
/*!40000 ALTER TABLE `task_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `test_case`
--

DROP TABLE IF EXISTS `test_case`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `test_case` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `test_case_id` varchar(64) NOT NULL COMMENT '用例集id',
  `author` varchar(100) DEFAULT NULL COMMENT '创建人',
  `service_id` varchar(64) DEFAULT NULL COMMENT '服务Id',
  `test_case_name` varchar(100) DEFAULT NULL COMMENT '用例集名称',
  `description` varchar(100) DEFAULT NULL COMMENT '用例集描述',
  `user_id` varchar(64) DEFAULT NULL COMMENT '创建人ID',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(20) DEFAULT NULL COMMENT '更新时间',
  `case_type` int(3) DEFAULT '1' COMMENT '测试集类型',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_test_case_id` (`test_case_id`),
  KEY `idx_service_id` (`service_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `test_case`
--

LOCK TABLES `test_case` WRITE;
/*!40000 ALTER TABLE `test_case` DISABLE KEYS */;
INSERT INTO `test_case` VALUES
(1926164714778284034,'876b5f2d0a9e4181b20deac4782b21af',NULL,'d738e2841bc041a38cab9d3179dd0434','演示测试集','用于演示如何测试服务功能','d595a3d55b4e47978dd25cb8acb3e753',1748068429071,1748068429071,1);
/*!40000 ALTER TABLE `test_case` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `test_case_config`
--

DROP TABLE IF EXISTS `test_case_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `test_case_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `config_id` varchar(100) DEFAULT NULL COMMENT '配置ID',
  `union_id` varchar(100) DEFAULT NULL COMMENT '关联Id',
  `parent_id` varchar(100) DEFAULT NULL COMMENT '父节点Id',
  `type` int(2) DEFAULT NULL COMMENT '节点类型',
  `param_key` varchar(100) DEFAULT NULL COMMENT '参数key',
  `param_type` varchar(100) DEFAULT NULL COMMENT '参数类型',
  `value` varchar(1000) DEFAULT NULL COMMENT '参数值',
  `description` varchar(100) DEFAULT NULL COMMENT '参数描述',
  `sort_order` int(2) DEFAULT NULL COMMENT '排序',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(20) DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `test_case_config`
--

LOCK TABLES `test_case_config` WRITE;
/*!40000 ALTER TABLE `test_case_config` DISABLE KEYS */;
INSERT INTO `test_case_config` VALUES
(1926167379017068545,'2ba14de42e45475cb8528febd2f7c754','876b5f2d0a9e4181b20deac4782b21af',NULL,NULL,'host','String','http://localhost:8888',NULL,0,1748069064277,1748069064277);
/*!40000 ALTER TABLE `test_case_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Primary key, auto-incremented',
  `user_id` varchar(255) NOT NULL COMMENT '用户ID',
  `user_name` varchar(255) NOT NULL COMMENT '用户名称',
  `nick_name` varchar(255) DEFAULT NULL COMMENT '用户昵称',
  `password` varchar(255) NOT NULL COMMENT '用户密码',
  `salt` varchar(255) DEFAULT NULL COMMENT '密码加密盐',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '用户状态 1 正常 0 冻结',
  `create_time` bigint(20) NOT NULL COMMENT '创建时间',
  `update_time` bigint(20) NOT NULL COMMENT '修改时间',
  `group_id` varchar(64) DEFAULT NULL COMMENT '群组ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES
(1,'d595a3d55b4e47978dd25cb8acb3e753','windy','管理员','admin','f5ca4b5992bd4366b84a046cc7ee18fd',1,1734488863245,1734488863245,'6af9723bf78445b1a2c2948bc04d97c8');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_role`
--

DROP TABLE IF EXISTS `user_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_role` (
  `user_id` varchar(64) NOT NULL DEFAULT '' COMMENT '用户ID',
  `role_id` varchar(64) NOT NULL DEFAULT '' COMMENT '角色ID',
  PRIMARY KEY (`user_id`,`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_role`
--

LOCK TABLES `user_role` WRITE;
/*!40000 ALTER TABLE `user_role` DISABLE KEYS */;
INSERT INTO `user_role` VALUES
('6af9723bf78445b1a2c2948bc04d97c8','5422e309eb7545279a33787706112601'),
('6af9723bf78445b1a2c2948bc04d97c8','5422e309eb7545279a3378ui706112602'),
('6af9723bf78445b1a2c2948bc04d97c8','6f1b9ec0df86416186b8c0bf2732b8f0'),
('6af9723bf78445b1a2c2948bc04d97c8','7ba4833884d54607b2af7267ef09984e'),
('6af9723bf78445b1a2c2948bc04d97c8','98322792c9f84e199b75d34ab0b4a92e'),
('6af9723bf78445b1a2c2948bc04d97c8','ea7de5d96d2e4e4992b98baef75d4238'),
('6af9723bf78445b1a2c2948bc04d97c8','fa16a5f788784e25a280fb6adadfc831'),
('8484171e0805487ca4afdb990c2a74d8','6f1b9ec0df86416186b8c0bf2732b8f0');
/*!40000 ALTER TABLE `user_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `windy_group`
--

DROP TABLE IF EXISTS `windy_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `windy_group` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Primary key, auto-incremented',
  `group_id` varchar(255) NOT NULL COMMENT '组织ID',
  `group_name` varchar(255) NOT NULL COMMENT '组织名称',
  `description` varchar(1024) DEFAULT NULL COMMENT '组织描述',
  `create_time` bigint(20) NOT NULL COMMENT '创建时间',
  `update_time` bigint(20) NOT NULL COMMENT '修改时间',
  `parent_id` varchar(64) DEFAULT NULL COMMENT '父组织id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COMMENT='组织表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `windy_group`
--

LOCK TABLES `windy_group` WRITE;
/*!40000 ALTER TABLE `windy_group` DISABLE KEYS */;
INSERT INTO `windy_group` VALUES
(1,'6af9723bf78445b1a2c2948bc04d97c8','默认组织',NULL,1724926244041,1734488795970,NULL);
/*!40000 ALTER TABLE `windy_group` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `work_task`
--

DROP TABLE IF EXISTS `work_task`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `work_task` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `task_id` varchar(64) DEFAULT NULL COMMENT '工作项ID',
  `task_name` varchar(50) DEFAULT NULL COMMENT '工作项名称',
  `description` varchar(256) DEFAULT NULL COMMENT '工作项描述',
  `related_id` varchar(64) DEFAULT NULL COMMENT '关联需求、任务ID',
  `related_type` int(11) DEFAULT NULL COMMENT '关联类型(需求、缺陷)',
  `creator` varchar(64) DEFAULT NULL COMMENT '创建人ID',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(20) DEFAULT NULL COMMENT '修改时间',
  `status` int(2) DEFAULT '1' COMMENT '工作项状态',
  `workload` int(11) DEFAULT NULL COMMENT '工作量(人日)',
  `complete_time` bigint(20) DEFAULT NULL COMMENT '完成时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `work_task`
--

LOCK TABLES `work_task` WRITE;
/*!40000 ALTER TABLE `work_task` DISABLE KEYS */;
/*!40000 ALTER TABLE `work_task` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'windy'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*M!100616 SET NOTE_VERBOSITY=@OLD_NOTE_VERBOSITY */;

-- Dump completed on 2025-05-24 16:29:04
