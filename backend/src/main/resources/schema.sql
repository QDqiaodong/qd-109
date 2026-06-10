SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS digital_community DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE digital_community;

CREATE TABLE IF NOT EXISTS t_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    nickname VARCHAR(50) NOT NULL,
    avatar VARCHAR(255),
    password VARCHAR(100) NOT NULL,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS t_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    icon VARCHAR(255),
    sort INT DEFAULT 0,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS t_post (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    images TEXT,
    image_groups TEXT COMMENT '图片分组JSON数组：外观/接口/上机效果/桌搭全景',
    accessory_cards TEXT COMMENT '配件参数卡片JSON数组',
    type TINYINT DEFAULT 1 COMMENT '1:体验分享 2:问题求助',
    view_count INT DEFAULT 0,
    like_count INT DEFAULT 0,
    comment_count INT DEFAULT 0,
    status TINYINT DEFAULT 1 COMMENT '1:正常 0:下架',
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_category_id (category_id),
    INDEX idx_create_time (create_time),
    INDEX idx_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS t_comment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    parent_id BIGINT,
    reply_user_id BIGINT,
    content TEXT NOT NULL,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_post_id (post_id),
    INDEX idx_user_id (user_id),
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO t_category (name, icon, sort) VALUES
('手机配件', '📱', 1),
('电脑配件', '💻', 2),
('影音设备', '🎧', 3),
('智能穿戴', '⌚', 4),
('摄影器材', '📷', 5),
('游戏外设', '🎮', 6),
('网络设备', '📶', 7),
('存储设备', '💾', 8);

INSERT INTO t_user (username, nickname, avatar, password) VALUES
('test001', '数码达人', 'https://api.dicebear.com/7.x/avataaars/svg?seed=test001', '123456'),
('test002', '配件玩家', 'https://api.dicebear.com/7.x/avataaars/svg?seed=test002', '123456'),
('test003', '科技爱好者', 'https://api.dicebear.com/7.x/avataaars/svg?seed=test003', '123456');

INSERT INTO t_post (user_id, category_id, title, content, images, accessory_cards, type, view_count, like_count, comment_count) VALUES
(1, 1, 'AirPods Pro 2 使用三个月真实体验', '用了三个月的AirPods Pro 2，降噪效果真的没得说，通勤地铁上基本听不到外界噪音。续航也很给力，单次使用6小时没问题，搭配充电盒能用30小时。唯一缺点就是价格有点贵，但确实一分钱一分货。', 'https://images.unsplash.com/photo-1606220588913-b3aacb4d2f46?w=600', '[{"model":"AirPods Pro 2","interfaceType":"蓝牙5.3 / Lightning","compatibleDevices":["iPhone","iPad","MacBook","Apple Watch"],"usageScenarios":["通勤出行","办公学习","运动健身"],"pros":["降噪效果好","续航持久","佩戴舒适","音质出色"],"cons":["价格偏高","易丢失"]}]', 1, 1256, 89, 23),
(2, 2, '机械键盘选购指南：樱桃轴 vs 佳达隆轴', '最近换了两把机械键盘，一把樱桃红轴，一把佳达隆黄轴。分享一下使用感受：樱桃红轴手感更清脆，打字声音大一点；佳达隆黄轴更顺滑，声音小适合办公室。预算够选樱桃，性价比选佳达隆。', 'https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=600', '[{"model":"樱桃红轴","interfaceType":"Type-C","compatibleDevices":["Windows电脑","MacBook"],"usageScenarios":["办公学习","游戏娱乐"],"pros":["手感清脆","做工精良"],"cons":["价格偏高","声音较大"]},{"model":"佳达隆黄轴","interfaceType":"Type-C","compatibleDevices":["Windows电脑","MacBook"],"usageScenarios":["办公学习"],"pros":["性价比高","声音小","手感顺滑"],"cons":["品牌知名度一般"]}]', 1, 892, 67, 15),
(3, 3, '求助！蓝牙耳机连接电脑有杂音怎么办', '刚买的蓝牙耳机连接手机没问题，但连接Windows电脑就有滋滋的杂音，试过重新配对、更新驱动都没用，有大佬遇到过类似问题吗？求解决方案！', NULL, NULL, 2, 567, 23, 42),
(1, 4, 'Apple Watch Ultra 2 表带搭配分享', '入手Ultra 2一个月，换了三条表带：原装高山回环最适合运动；米兰尼斯表带日常通勤很有质感；皮革表带正式场合必备。推荐大家根据不同场景换表带，体验感翻倍。', 'https://images.unsplash.com/photo-1579586337278-3befd40fd17a?w=600', NULL, 1, 756, 45, 12),
(2, 6, 'PS5手柄摇杆漂移了怎么办？', 'PS5用了一年多，左摇杆开始漂移了，玩游戏的时候人物自己会走。有没有自己维修过的朋友？需要买什么工具？还是直接送修比较好？', NULL, NULL, 2, 423, 34, 56);
