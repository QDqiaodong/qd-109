package com.digital.community.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

@Component
public class EncodingRepairRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(EncodingRepairRunner.class);
    private static final Charset WINDOWS_1252 = Charset.forName("windows-1252");

    private static final List<RepairTarget> TARGETS = List.of(
            new RepairTarget("t_category", "name"),
            new RepairTarget("t_category", "icon"),
            new RepairTarget("t_user", "nickname"),
            new RepairTarget("t_post", "title"),
            new RepairTarget("t_post", "content"),
            new RepairTarget("t_comment", "content")
    );

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void run(ApplicationArguments args) {
        redisTemplate.delete(List.of("post:latest", "post:hot"));

        int repairedRows = 0;
        for (RepairTarget target : TARGETS) {
            repairedRows += repairColumn(target);
        }
        repairedRows += repairSeedData();
        if (repairedRows > 0) {
            log.info("Repaired {} mojibake text value(s) in MySQL.", repairedRows);
        }
    }

    private int repairColumn(RepairTarget target) {
        String selectSql = "SELECT id, " + target.column() + " AS value FROM " + target.table() + " WHERE " + target.column() + " IS NOT NULL";
        String updateSql = "UPDATE " + target.table() + " SET " + target.column() + " = ? WHERE id = ?";

        int repaired = 0;
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(selectSql);
        for (Map<String, Object> row : rows) {
            Object value = row.get("value");
            if (!(value instanceof String text)) {
                continue;
            }

            String repairedText = repairIfNeeded(text);
            if (repairedText.equals(text)) {
                continue;
            }

            jdbcTemplate.update(updateSql, repairedText, row.get("id"));
            repaired++;
        }
        return repaired;
    }

    private String repairIfNeeded(String text) {
        if (!looksLikeMojibake(text)) {
            return text;
        }

        String repaired = new String(text.getBytes(WINDOWS_1252), java.nio.charset.StandardCharsets.UTF_8);
        return readabilityScore(repaired) > readabilityScore(text) ? repaired : text;
    }

    private int repairSeedData() {
        int repaired = 0;

        repaired += repairSeed("t_category", 1L, Map.of("name", "手机配件", "icon", "📱"));
        repaired += repairSeed("t_category", 2L, Map.of("name", "电脑配件", "icon", "💻"));
        repaired += repairSeed("t_category", 3L, Map.of("name", "影音设备", "icon", "🎧"));
        repaired += repairSeed("t_category", 4L, Map.of("name", "智能穿戴", "icon", "⌚"));
        repaired += repairSeed("t_category", 5L, Map.of("name", "摄影器材", "icon", "📷"));
        repaired += repairSeed("t_category", 6L, Map.of("name", "游戏外设", "icon", "🎮"));
        repaired += repairSeed("t_category", 7L, Map.of("name", "网络设备", "icon", "📶"));
        repaired += repairSeed("t_category", 8L, Map.of("name", "存储设备", "icon", "💾"));

        repaired += repairSeed("t_user", 1L, Map.of("nickname", "数码达人"));
        repaired += repairSeed("t_user", 2L, Map.of("nickname", "配件玩家"));
        repaired += repairSeed("t_user", 3L, Map.of("nickname", "科技爱好者"));

        repaired += repairSeed("t_post", 1L, Map.of(
                "title", "AirPods Pro 2 使用三个月真实体验",
                "content", "用了三个月的AirPods Pro 2，降噪效果真的没得说，通勤地铁上基本听不到外界噪音。续航也很给力，单次使用6小时没问题，搭配充电盒能用30小时。唯一缺点就是价格有点贵，但确实一分钱一分货。"
        ));
        repaired += repairSeed("t_post", 2L, Map.of(
                "title", "机械键盘选购指南：樱桃轴 vs 佳达隆轴",
                "content", "最近换了两把机械键盘，一把樱桃红轴，一把佳达隆黄轴。分享一下使用感受：樱桃红轴手感更清脆，打字声音大一点；佳达隆黄轴更顺滑，声音小适合办公室。预算够选樱桃，性价比选佳达隆。"
        ));
        repaired += repairSeed("t_post", 3L, Map.of(
                "title", "求助！蓝牙耳机连接电脑有杂音怎么办",
                "content", "刚买的蓝牙耳机连接手机没问题，但连接Windows电脑就有滋滋的杂音，试过重新配对、更新驱动都没用，有大佬遇到过类似问题吗？求解决方案！"
        ));
        repaired += repairSeed("t_post", 4L, Map.of(
                "title", "Apple Watch Ultra 2 表带搭配分享",
                "content", "入手Ultra 2一个月，换了三条表带：原装高山回环最适合运动；米兰尼斯表带日常通勤很有质感；皮革表带正式场合必备。推荐大家根据不同场景换表带，体验感翻倍。"
        ));
        repaired += repairSeed("t_post", 5L, Map.of(
                "title", "PS5手柄摇杆漂移了怎么办？",
                "content", "PS5用了一年多，左摇杆开始漂移了，玩游戏的时候人物自己会走。有没有自己维修过的朋友？需要买什么工具？还是直接送修比较好？"
        ));

        return repaired;
    }

    private int repairSeed(String table, Long id, Map<String, String> expectedValues) {
        Map<String, Object> row = jdbcTemplate.queryForMap("SELECT * FROM " + table + " WHERE id = ?", id);
        int repaired = 0;
        for (Map.Entry<String, String> entry : expectedValues.entrySet()) {
            Object value = row.get(entry.getKey());
            if (!(value instanceof String text)) {
                continue;
            }
            if (text.equals(entry.getValue()) || !looksBroken(text)) {
                continue;
            }
            jdbcTemplate.update("UPDATE " + table + " SET " + entry.getKey() + " = ? WHERE id = ?", entry.getValue(), id);
            repaired++;
        }
        return repaired;
    }

    private boolean looksLikeMojibake(String text) {
        int markers = 0;
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if ("ÃÂâðæçåé�".indexOf(ch) >= 0) {
                markers++;
            }
        }
        return markers >= 2 || text.contains("ðŸ") || text.contains("âŒ");
    }

    private boolean looksBroken(String text) {
        return looksLikeMojibake(text) || text.contains("?") || text.contains("�");
    }

    private int readabilityScore(String text) {
        int score = 0;
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (isChinese(ch)) {
                score += 3;
            } else if (isLikelyEmoji(ch)) {
                score += 2;
            } else if (Character.isLetterOrDigit(ch) || Character.isWhitespace(ch) || isCommonPunctuation(ch)) {
                score += 1;
            }

            if ("ÃÂâðæçåé�".indexOf(ch) >= 0) {
                score -= 2;
            }
        }
        return score;
    }

    private boolean isChinese(char ch) {
        return ch >= 0x4E00 && ch <= 0x9FFF;
    }

    private boolean isLikelyEmoji(char ch) {
        return ch >= 0x2600 && ch <= 0x27BF;
    }

    private boolean isCommonPunctuation(char ch) {
        return ".,!?;:'\"()[]{}<>-_/@#%&*+=，。！？、：；“”‘’（）".indexOf(ch) >= 0;
    }

    private record RepairTarget(String table, String column) {
    }
}
