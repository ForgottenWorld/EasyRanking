CREATE TABLE IF NOT EXISTS `easyranking_board` (
  `id` varchar(100) NOT NULL,
  `name` varchar(100) NOT NULL,
  `description` varchar(500) NOT NULL,
  `max_players` int(11) NOT NULL,
  `is_visible` int(11) NOT NULL DEFAULT 1,
  `is_deleted` int(11) NOT NULL DEFAULT 0,
  `is_default` int(11) NOT NULL DEFAULT 0,
  `user_score_name` varchar(100) NOT NULL,
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `easyranking_user` (
  `uuid` varchar(36) NOT NULL,
  `nickname` varchar(50) NOT NULL,
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `easyranking_user_score` (
  `id_user` varchar(36) NOT NULL,
  `id_board` varchar(100) NOT NULL,
  `amount` float NOT NULL DEFAULT '0',
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_user`,`id_board`),
  KEY `user_score_fk_board` (`id_board`),
  KEY `user_score_fk_user` (`id_user`),
  CONSTRAINT `user_score_fk_board` FOREIGN KEY (`id_board`) REFERENCES `easyranking_board` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `user_score_fk_user` FOREIGN KEY (`id_user`) REFERENCES `easyranking_user` (`uuid`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `easyranking_item_reward` (
  `id_board` varchar(100) NOT NULL,
  `rank_position` int(11) NOT NULL,
  `item_type` text,
  KEY `easyranking_reward_fk_easyranking_board` (`id_board`),
  CONSTRAINT `easyranking_item_reward_fk_easyranking_board` FOREIGN KEY (`id_board`) REFERENCES `easyranking_board` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `easyranking_money_reward` (
  `id_board` varchar(100) NOT NULL,
  `rank_position` int(11) NOT NULL,
  `amount` decimal(16,2) DEFAULT NULL,
  PRIMARY KEY (`id_board`,`rank_position`),
  KEY `easyranking_reward_fk_easyranking_board` (`id_board`),
  CONSTRAINT `easyranking_money_reward_fk_easyranking_board` FOREIGN KEY (`id_board`) REFERENCES `easyranking_board` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `easyranking_title_reward` (
  `id_board` varchar(100) NOT NULL,
  `rank_position` int(11) NOT NULL,
  `title` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`id_board`,`rank_position`),
  KEY `easyranking_reward_fk_easyranking_board` (`id_board`),
  CONSTRAINT `easyranking_title_reward_fk_easyranking_board` FOREIGN KEY (`id_board`) REFERENCES `easyranking_board` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;