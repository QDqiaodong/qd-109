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
(1, 2, 'MacBook Pro桌面搭配分享：扩展坞+显示器+键鼠', '花了一个月终于把桌面搭配搞明白了！MacBook Pro M3配CalDigit TS4扩展坞，再接上戴尔U2723QE显示器，加上罗技MX Master 3S鼠标和HHKB键盘，生产力直接拉满。', 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=600', '[{"category":"main","role":"整套方案的核心计算设备，提供强大的M3芯片性能和macOS生态体验","model":"MacBook Pro 14寸 M3","interfaceType":"Thunderbolt 4","compatibleDevices":["MacOS"],"usageScenarios":["办公学习","商务会议"],"pros":["性能强劲","屏幕出色","续航持久"],"cons":["价格偏高","接口少"]},{"category":"core","role":"扩展主机接口，连接显示器、键鼠、网线等外设，提供98W供电","model":"CalDigit TS4 扩展坞","interfaceType":"Thunderbolt 4","compatibleDevices":["MacBook","Windows电脑"],"usageScenarios":["办公学习","居家使用"],"pros":["接口丰富","供电稳定","传输速度快"],"cons":["体积大","价格贵"]},{"category":"core","role":"4K 27寸专业显示器，提供精准色彩显示和大屏工作空间","model":"戴尔 U2723QE 显示器","interfaceType":"HDMI / DP / USB-C","compatibleDevices":["MacBook","Windows电脑"],"usageScenarios":["办公学习","设计创作"],"pros":["4K分辨率","色彩准确","Type-C供电"],"cons":["价格高","底座占空间"]},{"category":"peripheral","role":"人体工学鼠标，提升长时间办公的舒适度和操作效率","model":"罗技 MX Master 3S 鼠标","interfaceType":"蓝牙 / 优联 / Type-C","compatibleDevices":["MacBook","Windows电脑","iPad"],"usageScenarios":["办公学习","设计创作"],"pros":["手感舒适","静音按键","跨屏控制"],"cons":["重量大","价格高"]},{"category":"peripheral","role":"静电容键盘，提供极佳的打字手感和长时间使用舒适度","model":"HHKB Professional HYBRID","interfaceType":"蓝牙 / Type-C","compatibleDevices":["MacBook","Windows电脑","iPad"],"usageScenarios":["办公学习","编程开发"],"pros":["手感极佳","静音设计","做工精致"],"cons":["学习成本高","价格昂贵"]}]', 1, NULL, 2340, 156, 67),
(2, 2, 'Windows主机桌搭方案：主机+机械键盘+游戏鼠标+耳机', '新配了一台Windows游戏主机，搭配了整套外设：樱桃MX3.0S机械键盘、罗技G502无线鼠标、赛睿寒冰7耳机。游戏体验直接起飞！', 'https://images.unsplash.com/photo-1587202372775-e229f172b9d7?w=600', '[{"category":"main","role":"高性能游戏主机，提供流畅的3A游戏体验和高帧率输出","model":"联想拯救者 游戏主机 i7-14700K + RTX4070","interfaceType":"USB 3.2 / 蓝牙5.3 / HDMI 2.1","compatibleDevices":["Windows 11"],"usageScenarios":["游戏娱乐","办公学习","视频剪辑"],"pros":["性能强劲","散热优秀","扩展性强"],"cons":["体积大","满载噪音"]},{"category":"core","role":"红轴机械键盘，兼顾游戏操作和日常打字的手感平衡","model":"樱桃 MX3.0S 机械键盘 红轴","interfaceType":"Type-C 有线","compatibleDevices":["Windows电脑","MacBook"],"usageScenarios":["游戏娱乐","办公学习","编程开发"],"pros":["手感出色","做工精良","PBT键帽"],"cons":["价格偏高","无无线版"]},{"category":"core","role":"Lightspeed无线技术鼠标，精准定位+可编程按键，游戏利器","model":"罗技 G502 X PLUS 鼠标","interfaceType":"USB 有线 / Lightspeed 无线","compatibleDevices":["Windows电脑","MacBook"],"usageScenarios":["游戏娱乐","办公学习"],"pros":["手感舒适","定位精准","可编程按键多"],"cons":["重量大","价格高"]},{"category":"peripheral","role":"2.4G无线游戏耳机，7.1环绕声+低延迟，沉浸式游戏体验","model":"赛睿 寒冰7 无线耳机","interfaceType":"2.4G无线 / 3.5mm有线","compatibleDevices":["Windows电脑","PS5","Switch"],"usageScenarios":["游戏娱乐","影音欣赏"],"pros":["音质好","佩戴舒适","低延迟"],"cons":["续航一般","塑料感强"]},{"category":"peripheral","role":"RGB电竞鼠标垫，提供顺滑操控和桌面氛围灯光","model":"赛睿 QcK Prism Cloth XL","interfaceType":"USB","compatibleDevices":["全平台"],"usageScenarios":["游戏娱乐"],"pros":["面积大","RGB灯效","顺滑表面"],"cons":["易脏","占用空间"]}]', 1, NULL, 1890, 123, 45),
(3, 1, 'iPhone 15 Pro配件全家桶：耳机+手表+充电器', '入手iPhone 15 Pro后陆续买了一堆配件：AirPods Pro 2耳机、Apple Watch Ultra 2手表、Anker 100W充电器。整套用下来体验真的无缝。', 'https://images.unsplash.com/photo-1592750475338-74b7b21085ab?w=600', '[{"category":"main","role":"日常使用的核心设备，通信、拍照、娱乐一体化的智能手机","model":"iPhone 15 Pro 256GB","interfaceType":"USB-C","compatibleDevices":["iOS 17","Apple生态"],"usageScenarios":["通勤出行","办公学习","摄影创作"],"pros":["A17 Pro芯片强","钛金属边框","4800万像素拍照"],"cons":["价格贵","续航一般","发热"]},{"category":"core","role":"主动降噪蓝牙耳机，通勤、办公场景提供沉浸音乐和清晰通话","model":"AirPods Pro 2 (USB-C版)","interfaceType":"蓝牙5.3 / USB-C充电","compatibleDevices":["iPhone","iPad","MacBook","Apple Watch"],"usageScenarios":["通勤出行","办公学习","运动健身"],"pros":["降噪效果好","续航持久","佩戴舒适","空间音频"],"cons":["价格偏高","易丢失"]},{"category":"core","role":"运动健康监测+通知助手，无缝接力iPhone的可穿戴设备","model":"Apple Watch Ultra 2","interfaceType":"蓝牙","compatibleDevices":["iPhone"],"usageScenarios":["运动健身","户外旅行","商务会议","日常通勤"],"pros":["续航长(36h)","精准双频GPS","坚固耐用","健康监测"],"cons":["价格贵","重量大(61g)"]},{"category":"peripheral","role":"四口氮化镓充电器，同时为iPhone、手表、耳机、MacBook供电","model":"Anker 100W GaN 氮化镓充电器","interfaceType":"USB-C x4 (100W MAX)","compatibleDevices":["iPhone","MacBook","iPad","Switch","AirPods"],"usageScenarios":["通勤出行","办公学习","出差旅行"],"pros":["功率大(100W)","体积小氮化镓","多口同时充"],"cons":["满载发热","价格偏高"]}]', 1, NULL, 3120, 201, 89),
(1, 6, 'PS5玩家必备配件：手柄+耳机+充电底座', 'PS5玩家的三件套：DualSense手柄、Pulse 3D耳机、官方充电底座。有了这三样，游戏体验直接上一个档次。', 'https://images.unsplash.com/photo-1606144042614-b2417e99c4e3?w=600', '[{"category":"main","role":"次世代游戏主机，提供光追特效、4K120帧和大量独占大作","model":"PS5 光驱版","interfaceType":"HDMI 2.1 / USB 3.2 / Wi-Fi 6","compatibleDevices":["PS5游戏","蓝光光盘"],"usageScenarios":["游戏娱乐","影音欣赏"],"pros":["性能强","独占多","DualSense手柄体验好"],"cons":["体积大","价格高","光驱噪音"]},{"category":"core","role":"原配无线手柄，自适应扳机+触觉反馈，游戏沉浸感拉满","model":"DualSense 无线手柄","interfaceType":"蓝牙 / Type-C充电","compatibleDevices":["PS5","PC","手机"],"usageScenarios":["游戏娱乐"],"pros":["手感好","自适应扳机","震动细腻","内置麦克风"],"cons":["漂移问题","价格高","续航约12h"]},{"category":"core","role":"PS5官方3D音频耳机，Tempest 3D音效精准定位敌人方位","model":"Pulse 3D 无线耳机","interfaceType":"2.4G无线 / 3.5mm有线","compatibleDevices":["PS5","PC","Mac"],"usageScenarios":["游戏娱乐","影音欣赏"],"pros":["3D音效出色","佩戴舒适","续航约12h"],"cons":["音质一般","塑料感强"]},{"category":"peripheral","role":"双手柄同时充电底座，整洁桌面+随时满电的备用手柄","model":"PS5 官方 DualSense 充电底座","interfaceType":"磁吸充电","compatibleDevices":["DualSense手柄"],"usageScenarios":["居家使用"],"pros":["同时充两个","原装不漂移","整洁桌面"],"cons":["价格偏高","仅原装手柄"]}]', 1, NULL, 1567, 98, 34),
(2, 5, '索尼A7M4创作者套装：镜头+稳定器+麦克风', '做视频创作一年了，分享一下我的索尼A7M4套装：适马24-70 F2.8镜头、智云Weebill 3稳定器、罗德Wireless GO II麦克风。这套组合拍视频真的香！', 'https://images.unsplash.com/photo-1502920917128-1aa500764cbd?w=600', '[{"category":"main","role":"全画幅微单主力机身，3300万像素拍照+4K60p视频全能创作","model":"索尼 A7M4 全画幅微单","interfaceType":"E卡口 / USB-C / HDMI / 3.5mm","compatibleDevices":["索尼E卡口全画幅镜头"],"usageScenarios":["摄影创作","视频制作","直播","旅行记录"],"pros":["画质好","对焦快","视频功能强","色彩科学优秀"],"cons":["价格高","单卡槽UHS-II","续航一般"]},{"category":"core","role":"标准变焦大三元镜头，F2.8恒定光圈覆盖全焦段创作需求","model":"适马 24-70mm F2.8 DG DN ART","interfaceType":"索尼E卡口","compatibleDevices":["索尼全画幅微单"],"usageScenarios":["摄影创作","视频制作","人像","风景"],"pros":["画质锐不可当","光圈大F2.8","对焦安静快速"],"cons":["重量大(830g)","价格高"]},{"category":"core","role":"专业视频稳定器，消除手抖，拍出电影级运镜和移动画面","model":"智云 Weebill 3 稳定器","interfaceType":"Type-C","compatibleDevices":["微单相机","无反相机"],"usageScenarios":["视频制作","户外旅行","活动记录","Vlog"],"pros":["防抖效果顶级","机身轻","自带补光灯+麦克风"],"cons":["学习成本高","调平繁琐"]},{"category":"peripheral","role":"双通道无线领夹麦克风，户外人声清晰收录，视频必备","model":"罗德 Wireless GO II 麦克风","interfaceType":"3.5mm / USB-C","compatibleDevices":["相机","手机","电脑","平板"],"usageScenarios":["视频制作","直播","Vlog","采访"],"pros":["音质清晰","双通道","传输稳定","续航约7h"],"cons":["价格高","防风一般需配毛衣"]}]', 1, NULL, 2100, 145, 56),
(3, 2, '极简桌面：笔记本+蓝牙键鼠', '分享我的极简桌面搭配：MacBook Air M2 + 妙控键盘 + 妙控鼠标。虽然简单但效率很高，颜值也在线。', 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=600', '[{"category":"main","role":"轻薄本机身+M2芯片，日常办公毫无压力，静音无风扇","model":"MacBook Air M2 13寸","interfaceType":"USB-C x2 / MagSafe 3 / 3.5mm","compatibleDevices":["macOS"],"usageScenarios":["办公学习","居家使用","出差旅行"],"pros":["轻薄便携(1.24kg)","续航超长18h","静音无风扇"],"cons":["接口少","性能不如Pro","刘海屏"]},{"category":"core","role":"剪刀脚结构蓝牙键盘，短键程快速打字，和Mac完美适配","model":"Apple 妙控键盘 (2022)","interfaceType":"蓝牙5.0 / Lightning充电","compatibleDevices":["MacBook","iPad","iPhone"],"usageScenarios":["办公学习","居家使用"],"pros":["手感舒服","续航超长","Touch ID","体积小颜值高"],"cons":["价格高","键程短","无方向键全尺寸"]},{"category":"peripheral","role":"多点触控蓝牙鼠标，手势操作流畅，配合Mac效率翻倍","model":"Apple 妙控鼠标 2 (白色)","interfaceType":"蓝牙 / Lightning充电","compatibleDevices":["MacBook","iPad","iPhone"],"usageScenarios":["办公学习","居家使用"],"pros":["颜值高","手势操作多","续航长","静音点击"],"cons":["手感反人类","充电口在底部"]}]', 1, NULL, 1456, 87, 29),
(1, 3, '沉浸式音乐体验：播放器+耳机+解码耳放', '入坑HiFi一年，分享我的随身套装：索尼NW-A306播放器 + 森海塞尔IE600耳机 + 山灵UA5解码耳放线。虽然折腾但音质提升很明显。', 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600', '[{"category":"main","role":"安卓系统高清音乐播放器，独立DAC芯片，音质比手机强很多","model":"索尼 NW-A306 无损播放器","interfaceType":"Type-C / 3.5mm单端 / 4.4mm平衡","compatibleDevices":["Android 12","各类音乐APP"],"usageScenarios":["通勤出行","影音欣赏","休闲聆听"],"pros":["音质出色","续航约30h","安卓系统灵活","颜值高"],"cons":["价格高","存储空间32GB","体积略大"]},{"category":"core","role":"旗舰级动圈入耳耳机，高解析力+宽大声场，还原音乐细节","model":"森海塞尔 IE600 HiFi耳机","interfaceType":"MMCX可换线 / 3.5mm","compatibleDevices":["播放器","手机","电脑","解码耳放"],"usageScenarios":["通勤出行","影音欣赏","发烧聆听"],"pros":["解析力顶级","声场宽阔","做工精致","人体工学佩戴"],"cons":["价格高(5000+)","线材一般"]},{"category":"peripheral","role":"手机/电脑用解码耳放线，小体积大推力，直推大阻抗耳机","model":"山灵 UA5 解码耳放线","interfaceType":"Type-C / Lightning / 3.5mm / 4.4mm","compatibleDevices":["手机","电脑","平板","播放器"],"usageScenarios":["通勤出行","办公学习","影音欣赏"],"pros":["推力大(单端125mW)","双DAC芯片","便携即插即用"],"cons":["使用发热","价格不低(1500+)"]}]', 1, NULL, 987, 76, 23);
