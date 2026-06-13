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
    required_fields TEXT COMMENT '求助帖必填字段JSON数组',
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
    fault_info TEXT COMMENT '求助帖故障信息JSON',
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
    root_id BIGINT COMMENT '根评论ID（主评论）',
    reply_user_id BIGINT,
    content TEXT NOT NULL,
    depth INT DEFAULT 1 COMMENT '评论深度，1为主评论，2为直接回复，以此类推',
    is_collapsed TINYINT DEFAULT 0 COMMENT '是否因深度超限被自动收束 0:否 1:是',
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_post_id (post_id),
    INDEX idx_user_id (user_id),
    INDEX idx_parent_id (parent_id),
    INDEX idx_root_id (root_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO t_category (name, icon, sort, required_fields) VALUES
('手机配件', '📱', 1, '["deviceModel","accessoryModel","symptoms"]'),
('电脑配件', '💻', 2, '["deviceModel","accessoryModel","symptoms"]'),
('影音设备', '🎧', 3, '["deviceModel","accessoryModel","connectionType","symptoms"]'),
('智能穿戴', '⌚', 4, '["deviceModel","accessoryModel","symptoms"]'),
('摄影器材', '📷', 5, '["deviceModel","accessoryModel","symptoms"]'),
('游戏外设', '🎮', 6, '["deviceModel","accessoryModel","connectionType","symptoms"]'),
('网络设备', '📶', 7, '["deviceModel","accessoryModel","symptoms"]'),
('存储设备', '💾', 8, '["deviceModel","accessoryModel","connectionType","platform","environment","symptoms"]');

INSERT INTO t_user (username, nickname, avatar, password) VALUES
('test001', '数码达人', 'https://api.dicebear.com/7.x/avataaars/svg?seed=test001', '123456'),
('test002', '配件玩家', 'https://api.dicebear.com/7.x/avataaars/svg?seed=test002', '123456'),
('test003', '科技爱好者', 'https://api.dicebear.com/7.x/avataaars/svg?seed=test003', '123456');

INSERT INTO t_post (user_id, category_id, title, content, images, accessory_cards, type, fault_info, view_count, like_count, comment_count) VALUES
(1, 1, 'AirPods Pro 2 使用三个月真实体验', '用了三个月的AirPods Pro 2，降噪效果真的没得说，通勤地铁上基本听不到外界噪音。续航也很给力，单次使用6小时没问题，搭配充电盒能用30小时。唯一缺点就是价格有点贵，但确实一分钱一分货。', 'https://images.unsplash.com/photo-1606220588913-b3aacb4d2f46?w=600', '[{"model":"AirPods Pro 2","interfaceType":"蓝牙5.3 / Lightning","compatibleDevices":["iPhone","iPad","MacBook","Apple Watch"],"usageScenarios":["通勤出行","办公学习","运动健身"],"pros":["降噪效果好","续航持久","佩戴舒适","音质出色"],"cons":["价格偏高","易丢失"]}]', 1, NULL, 1256, 89, 23),
(2, 2, '机械键盘选购指南：樱桃轴 vs 佳达隆轴', '最近换了两把机械键盘，一把樱桃红轴，一把佳达隆黄轴。分享一下使用感受：樱桃红轴手感更清脆，打字声音大一点；佳达隆黄轴更顺滑，声音小适合办公室。预算够选樱桃，性价比选佳达隆。', 'https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=600', '[{"model":"樱桃红轴","interfaceType":"Type-C","compatibleDevices":["Windows电脑","MacBook"],"usageScenarios":["办公学习","游戏娱乐"],"pros":["手感清脆","做工精良"],"cons":["价格偏高","声音较大"]},{"model":"佳达隆黄轴","interfaceType":"Type-C","compatibleDevices":["Windows电脑","MacBook"],"usageScenarios":["办公学习"],"pros":["性价比高","声音小","手感顺滑"],"cons":["品牌知名度一般"]}]', 1, NULL, 892, 67, 15),
(3, 3, '求助！AirPods Pro 2连接电脑有杂音怎么办', '刚买的AirPods Pro 2连接手机没问题，但连接Windows 11电脑就有滋滋的杂音和底噪，还有电流声，试过重新配对、更新蓝牙驱动都没用，有大佬遇到过类似问题吗？求解决方案！', NULL, NULL, 2, '{"deviceModel":"Windows 11 PC","accessoryModel":"AirPods Pro 2","connectionType":"蓝牙","symptoms":"连接电脑有滋滋的杂音、底噪、电流声","triedActions":"重新配对、更新蓝牙驱动"}', 567, 23, 42),
(1, 4, 'Apple Watch Ultra 2 表带搭配分享', '入手Ultra 2一个月，换了三条表带：原装高山回环最适合运动；米兰尼斯表带日常通勤很有质感；皮革表带正式场合必备。推荐大家根据不同场景换表带，体验感翻倍。', 'https://images.unsplash.com/photo-1579586337278-3befd40fd17a?w=600', NULL, 1, NULL, 756, 45, 12),
(2, 6, '求助！PS5手柄摇杆漂移了怎么办？', 'PS5用了一年多，左摇杆开始漂移了，玩游戏的时候人物自己会走。按键也有失灵的情况，有没有自己维修过的朋友？需要买什么工具？还是直接送修比较好？', NULL, NULL, 2, '{"deviceModel":"PS5","accessoryModel":"DualSense手柄","connectionType":"蓝牙/有线","symptoms":"左摇杆漂移、按键失灵","triedActions":"重置手柄、更新固件"}', 423, 34, 56),
(1, 3, '求助！AirPods Pro 2频繁断连问题', 'AirPods Pro 2用了半年，最近开始频繁掉连，连接iPhone 15 Pro时经常断开，地铁上更是时断时续不稳定，有时候需要重新配对才能恢复，非常影响使用体验！', NULL, NULL, 2, '{"deviceModel":"iPhone 15 Pro","accessoryModel":"AirPods Pro 2","connectionType":"蓝牙","symptoms":"频繁掉连、断开、时断时续、连接不稳定","triedActions":"忽略设备重新配对、重置AirPods、更新iOS"}', 892, 67, 89),
(3, 3, '求助！AirPods Pro 2一边没声音', 'AirPods Pro 2右耳突然没声音了，左耳正常。检查了音量平衡没问题，重置了也没用。有时候戴上麦克风也没声，通话对方听不到。', NULL, NULL, 2, '{"deviceModel":"iPhone 15 Pro","accessoryModel":"AirPods Pro 2","connectionType":"蓝牙","symptoms":"右耳没声音、一边无声、麦克风没声","triedActions":"重置AirPods、清洁耳塞、检查设置"}', 345, 18, 34),
(2, 8, '求助！三星T7移动硬盘不兼容Mac', '刚买的三星T7 2TB移动硬盘，插在MacBook Pro上识别不了，磁盘工具里也看不到。换了USB线也没用，Windows电脑上可以正常读写。这是兼容异常吗？求解决办法！', NULL, NULL, 2, '{"deviceModel":"MacBook Pro M3","accessoryModel":"三星T7 2TB","connectionType":"USB-C","platform":"macOS Sonoma","environment":"Thunderbolt 4","symptoms":"不兼容、识别不了、检测不到、读不出来","triedActions":"更换USB线、查看磁盘工具、重启Mac"}', 512, 28, 45),
(1, 8, '求助！三星T7传输速度慢供电不足', '三星T7移动硬盘传大文件时经常传输失败，速度也很慢，怀疑是USB接口供电不足。尤其在拷贝10G以上的视频时经常中途断掉，显示传输失败。', NULL, NULL, 2, '{"deviceModel":"MacBook Air M2","accessoryModel":"三星T7 1TB","connectionType":"USB-C","platform":"macOS Ventura","environment":"USB 3.2","symptoms":"供电不足、传输慢、传输失败、拷贝慢、写入慢、丢数据","triedActions":"换接口、换线、格式化硬盘"}', 438, 22, 31),
(3, 6, '求助！Xbox手柄蓝牙搜不到配对失败', 'Xbox Series X手柄想连Windows电脑玩游戏，但是蓝牙搜不到手柄，搜索不到设备。用有线可以，但是蓝牙就是配对失败。驱动都装好了啊！', NULL, NULL, 2, '{"deviceModel":"Windows 10 PC","accessoryModel":"Xbox Series X手柄","connectionType":"蓝牙","symptoms":"蓝牙搜不到、搜索不到、配对失败","triedActions":"更新蓝牙驱动、重启蓝牙服务、换USB蓝牙适配器"}', 378, 25, 41),
(2, 6, '求助！Switch Pro手柄按键连击', 'Switch Pro手柄用了8个月，A键开始连击了，按一下出两次，玩塞尔达都没法正常选菜单。按键手感也变得奇怪。', NULL, NULL, 2, '{"deviceModel":"Switch OLED","accessoryModel":"Switch Pro手柄","connectionType":"蓝牙","symptoms":"按键连击、双击、A键失灵","triedActions":"用WD40喷、拆开清理、联系售后"}', 445, 31, 58),
(1, 7, '求助！小米路由器AX6000频繁掉线', '小米AX6000用了三个月，Wi-Fi频繁掉线，2.4G和5G都会断连。每天都要重启好几次，非常不稳定。固件已经是最新的了。', NULL, NULL, 2, '{"deviceModel":"小米AX6000","accessoryModel":"无","connectionType":"Wi-Fi","symptoms":"频繁掉线、掉连、Wi-Fi断开、连接不稳定","triedActions":"更新固件、修改信道、恢复出厂设置"}', 523, 36, 67),
(3, 3, '求助！索尼WH-1000XM5底噪严重', '索尼WH-1000XM5降噪模式下底噪特别明显，安静环境下能听到沙沙声和滋滋声。LDAC编码的时候延迟也很高，音画不同步。', NULL, NULL, 2, '{"deviceModel":"iPhone 15 Pro Max","accessoryModel":"索尼WH-1000XM5","connectionType":"蓝牙","symptoms":"底噪、沙沙声、滋滋声、延迟高、音画不同步","triedActions":"切换AAC编码、重置耳机、更换连接设备"}', 401, 27, 38),
(2, 2, '求助！罗技MX Master 3S滚轮卡顿', '罗技MX Master 3S用了半年，电磁滚轮最近开始卡顿，滚动网页的时候不流畅。用蓝牙连接延迟也明显，不如接收器。', NULL, NULL, 2, '{"deviceModel":"MacBook Pro 14寸 M3","accessoryModel":"罗技MX Master 3S","connectionType":"蓝牙/优联","symptoms":"滚轮卡顿、滚动不流畅、蓝牙延迟高","triedActions":"重新配对、更新Options+软件、清洁滚轮"}', 289, 19, 27),
(1, 5, '求助！索尼A7M4存储卡兼容性问题', '索尼A7M4用新的V60 SD卡经常显示卡错误，写入速度也慢，拍4K视频经常中断。换回旧卡就没事，这是兼容异常吗？', NULL, NULL, 2, '{"deviceModel":"索尼A7M4","accessoryModel":"某品牌V60 SD卡 128GB","connectionType":"SD卡槽","symptoms":"卡错误、不兼容、写入慢、录制中断","triedActions":"格式化、换卡槽、咨询客服"}', 198, 14, 22);

INSERT INTO t_post (user_id, category_id, title, content, images, accessory_cards, type, fault_info, view_count, like_count, comment_count) VALUES
(1, 2, 'MacBook Pro桌面搭配分享：扩展坞+显示器+键鼠', '花了一个月终于把桌面搭配搞明白了！MacBook Pro M3配CalDigit TS4扩展坞，再接上戴尔U2723QE显示器，加上罗技MX Master 3S鼠标和HHKB键盘，生产力直接拉满。', 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=600', '[{"model":"MacBook Pro 14寸 M3","interfaceType":"Thunderbolt 4","compatibleDevices":["MacOS"],"usageScenarios":["办公学习","商务会议"],"pros":["性能强劲","屏幕出色","续航持久"],"cons":["价格偏高","接口少"]},{"model":"CalDigit TS4 扩展坞","interfaceType":"Thunderbolt 4","compatibleDevices":["MacBook","Windows电脑"],"usageScenarios":["办公学习","居家使用"],"pros":["接口丰富","供电稳定","传输速度快"],"cons":["体积大","价格贵"]},{"model":"戴尔 U2723QE 显示器","interfaceType":"HDMI / DP / USB-C","compatibleDevices":["MacBook","Windows电脑"],"usageScenarios":["办公学习","设计创作"],"pros":["4K分辨率","色彩准确","Type-C供电"],"cons":["价格高","底座占空间"]}]', 1, NULL, 2340, 156, 67),
(2, 2, 'Windows主机桌搭方案：主机+机械键盘+游戏鼠标+耳机', '新配了一台Windows游戏主机，搭配了整套外设：樱桃MX3.0S机械键盘、罗技G502无线鼠标、赛睿寒冰7耳机。游戏体验直接起飞！', 'https://images.unsplash.com/photo-1587202372775-e229f172b9d7?w=600', '[{"model":"联想拯救者 游戏主机","interfaceType":"USB / 蓝牙","compatibleDevices":["Windows"],"usageScenarios":["游戏娱乐","办公学习"],"pros":["性能强劲","散热好","扩展性强"],"cons":["体积大","噪音大"]},{"model":"樱桃 MX3.0S 机械键盘","interfaceType":"Type-C 有线","compatibleDevices":["Windows电脑","MacBook"],"usageScenarios":["游戏娱乐","办公学习"],"pros":["手感出色","做工精良","PBT键帽"],"cons":["价格偏高","无无线版"]},{"model":"罗技 G502 X 鼠标","interfaceType":"USB 有线 / Lightspeed","compatibleDevices":["Windows电脑","MacBook"],"usageScenarios":["游戏娱乐","办公学习"],"pros":["手感舒适","定位精准","功能丰富"],"cons":["重量大","价格高"]},{"model":"赛睿 寒冰7 无线耳机","interfaceType":"2.4G无线 / 3.5mm","compatibleDevices":["Windows电脑","PS5","Switch"],"usageScenarios":["游戏娱乐","影音欣赏"],"pros":["音质好","佩戴舒适","低延迟"],"cons":["续航一般","塑料感强"]}]', 1, NULL, 1890, 123, 45),
(3, 1, 'iPhone 15 Pro配件全家桶：耳机+手表+充电器', '入手iPhone 15 Pro后陆续买了一堆配件：AirPods Pro 2耳机、Apple Watch Ultra 2手表、Anker 100W充电器。整套用下来体验真的无缝。', 'https://images.unsplash.com/photo-1592750475338-74b7b21085ab?w=600', '[{"model":"iPhone 15 Pro","interfaceType":"USB-C","compatibleDevices":["iOS"],"usageScenarios":["通勤出行","办公学习","摄影创作"],"pros":["性能强","拍照好","质感佳"],"cons":["价格贵","续航一般"]},{"model":"AirPods Pro 2","interfaceType":"蓝牙5.3 / USB-C","compatibleDevices":["iPhone","iPad","MacBook","Apple Watch"],"usageScenarios":["通勤出行","办公学习","运动健身"],"pros":["降噪效果好","续航持久","佩戴舒适"],"cons":["价格偏高","易丢失"]},{"model":"Apple Watch Ultra 2","interfaceType":"蓝牙","compatibleDevices":["iPhone"],"usageScenarios":["运动健身","户外旅行","商务会议"],"pros":["续航长","功能丰富","坚固耐用"],"cons":["价格贵","重量大"]},{"model":"Anker 100W GaN 充电器","interfaceType":"USB-C x4","compatibleDevices":["iPhone","MacBook","iPad","Switch"],"usageScenarios":["通勤出行","办公学习"],"pros":["功率大","体积小","多口同时充"],"cons":["发热明显","价格偏高"]}]', 1, NULL, 3120, 201, 89),
(1, 6, 'PS5玩家必备配件：手柄+耳机+充电底座', 'PS5玩家的三件套：DualSense手柄、Pulse 3D耳机、官方充电底座。有了这三样，游戏体验直接上一个档次。', 'https://images.unsplash.com/photo-1606144042614-b2417e99c4e3?w=600', '[{"model":"PS5 主机","interfaceType":"HDMI 2.1 / USB","compatibleDevices":["PS5游戏"],"usageScenarios":["游戏娱乐","影音欣赏"],"pros":["性能强","独占多","手柄体验好"],"cons":["体积大","价格高"]},{"model":"DualSense 无线手柄","interfaceType":"蓝牙 / Type-C","compatibleDevices":["PS5","PC","手机"],"usageScenarios":["游戏娱乐"],"pros":["手感好","自适应扳机","震动细腻"],"cons":["漂移问题","价格高"]},{"model":"Pulse 3D 无线耳机","interfaceType":"2.4G无线 / 3.5mm","compatibleDevices":["PS5","PC"],"usageScenarios":["游戏娱乐","影音欣赏"],"pros":["3D音效好","佩戴舒适","续航不错"],"cons":["音质一般","塑料感强"]}]', 1, NULL, 1567, 98, 34),
(2, 5, '索尼A7M4创作者套装：镜头+稳定器+麦克风', '做视频创作一年了，分享一下我的索尼A7M4套装：适马24-70 F2.8镜头、智云Weebill 3稳定器、罗德Wireless GO II麦克风。这套组合拍视频真的香！', 'https://images.unsplash.com/photo-1502920917128-1aa500764cbd?w=600', '[{"model":"索尼 A7M4 相机","interfaceType":"E卡口 / USB-C / HDMI","compatibleDevices":["索尼E卡口镜头"],"usageScenarios":["摄影创作","视频制作"],"pros":["画质好","对焦快","视频功能强"],"cons":["价格高","续航一般"]},{"model":"适马 24-70mm F2.8 DN","interfaceType":"索尼E卡口","compatibleDevices":["索尼全画幅微单"],"usageScenarios":["摄影创作","视频制作"],"pros":["画质锐","光圈大","对焦快"],"cons":["重量大","价格高"]},{"model":"智云 Weebill 3 稳定器","interfaceType":"Type-C","compatibleDevices":["微单相机"],"usageScenarios":["视频制作","户外旅行"],"pros":["防抖强","轻量化","功能多"],"cons":["学习成本高","价格不便宜"]},{"model":"罗德 Wireless GO II 麦克风","interfaceType":"3.5mm / USB-C","compatibleDevices":["相机","手机","电脑"],"usageScenarios":["视频制作","直播"],"pros":["音质好","便携","续航长"],"cons":["价格高","防风一般"]}]', 1, NULL, 2100, 145, 56),
(3, 2, '极简桌面：笔记本+蓝牙键鼠', '分享我的极简桌面搭配：MacBook Air M2 + 妙控键盘 + 妙控鼠标。虽然简单但效率很高，颜值也在线。', 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=600', '[{"model":"MacBook Air M2","interfaceType":"USB-C / MagSafe","compatibleDevices":["MacOS"],"usageScenarios":["办公学习","居家使用"],"pros":["轻薄便携","续航超长","静音"],"cons":["接口少","性能一般"]},{"model":"Apple 妙控键盘","interfaceType":"蓝牙 / Lightning","compatibleDevices":["MacBook","iPad","iPhone"],"usageScenarios":["办公学习"],"pros":["手感好","续航长","颜值高"],"cons":["价格高","键程短"]},{"model":"Apple 妙控鼠标","interfaceType":"蓝牙 / Lightning","compatibleDevices":["MacBook","iPad","iPhone"],"usageScenarios":["办公学习"],"pros":["颜值高","手势操作","续航长"],"cons":["手感一般","充电口设计反人类"]}]', 1, NULL, 1456, 87, 29),
(1, 3, '沉浸式音乐体验：播放器+耳机+解码耳放', '入坑HiFi一年，分享我的随身套装：索尼NW-A306播放器 + 森海塞尔IE600耳机 + 山灵UA5解码耳放线。虽然折腾但音质提升很明显。', 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600', '[{"model":"索尼 NW-A306 播放器","interfaceType":"Type-C / 3.5mm / 4.4mm","compatibleDevices":["安卓系统"],"usageScenarios":["通勤出行","影音欣赏"],"pros":["音质好","续航长","颜值高"],"cons":["价格高","存储空间小"]},{"model":"森海塞尔 IE600 耳机","interfaceType":"MMCX / 3.5mm","compatibleDevices":["播放器","手机"],"usageScenarios":["通勤出行","影音欣赏"],"pros":["解析力强","声场宽","做工精致"],"cons":["价格高","线材一般"]},{"model":"山灵 UA5 解码耳放线","interfaceType":"Type-C / Lightning / 3.5mm / 4.4mm","compatibleDevices":["手机","电脑","播放器"],"usageScenarios":["通勤出行","办公学习"],"pros":["推力大","音质提升明显","便携"],"cons":["发热","价格不低"]}]', 1, NULL, 987, 76, 23);
