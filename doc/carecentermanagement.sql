/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 50719
 Source Host           : localhost:3306
 Source Schema         : carecentermanagement

 Target Server Type    : MySQL
 Target Server Version : 50719
 File Encoding         : 65001

 Date: 25/05/2026 00:10:21
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for admin
-- ----------------------------
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `age` int(11) NULL DEFAULT NULL,
  `gender` enum('男','女') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `phone_num` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `dormbuild_id` int(11) NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username`) USING BTREE,
  INDEX `idx_dormbuild_id`(`dormbuild_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of admin
-- ----------------------------
INSERT INTO `admin` VALUES (1, 'admin', '$2a$10$GrFZuvtO8KTTJvNOvnU1hug.4Lm2VClQZ0BJaYdDDG.stYab/.ZW.', '大强', 18, '男', '14785412478', NULL, NULL, '2026-03-03 12:42:10', '2026-03-30 20:10:59');
INSERT INTO `admin` VALUES (2, 'Atest', '123456', '测试管理员', 22, '男', '14785412478', NULL, NULL, '2026-03-03 12:42:10', '2026-03-03 12:42:10');
INSERT INTO `admin` VALUES (3, 'dorm1', '$2a$10$.n6ychQOHJACd//uoJl/sugyWBEFhU7kYuktdvB/TTf7ciykVDQx2', '张三', 35, '男', '15222223333', '12@email.com', 1, '2026-03-03 12:42:10', '2026-05-01 01:33:27');
INSERT INTO `admin` VALUES (4, 'dorm2', '123456', '李四', 55, '女', '15333332222', NULL, 2, '2026-03-03 12:42:10', '2026-03-03 12:42:10');
INSERT INTO `admin` VALUES (5, 'dorm3', '123456', '王五', 38, '男', '15855552222', NULL, 3, '2026-03-03 12:42:10', '2026-03-03 12:42:10');
INSERT INTO `admin` VALUES (6, 'dorm4', '123456', '赵花', 40, '女', '15877776666', NULL, 4, '2026-03-03 12:42:10', '2026-03-03 12:42:10');
INSERT INTO `admin` VALUES (7, 'Mtest', '123456', '宿管测试', 22, '男', '15899999999', NULL, 2, '2026-03-03 12:42:10', '2026-03-03 12:42:10');

