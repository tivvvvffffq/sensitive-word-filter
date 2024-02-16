CREATE TABLE `SensitiveWords` (
    `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '敏感词ID',
    `word` varchar(255) NOT NULL COMMENT '敏感词',
    `categoryID` int(11) DEFAULT NULL COMMENT '分类ID',
    `type` tinyint(4) NOT NULL COMMENT '敏感词类型（0：普通，1：组合）',
    `isDeleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除（0：未删除，1：删除）',
    `addedTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
    PRIMARY KEY (`id`)
) COMMENT='敏感词表';