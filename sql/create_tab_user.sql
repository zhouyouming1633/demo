DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键Id',
  `pin` varchar(50) NOT NULL COMMENT '用户账号',
  `name` varchar(200) NOT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '密码',
  `phone` varchar(20) NOT NULL COMMENT '联系电话',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `status` tinyint(1) NOT NULL COMMENT '状态：0-无效 1-有效',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_account`(`account`) USING BTREE,
  INDEX `idx_name`(`name`) USING BTREE,
  INDEX `idx_phone`(`phone`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4

/**测试数据*/
INSERT INTO `user` (`pin`, `name`, `password`, `phone`, `email`, `status`, `remark`, `create_time`, `update_time`) VALUES ('jdxsd_34edsop', '刘邦', 'sddssza@sededf3', '15001923333', 'xxx@163.mail', 1, NULL, '2025-05-24 13:44:52', '2025-05-24 13:44:55');
