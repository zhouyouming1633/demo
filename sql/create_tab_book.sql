DROP TABLE IF EXISTS `books`;
CREATE TABLE `books`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键Id',
  `isbn` varchar(13) NOT NULL COMMENT '图书isbn编码',
  `name` varchar(200) NOT NULL COMMENT '图书名称',
  `author` varchar(100) NOT NULL COMMENT '作者',
  `publisher` varchar(100) NOT NULL COMMENT '出版商',
  `status` tinyint(1) NOT NULL COMMENT '状态：0-下架 1-在库 2-借出',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_isbn`(`isbn`) USING BTREE,
  INDEX `idx_name`(`name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4

/**测试数据*/
INSERT INTO `book` (`isbn`, `name`, `author`, `publisher`, `status`, `remark`, `create_time`, `update_time`) VALUES ('978-7-544-25', 'jack', 'jim', '浙江工业', 1, NULL, '2025-05-24 13:42:34', '2025-05-24 17:36:42');
INSERT INTO `book` (`isbn`, `name`, `author`, `publisher`, `status`, `remark`, `create_time`, `update_time`) VALUES ('978-7-544-26', '环球科学2', 'jim', '湖北工业出版社', 1, NULL, '2025-05-24 14:45:10', '2025-05-24 15:05:01');