-- ----------------------------
-- Table structure for approval_request
-- ----------------------------
DROP TABLE IF EXISTS `approval_request`;
CREATE TABLE `approval_request`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `request_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'meal_reservation, visitor_appointment, leave_request',
  `requester_username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `requester_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'parent, student',
  `student_username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `student_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'pending, approved, rejected, cancelled',
  `reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `admin_reply` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `request_data` json NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_request_type`(`request_type`) USING BTREE,
  INDEX `idx_requester`(`requester_username`) USING BTREE,
  INDEX `idx_student`(`student_username`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '统一审批请求表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of approval_request
-- ----------------------------
INSERT INTO `approval_request` VALUES (1, 'meal_reservation', 'parent1', 'parent', 'stu1', '张三', 'approved', NULL, '管理员已批准', '{\"meal_type\": \"lunch\", \"mealConfirmed\": true, \"reservation_date\": \"2026-03-04\", \"mealConfirmedTime\": \"2026-03-04T02:21:06.958\"}', '2026-03-04 02:20:50', '2026-03-04 02:21:07');
INSERT INTO `approval_request` VALUES (2, 'meal_reservation', 'parent1', 'parent', 'stu1', '张三', 'approved', NULL, '管理员已批准', '{\"meal_type\": \"lunch\", \"mealConfirmed\": true, \"reservation_date\": \"2026-03-05\", \"mealConfirmedTime\": \"2026-03-04T02:21:09.326\"}', '2026-03-04 02:20:50', '2026-03-04 02:21:09');
INSERT INTO `approval_request` VALUES (3, 'meal_reservation', 'parent1', 'parent', 'stu1', '张三', 'approved', NULL, '管理员已批准', '{\"meal_type\": \"lunch\", \"mealConfirmed\": true, \"reservation_date\": \"2026-03-06\", \"mealConfirmedTime\": \"2026-03-04T02:21:11.173\"}', '2026-03-04 02:20:50', '2026-03-04 02:21:11');
INSERT INTO `approval_request` VALUES (5, 'visitor_appointment', 'parent1', 'parent', 'stu1', '张三', 'approved', NULL, '', '{\"visit_date\": \"2026-03-09\", \"visit_time\": \"下午 (14:00-17:00)\", \"parent_name\": \"张父\", \"parent_phone\": \"13800138000\", \"visitor_name\": null, \"visit_purpose\": \"给孩子送衣服\", \"visitor_count\": 1, \"visitor_phone\": null, \"visitor_id_card\": null}', '2026-03-07 14:44:11', '2026-03-07 14:48:21');
INSERT INTO `approval_request` VALUES (6, 'bed_selection', '1432', 'parent', 'shanshan', '姗姗', 'approved', '1', '管理员已批准，已完成床位分配', '{\"grade\": null, \"bed_num\": 3, \"bed_name\": \"third_bed\", \"idem_key\": \"bed_1772886031704_xqfa0n8a\", \"approved_by\": \"admin\", \"dorm_room_id\": \"1104\", \"requested_at\": \"2026-03-07T20:20:31.803\", \"approved_time\": \"2026-03-07T22:11:02.060\", \"dorm_build_id\": 1, \"schedule_type\": null, \"diet_preference\": null, \"allergy_preference\": null}', '2026-03-07 20:20:32', '2026-03-07 22:11:02');
INSERT INTO `approval_request` VALUES (7, 'meal_reservation', 'jiajia', 'parent', NULL, NULL, 'approved', NULL, '管理员已批准', '{\"meal_type\": \"lunch\", \"mealConfirmed\": true, \"reservation_date\": \"2026-03-20\", \"mealConfirmedTime\": \"2026-03-16T11:58:52.141\"}', '2026-03-16 11:33:05', '2026-03-16 11:58:52');
INSERT INTO `approval_request` VALUES (8, 'meal_reservation', 'jiajia', 'parent', NULL, NULL, 'approved', NULL, '管理员已批准', '{\"meal_type\": \"lunch\", \"mealConfirmed\": true, \"reservation_date\": \"2026-03-22\", \"mealConfirmedTime\": \"2026-03-16T11:59:03.583\"}', '2026-03-16 11:36:40', '2026-03-16 11:59:04');
INSERT INTO `approval_request` VALUES (9, 'meal_reservation', 'jiajia', 'parent', NULL, NULL, 'approved', NULL, '管理员已批准', '{\"meal_type\": \"lunch\", \"mealConfirmed\": true, \"reservation_date\": \"2026-03-21\", \"mealConfirmedTime\": \"2026-03-16T11:58:54.286\"}', '2026-03-16 11:36:40', '2026-03-16 11:58:54');
INSERT INTO `approval_request` VALUES (10, 'meal_reservation', 'jiajia', 'parent', NULL, NULL, 'approved', NULL, '管理员已批准', '{\"meal_type\": \"dinner\", \"mealConfirmed\": true, \"reservation_date\": \"2026-03-19\", \"mealConfirmedTime\": \"2026-03-16T11:59:00.469\"}', '2026-03-16 11:38:01', '2026-03-16 11:59:00');
INSERT INTO `approval_request` VALUES (11, 'meal_reservation', 'jiajia', 'parent', NULL, NULL, 'approved', NULL, '管理员已批准', '{\"meal_type\": \"lunch\", \"mealConfirmed\": true, \"reservation_date\": \"2026-03-19\", \"mealConfirmedTime\": \"2026-03-16T11:58:49.965\"}', '2026-03-16 11:45:45', '2026-03-16 11:58:50');
INSERT INTO `approval_request` VALUES (12, 'meal_reservation', 'jiajia', 'parent', NULL, NULL, 'approved', NULL, '管理员已批准', '{\"meal_type\": \"lunch\", \"reservation_date\": \"2026-04-03\"}', '2026-03-16 11:45:45', '2026-03-16 11:58:35');
INSERT INTO `approval_request` VALUES (14, 'leave_request', 'stu1', 'student', 'stu1', '张三', 'approved', '回家换衣服', '同意请假', '{\"end_time\": \"2026-03-18 13:00:00\", \"leave_type\": \"home\", \"start_time\": \"2026-03-18 12:00:00\", \"destination\": \"\", \"return_time\": \"2026-03-16 04:24:44\", \"contact_phone\": \"\", \"parent_username\": \"parent1\", \"return_cancelled\": true}', '2026-03-16 12:23:33', '2026-03-16 12:24:45');
INSERT INTO `approval_request` VALUES (15, 'bed_selection', 'parent1', 'parent', 'stu1', '张三', 'approved', '家长申请床位更换', '管理员已批准，已完成床位分配', '{\"grade\": null, \"bed_num\": 3, \"bed_name\": \"third_bed\", \"idem_key\": \"bed_1773638674494_q5frzhyp\", \"approved_by\": \"admin\", \"requested_at\": \"2026-03-16T13:24:34.519\", \"approved_time\": \"2026-03-16T13:25:47.622\", \"schedule_type\": null, \"center_room_id\": \"1101\", \"origin_bed_num\": 1, \"diet_preference\": null, \"origin_bed_name\": \"first_bed\", \"allergy_preference\": null, \"center_building_id\": 1, \"origin_center_room_id\": \"1201\"}', '2026-03-16 13:24:35', '2026-03-16 13:25:48');
INSERT INTO `approval_request` VALUES (16, 'visitor_appointment', 'jiajia', 'parent', NULL, '', 'pending', NULL, NULL, '{\"visit_date\": \"2026-03-18\", \"visit_time\": \"下午 (14:00-17:00)\", \"parent_name\": \"佳佳\", \"parent_phone\": \"13557936673\", \"visitor_name\": null, \"visit_purpose\": \"来看午托环境\", \"visitor_count\": 1, \"visitor_phone\": null, \"visitor_id_card\": null}', '2026-03-16 13:57:19', '2026-03-16 13:57:19');
INSERT INTO `approval_request` VALUES (17, 'visitor_appointment', 'parent1', 'parent', 'stu1', '张三', 'approved', NULL, '', '{\"visit_date\": \"2026-05-08\", \"visit_time\": \"下午 (14:00-17:00)\", \"parent_name\": \"张父\", \"parent_phone\": \"13800138000\", \"visitor_name\": null, \"visit_purpose\": \"来给孩子换被子\", \"visitor_count\": 1, \"visitor_phone\": null, \"visitor_id_card\": null}', '2026-05-04 23:58:04', '2026-05-05 00:01:49');

-- ----------------------------
-- Table structure for billing_record
-- ----------------------------
DROP TABLE IF EXISTS `billing_record`;
CREATE TABLE `billing_record`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `student_username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `student_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `bill_month` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `bill_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'meal, payment',
  `boarding_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `breakfast_days` int(11) NULL DEFAULT NULL,
  `lunch_days` int(11) NULL DEFAULT NULL,
  `dinner_days` int(11) NULL DEFAULT NULL,
  `breakfast_price` decimal(10, 2) NULL DEFAULT NULL,
  `lunch_price` decimal(10, 2) NULL DEFAULT NULL,
  `dinner_price` decimal(10, 2) NULL DEFAULT NULL,
  `boarding_fee` decimal(10, 2) NULL DEFAULT NULL,
  `leave_deduct_days` int(11) NULL DEFAULT NULL,
  `meal_total` decimal(10, 2) NULL DEFAULT NULL,
  `total_amount` decimal(10, 2) NOT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'unpaid, paid, overdue',
  `payment_time` datetime NULL DEFAULT NULL,
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_student`(`student_username`) USING BTREE,
  INDEX `idx_bill_month`(`bill_month`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_bill_type`(`bill_type`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 50 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '统一账单记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of billing_record
-- ----------------------------
INSERT INTO `billing_record` VALUES (1, 'stu1', '张三', '2026-03', 'meal', 'meal_and_rest', 0, 3, 0, 0.00, 15.00, 20.00, 500.00, 0, 45.00, 545.00, 'paid', '2026-05-15 00:35:46', NULL, '2026-03-04 16:09:54', '2026-05-15 00:35:46');
INSERT INTO `billing_record` VALUES (2, 'stu10', '马克', '2026-03', 'meal', 'meal_and_rest', 0, 0, 0, 0.00, 15.00, 20.00, 500.00, 0, 0.00, 500.00, 'paid', '2026-05-15 00:35:46', NULL, '2026-03-04 16:09:54', '2026-05-15 00:35:46');
INSERT INTO `billing_record` VALUES (3, 'stu11', '巧巧', '2026-03', 'meal', 'meal_and_rest', 0, 0, 0, 0.00, 15.00, 20.00, 500.00, 0, 0.00, 500.00, 'paid', '2026-05-15 00:35:46', NULL, '2026-03-04 16:09:54', '2026-05-15 00:35:46');
INSERT INTO `billing_record` VALUES (4, 'stu12', '丽丽', '2026-03', 'meal', 'meal_and_rest', 0, 0, 0, 0.00, 15.00, 20.00, 500.00, 0, 0.00, 500.00, 'paid', '2026-05-15 00:35:46', NULL, '2026-03-04 16:09:54', '2026-05-15 00:35:46');
INSERT INTO `billing_record` VALUES (5, 'stu13', '美美', '2026-03', 'meal', 'meal_and_rest', 0, 0, 0, 0.00, 15.00, 20.00, 500.00, 0, 0.00, 500.00, 'paid', '2026-05-15 00:35:46', NULL, '2026-03-04 16:09:54', '2026-05-15 00:35:46');
INSERT INTO `billing_record` VALUES (6, 'stu14', '拉拉', '2026-03', 'meal', 'meal_and_rest', 0, 0, 0, 0.00, 15.00, 20.00, 500.00, 0, 0.00, 500.00, 'paid', '2026-05-15 00:35:46', NULL, '2026-03-04 16:09:54', '2026-05-15 00:35:46');
INSERT INTO `billing_record` VALUES (7, 'stu15', '贝贝', '2026-03', 'meal', 'meal_only', 0, 0, 0, 0.00, 15.00, 20.00, 0.00, 0, 0.00, 0.00, 'paid', '2026-05-15 00:35:46', NULL, '2026-03-04 16:09:54', '2026-05-15 00:35:46');
INSERT INTO `billing_record` VALUES (8, 'stu16', '力力', '2026-03', 'meal', 'meal_only', 0, 0, 0, 0.00, 15.00, 20.00, 0.00, 0, 0.00, 0.00, 'paid', '2026-05-15 00:35:46', NULL, '2026-03-04 16:09:54', '2026-05-15 00:35:46');
INSERT INTO `billing_record` VALUES (9, 'stu17', '阿成', '2026-03', 'meal', 'meal_only', 0, 0, 0, 0.00, 15.00, 20.00, 0.00, 0, 0.00, 0.00, 'paid', '2026-05-15 00:35:46', NULL, '2026-03-04 16:09:54', '2026-05-15 00:35:46');
INSERT INTO `billing_record` VALUES (10, 'stu18', '阿达', '2026-03', 'meal', 'meal_and_rest', 0, 0, 0, 0.00, 15.00, 20.00, 500.00, 0, 0.00, 500.00, 'paid', '2026-05-15 00:35:46', NULL, '2026-03-04 16:09:54', '2026-05-15 00:35:46');
INSERT INTO `billing_record` VALUES (11, 'stu19', '帕森斯', '2026-03', 'meal', 'meal_and_rest', 0, 0, 0, 0.00, 15.00, 20.00, 500.00, 0, 0.00, 500.00, 'paid', '2026-05-15 00:35:46', NULL, '2026-03-04 16:09:54', '2026-05-15 00:35:46');
INSERT INTO `billing_record` VALUES (12, 'stu2', '田田', '2026-03', 'meal', 'meal_and_rest', 0, 0, 0, 0.00, 15.00, 20.00, 500.00, 0, 0.00, 500.00, 'paid', '2026-05-15 00:35:46', NULL, '2026-03-04 16:09:54', '2026-05-15 00:35:46');
INSERT INTO `billing_record` VALUES (13, 'stu20', '柠檬', '2026-03', 'meal', 'meal_and_rest', 0, 0, 0, 0.00, 15.00, 20.00, 500.00, 0, 0.00, 500.00, 'paid', '2026-05-15 00:35:46', NULL, '2026-03-04 16:09:54', '2026-05-15 00:35:46');
INSERT INTO `billing_record` VALUES (14, 'stu21', '面对', '2026-03', 'meal', 'meal_only', 0, 0, 0, 0.00, 15.00, 20.00, 0.00, 0, 0.00, 0.00, 'paid', '2026-05-15 00:35:46', NULL, '2026-03-04 16:09:54', '2026-05-15 00:35:46');
INSERT INTO `billing_record` VALUES (15, 'stu22', '等等', '2026-03', 'meal', 'meal_only', 0, 0, 0, 0.00, 15.00, 20.00, 0.00, 0, 0.00, 0.00, 'paid', '2026-05-15 00:35:46', NULL, '2026-03-04 16:09:54', '2026-05-15 00:35:46');
INSERT INTO `billing_record` VALUES (16, 'stu3', '吉安', '2026-03', 'meal', 'meal_only', 0, 0, 0, 0.00, 15.00, 20.00, 0.00, 0, 0.00, 0.00, 'paid', '2026-05-15 00:35:46', NULL, '2026-03-04 16:09:54', '2026-05-15 00:35:46');
INSERT INTO `billing_record` VALUES (17, 'stu4', '力力', '2026-03', 'meal', 'meal_only', 0, 0, 0, 0.00, 15.00, 20.00, 0.00, 0, 0.00, 0.00, 'paid', '2026-05-15 00:35:46', NULL, '2026-03-04 16:09:54', '2026-05-15 00:35:46');
INSERT INTO `billing_record` VALUES (18, 'stu5', '哦哦', '2026-03', 'meal', 'meal_only', 0, 0, 0, 0.00, 15.00, 20.00, 0.00, 0, 0.00, 0.00, 'paid', '2026-05-15 00:35:46', NULL, '2026-03-04 16:09:54', '2026-05-15 00:35:46');
INSERT INTO `billing_record` VALUES (19, 'stu6', '泡泡', '2026-03', 'meal', 'meal_only', 0, 0, 0, 0.00, 15.00, 20.00, 0.00, 0, 0.00, 0.00, 'paid', '2026-05-15 00:35:46', NULL, '2026-03-04 16:09:54', '2026-05-15 00:35:46');
INSERT INTO `billing_record` VALUES (20, 'stu7', '刚刚', '2026-03', 'meal', 'meal_only', 0, 0, 0, 0.00, 15.00, 20.00, 0.00, 0, 0.00, 0.00, 'paid', '2026-05-15 00:35:46', NULL, '2026-03-04 16:09:54', '2026-05-15 00:35:46');
INSERT INTO `billing_record` VALUES (21, 'stu8', '七七', '2026-03', 'meal', 'meal_only', 0, 0, 0, 0.00, 15.00, 20.00, 0.00, 0, 0.00, 0.00, 'paid', '2026-05-15 00:35:46', NULL, '2026-03-04 16:09:54', '2026-05-15 00:35:46');
INSERT INTO `billing_record` VALUES (22, 'stu9', '德萨', '2026-03', 'meal', 'meal_and_rest', 0, 0, 0, 0.00, 15.00, 20.00, 500.00, 0, 0.00, 500.00, 'paid', '2026-05-15 00:35:46', NULL, '2026-03-04 16:09:54', '2026-05-15 00:35:46');
INSERT INTO `billing_record` VALUES (23, 'shanshan', '姗姗', '2026-03', 'meal', 'meal_and_rest', 0, 0, 0, 0.00, 15.00, 20.00, 500.00, 0, 0.00, 500.00, 'paid', '2026-05-15 00:35:46', NULL, '2026-03-16 11:59:54', '2026-05-15 00:35:46');
INSERT INTO `billing_record` VALUES (24, 'jiajia', '佳佳', '2026-03', 'meal', 'meal_only', 0, 4, 1, 0.00, 15.00, 20.00, 0.00, 0, 80.00, 80.00, 'paid', '2026-05-15 00:35:46', 'parent_self_meal_bill', '2026-03-16 11:59:54', '2026-05-15 00:35:46');
INSERT INTO `billing_record` VALUES (25, 'shanshan', '姗姗', '2026-04', 'meal', 'meal_and_rest', 0, 0, 0, 0.00, 15.00, 20.00, 500.00, 0, 0.00, 500.00, 'paid', '2026-05-15 00:35:46', NULL, '2026-05-05 01:23:34', '2026-05-15 00:35:46');
INSERT INTO `billing_record` VALUES (26, 'stu1', '张三', '2026-04', 'meal', 'meal_and_rest', 0, 0, 0, 0.00, 15.00, 20.00, 500.00, 0, 0.00, 500.00, 'paid', '2026-05-15 00:35:46', NULL, '2026-05-05 01:23:34', '2026-05-15 00:35:46');
INSERT INTO `billing_record` VALUES (27, 'stu10', '马克', '2026-04', 'meal', 'meal_and_rest', 0, 0, 0, 0.00, 15.00, 20.00, 500.00, 0, 0.00, 500.00, 'paid', '2026-05-15 00:35:46', NULL, '2026-05-05 01:23:34', '2026-05-15 00:35:46');
INSERT INTO `billing_record` VALUES (28, 'stu11', '巧巧', '2026-04', 'meal', 'meal_and_rest', 0, 0, 0, 0.00, 15.00, 20.00, 500.00, 0, 0.00, 500.00, 'paid', '2026-05-15 00:35:46', NULL, '2026-05-05 01:23:34', '2026-05-15 00:35:46');
INSERT INTO `billing_record` VALUES (29, 'stu12', '丽丽', '2026-04', 'meal', 'meal_and_rest', 0, 0, 0, 0.00, 15.00, 20.00, 500.00, 0, 0.00, 500.00, 'paid', '2026-05-15 00:35:46', NULL, '2026-05-05 01:23:34', '2026-05-15 00:35:46');
INSERT INTO `billing_record` VALUES (30, 'stu13', '美美', '2026-04', 'meal', 'meal_and_rest', 0, 0, 0, 0.00, 15.00, 20.00, 500.00, 0, 0.00, 500.00, 'paid', '2026-05-15 00:35:46', NULL, '2026-05-05 01:23:34', '2026-05-15 00:35:46');
INSERT INTO `billing_record` VALUES (31, 'stu14', '拉拉', '2026-04', 'meal', 'meal_and_rest', 0, 0, 0, 0.00, 15.00, 20.00, 500.00, 0, 0.00, 500.00, 'paid', '2026-05-15 00:35:46', NULL, '2026-05-05 01:23:34', '2026-05-15 00:35:46');
INSERT INTO `billing_record` VALUES (32, 'stu15', '贝贝', '2026-04', 'meal', 'meal_only', 0, 0, 0, 0.00, 15.00, 20.00, 0.00, 0, 0.00, 0.00, 'paid', '2026-05-15 00:35:46', NULL, '2026-05-05 01:23:34', '2026-05-15 00:35:46');
INSERT INTO `billing_record` VALUES (33, 'stu16', '力力', '2026-04', 'meal', 'meal_only', 0, 0, 0, 0.00, 15.00, 20.00, 0.00, 0, 0.00, 0.00, 'paid', '2026-05-15 00:35:46', NULL, '2026-05-05 01:23:34', '2026-05-15 00:35:46');
INSERT INTO `billing_record` VALUES (34, 'stu17', '阿成', '2026-04', 'meal', 'meal_only', 0, 0, 0, 0.00, 15.00, 20.00, 0.00, 0, 0.00, 0.00, 'paid', '2026-05-15 00:35:46', NULL, '2026-05-05 01:23:34', '2026-05-15 00:35:46');
INSERT INTO `billing_record` VALUES (35, 'stu18', '阿达', '2026-04', 'meal', 'meal_and_rest', 0, 0, 0, 0.00, 15.00, 20.00, 500.00, 0, 0.00, 500.00, 'paid', '2026-05-15 00:35:46', NULL, '2026-05-05 01:23:34', '2026-05-15 00:35:46');
INSERT INTO `billing_record` VALUES (36, 'stu19', '帕森斯', '2026-04', 'meal', 'meal_and_rest', 0, 0, 0, 0.00, 15.00, 20.00, 500.00, 0, 0.00, 500.00, 'paid', '2026-05-15 00:35:46', NULL, '2026-05-05 01:23:34', '2026-05-15 00:35:46');
INSERT INTO `billing_record` VALUES (37, 'stu2', '田田', '2026-04', 'meal', 'meal_and_rest', 0, 0, 0, 0.00, 15.00, 20.00, 500.00, 0, 0.00, 500.00, 'paid', '2026-05-15 00:35:46', NULL, '2026-05-05 01:23:34', '2026-05-15 00:35:46');
INSERT INTO `billing_record` VALUES (38, 'stu20', '柠檬', '2026-04', 'meal', 'meal_and_rest', 0, 0, 0, 0.00, 15.00, 20.00, 500.00, 0, 0.00, 500.00, 'paid', '2026-05-15 00:35:46', NULL, '2026-05-05 01:23:34', '2026-05-15 00:35:46');
INSERT INTO `billing_record` VALUES (39, 'stu21', '面对', '2026-04', 'meal', 'meal_only', 0, 0, 0, 0.00, 15.00, 20.00, 0.00, 0, 0.00, 0.00, 'paid', '2026-05-15 00:35:46', NULL, '2026-05-05 01:23:34', '2026-05-15 00:35:46');
INSERT INTO `billing_record` VALUES (40, 'stu22', '等等', '2026-04', 'meal', 'meal_only', 0, 0, 0, 0.00, 15.00, 20.00, 0.00, 0, 0.00, 0.00, 'paid', '2026-05-15 00:35:46', NULL, '2026-05-05 01:23:34', '2026-05-15 00:35:46');
INSERT INTO `billing_record` VALUES (41, 'stu3', '吉安', '2026-04', 'meal', 'meal_only', 0, 0, 0, 0.00, 15.00, 20.00, 0.00, 0, 0.00, 0.00, 'paid', '2026-05-15 00:35:46', NULL, '2026-05-05 01:23:34', '2026-05-15 00:35:46');
INSERT INTO `billing_record` VALUES (42, 'stu4', '力力', '2026-04', 'meal', 'meal_only', 0, 0, 0, 0.00, 15.00, 20.00, 0.00, 0, 0.00, 0.00, 'paid', '2026-05-15 00:35:46', NULL, '2026-05-05 01:23:34', '2026-05-15 00:35:46');
INSERT INTO `billing_record` VALUES (43, 'stu5', '哦哦', '2026-04', 'meal', 'meal_only', 0, 0, 0, 0.00, 15.00, 20.00, 0.00, 0, 0.00, 0.00, 'paid', '2026-05-15 00:35:46', NULL, '2026-05-05 01:23:34', '2026-05-15 00:35:46');
INSERT INTO `billing_record` VALUES (44, 'stu6', '泡泡', '2026-04', 'meal', 'meal_only', 0, 0, 0, 0.00, 15.00, 20.00, 0.00, 0, 0.00, 0.00, 'paid', '2026-05-15 00:35:46', NULL, '2026-05-05 01:23:34', '2026-05-15 00:35:46');
INSERT INTO `billing_record` VALUES (45, 'stu7', '刚刚', '2026-04', 'meal', 'meal_only', 0, 0, 0, 0.00, 15.00, 20.00, 0.00, 0, 0.00, 0.00, 'paid', '2026-05-15 00:35:46', NULL, '2026-05-05 01:23:34', '2026-05-15 00:35:46');
INSERT INTO `billing_record` VALUES (46, 'stu8', '七七', '2026-04', 'meal', 'meal_only', 0, 0, 0, 0.00, 15.00, 20.00, 0.00, 0, 0.00, 0.00, 'paid', '2026-05-15 00:35:46', NULL, '2026-05-05 01:23:34', '2026-05-15 00:35:46');
INSERT INTO `billing_record` VALUES (47, 'stu9', '德萨', '2026-04', 'meal', 'meal_and_rest', 0, 0, 0, 0.00, 15.00, 20.00, 500.00, 0, 0.00, 500.00, 'paid', '2026-05-15 00:35:46', NULL, '2026-05-05 01:23:34', '2026-05-15 00:35:46');
INSERT INTO `billing_record` VALUES (48, 'happy', '开心', '2026-04', 'meal', 'meal_and_rest', 0, 0, 0, 0.00, 15.00, 20.00, 500.00, 0, 0.00, 500.00, 'paid', '2026-05-15 00:35:46', NULL, '2026-05-05 01:23:34', '2026-05-15 00:35:46');
INSERT INTO `billing_record` VALUES (49, 'jiajia', '佳佳', '2026-04', 'meal', 'meal_only', 0, 1, 0, 0.00, 15.00, 20.00, 0.00, 0, 15.00, 15.00, 'paid', '2026-05-15 00:35:46', 'parent_self_meal_bill', '2026-05-05 01:23:34', '2026-05-15 00:35:46');

-- ----------------------------
-- Table structure for center_building
-- ----------------------------
DROP TABLE IF EXISTS `center_building`;
CREATE TABLE `center_building`  (
  `dormbuild_id` int(11) NOT NULL COMMENT '宿舍楼号码',
  `dormbuild_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '宿舍楼名称',
  `dormbuild_detail` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '宿舍楼备注',
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of center_building
-- ----------------------------
INSERT INTO `center_building` VALUES (1, '鼓楼店', '', 1);
INSERT INTO `center_building` VALUES (2, '溧水店', '', 2);
INSERT INTO `center_building` VALUES (3, '玄武店', '', 3);
INSERT INTO `center_building` VALUES (4, '江宁店', '', 4);

-- ----------------------------
-- Table structure for center_room
-- ----------------------------
DROP TABLE IF EXISTS `center_room`;
CREATE TABLE `center_room`  (
  `dormroom_id` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '宿舍房间号',
  `dormbuild_id` int(11) NOT NULL COMMENT '宿舍楼号',
  `floor_num` int(11) NOT NULL COMMENT '楼层',
  `max_capacity` int(11) NOT NULL DEFAULT 4 COMMENT '房间最大入住人数',
  `current_capacity` int(11) NOT NULL DEFAULT 0 COMMENT '当前房间入住人数',
  `first_bed` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '一号床位',
  `second_bed` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '二号床位',
  `third_bed` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '三号床位',
  `fourth_bed` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '四号床位',
  PRIMARY KEY (`dormroom_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of center_room
-- ----------------------------
INSERT INTO `center_room` VALUES ('1101', 1, 1, 8, 5, 'stu22', NULL, 'stu1', 'stu4');
INSERT INTO `center_room` VALUES ('1103', 1, 1, 4, 4, 'stu8', 'stu9', 'stu10', 'stu11');
INSERT INTO `center_room` VALUES ('1104', 1, 1, 4, 3, 'stu2', 'stu3', 'shanshan', NULL);
INSERT INTO `center_room` VALUES ('1201', 1, 2, 3, 1, NULL, 'stu5', NULL, NULL);
INSERT INTO `center_room` VALUES ('2101', 2, 1, 4, 4, 'stu12', 'stu13', 'stu14', 'stu20');
INSERT INTO `center_room` VALUES ('3101', 3, 1, 4, 3, 'stu15', 'stu16', 'stu16', NULL);
INSERT INTO `center_room` VALUES ('4102', 4, 1, 4, 3, 'stu17', 'stu18', 'stu19', NULL);

-- ----------------------------
-- Table structure for center_room_bed
-- ----------------------------
DROP TABLE IF EXISTS `center_room_bed`;
CREATE TABLE `center_room_bed`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `dormroom_id` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '宿舍房间号',
  `bed_no` int(11) NOT NULL COMMENT '床位编号',
  `occupant_username` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '入住学生用户名',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_room_bed`(`dormroom_id`, `bed_no`) USING BTREE,
  INDEX `idx_room`(`dormroom_id`) USING BTREE,
  INDEX `idx_occupant`(`occupant_username`) USING BTREE,
  CONSTRAINT `fk_center_room_bed_room` FOREIGN KEY (`dormroom_id`) REFERENCES `center_room` (`dormroom_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 33 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '宿舍床位明细表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of center_room_bed
-- ----------------------------
INSERT INTO `center_room_bed` VALUES (1, '1101', 1, 'stu22');
INSERT INTO `center_room_bed` VALUES (2, '1103', 1, 'stu8');
INSERT INTO `center_room_bed` VALUES (3, '1104', 1, 'stu2');
INSERT INTO `center_room_bed` VALUES (4, '1201', 1, NULL);
INSERT INTO `center_room_bed` VALUES (5, '2101', 1, 'stu12');
INSERT INTO `center_room_bed` VALUES (6, '3101', 1, 'stu15');
INSERT INTO `center_room_bed` VALUES (7, '4102', 1, 'stu17');
INSERT INTO `center_room_bed` VALUES (8, '1101', 2, 'stu6');
INSERT INTO `center_room_bed` VALUES (9, '1103', 2, 'stu9');
INSERT INTO `center_room_bed` VALUES (10, '1104', 2, 'stu3');
INSERT INTO `center_room_bed` VALUES (11, '1201', 2, 'stu5');
INSERT INTO `center_room_bed` VALUES (12, '2101', 2, 'stu13');
INSERT INTO `center_room_bed` VALUES (13, '3101', 2, 'stu16');
INSERT INTO `center_room_bed` VALUES (14, '4102', 2, 'stu18');
INSERT INTO `center_room_bed` VALUES (15, '1101', 3, 'stu1');
INSERT INTO `center_room_bed` VALUES (16, '1103', 3, 'stu10');
INSERT INTO `center_room_bed` VALUES (17, '1104', 3, 'shanshan');
INSERT INTO `center_room_bed` VALUES (18, '1201', 3, NULL);
INSERT INTO `center_room_bed` VALUES (19, '2101', 3, 'stu14');
INSERT INTO `center_room_bed` VALUES (20, '3101', 3, 'stu16');
INSERT INTO `center_room_bed` VALUES (21, '4102', 3, 'stu19');
INSERT INTO `center_room_bed` VALUES (22, '1101', 4, 'stu4');
INSERT INTO `center_room_bed` VALUES (23, '1103', 4, 'stu11');
INSERT INTO `center_room_bed` VALUES (24, '1104', 4, NULL);
INSERT INTO `center_room_bed` VALUES (25, '1201', 4, NULL);
INSERT INTO `center_room_bed` VALUES (26, '2101', 4, 'stu20');
INSERT INTO `center_room_bed` VALUES (27, '3101', 4, NULL);
INSERT INTO `center_room_bed` VALUES (28, '4102', 4, NULL);
INSERT INTO `center_room_bed` VALUES (29, '1101', 5, 'stu7');
INSERT INTO `center_room_bed` VALUES (30, '1101', 6, NULL);
INSERT INTO `center_room_bed` VALUES (31, '1101', 7, NULL);
INSERT INTO `center_room_bed` VALUES (32, '1101', 8, NULL);

-- ----------------------------
-- Table structure for daily_menu_photo
-- ----------------------------
DROP TABLE IF EXISTS `daily_menu_photo`;
CREATE TABLE `daily_menu_photo`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `photo_date` date NOT NULL COMMENT '菜谱日期',
  `meal_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '餐次类型：breakfast(早餐)、lunch(午餐)、dinner(晚餐)',
  `photo_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '菜谱照片URL',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '菜谱描述',
  `uploader` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '上传者用户名',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_photo_date`(`photo_date`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '每日菜谱照片表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of daily_menu_photo
-- ----------------------------
INSERT INTO `daily_menu_photo` VALUES (1, '2026-03-02', 'lunch', 'f6301883e64f4e0e84808bd63f9ae60b.jpg', '番茄炒蛋', 'admin', '2026-03-02 13:05:26', '2026-03-02 13:05:26');
INSERT INTO `daily_menu_photo` VALUES (3, '2026-05-05', 'dinner', 'a0abffe22dd94dcf9e64ea96e3bcb607.jpg', '', 'admin', '2026-05-05 00:18:26', '2026-05-05 00:18:26');
INSERT INTO `daily_menu_photo` VALUES (9, '2026-05-05', 'lunch', '2d2139f83db948f0b7db88cca9e3d7ae.jpg', '', 'admin', '2026-05-05 01:02:39', '2026-05-05 01:02:39');
INSERT INTO `daily_menu_photo` VALUES (15, '2026-05-05', 'lunch', '3fbfd171d8c94ab9aa1732480f522763.jpg', '', 'admin', '2026-05-05 01:11:36', '2026-05-05 01:11:36');
INSERT INTO `daily_menu_photo` VALUES (18, '2026-05-15', 'lunch', '979c62b7d59741c6ac1e9d8a3ebaa918.jpg', '', 'admin', '2026-05-15 00:22:05', '2026-05-15 00:22:05');
INSERT INTO `daily_menu_photo` VALUES (19, '2026-05-15', 'lunch', 'c1a4d933742e4198ade30c627fa29126.jpg', '', 'admin', '2026-05-15 00:22:05', '2026-05-15 00:22:05');

-- ----------------------------
-- Table structure for discussion_message
-- ----------------------------
DROP TABLE IF EXISTS `discussion_message`;
CREATE TABLE `discussion_message`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `sender_username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '发送者用户名',
  `sender_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '发送者姓名',
  `sender_role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '发送者角色：admin(管理员)、parent(家长)',
  `content` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '消息内容',
  `message_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'text' COMMENT '消息类型：text(文字)、image(图片)',
  `image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '图片URL(当消息类型为image时)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sender`(`sender_username`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '讨论组消息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of discussion_message
-- ----------------------------
INSERT INTO `discussion_message` VALUES (2, 'admin', '大强', 'admin', '家长们好', 'text', NULL, '2026-03-02 05:27:25');
INSERT INTO `discussion_message` VALUES (3, 'parent1', '张父', 'parent', '你好', 'text', NULL, '2026-03-02 05:27:50');
INSERT INTO `discussion_message` VALUES (4, 'parent1', '张父', 'parent', '可以更改床位吗？', 'text', NULL, '2026-03-16 04:51:31');
INSERT INTO `discussion_message` VALUES (5, '1432', '姗姗爸爸', 'parent', '我也想知道', 'text', NULL, '2026-03-16 05:58:06');

-- ----------------------------
-- Table structure for dish_suggestion
-- ----------------------------
DROP TABLE IF EXISTS `dish_suggestion`;
CREATE TABLE `dish_suggestion`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户名(学号或家长账号)',
  `user_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户类型(student/parent)',
  `suggestion_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '建议类型(improvement/new_dish)',
  `dish_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '建议的菜品名称',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '建议内容',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'pending' COMMENT '状态(pending/reviewed/adopted)',
  `admin_reply` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '管理员回复',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user`(`user_username`, `user_type`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '菜品建议表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of dish_suggestion
-- ----------------------------
INSERT INTO `dish_suggestion` VALUES (1, 'parent1', 'parent', 'new_dish', '阿斯顿', '第三方', 'reviewed', '看不懂', '2026-03-02 17:34:57', '2026-03-03 01:27:18');
INSERT INTO `dish_suggestion` VALUES (2, 'parent1', 'parent', 'new_dish', '白切鸡', '白切鸡', 'adopted', '介绍', '2026-03-02 17:35:13', '2026-03-03 01:09:43');
INSERT INTO `dish_suggestion` VALUES (3, 'parent1', 'parent', 'new_dish', '煎酿三宝', '煎酿三宝', 'pending', NULL, '2026-03-03 01:26:56', '2026-03-03 01:26:56');

-- ----------------------------
-- Table structure for entry_exit_record
-- ----------------------------
DROP TABLE IF EXISTS `entry_exit_record`;
CREATE TABLE `entry_exit_record`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `student_username` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `student_name` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `record_type` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `record_time` datetime NOT NULL,
  `admin_username` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `status` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT 'normal',
  `alert_sent` tinyint(4) NULL DEFAULT 0,
  `appeal_reason` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `appeal_status` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 72 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of entry_exit_record
-- ----------------------------
INSERT INTO `entry_exit_record` VALUES (1, 'stu1', '张三', 'entry', '2026-03-02 01:35:39', NULL, 'normal', 0, NULL, NULL, '2026-03-02 01:35:39');
INSERT INTO `entry_exit_record` VALUES (2, 'stu10', '马克', 'entry', '2026-03-02 01:35:39', NULL, 'normal', 0, NULL, NULL, '2026-03-02 01:35:39');
INSERT INTO `entry_exit_record` VALUES (3, 'stu11', '巧巧', 'entry', '2026-03-02 01:35:39', NULL, 'normal', 0, NULL, NULL, '2026-03-02 01:35:39');
INSERT INTO `entry_exit_record` VALUES (4, 'stu12', '丽丽', 'entry', '2026-03-02 01:35:39', NULL, 'normal', 0, NULL, NULL, '2026-03-02 01:35:39');
INSERT INTO `entry_exit_record` VALUES (5, 'stu13', '美美', 'entry', '2026-03-02 01:35:39', NULL, 'normal', 0, NULL, NULL, '2026-03-02 01:35:39');
INSERT INTO `entry_exit_record` VALUES (6, 'stu14', '拉拉', 'entry', '2026-03-02 01:35:39', NULL, 'normal', 0, NULL, NULL, '2026-03-02 01:35:39');
INSERT INTO `entry_exit_record` VALUES (7, 'stu15', '贝贝', 'entry', '2026-03-02 01:35:39', NULL, 'normal', 0, NULL, NULL, '2026-03-02 01:35:39');
INSERT INTO `entry_exit_record` VALUES (8, 'stu16', '力力', 'entry', '2026-03-02 01:35:39', NULL, 'normal', 0, NULL, NULL, '2026-03-02 01:35:39');
INSERT INTO `entry_exit_record` VALUES (9, 'stu17', '阿成', 'entry', '2026-03-02 01:35:39', NULL, 'normal', 0, NULL, NULL, '2026-03-02 01:35:39');
INSERT INTO `entry_exit_record` VALUES (10, 'stu18', '阿达', 'entry', '2026-03-02 01:35:39', NULL, 'normal', 0, NULL, NULL, '2026-03-02 01:35:39');
INSERT INTO `entry_exit_record` VALUES (11, 'stu1', '张三', 'entry', '2026-03-02 01:35:51', NULL, 'normal', 0, NULL, NULL, '2026-03-02 01:35:51');
INSERT INTO `entry_exit_record` VALUES (12, 'stu10', '马克', 'entry', '2026-03-02 01:35:51', NULL, 'normal', 0, NULL, NULL, '2026-03-02 01:35:51');
INSERT INTO `entry_exit_record` VALUES (13, 'stu11', '巧巧', 'entry', '2026-03-02 01:35:51', NULL, 'normal', 0, NULL, NULL, '2026-03-02 01:35:51');
INSERT INTO `entry_exit_record` VALUES (14, 'stu12', '丽丽', 'entry', '2026-03-02 01:35:51', NULL, 'normal', 0, NULL, NULL, '2026-03-02 01:35:51');
INSERT INTO `entry_exit_record` VALUES (15, 'stu13', '美美', 'entry', '2026-03-02 01:35:51', NULL, 'normal', 0, NULL, NULL, '2026-03-02 01:35:51');
INSERT INTO `entry_exit_record` VALUES (16, 'stu14', '拉拉', 'entry', '2026-03-02 01:35:51', NULL, 'normal', 0, NULL, NULL, '2026-03-02 01:35:51');
INSERT INTO `entry_exit_record` VALUES (17, 'stu15', '贝贝', 'entry', '2026-03-02 01:35:51', NULL, 'normal', 0, NULL, NULL, '2026-03-02 01:35:51');
INSERT INTO `entry_exit_record` VALUES (18, 'stu16', '力力', 'entry', '2026-03-02 01:35:51', NULL, 'normal', 0, NULL, NULL, '2026-03-02 01:35:51');
INSERT INTO `entry_exit_record` VALUES (19, 'stu17', '阿成', 'entry', '2026-03-02 01:35:51', NULL, 'normal', 0, NULL, NULL, '2026-03-02 01:35:51');
INSERT INTO `entry_exit_record` VALUES (20, 'stu18', '阿达', 'entry', '2026-03-02 01:35:51', NULL, 'normal', 0, NULL, NULL, '2026-03-02 01:35:51');
INSERT INTO `entry_exit_record` VALUES (21, 'stu1', '张三', 'entry', '2026-03-02 01:36:19', NULL, 'normal', 0, NULL, NULL, '2026-03-02 01:36:19');
INSERT INTO `entry_exit_record` VALUES (22, 'stu10', '马克', 'exit', '2026-03-02 01:37:22', NULL, 'normal', 0, NULL, NULL, '2026-03-02 01:37:22');
INSERT INTO `entry_exit_record` VALUES (23, 'stu1', '张三', 'exit', '2026-03-02 01:37:22', NULL, 'normal', 0, NULL, NULL, '2026-03-02 01:37:22');
INSERT INTO `entry_exit_record` VALUES (24, 'stu1', '张三', 'entry', '2026-03-02 01:47:39', NULL, 'normal', 0, NULL, NULL, '2026-03-02 01:47:39');
INSERT INTO `entry_exit_record` VALUES (25, 'stu10', '马克', 'entry', '2026-03-02 01:47:40', NULL, 'normal', 0, NULL, NULL, '2026-03-02 01:47:40');
INSERT INTO `entry_exit_record` VALUES (26, 'stu1', '张三', 'exit', '2026-03-02 01:47:48', NULL, 'normal', 0, NULL, NULL, '2026-03-02 01:47:48');
INSERT INTO `entry_exit_record` VALUES (27, 'stu1', '张三', 'exit', '2026-03-02 01:49:19', NULL, 'late', 1, NULL, NULL, '2026-03-02 01:49:19');
INSERT INTO `entry_exit_record` VALUES (28, 'stu1', '张三', 'entry', '2026-03-16 04:24:44', NULL, 'normal', 0, NULL, NULL, '2026-03-16 12:24:45');
INSERT INTO `entry_exit_record` VALUES (29, 'shanshan', '姗姗', 'entry', '2026-05-05 01:41:33', 'admin', 'normal', 0, NULL, NULL, '2026-05-05 01:41:33');
INSERT INTO `entry_exit_record` VALUES (30, 'stu1', '张三', 'entry', '2026-05-05 01:41:33', 'admin', 'late', 1, NULL, NULL, '2026-05-05 01:41:33');
INSERT INTO `entry_exit_record` VALUES (31, 'stu10', '马克', 'entry', '2026-05-05 01:41:33', 'admin', 'normal', 0, NULL, NULL, '2026-05-05 01:41:33');
INSERT INTO `entry_exit_record` VALUES (32, 'stu11', '巧巧', 'entry', '2026-05-05 01:41:33', 'admin', 'normal', 0, NULL, NULL, '2026-05-05 01:41:33');
INSERT INTO `entry_exit_record` VALUES (33, 'stu12', '丽丽', 'entry', '2026-05-05 01:41:33', 'admin', 'normal', 0, NULL, NULL, '2026-05-05 01:41:33');
INSERT INTO `entry_exit_record` VALUES (34, 'stu13', '美美', 'entry', '2026-05-05 01:41:33', 'admin', 'normal', 0, NULL, NULL, '2026-05-05 01:41:33');
INSERT INTO `entry_exit_record` VALUES (35, 'stu14', '拉拉', 'entry', '2026-05-05 01:41:33', 'admin', 'normal', 0, NULL, NULL, '2026-05-05 01:41:33');
INSERT INTO `entry_exit_record` VALUES (36, 'stu15', '贝贝', 'entry', '2026-05-05 01:41:33', 'admin', 'normal', 0, NULL, NULL, '2026-05-05 01:41:33');
INSERT INTO `entry_exit_record` VALUES (37, 'stu16', '力力', 'entry', '2026-05-05 01:41:33', 'admin', 'normal', 0, NULL, NULL, '2026-05-05 01:41:33');
INSERT INTO `entry_exit_record` VALUES (38, 'stu17', '阿成', 'entry', '2026-05-05 01:41:33', 'admin', 'normal', 0, NULL, NULL, '2026-05-05 01:41:33');
INSERT INTO `entry_exit_record` VALUES (39, 'stu18', '阿达', 'entry', '2026-05-05 01:41:33', 'admin', 'absent', 1, NULL, NULL, '2026-05-05 01:41:33');
INSERT INTO `entry_exit_record` VALUES (40, 'stu19', '帕森斯', 'entry', '2026-05-05 01:41:33', 'admin', 'late', 1, NULL, NULL, '2026-05-05 01:41:33');
INSERT INTO `entry_exit_record` VALUES (41, 'stu2', '田田', 'entry', '2026-05-05 01:41:33', 'admin', 'normal', 0, NULL, NULL, '2026-05-05 01:41:33');
INSERT INTO `entry_exit_record` VALUES (42, 'stu20', '柠檬', 'entry', '2026-05-05 01:41:33', 'admin', 'normal', 0, NULL, NULL, '2026-05-05 01:41:33');
INSERT INTO `entry_exit_record` VALUES (43, 'stu21', '面对', 'entry', '2026-05-05 01:41:33', 'admin', 'normal', 0, NULL, NULL, '2026-05-05 01:41:33');
INSERT INTO `entry_exit_record` VALUES (44, 'stu22', '等等', 'entry', '2026-05-05 01:41:33', 'admin', 'normal', 0, NULL, NULL, '2026-05-05 01:41:33');
INSERT INTO `entry_exit_record` VALUES (45, 'stu3', '吉安', 'entry', '2026-05-05 01:41:33', 'admin', 'normal', 0, NULL, NULL, '2026-05-05 01:41:33');
INSERT INTO `entry_exit_record` VALUES (46, 'stu4', '力力', 'entry', '2026-05-05 01:41:33', 'admin', 'normal', 0, NULL, NULL, '2026-05-05 01:41:33');
INSERT INTO `entry_exit_record` VALUES (47, 'stu5', '哦哦', 'entry', '2026-05-05 01:41:33', 'admin', 'normal', 0, NULL, NULL, '2026-05-05 01:41:33');
INSERT INTO `entry_exit_record` VALUES (48, 'stu6', '泡泡', 'entry', '2026-05-05 01:41:33', 'admin', 'normal', 0, NULL, NULL, '2026-05-05 01:41:33');
INSERT INTO `entry_exit_record` VALUES (49, 'stu1', '张三', 'entry', '2026-05-14 22:54:20', 'admin', 'normal', 0, NULL, NULL, '2026-05-14 22:54:20');
INSERT INTO `entry_exit_record` VALUES (50, 'stu1', '张三', 'entry', '2026-05-15 00:18:26', 'admin', 'normal', 0, NULL, NULL, '2026-05-15 00:18:26');
INSERT INTO `entry_exit_record` VALUES (51, 'stu10', '马克', 'entry', '2026-05-15 00:18:27', 'admin', 'normal', 0, NULL, NULL, '2026-05-15 00:18:27');
INSERT INTO `entry_exit_record` VALUES (52, 'shanshan', '姗姗', 'entry', '2026-05-15 00:18:30', 'admin', 'normal', 0, NULL, NULL, '2026-05-15 00:18:30');
INSERT INTO `entry_exit_record` VALUES (53, 'stu1', '张三', 'entry', '2026-05-15 00:18:30', 'admin', 'absent', 1, NULL, NULL, '2026-05-15 00:18:30');
INSERT INTO `entry_exit_record` VALUES (54, 'stu10', '马克', 'entry', '2026-05-15 00:18:30', 'admin', 'normal', 0, NULL, NULL, '2026-05-15 00:18:30');
INSERT INTO `entry_exit_record` VALUES (55, 'stu11', '巧巧', 'entry', '2026-05-15 00:18:30', 'admin', 'normal', 0, NULL, NULL, '2026-05-15 00:18:30');
INSERT INTO `entry_exit_record` VALUES (56, 'stu12', '丽丽', 'entry', '2026-05-15 00:18:30', 'admin', 'normal', 0, NULL, NULL, '2026-05-15 00:18:30');
INSERT INTO `entry_exit_record` VALUES (57, 'stu13', '美美', 'entry', '2026-05-15 00:18:30', 'admin', 'normal', 0, NULL, NULL, '2026-05-15 00:18:30');
INSERT INTO `entry_exit_record` VALUES (58, 'stu14', '拉拉', 'entry', '2026-05-15 00:18:30', 'admin', 'normal', 0, NULL, NULL, '2026-05-15 00:18:30');
INSERT INTO `entry_exit_record` VALUES (59, 'stu15', '贝贝', 'entry', '2026-05-15 00:18:30', 'admin', 'normal', 0, NULL, NULL, '2026-05-15 00:18:30');
INSERT INTO `entry_exit_record` VALUES (60, 'stu16', '力力', 'entry', '2026-05-15 00:18:30', 'admin', 'normal', 0, NULL, NULL, '2026-05-15 00:18:30');
INSERT INTO `entry_exit_record` VALUES (61, 'stu17', '阿成', 'entry', '2026-05-15 00:18:30', 'admin', 'normal', 0, NULL, NULL, '2026-05-15 00:18:30');
INSERT INTO `entry_exit_record` VALUES (62, 'stu18', '阿达', 'entry', '2026-05-15 00:18:37', 'admin', 'normal', 0, NULL, NULL, '2026-05-15 00:18:37');
INSERT INTO `entry_exit_record` VALUES (63, 'stu19', '帕森斯', 'entry', '2026-05-15 00:18:37', 'admin', 'normal', 1, NULL, NULL, '2026-05-15 00:18:37');
INSERT INTO `entry_exit_record` VALUES (64, 'stu2', '田田', 'entry', '2026-05-15 00:18:37', 'admin', 'normal', 0, NULL, NULL, '2026-05-15 00:18:37');
INSERT INTO `entry_exit_record` VALUES (65, 'stu20', '柠檬', 'entry', '2026-05-15 00:18:37', 'admin', 'normal', 0, NULL, NULL, '2026-05-15 00:18:37');
INSERT INTO `entry_exit_record` VALUES (66, 'stu21', '面对', 'entry', '2026-05-15 00:18:37', 'admin', 'normal', 0, NULL, NULL, '2026-05-15 00:18:37');
INSERT INTO `entry_exit_record` VALUES (67, 'stu22', '等等', 'entry', '2026-05-15 00:18:37', 'admin', 'normal', 0, NULL, NULL, '2026-05-15 00:18:37');
INSERT INTO `entry_exit_record` VALUES (68, 'stu3', '吉安', 'entry', '2026-05-15 00:18:37', 'admin', 'normal', 0, NULL, NULL, '2026-05-15 00:18:37');
INSERT INTO `entry_exit_record` VALUES (69, 'stu4', '力力', 'entry', '2026-05-15 00:18:37', 'admin', 'normal', 0, NULL, NULL, '2026-05-15 00:18:37');
INSERT INTO `entry_exit_record` VALUES (70, 'stu5', '哦哦', 'entry', '2026-05-15 00:18:37', 'admin', 'normal', 0, NULL, NULL, '2026-05-15 00:18:37');
INSERT INTO `entry_exit_record` VALUES (71, 'stu6', '泡泡', 'entry', '2026-05-15 00:18:37', 'admin', 'normal', 0, NULL, NULL, '2026-05-15 00:18:37');

-- ----------------------------
-- Table structure for meal_billing_config
-- ----------------------------
DROP TABLE IF EXISTS `meal_billing_config`;
CREATE TABLE `meal_billing_config`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_key` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '配置键',
  `config_value` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '配置值',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '配置描述',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_config_key`(`config_key`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用餐计费配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of meal_billing_config
-- ----------------------------
INSERT INTO `meal_billing_config` VALUES (1, 'lunch_price', '15.00', '午餐单价（元/天）', '2026-03-02 13:01:07', '2026-03-02 13:01:07');
INSERT INTO `meal_billing_config` VALUES (2, 'dinner_price', '20.00', '晚餐单价（元/天）', '2026-03-02 13:01:07', '2026-03-02 15:32:04');
INSERT INTO `meal_billing_config` VALUES (3, 'boarding_fee', '500.00', '午休费（元/月）', '2026-03-02 13:01:07', '2026-03-02 13:01:07');
INSERT INTO `meal_billing_config` VALUES (4, 'entry_inspection_time', '23:50', '进出记录自动巡检时间(HH:mm)，超过该时间仍未签到(entry)将自动通知家长', '2026-05-05 01:50:26', '2026-05-14 23:46:38');

-- ----------------------------
-- Table structure for menu
-- ----------------------------
DROP TABLE IF EXISTS `menu`;
CREATE TABLE `menu`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '菜谱ID',
  `dish_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '菜品名称',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '菜品分类(如：主食、荤菜、素菜、汤品等)',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '菜品描述',
  `price` decimal(10, 2) NULL DEFAULT NULL COMMENT '菜品价格',
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '菜品图片URL',
  `nutrition_info` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '营养信息',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 109 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '菜谱信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of menu
-- ----------------------------
INSERT INTO `menu` VALUES (1, '红烧肉', '荤菜', '经典红烧肉，肥而不腻', NULL, NULL, '热量:500kcal 蛋白质:25g', '2026-01-21 01:29:48');
INSERT INTO `menu` VALUES (2, '青椒土豆丝', '素菜', '清脆爽口的家常菜', NULL, NULL, '热量:150kcal 蛋白质:5g', '2026-01-21 01:29:48');
INSERT INTO `menu` VALUES (3, '西红柿鸡蛋汤', '汤品', '营养丰富的家常汤', NULL, NULL, '热量:100kcal 蛋白质:8g', '2026-01-21 01:29:48');
INSERT INTO `menu` VALUES (4, '米饭', '主食', '香喷喷的白米饭', NULL, NULL, '热量:200kcal 碳水化合物:45g', '2026-01-21 01:29:48');
INSERT INTO `menu` VALUES (5, '宫保鸡丁', '荤菜', '川菜经典菜品', NULL, NULL, '热量:450kcal 蛋白质:30g', '2026-01-21 01:29:48');
INSERT INTO `menu` VALUES (6, '清炒时蔬', '素菜', '新鲜时令蔬菜', NULL, NULL, '热量:120kcal 蛋白质:4g', '2026-01-21 01:29:48');
INSERT INTO `menu` VALUES (7, '紫菜蛋花汤', '汤品', '简单美味的汤品', NULL, NULL, '热量:80kcal 蛋白质:6g', '2026-01-21 01:29:48');
INSERT INTO `menu` VALUES (8, '馒头', '主食', '松软可口的馒头', NULL, NULL, '热量:180kcal 碳水化合物:38g', '2026-01-21 01:29:48');
INSERT INTO `menu` VALUES (9, '鱼香肉丝', '荤菜', '咸鲜微辣下饭', NULL, NULL, '热量:360kcal 蛋白质:22g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (10, '回锅肉', '荤菜', '经典川味回锅肉', NULL, NULL, '热量:420kcal 蛋白质:20g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (11, '土豆烧牛肉', '荤菜', '软糯土豆配牛肉', NULL, NULL, '热量:410kcal 蛋白质:24g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (12, '红烧排骨', '荤菜', '酱香浓郁口感软烂', NULL, NULL, '热量:430kcal 蛋白质:23g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (13, '糖醋里脊', '荤菜', '酸甜开胃老少皆宜', NULL, NULL, '热量:390kcal 蛋白质:21g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (14, '可乐鸡翅', '荤菜', '甜咸适口鸡翅入味', NULL, NULL, '热量:370kcal 蛋白质:20g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (15, '蒜苔炒肉', '荤菜', '蒜香浓郁口感脆嫩', NULL, NULL, '热量:320kcal 蛋白质:18g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (16, '芹菜炒肉丝', '荤菜', '清香爽口家常小炒', NULL, NULL, '热量:300kcal 蛋白质:18g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (17, '青椒肉丝', '荤菜', '鲜辣开胃经典配饭', NULL, NULL, '热量:310kcal 蛋白质:19g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (18, '木须肉', '荤菜', '鸡蛋木耳口感丰富', NULL, NULL, '热量:340kcal 蛋白质:20g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (19, '京酱肉丝', '荤菜', '酱香浓郁咸甜适中', NULL, NULL, '热量:350kcal 蛋白质:21g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (20, '农家小炒肉', '荤菜', '辣椒五花肉香气足', NULL, NULL, '热量:410kcal 蛋白质:19g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (21, '香菇滑鸡', '荤菜', '鸡肉嫩滑香菇鲜香', NULL, NULL, '热量:330kcal 蛋白质:22g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (22, '黄焖鸡块', '荤菜', '酱香浓郁鸡肉入味', NULL, NULL, '热量:360kcal 蛋白质:23g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (23, '孜然羊肉', '荤菜', '孜然香气十足', NULL, NULL, '热量:390kcal 蛋白质:24g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (24, '红烧鸡块', '荤菜', '色泽红亮口味醇厚', NULL, NULL, '热量:350kcal 蛋白质:22g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (25, '啤酒鸭', '荤菜', '鸭肉鲜香微辣', NULL, NULL, '热量:380kcal 蛋白质:23g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (26, '香干回锅肉', '荤菜', '豆干与五花肉同炒', NULL, NULL, '热量:400kcal 蛋白质:21g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (27, '番茄牛腩', '荤菜', '番茄酸香牛腩软烂', NULL, NULL, '热量:370kcal 蛋白质:24g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (28, '酱爆鸡丁', '荤菜', '酱香鸡丁口感紧实', NULL, NULL, '热量:340kcal 蛋白质:22g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (29, '咖喱鸡块', '荤菜', '咖喱浓郁微辣下饭', NULL, NULL, '热量:360kcal 蛋白质:21g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (30, '梅菜扣肉', '荤菜', '梅菜咸香扣肉软糯', NULL, NULL, '热量:460kcal 蛋白质:20g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (31, '蒜香排骨', '荤菜', '蒜香突出外酥里嫩', NULL, NULL, '热量:420kcal 蛋白质:23g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (32, '辣子鸡', '荤菜', '干香麻辣鸡块酥香', NULL, NULL, '热量:410kcal 蛋白质:24g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (33, '蚂蚁上树', '荤菜', '粉丝肉末香辣入味', NULL, NULL, '热量:330kcal 蛋白质:17g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (34, '肉末茄子', '荤菜', '软糯茄子配肉末', NULL, NULL, '热量:340kcal 蛋白质:16g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (35, '青椒鸡丁', '荤菜', '鸡丁嫩滑青椒清香', NULL, NULL, '热量:320kcal 蛋白质:21g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (36, '荷兰豆炒腊肉', '荤菜', '腊肉咸香豆荚清甜', NULL, NULL, '热量:360kcal 蛋白质:18g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (37, '冬瓜烧肉', '荤菜', '冬瓜清甜吸收肉香', NULL, NULL, '热量:330kcal 蛋白质:19g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (38, '白菜炖豆腐', '素菜', '清淡温润营养均衡', NULL, NULL, '热量:180kcal 蛋白质:10g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (39, '地三鲜', '素菜', '土豆茄子青椒香浓', NULL, NULL, '热量:230kcal 蛋白质:6g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (40, '干煸豆角', '素菜', '豆角干香微辣', NULL, NULL, '热量:210kcal 蛋白质:7g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (41, '麻婆豆腐', '素菜', '麻辣嫩滑下饭佳品', NULL, NULL, '热量:220kcal 蛋白质:12g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (42, '清炒西兰花', '素菜', '西兰花清爽脆嫩', NULL, NULL, '热量:130kcal 蛋白质:6g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (43, '蒜蓉空心菜', '素菜', '蒜香十足口感脆爽', NULL, NULL, '热量:120kcal 蛋白质:4g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (44, '手撕包菜', '素菜', '香辣爽口家常快手', NULL, NULL, '热量:140kcal 蛋白质:3g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (45, '酸辣土豆丝', '素菜', '酸辣脆爽非常下饭', NULL, NULL, '热量:170kcal 蛋白质:4g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (46, '番茄炒蛋', '素菜', '酸甜软嫩经典搭配', NULL, NULL, '热量:210kcal 蛋白质:11g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (47, '韭菜炒鸡蛋', '素菜', '韭香浓郁鸡蛋松软', NULL, NULL, '热量:230kcal 蛋白质:12g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (48, '苦瓜炒蛋', '素菜', '微苦清爽营养丰富', NULL, NULL, '热量:190kcal 蛋白质:11g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (49, '木耳炒山药', '素菜', '口感脆嫩清淡爽口', NULL, NULL, '热量:150kcal 蛋白质:5g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (50, '蒜蓉油麦菜', '素菜', '清爽低脂蒜香扑鼻', NULL, NULL, '热量:110kcal 蛋白质:3g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (51, '清炒小白菜', '素菜', '鲜嫩清香家常必备', NULL, NULL, '热量:100kcal 蛋白质:3g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (52, '香菇青菜', '素菜', '香菇提鲜青菜爽口', NULL, NULL, '热量:140kcal 蛋白质:5g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (53, '豆角炒茄子', '素菜', '软糯咸香家常口味', NULL, NULL, '热量:220kcal 蛋白质:6g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (54, '丝瓜炒蛋', '素菜', '丝瓜清甜鸡蛋嫩滑', NULL, NULL, '热量:180kcal 蛋白质:10g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (55, '西芹百合', '素菜', '清脆爽口色泽清新', NULL, NULL, '热量:130kcal 蛋白质:4g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (56, '清炒四季豆', '素菜', '四季豆脆嫩清香', NULL, NULL, '热量:150kcal 蛋白质:5g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (57, '蒜香南瓜', '素菜', '南瓜软糯清甜', NULL, NULL, '热量:160kcal 蛋白质:3g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (58, '炝炒莲藕', '素菜', '藕片脆爽微辣开胃', NULL, NULL, '热量:170kcal 蛋白质:4g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (59, '蚝油生菜', '素菜', '生菜脆嫩蚝油鲜香', NULL, NULL, '热量:120kcal 蛋白质:3g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (60, '家常豆腐', '素菜', '外焦里嫩酱香浓郁', NULL, NULL, '热量:230kcal 蛋白质:13g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (61, '黄瓜炒鸡蛋', '素菜', '清新爽口营养均衡', NULL, NULL, '热量:180kcal 蛋白质:10g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (62, '青椒杏鲍菇', '素菜', '菌香浓郁口感弹嫩', NULL, NULL, '热量:150kcal 蛋白质:6g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (63, '红烧茄子', '素菜', '酱香浓郁软糯下饭', NULL, NULL, '热量:240kcal 蛋白质:5g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (64, '蒜蓉西兰花', '素菜', '维生素丰富清淡可口', NULL, NULL, '热量:130kcal 蛋白质:6g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (65, '清炒上海青', '素菜', '口感脆嫩清香宜人', NULL, NULL, '热量:100kcal 蛋白质:3g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (66, '上汤娃娃菜', '汤品', '清甜鲜美口感细嫩', NULL, NULL, '热量:90kcal 蛋白质:5g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (67, '冬瓜排骨汤', '汤品', '汤鲜味美营养丰富', NULL, NULL, '热量:180kcal 蛋白质:12g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (68, '萝卜排骨汤', '汤品', '萝卜清甜排骨鲜香', NULL, NULL, '热量:190kcal 蛋白质:13g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (69, '海带排骨汤', '汤品', '海带软糯汤汁鲜美', NULL, NULL, '热量:170kcal 蛋白质:12g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (70, '玉米排骨汤', '汤品', '玉米清甜汤味浓郁', NULL, NULL, '热量:180kcal 蛋白质:12g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (71, '莲藕排骨汤', '汤品', '莲藕粉糯香气足', NULL, NULL, '热量:185kcal 蛋白质:12g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (72, '山药排骨汤', '汤品', '健脾暖胃清润鲜香', NULL, NULL, '热量:175kcal 蛋白质:11g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (73, '金针菇豆腐汤', '汤品', '鲜香滑嫩清淡顺口', NULL, NULL, '热量:110kcal 蛋白质:8g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (74, '番茄豆腐汤', '汤品', '酸甜开胃豆腐嫩滑', NULL, NULL, '热量:95kcal 蛋白质:7g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (75, '菌菇鸡汤', '汤品', '菌香浓郁鸡汤鲜甜', NULL, NULL, '热量:160kcal 蛋白质:14g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (76, '香菇鸡汤', '汤品', '香菇鲜香营养温润', NULL, NULL, '热量:155kcal 蛋白质:13g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (77, '丝瓜蛋花汤', '汤品', '清甜爽口蛋香浓郁', NULL, NULL, '热量:90kcal 蛋白质:6g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (78, '黄瓜皮蛋汤', '汤品', '清爽解腻汤色清亮', NULL, NULL, '热量:100kcal 蛋白质:7g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (79, '菠菜蛋花汤', '汤品', '菠菜鲜嫩营养丰富', NULL, NULL, '热量:95kcal 蛋白质:7g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (80, '白菜豆腐汤', '汤品', '家常清淡暖胃舒适', NULL, NULL, '热量:90kcal 蛋白质:7g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (81, '酸辣汤', '汤品', '酸辣开胃口感丰富', NULL, NULL, '热量:120kcal 蛋白质:8g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (82, '小酥肉', '荤菜', '外酥里嫩椒香四溢', NULL, NULL, '热量:400kcal 蛋白质:19g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (83, '香辣鸡块', '荤菜', '香辣开胃鸡块入味', NULL, NULL, '热量:370kcal 蛋白质:22g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (84, '青椒牛柳', '荤菜', '牛柳嫩滑青椒清香', NULL, NULL, '热量:360kcal 蛋白质:23g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (85, '土豆炖鸡块', '荤菜', '鸡肉软嫩土豆粉糯', NULL, NULL, '热量:350kcal 蛋白质:22g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (86, '香芋烧鸡', '荤菜', '香芋绵密鸡肉鲜香', NULL, NULL, '热量:360kcal 蛋白质:21g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (87, '香菇烧肉', '荤菜', '香菇吸汁肉香浓郁', NULL, NULL, '热量:390kcal 蛋白质:20g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (88, '黄瓜炒肉片', '荤菜', '清爽不腻家常快炒', NULL, NULL, '热量:300kcal 蛋白质:18g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (89, '蒜苗回锅肉', '荤菜', '蒜苗增香肥瘦适中', NULL, NULL, '热量:410kcal 蛋白质:20g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (90, '蒜香鸡腿肉', '荤菜', '蒜香突出肉质紧实', NULL, NULL, '热量:350kcal 蛋白质:23g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (91, '辣椒炒鸡胗', '荤菜', '脆嫩爽辣口感十足', NULL, NULL, '热量:320kcal 蛋白质:24g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (92, '西芹炒牛肉', '荤菜', '西芹清香牛肉嫩滑', NULL, NULL, '热量:340kcal 蛋白质:23g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (93, '豆角炒肉末', '荤菜', '肉末豆角香浓下饭', NULL, NULL, '热量:320kcal 蛋白质:18g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (94, '茄汁大虾', '荤菜', '番茄酸甜虾肉弹嫩', NULL, NULL, '热量:280kcal 蛋白质:20g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (95, '蒜蓉粉丝虾', '荤菜', '蒜香浓郁虾鲜粉丝滑', NULL, NULL, '热量:300kcal 蛋白质:21g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (96, '家常烧带鱼', '荤菜', '外酥内嫩咸香适口', NULL, NULL, '热量:330kcal 蛋白质:22g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (97, '红烧鲫鱼', '荤菜', '鱼肉细嫩酱香浓郁', NULL, NULL, '热量:310kcal 蛋白质:24g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (98, '葱油鲈鱼', '荤菜', '清鲜不腻葱香四溢', NULL, NULL, '热量:290kcal 蛋白质:25g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (99, '剁椒鱼头', '荤菜', '鲜辣过瘾肉质细嫩', NULL, NULL, '热量:320kcal 蛋白质:23g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (100, '清蒸鸡腿', '荤菜', '少油健康肉嫩多汁', NULL, NULL, '热量:260kcal 蛋白质:24g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (101, '蒸蛋羹', '素菜', '细腻嫩滑老少皆宜', NULL, NULL, '热量:150kcal 蛋白质:9g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (102, '葱花鸡蛋饼', '素菜', '外香内软早餐可选', NULL, NULL, '热量:210kcal 蛋白质:8g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (103, '凉拌黄瓜', '素菜', '清爽开胃解腻小菜', NULL, NULL, '热量:70kcal 蛋白质:2g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (104, '凉拌木耳', '素菜', '爽脆可口低脂健康', NULL, NULL, '热量:80kcal 蛋白质:3g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (105, '凉拌海带丝', '素菜', '鲜香爽口补充矿物质', NULL, NULL, '热量:85kcal 蛋白质:3g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (106, '拍黄瓜', '素菜', '蒜香酸辣脆爽开胃', NULL, NULL, '热量:75kcal 蛋白质:2g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (107, '清炒莴笋丝', '素菜', '莴笋脆嫩色泽清新', NULL, NULL, '热量:110kcal 蛋白质:3g', '2026-03-04 16:00:00');
INSERT INTO `menu` VALUES (108, '蒜蓉菜心', '素菜', '清香脆嫩蒜味浓郁', NULL, NULL, '热量:120kcal 蛋白质:4g', '2026-03-04 16:00:00');

-- ----------------------------
-- Table structure for notice
-- ----------------------------
DROP TABLE IF EXISTS `notice`;
CREATE TABLE `notice`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '主题',
  `content` longtext CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '内容',
  `author` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '作者',
  `release_time` datetime NOT NULL COMMENT '发布时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of notice
-- ----------------------------
INSERT INTO `notice` VALUES (1, '入冬提醒', '<p>近期我校所在地区天气逐渐降低，同学们要注意多穿衣服，少熬夜，避免感染风寒！</p>', '大强', '2025-11-07 12:17:27');
INSERT INTO `notice` VALUES (2, '关于宿舍卫生的新规定', '<p>学生公寓是学生们主要的生活区域，兼具休息、学习、交际等多种功能，是培养、提升学生全面素质不可或缺的重要阵地。为了培养学生良好的行为素养和生活习惯，我们实行宿舍长内务准军事化管理，切实把学生公寓建成学生自我教育，自我管理和自我服务的家园。</p>', '大强', '2026-02-08 06:02:59');

-- ----------------------------
-- Table structure for parent
-- ----------------------------
DROP TABLE IF EXISTS `parent`;
CREATE TABLE `parent`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `age` int(11) NULL DEFAULT NULL,
  `gender` enum('男','女') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `phone_num` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `student_username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username`) USING BTREE,
  INDEX `idx_student_username`(`student_username`) USING BTREE,
  CONSTRAINT `fk_parent_student` FOREIGN KEY (`student_username`) REFERENCES `student` (`username`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of parent
-- ----------------------------
INSERT INTO `parent` VALUES (5, '1432', '123456', '姗姗爸爸', 18, '男', '15279368236', NULL, 'shanshan', '2026-07-03 19:35:51', '2026-07-03 19:35:51');
INSERT INTO `parent` VALUES (6, 'jiajia', '123456', '佳佳', 45, '女', '13557936673', NULL, NULL, '2026-03-16 11:14:48', '2026-03-16 11:14:48');
INSERT INTO `parent` VALUES (7, 'parent1', '$2a$10$/Uu4YFSD4FoqkMqvWQ.6Dep4w7/Y5x1KEQt6.MezIYocP4PVN9n1G', '张父', 45, '男', '13800138000', 'parent1@qq.com', 'stu1', '2026-03-03 12:42:10', '2026-05-04 23:42:40');
INSERT INTO `parent` VALUES (8, 'parent2', '$2a$10$0JdiMbWm3S94ZTtvZadgvegIYntLkPWv5HJ6xAPwChk92LgyLgJti', '丽丽妈妈', 18, '女', '13366483937', NULL, 'stu12', '2026-03-03 12:42:10', '2026-05-05 00:13:44');

-- ----------------------------
-- Table structure for parent_notification
-- ----------------------------
DROP TABLE IF EXISTS `parent_notification`;
CREATE TABLE `parent_notification`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `parent_username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '家长用户名',
  `student_username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '学生用户名',
  `student_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '学生姓名',
  `notification_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '通知类型：leave_request(请假申请)、unauthorized_out(未授权外出)',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '通知标题',
  `content` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '通知内容',
  `related_request_id` int(11) NULL DEFAULT NULL COMMENT '关联的请假申请ID',
  `is_read` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '通知创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_parent`(`parent_username`) USING BTREE,
  INDEX `idx_read`(`is_read`) USING BTREE,
  INDEX `idx_type`(`notification_type`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 34 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '家长通知表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of parent_notification
-- ----------------------------
INSERT INTO `parent_notification` VALUES (1, 'parent1', 'stu1', '张三', 'leave_request', '学生请假申请', '您的孩子张三申请回家，时间：Sun Mar 01 20:16:55 CST 2026 至 Wed Mar 04 00:00:00 CST 2026，原因：我要回家休息', 1, 1, '2026-03-01 12:17:21');
INSERT INTO `parent_notification` VALUES (2, 'parent1', 'stu1', '张三', 'entry_exit_alert', '学生进出异常提醒', '您的孩子张三在2026-03-02T01:49:18.915出现未归情况，请关注。', NULL, 1, '2026-03-01 17:49:19');
INSERT INTO `parent_notification` VALUES (3, 'parent1', 'stu1', '张三', 'admin_message', '未归', '孩子超时未归', NULL, 0, '2026-03-01 18:05:57');
INSERT INTO `parent_notification` VALUES (4, 'parent1', 'stu1', '张三', 'entry_exit_alert', '学生进出异常提醒', '您的孩子张三在2026-03-02T01:49:19出现晚归情况，请关注。', NULL, 0, '2026-03-01 18:06:29');
INSERT INTO `parent_notification` VALUES (5, 'parent1', 'stu1', '张三', 'entry_signin_missing', '学生未签到提醒', '截至12:30，您的孩子张三仍未完成签到，请及时关注。', NULL, 0, '2026-03-06 09:57:00');
INSERT INTO `parent_notification` VALUES (6, 'parent2', 'stu12', '丽丽', 'entry_signin_missing', '学生未签到提醒', '截至12:30，您的孩子丽丽仍未完成签到，请及时关注。', NULL, 0, '2026-03-06 09:57:00');
INSERT INTO `parent_notification` VALUES (7, 'parent1', 'stu1', '张三', 'entry_signin_missing', '学生未签到提醒', '截至12:30，您的孩子张三仍未完成签到，请及时关注。', NULL, 0, '2026-03-07 05:59:00');
INSERT INTO `parent_notification` VALUES (8, 'parent2', 'stu12', '丽丽', 'entry_signin_missing', '学生未签到提醒', '截至12:30，您的孩子丽丽仍未完成签到，请及时关注。', NULL, 0, '2026-03-07 05:59:00');
INSERT INTO `parent_notification` VALUES (9, '1432', 'shanshan', '姗姗', 'entry_signin_missing', '学生未签到提醒', '截至12:30，您的孩子姗姗仍未完成签到，请及时关注。', NULL, 0, '2026-03-07 11:39:00');
INSERT INTO `parent_notification` VALUES (10, 'parent1', 'stu1', '张三', 'leave_request', '学生请假申请', '您的孩子张三申请回家，时间：2026-03-18 18:00:00 至 2026-03-17 03:17:28，原因：我要回家换衣服', NULL, 0, '2026-03-16 04:17:41');
INSERT INTO `parent_notification` VALUES (11, 'parent1', 'stu1', '张三', 'leave_request', '学生请假申请', '您的孩子张三申请回家，时间：2026-03-18 12:00:00 至 2026-03-18 13:00:00，原因：回家换衣服', NULL, 0, '2026-03-16 04:23:33');
INSERT INTO `parent_notification` VALUES (12, '1432', 'shanshan', '姗姗', 'entry_signin_missing', '学生未签到提醒', '截至12:30，您的孩子姗姗仍未完成签到，请及时关注', NULL, 0, '2026-03-16 05:33:00');
INSERT INTO `parent_notification` VALUES (13, 'parent2', 'stu12', '丽丽', 'entry_signin_missing', '学生未签到提醒', '截至12:30，您的孩子丽丽仍未完成签到，请及时关注', NULL, 0, '2026-03-16 05:33:00');
INSERT INTO `parent_notification` VALUES (14, '1432', 'shanshan', '姗姗', 'entry_signin_missing', '学生未签到提醒', '截至12:30，您的孩子姗姗仍未完成签到，请及时关注', NULL, 0, '2026-03-30 12:10:00');
INSERT INTO `parent_notification` VALUES (15, 'parent1', 'stu1', '张三', 'entry_signin_missing', '学生未签到提醒', '截至12:30，您的孩子张三仍未完成签到，请及时关注', NULL, 0, '2026-03-30 12:10:00');
INSERT INTO `parent_notification` VALUES (16, 'parent2', 'stu12', '丽丽', 'entry_signin_missing', '学生未签到提醒', '截至12:30，您的孩子丽丽仍未完成签到，请及时关注', NULL, 0, '2026-03-30 12:10:00');
INSERT INTO `parent_notification` VALUES (17, '1432', 'shanshan', '姗姗', 'entry_signin_missing', '学生未签到提醒', '截至12:30，您的孩子姗姗仍未完成签到，请及时关注', NULL, 0, '2026-04-05 14:28:00');
INSERT INTO `parent_notification` VALUES (18, 'parent1', 'stu1', '张三', 'entry_signin_missing', '学生未签到提醒', '截至12:30，您的孩子张三仍未完成签到，请及时关注', NULL, 0, '2026-04-05 14:28:00');
INSERT INTO `parent_notification` VALUES (19, 'parent2', 'stu12', '丽丽', 'entry_signin_missing', '学生未签到提醒', '截至12:30，您的孩子丽丽仍未完成签到，请及时关注', NULL, 0, '2026-04-05 14:28:00');
INSERT INTO `parent_notification` VALUES (20, '1432', 'shanshan', '姗姗', 'entry_signin_missing', '学生未签到提醒', '截至12:30，您的孩子姗姗仍未完成签到，请及时关注', NULL, 0, '2026-05-04 15:40:00');
INSERT INTO `parent_notification` VALUES (21, 'parent1', 'stu1', '张三', 'entry_signin_missing', '学生未签到提醒', '截至12:30，您的孩子张三仍未完成签到，请及时关注', NULL, 0, '2026-05-04 15:40:00');
INSERT INTO `parent_notification` VALUES (22, 'parent2', 'stu12', '丽丽', 'entry_signin_missing', '学生未签到提醒', '截至12:30，您的孩子丽丽仍未完成签到，请及时关注', NULL, 0, '2026-05-04 15:40:00');
INSERT INTO `parent_notification` VALUES (23, '1432', 'shanshan', '姗姗', 'meal_bill_reminder', '餐费账单缴费提醒', '您的孩子姗姗(shanshan)的餐费账单2026-04)尚未缴纳，请及时缴费', NULL, 0, '2026-05-04 17:23:53');
INSERT INTO `parent_notification` VALUES (24, 'parent1', 'stu1', '张三', 'meal_bill_reminder', '餐费账单缴费提醒', '您的孩子张三(stu1)的餐费账单2026-04)尚未缴纳，请及时缴费', NULL, 0, '2026-05-04 17:23:53');
INSERT INTO `parent_notification` VALUES (25, 'parent2', 'stu12', '丽丽', 'meal_bill_reminder', '餐费账单缴费提醒', '您的孩子丽丽(stu12)的餐费账单2026-04)尚未缴纳，请及时缴费', NULL, 0, '2026-05-04 17:23:53');
INSERT INTO `parent_notification` VALUES (26, 'parent1', 'stu1', '张三', 'entry_exit_alert', '学生进出异常提醒', '您的孩子张三于2026-05-05T01:41:33出现晚归情况，请关注', NULL, 0, '2026-05-04 18:04:16');
INSERT INTO `parent_notification` VALUES (27, '1432', 'shanshan', '姗姗', 'entry_signin_missing', '学生未签到提醒', '截至12:30，您的孩子姗姗仍未完成签到，请及时关注', NULL, 0, '2026-05-13 04:30:00');
INSERT INTO `parent_notification` VALUES (28, 'parent1', 'stu1', '张三', 'entry_signin_missing', '学生未签到提醒', '截至12:30，您的孩子张三仍未完成签到，请及时关注', NULL, 0, '2026-05-13 04:30:00');
INSERT INTO `parent_notification` VALUES (29, 'parent2', 'stu12', '丽丽', 'entry_signin_missing', '学生未签到提醒', '截至12:30，您的孩子丽丽仍未完成签到，请及时关注', NULL, 0, '2026-05-13 04:30:00');
INSERT INTO `parent_notification` VALUES (30, '1432', 'shanshan', '姗姗', 'entry_signin_missing', '学生未签到提醒', '截至12:30，您的孩子姗姗仍未完成签到，请及时关注', NULL, 0, '2026-05-14 14:03:00');
INSERT INTO `parent_notification` VALUES (31, 'parent1', 'stu1', '张三', 'entry_signin_missing', '学生未签到提醒', '截至12:30，您的孩子张三仍未完成签到，请及时关注', NULL, 0, '2026-05-14 14:03:00');
INSERT INTO `parent_notification` VALUES (32, 'parent2', 'stu12', '丽丽', 'entry_signin_missing', '学生未签到提醒', '截至12:30，您的孩子丽丽仍未完成签到，请及时关注', NULL, 0, '2026-05-14 14:03:00');
INSERT INTO `parent_notification` VALUES (33, 'parent1', 'stu1', '张三', 'entry_exit_alert', '学生进出异常提醒', '您的孩子张三于2026-05-15T00:18:30出现未归情况，请关注', NULL, 0, '2026-05-14 16:18:59');

-- ----------------------------
-- Table structure for student
-- ----------------------------
DROP TABLE IF EXISTS `student`;
CREATE TABLE `student`  (
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `age` int(11) NULL DEFAULT NULL,
  `gender` enum('男','女') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `phone_num` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `boarding_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`username`) USING BTREE,
  UNIQUE INDEX `username`(`username`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of student
-- ----------------------------
INSERT INTO `student` VALUES ('happy', '$2a$10$3Dex5Y2llG03tHgSnzxEquzNbzB/p1yDTz7ObVlv/Op7neOf2jH2a', '开心', 18, '男', '13442655764', NULL, NULL, '2026-03-30 20:20:28', '2026-03-30 20:20:28');
INSERT INTO `student` VALUES ('shanshan', '123456', '姗姗', 18, '女', '14156848252', NULL, NULL, '2026-03-07 19:37:07', '2026-03-07 19:37:07');
INSERT INTO `student` VALUES ('stu1', '$2a$10$cYH/EGFMO/vSr3AgadXFJ.nIeg2VNVXAGE3kvTlMTpsFldMHGyFu6', '张三', 18, '男', '13124143214', NULL, 'meal_and_rest', '2026-03-03 12:42:10', '2026-05-05 00:13:06');
INSERT INTO `student` VALUES ('stu10', '123456', '马克', 19, '女', '15833333333', NULL, 'meal_and_rest', '2026-03-03 12:42:10', '2026-03-04 15:26:35');
INSERT INTO `student` VALUES ('stu11', '123456', '巧巧', 16, '女', '18922223333', NULL, 'meal_and_rest', '2026-03-03 12:42:10', '2026-03-04 15:27:08');
INSERT INTO `student` VALUES ('stu12', '123456', '丽丽', 17, '女', '17922222222', NULL, 'meal_and_rest', '2026-03-03 12:42:10', '2026-03-04 15:27:17');
INSERT INTO `student` VALUES ('stu13', '123456', '美美', 18, '女', '15822222222', NULL, 'meal_and_rest', '2026-03-03 12:42:10', '2026-03-04 15:27:36');
INSERT INTO `student` VALUES ('stu14', '123456', '拉拉', 20, '女', '13355556666', NULL, 'meal_and_rest', '2026-03-03 12:42:10', '2026-03-04 15:27:39');
INSERT INTO `student` VALUES ('stu15', '123456', '贝贝', 18, '男', '15899999999', NULL, 'meal_only', '2026-03-03 12:42:10', '2026-03-03 12:42:10');
INSERT INTO `student` VALUES ('stu16', '123456', '力力', 18, '男', '14596475257', NULL, 'meal_only', '2026-03-03 12:42:10', '2026-03-03 12:42:10');
INSERT INTO `student` VALUES ('stu17', '123456', '阿成', 18, '男', '15896542147', NULL, 'meal_only', '2026-03-03 12:42:10', '2026-03-03 12:42:10');
INSERT INTO `student` VALUES ('stu18', '123456', '阿达', 19, '女', '14785635874', 'akk@akkmail.com', 'meal_and_rest', '2026-03-03 12:42:10', '2026-03-04 16:09:23');
INSERT INTO `student` VALUES ('stu19', '123456', '帕森斯', 19, '男', '15889658475', NULL, 'meal_and_rest', '2026-03-03 12:42:10', '2026-03-04 16:09:27');
INSERT INTO `student` VALUES ('stu2', '123456', '田田', 18, '男', '15875359641', NULL, 'meal_and_rest', '2026-03-03 12:42:10', '2026-03-04 16:09:36');
INSERT INTO `student` VALUES ('stu20', '123456', '柠檬', 21, '男', '15874563558', NULL, 'meal_and_rest', '2026-03-03 12:42:10', '2026-03-04 16:09:31');
INSERT INTO `student` VALUES ('stu21', '123456', '面对', 21, '男', '15889635874', NULL, 'meal_only', '2026-03-03 12:42:10', '2026-03-03 12:42:10');
INSERT INTO `student` VALUES ('stu22', '123456', '等等', 25, '男', '13412341234', 'akkk@kkk.com', 'meal_only', '2026-03-03 12:42:10', '2026-03-03 12:42:10');
INSERT INTO `student` VALUES ('stu3', '123456', '吉安', 18, '男', '15798657350', NULL, 'meal_only', '2026-03-03 12:42:10', '2026-03-03 12:42:10');
INSERT INTO `student` VALUES ('stu4', '123456', '力力', 22, '男', '15878965874', NULL, 'meal_only', '2026-03-03 12:42:10', '2026-03-03 12:42:10');
INSERT INTO `student` VALUES ('stu5', '123456', '哦哦', 19, '男', '15897535478', NULL, 'meal_only', '2026-03-03 12:42:10', '2026-03-03 12:42:10');
INSERT INTO `student` VALUES ('stu6', '123456', '泡泡', 18, '男', '18987554765', NULL, 'meal_only', '2026-03-03 12:42:10', '2026-03-03 12:42:10');
INSERT INTO `student` VALUES ('stu7', '123456', '刚刚', 15, '男', '15897543854', NULL, 'meal_only', '2026-03-03 12:42:10', '2026-03-03 12:42:10');
INSERT INTO `student` VALUES ('stu8', '123456', '七七', 18, '男', '12332143215', NULL, 'meal_only', '2026-03-03 12:42:10', '2026-03-03 12:42:10');
INSERT INTO `student` VALUES ('stu9', '123456', '德萨', 20, '男', '15889658741', NULL, 'meal_and_rest', '2026-03-03 12:42:10', '2026-03-04 16:09:15');

-- ----------------------------
-- Table structure for student_parent_binding_request
-- ----------------------------
DROP TABLE IF EXISTS `student_parent_binding_request`;
CREATE TABLE `student_parent_binding_request`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `parent_username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `student_username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT 'pending',
  `student_confirmed` tinyint(4) NULL DEFAULT 0,
  `student_confirm_time` datetime NULL DEFAULT NULL,
  `admin_status` tinyint(4) NULL DEFAULT 0,
  `admin_confirm_time` datetime NULL DEFAULT NULL,
  `admin_username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `reject_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of student_parent_binding_request
-- ----------------------------
INSERT INTO `student_parent_binding_request` VALUES (1, '1432', 'shanshan', 'completed', 1, NULL, 1, NULL, NULL, NULL, '2026-07-03 19:35:51', '2026-07-03 19:35:51');
INSERT INTO `student_parent_binding_request` VALUES (2, 'parent1', 'stu1', 'completed', 1, NULL, 1, NULL, NULL, NULL, '2026-03-03 12:42:10', '2026-05-04 23:42:40');
INSERT INTO `student_parent_binding_request` VALUES (3, 'parent2', 'stu12', 'completed', 1, NULL, 1, NULL, NULL, NULL, '2026-03-03 12:42:10', '2026-05-05 00:13:44');

-- ----------------------------
-- Table structure for student_status_report
-- ----------------------------
DROP TABLE IF EXISTS `student_status_report`;
CREATE TABLE `student_status_report`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `student_username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '学生用户名',
  `student_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '学生姓名',
  `parent_username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '家长用户名',
  `report_date` date NOT NULL COMMENT '报告日期',
  `rest_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'normal' COMMENT '休息状况: good/normal/poor',
  `meal_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'normal' COMMENT '就餐状况: good/normal/poor',
  `mood_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'normal' COMMENT '情绪状况: good/normal/poor',
  `health_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'normal' COMMENT '健康状况: good/normal/poor',
  `remark` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注说明',
  `reporter_username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '记录人(管理员)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_student_date`(`student_username`, `report_date`) USING BTREE,
  INDEX `idx_parent_date`(`parent_username`, `report_date`) USING BTREE,
  INDEX `idx_student_date`(`student_username`, `report_date`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '学生状况报告表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of student_status_report
-- ----------------------------
INSERT INTO `student_status_report` VALUES (1, 'stu1', '张三', 'parent1', '2026-03-06', 'poor', 'good', 'normal', 'normal', '', 'admin', '2026-03-07 14:49:31', '2026-03-07 14:49:31');
INSERT INTO `student_status_report` VALUES (2, 'stu1', '张三', 'parent1', '2026-02-19', 'poor', 'poor', 'normal', 'normal', '', 'admin', '2026-03-07 14:50:32', '2026-03-07 14:50:32');
INSERT INTO `student_status_report` VALUES (3, 'shanshan', '姗姗', '1432', '2026-05-16', 'good', 'normal', 'normal', 'normal', '', 'admin', '2026-05-16 10:22:52', '2026-05-16 10:22:52');
INSERT INTO `student_status_report` VALUES (4, 'stu1', '张三', 'parent1', '2026-05-16', 'normal', 'good', 'good', 'normal', '', 'admin', '2026-05-16 10:23:03', '2026-05-16 10:23:03');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `age` int(11) NULL DEFAULT NULL,
  `gender` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `phone_num` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `user_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'student, parent, admin',
  `boarding_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `student_username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `dormbuild_id` int(11) NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`, `username`) USING BTREE,
  INDEX `idx_user_type`(`user_type`) USING BTREE,
  INDEX `idx_student_username`(`student_username`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 37 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '统一用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, '1432', '123456', '姗姗爸爸', 18, '男', '15279368236', NULL, 'parent', NULL, 'shanshan', NULL, '2026-03-07 19:35:51', '2026-03-07 19:35:51');
INSERT INTO `user` VALUES (2, 'admin', '$2a$10$GrFZuvtO8KTTJvNOvnU1hug.4Lm2VClQZ0BJaYdDDG.stYab/.ZW.', '大强', 18, '男', '14785412478', NULL, 'admin', NULL, NULL, NULL, '2026-03-03 12:42:10', '2026-03-30 20:10:59');
INSERT INTO `user` VALUES (3, 'Atest', '123456', '测试管理员', 22, '男', '14785412478', NULL, 'admin', NULL, NULL, NULL, '2026-03-03 12:42:10', '2026-03-03 12:42:10');
INSERT INTO `user` VALUES (4, 'dorm1', '$2a$10$.n6ychQOHJACd//uoJl/sugyWBEFhU7kYuktdvB/TTf7ciykVDQx2', '张三', 35, '男', '15222223333', '12@email.com', 'admin', NULL, NULL, 1, '2026-03-03 12:42:10', '2026-05-01 01:33:27');
INSERT INTO `user` VALUES (5, 'dorm2', '123456', '李四', 55, '女', '15333332222', NULL, 'admin', NULL, NULL, 2, '2026-03-03 12:42:10', '2026-03-03 12:42:10');
INSERT INTO `user` VALUES (6, 'dorm3', '123456', '王五', 38, '男', '15855552222', NULL, 'admin', NULL, NULL, 3, '2026-03-03 12:42:10', '2026-03-03 12:42:10');
INSERT INTO `user` VALUES (7, 'dorm4', '123456', '赵花', 40, '女', '15877776666', NULL, 'admin', NULL, NULL, 4, '2026-03-03 12:42:10', '2026-03-03 12:42:10');
INSERT INTO `user` VALUES (8, 'jiajia', '123456', '佳佳', 45, '女', '13557936673', NULL, 'parent', NULL, NULL, NULL, '2026-03-16 11:14:48', '2026-03-16 11:14:48');
INSERT INTO `user` VALUES (9, 'Mtest', '123456', '宿管测试', 22, '男', '15899999999', NULL, 'admin', NULL, NULL, 2, '2026-03-03 12:42:10', '2026-03-03 12:42:10');
INSERT INTO `user` VALUES (10, 'parent1', '$2a$10$/Uu4YFSD4FoqkMqvWQ.6Dep4w7/Y5x1KEQt6.MezIYocP4PVN9n1G', '张父', 45, '男', '13800138000', 'parent1@qq.com', 'parent', NULL, 'stu1', NULL, '2026-03-03 12:42:10', '2026-05-04 23:42:40');
INSERT INTO `user` VALUES (11, 'parent2', '$2a$10$0JdiMbWm3S94ZTtvZadgvegIYntLkPWv5HJ6xAPwChk92LgyLgJti', '丽丽妈妈', 18, '女', '13366483937', NULL, 'parent', NULL, 'stu12', NULL, '2026-03-03 12:42:10', '2026-05-05 00:13:44');
INSERT INTO `user` VALUES (12, 'shanshan', '123456', '姗姗', 18, '女', '14156848252', NULL, 'student', NULL, NULL, NULL, '2026-03-07 19:37:07', '2026-03-07 19:37:07');
INSERT INTO `user` VALUES (13, 'stu1', '$2a$10$cYH/EGFMO/vSr3AgadXFJ.nIeg2VNVXAGE3kvTlMTpsFldMHGyFu6', '张三', 18, '男', '13124143214', NULL, 'student', 'meal_and_rest', NULL, NULL, '2026-03-03 12:42:10', '2026-05-05 00:13:06');
INSERT INTO `user` VALUES (14, 'stu10', '123456', '马克', 19, '女', '15833333333', NULL, 'student', 'meal_and_rest', NULL, NULL, '2026-03-03 12:42:10', '2026-03-04 15:26:35');
INSERT INTO `user` VALUES (15, 'stu11', '123456', '巧巧', 16, '女', '18922223333', NULL, 'student', 'meal_and_rest', NULL, NULL, '2026-03-03 12:42:10', '2026-03-04 15:27:08');
INSERT INTO `user` VALUES (16, 'stu12', '123456', '丽丽', 17, '女', '17922222222', NULL, 'student', 'meal_and_rest', NULL, NULL, '2026-03-03 12:42:10', '2026-03-04 15:27:17');
INSERT INTO `user` VALUES (17, 'stu13', '123456', '美美', 18, '女', '15822222222', NULL, 'student', 'meal_and_rest', NULL, NULL, '2026-03-03 12:42:10', '2026-03-04 15:27:36');
INSERT INTO `user` VALUES (18, 'stu14', '123456', '拉拉', 20, '女', '13355556666', NULL, 'student', 'meal_and_rest', NULL, NULL, '2026-03-03 12:42:10', '2026-03-04 15:27:39');
INSERT INTO `user` VALUES (19, 'stu15', '123456', '贝贝', 18, '男', '15899999999', NULL, 'student', 'meal_only', NULL, NULL, '2026-03-03 12:42:10', '2026-03-03 12:42:10');
INSERT INTO `user` VALUES (20, 'stu16', '123456', '力力', 18, '男', '14596475257', NULL, 'student', 'meal_only', NULL, NULL, '2026-03-03 12:42:10', '2026-03-03 12:42:10');
INSERT INTO `user` VALUES (21, 'stu17', '123456', '阿成', 18, '男', '15896542147', NULL, 'student', 'meal_only', NULL, NULL, '2026-03-03 12:42:10', '2026-03-03 12:42:10');
INSERT INTO `user` VALUES (22, 'stu18', '123456', '阿达', 19, '女', '14785635874', 'akk@akkmail.com', 'student', 'meal_and_rest', NULL, NULL, '2026-03-03 12:42:10', '2026-03-04 16:09:23');
INSERT INTO `user` VALUES (23, 'stu19', '123456', '帕森斯', 19, '男', '15889658475', NULL, 'student', 'meal_and_rest', NULL, NULL, '2026-03-03 12:42:10', '2026-03-04 16:09:27');
INSERT INTO `user` VALUES (24, 'stu2', '123456', '田田', 18, '男', '15875359641', NULL, 'student', 'meal_and_rest', NULL, NULL, '2026-03-03 12:42:10', '2026-03-04 16:09:36');
INSERT INTO `user` VALUES (25, 'stu20', '123456', '柠檬', 21, '男', '15874563558', NULL, 'student', 'meal_and_rest', NULL, NULL, '2026-03-03 12:42:10', '2026-03-04 16:09:31');
INSERT INTO `user` VALUES (26, 'stu21', '123456', '面对', 21, '男', '15889635874', NULL, 'student', 'meal_only', NULL, NULL, '2026-03-03 12:42:10', '2026-03-03 12:42:10');
INSERT INTO `user` VALUES (27, 'stu22', '123456', '等等', 25, '男', '13412341234', 'akkk@kkk.com', 'student', 'meal_only', NULL, NULL, '2026-03-03 12:42:10', '2026-03-03 12:42:10');
INSERT INTO `user` VALUES (28, 'stu3', '123456', '吉安', 18, '男', '15798657350', NULL, 'student', 'meal_only', NULL, NULL, '2026-03-03 12:42:10', '2026-03-03 12:42:10');
INSERT INTO `user` VALUES (29, 'stu4', '123456', '力力', 22, '男', '15878965874', NULL, 'student', 'meal_only', NULL, NULL, '2026-03-03 12:42:10', '2026-03-03 12:42:10');
INSERT INTO `user` VALUES (30, 'stu5', '123456', '哦哦', 19, '男', '15897535478', NULL, 'student', 'meal_only', NULL, NULL, '2026-03-03 12:42:10', '2026-03-03 12:42:10');
INSERT INTO `user` VALUES (31, 'stu6', '123456', '泡泡', 18, '男', '18987554765', NULL, 'student', 'meal_only', NULL, NULL, '2026-03-03 12:42:10', '2026-03-03 12:42:10');
INSERT INTO `user` VALUES (32, 'stu7', '123456', '刚刚', 15, '男', '15897543854', NULL, 'student', 'meal_only', NULL, NULL, '2026-03-03 12:42:10', '2026-03-03 12:42:10');
INSERT INTO `user` VALUES (33, 'stu8', '123456', '七七', 18, '男', '12332143215', NULL, 'student', 'meal_only', NULL, NULL, '2026-03-03 12:42:10', '2026-03-03 12:42:10');
INSERT INTO `user` VALUES (34, 'stu9', '123456', '德萨', 20, '男', '15889658741', NULL, 'student', 'meal_and_rest', NULL, NULL, '2026-03-03 12:42:10', '2026-03-04 16:09:15');
INSERT INTO `user` VALUES (35, 'happy', '$2a$10$3Dex5Y2llG03tHgSnzxEquzNbzB/p1yDTz7ObVlv/Op7neOf2jH2a', '开心', 18, '男', '13442655764', NULL, 'student', NULL, NULL, NULL, '2026-03-30 20:20:28', '2026-03-30 20:20:28');
INSERT INTO `user` VALUES (36, 'parent11', '$2a$10$O5/nPaYr8XPFMV0nxNBzkuzVdzK4.DvGTYPA4abXFjIuUBcnsbkhm', '马克爸爸', 18, '男', '13851758937', NULL, 'parent', NULL, 'stu10', NULL, '2026-05-15 02:10:10', '2026-05-15 02:10:10');

-- ----------------------------
-- Table structure for visitor
-- ----------------------------
DROP TABLE IF EXISTS `visitor`;
CREATE TABLE `visitor`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '姓名',
  `gender` enum('男','女') CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '男' COMMENT '性别',
  `phone_num` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '电话',
  `origin_city` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '来源城市',
  `visit_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '来访时间',
  `content` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '事情',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of visitor
-- ----------------------------
INSERT INTO `visitor` VALUES (1, '张三', '男', '13433333333', '武汉', '2026-03-18 13:10:00', '探访孩子');
INSERT INTO `visitor` VALUES (2, '李四', '女', '15722222222', '江苏', '2026-03-19 17:08:06', '运送快递');
INSERT INTO `visitor` VALUES (3, '啊啊', '女', '13255555555', '湖北', '2025-12-19 16:41:21', '运送食品');

-- ----------------------------
-- Table structure for weekly_menu
-- ----------------------------
DROP TABLE IF EXISTS `weekly_menu`;
CREATE TABLE `weekly_menu`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '每周菜谱ID',
  `menu_id` int(11) NOT NULL COMMENT '菜谱ID',
  `week_day` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '星期(Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday)',
  `meal_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '餐次类型(breakfast, lunch, dinner)',
  `week_start_date` date NOT NULL COMMENT '本周开始日期',
  `week_end_date` date NOT NULL COMMENT '本周结束日期',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_menu_id`(`menu_id`) USING BTREE,
  INDEX `idx_week_date`(`week_start_date`, `week_end_date`) USING BTREE,
  CONSTRAINT `fk_weekly_menu_menu` FOREIGN KEY (`menu_id`) REFERENCES `menu` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 123 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '每周菜谱表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of weekly_menu
-- ----------------------------
INSERT INTO `weekly_menu` VALUES (13, 3, '1', 'lunch', '2025-12-29', '2026-01-04', '2026-01-25 16:45:25');
INSERT INTO `weekly_menu` VALUES (14, 5, '1', 'lunch', '2025-12-29', '2026-01-04', '2026-01-25 16:45:25');
INSERT INTO `weekly_menu` VALUES (15, 1, '1', 'lunch', '2025-12-29', '2026-01-04', '2026-01-25 16:45:25');
INSERT INTO `weekly_menu` VALUES (16, 2, '1', 'lunch', '2025-12-29', '2026-01-04', '2026-01-25 16:45:25');
INSERT INTO `weekly_menu` VALUES (17, 6, '1', 'lunch', '2025-12-29', '2026-01-04', '2026-01-25 16:45:25');
INSERT INTO `weekly_menu` VALUES (18, 3, '6', 'lunch', '2025-12-29', '2026-01-04', '2026-01-25 16:46:57');
INSERT INTO `weekly_menu` VALUES (19, 5, '6', 'lunch', '2025-12-29', '2026-01-04', '2026-01-25 16:46:57');
INSERT INTO `weekly_menu` VALUES (20, 1, '6', 'lunch', '2025-12-29', '2026-01-04', '2026-01-25 16:46:57');
INSERT INTO `weekly_menu` VALUES (21, 2, '6', 'lunch', '2025-12-29', '2026-01-04', '2026-01-25 16:46:57');
INSERT INTO `weekly_menu` VALUES (22, 3, 'Tuesday', 'lunch', '2026-01-26', '2026-02-01', '2026-01-25 17:30:32');
INSERT INTO `weekly_menu` VALUES (23, 1, 'Tuesday', 'lunch', '2026-01-26', '2026-02-01', '2026-01-25 17:30:32');
INSERT INTO `weekly_menu` VALUES (24, 5, 'Tuesday', 'lunch', '2026-01-26', '2026-02-01', '2026-01-25 17:30:32');
INSERT INTO `weekly_menu` VALUES (25, 2, 'Tuesday', 'lunch', '2026-01-26', '2026-02-01', '2026-01-25 17:30:32');
INSERT INTO `weekly_menu` VALUES (26, 6, 'Tuesday', 'lunch', '2026-01-26', '2026-02-01', '2026-01-25 17:30:32');
INSERT INTO `weekly_menu` VALUES (27, 7, 'Wednesday', 'dinner', '2026-01-05', '2026-01-11', '2026-01-25 17:31:15');
INSERT INTO `weekly_menu` VALUES (28, 5, 'Wednesday', 'dinner', '2026-01-05', '2026-01-11', '2026-01-25 17:31:15');
INSERT INTO `weekly_menu` VALUES (29, 1, 'Wednesday', 'dinner', '2026-01-05', '2026-01-11', '2026-01-25 17:31:15');
INSERT INTO `weekly_menu` VALUES (30, 2, 'Wednesday', 'dinner', '2026-01-05', '2026-01-11', '2026-01-25 17:31:15');
INSERT INTO `weekly_menu` VALUES (31, 7, 'Wednesday', 'lunch', '2026-01-05', '2026-01-11', '2026-01-25 17:31:49');
INSERT INTO `weekly_menu` VALUES (32, 5, 'Wednesday', 'lunch', '2026-01-05', '2026-01-11', '2026-01-25 17:31:49');
INSERT INTO `weekly_menu` VALUES (33, 1, 'Wednesday', 'lunch', '2026-01-05', '2026-01-11', '2026-01-25 17:31:49');
INSERT INTO `weekly_menu` VALUES (34, 6, 'Wednesday', 'lunch', '2026-01-05', '2026-01-11', '2026-01-25 17:31:49');
INSERT INTO `weekly_menu` VALUES (35, 2, 'Wednesday', 'lunch', '2026-01-05', '2026-01-11', '2026-01-25 17:31:49');
INSERT INTO `weekly_menu` VALUES (36, 3, 'Wednesday', 'lunch', '2026-03-02', '2026-03-08', '2026-03-02 17:50:22');
INSERT INTO `weekly_menu` VALUES (37, 1, 'Wednesday', 'lunch', '2026-03-02', '2026-03-08', '2026-03-02 17:50:22');
INSERT INTO `weekly_menu` VALUES (38, 5, 'Wednesday', 'lunch', '2026-03-02', '2026-03-08', '2026-03-02 17:50:22');
INSERT INTO `weekly_menu` VALUES (39, 2, 'Wednesday', 'lunch', '2026-03-02', '2026-03-08', '2026-03-02 17:50:22');
INSERT INTO `weekly_menu` VALUES (40, 81, 'Wednesday', 'dinner', '2026-03-09', '2026-03-15', '2026-03-16 13:59:22');
INSERT INTO `weekly_menu` VALUES (41, 21, 'Wednesday', 'dinner', '2026-03-09', '2026-03-15', '2026-03-16 13:59:22');
INSERT INTO `weekly_menu` VALUES (42, 91, 'Wednesday', 'dinner', '2026-03-09', '2026-03-15', '2026-03-16 13:59:22');
INSERT INTO `weekly_menu` VALUES (43, 64, 'Wednesday', 'dinner', '2026-03-09', '2026-03-15', '2026-03-16 13:59:22');
INSERT INTO `weekly_menu` VALUES (44, 4, 'Wednesday', 'dinner', '2026-03-08', '2026-03-14', '2026-03-16 13:59:22');
INSERT INTO `weekly_menu` VALUES (50, 4, 'Wednesday', 'breakfast', '2026-04-05', '2026-04-11', '2026-04-05 22:36:03');
INSERT INTO `weekly_menu` VALUES (56, 4, 'Thursday', 'lunch', '2026-04-05', '2026-04-11', '2026-04-05 22:36:51');
INSERT INTO `weekly_menu` VALUES (62, 4, 'Thursday', 'dinner', '2026-04-05', '2026-04-11', '2026-04-05 22:59:45');
INSERT INTO `weekly_menu` VALUES (63, 5, 'Monday', 'lunch', '2026-04-06', '2026-04-12', '2026-04-05 23:00:20');
INSERT INTO `weekly_menu` VALUES (64, 3, 'Monday', 'lunch', '2026-04-06', '2026-04-12', '2026-04-05 23:00:20');
INSERT INTO `weekly_menu` VALUES (65, 19, 'Monday', 'lunch', '2026-04-06', '2026-04-12', '2026-04-05 23:00:20');
INSERT INTO `weekly_menu` VALUES (66, 27, 'Monday', 'dinner', '2026-04-06', '2026-04-12', '2026-04-05 23:00:20');
INSERT INTO `weekly_menu` VALUES (67, 61, 'Monday', 'dinner', '2026-04-06', '2026-04-12', '2026-04-05 23:00:20');
INSERT INTO `weekly_menu` VALUES (68, 81, 'Monday', 'dinner', '2026-04-06', '2026-04-12', '2026-04-05 23:00:20');
INSERT INTO `weekly_menu` VALUES (69, 73, 'Thursday', 'lunch', '2026-04-06', '2026-04-12', '2026-04-05 23:00:20');
INSERT INTO `weekly_menu` VALUES (70, 86, 'Thursday', 'lunch', '2026-04-06', '2026-04-12', '2026-04-05 23:00:20');
INSERT INTO `weekly_menu` VALUES (71, 33, 'Thursday', 'lunch', '2026-04-06', '2026-04-12', '2026-04-05 23:00:20');
INSERT INTO `weekly_menu` VALUES (72, 85, 'Thursday', 'lunch', '2026-04-06', '2026-04-12', '2026-04-05 23:00:20');
INSERT INTO `weekly_menu` VALUES (73, 65, 'Thursday', 'lunch', '2026-04-06', '2026-04-12', '2026-04-05 23:00:20');
INSERT INTO `weekly_menu` VALUES (74, 67, 'Thursday', 'dinner', '2026-04-06', '2026-04-12', '2026-04-05 23:00:20');
INSERT INTO `weekly_menu` VALUES (75, 29, 'Thursday', 'dinner', '2026-04-06', '2026-04-12', '2026-04-05 23:00:20');
INSERT INTO `weekly_menu` VALUES (76, 17, 'Thursday', 'dinner', '2026-04-06', '2026-04-12', '2026-04-05 23:00:20');
INSERT INTO `weekly_menu` VALUES (77, 30, 'Thursday', 'dinner', '2026-04-06', '2026-04-12', '2026-04-05 23:00:20');
INSERT INTO `weekly_menu` VALUES (78, 101, 'Thursday', 'dinner', '2026-04-06', '2026-04-12', '2026-04-05 23:00:20');
INSERT INTO `weekly_menu` VALUES (79, 4, 'Monday', 'lunch', '2026-04-05', '2026-04-11', '2026-04-05 23:00:20');
INSERT INTO `weekly_menu` VALUES (80, 4, 'Thursday', 'lunch', '2026-04-05', '2026-04-11', '2026-04-05 23:00:20');
INSERT INTO `weekly_menu` VALUES (81, 4, 'Thursday', 'dinner', '2026-04-05', '2026-04-11', '2026-04-05 23:00:20');
INSERT INTO `weekly_menu` VALUES (82, 4, 'Monday', 'dinner', '2026-04-05', '2026-04-11', '2026-04-05 23:00:20');
INSERT INTO `weekly_menu` VALUES (88, 4, 'Monday', 'lunch', '2026-05-03', '2026-05-09', '2026-05-15 00:17:16');
INSERT INTO `weekly_menu` VALUES (95, 4, 'Wednesday', 'lunch', '2026-05-03', '2026-05-09', '2026-05-15 00:21:42');
INSERT INTO `weekly_menu` VALUES (102, 4, 'Thursday', 'lunch', '2026-05-03', '2026-05-09', '2026-05-15 00:30:00');
INSERT INTO `weekly_menu` VALUES (108, 4, 'Thursday', 'lunch', '2026-05-03', '2026-05-09', '2026-05-15 00:35:04');
INSERT INTO `weekly_menu` VALUES (109, 99, 'Thursday', 'lunch', '2026-05-04', '2026-05-10', '2026-05-15 01:03:21');
INSERT INTO `weekly_menu` VALUES (110, 4, 'Thursday', 'lunch', '2026-05-03', '2026-05-09', '2026-05-15 01:03:21');
INSERT INTO `weekly_menu` VALUES (111, 70, 'Thursday', 'lunch', '2026-05-04', '2026-05-10', '2026-05-15 01:04:00');
INSERT INTO `weekly_menu` VALUES (112, 100, 'Thursday', 'lunch', '2026-05-04', '2026-05-10', '2026-05-15 01:04:00');
INSERT INTO `weekly_menu` VALUES (113, 92, 'Thursday', 'lunch', '2026-05-04', '2026-05-10', '2026-05-15 01:04:00');
INSERT INTO `weekly_menu` VALUES (114, 43, 'Thursday', 'lunch', '2026-05-04', '2026-05-10', '2026-05-15 01:04:00');
INSERT INTO `weekly_menu` VALUES (115, 58, 'Thursday', 'lunch', '2026-05-04', '2026-05-10', '2026-05-15 01:04:00');
INSERT INTO `weekly_menu` VALUES (116, 4, 'Thursday', 'lunch', '2026-05-03', '2026-05-09', '2026-05-15 01:04:00');
INSERT INTO `weekly_menu` VALUES (117, 79, 'Thursday', 'dinner', '2026-05-04', '2026-05-10', '2026-05-15 01:04:23');
INSERT INTO `weekly_menu` VALUES (118, 37, 'Thursday', 'dinner', '2026-05-04', '2026-05-10', '2026-05-15 01:04:23');
INSERT INTO `weekly_menu` VALUES (119, 86, 'Thursday', 'dinner', '2026-05-04', '2026-05-10', '2026-05-15 01:04:23');
INSERT INTO `weekly_menu` VALUES (120, 82, 'Thursday', 'dinner', '2026-05-04', '2026-05-10', '2026-05-15 01:04:23');
INSERT INTO `weekly_menu` VALUES (121, 44, 'Thursday', 'dinner', '2026-05-04', '2026-05-10', '2026-05-15 01:04:23');
INSERT INTO `weekly_menu` VALUES (122, 4, 'Thursday', 'dinner', '2026-05-03', '2026-05-09', '2026-05-15 01:04:23');

SET FOREIGN_KEY_CHECKS = 1;
