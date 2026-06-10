package com.digital.community.config;

import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(0)
public class SchemaMigrationRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SchemaMigrationRunner.class);

    private static final String ACCESSORY_CARDS_COLUMN = "accessory_cards";

    private static final List<SeedCard> SEED_CARDS = List.of(
            new SeedCard(
                    1L,
                    "[{\"model\":\"AirPods Pro 2\",\"interfaceType\":\"蓝牙5.3 / Lightning\",\"compatibleDevices\":[\"iPhone\",\"iPad\",\"MacBook\",\"Apple Watch\"],\"usageScenarios\":[\"通勤出行\",\"办公学习\",\"运动健身\"],\"pros\":[\"降噪效果好\",\"续航持久\",\"佩戴舒适\",\"音质出色\"],\"cons\":[\"价格偏高\",\"易丢失\"]}]"
            ),
            new SeedCard(
                    2L,
                    "[{\"model\":\"樱桃红轴\",\"interfaceType\":\"Type-C\",\"compatibleDevices\":[\"Windows电脑\",\"MacBook\"],\"usageScenarios\":[\"办公学习\",\"游戏娱乐\"],\"pros\":[\"手感清脆\",\"做工精良\"],\"cons\":[\"价格偏高\",\"声音较大\"]},{\"model\":\"佳达隆黄轴\",\"interfaceType\":\"Type-C\",\"compatibleDevices\":[\"Windows电脑\",\"MacBook\"],\"usageScenarios\":[\"办公学习\"],\"pros\":[\"性价比高\",\"声音小\",\"手感顺滑\"],\"cons\":[\"品牌知名度一般\"]}]"
            )
    );

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        ensureAccessoryCardsColumn();
        syncSeedCards();
    }

    private void ensureAccessoryCardsColumn() {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 't_post'
                  AND COLUMN_NAME = ?
                """,
                Integer.class,
                ACCESSORY_CARDS_COLUMN
        );

        if (count != null && count > 0) {
            return;
        }

        jdbcTemplate.execute(
                "ALTER TABLE t_post ADD COLUMN accessory_cards TEXT NULL COMMENT '配件参数卡片JSON数组' AFTER images"
        );
        log.info("Added missing column t_post.{} for accessory card support.", ACCESSORY_CARDS_COLUMN);
    }

    private void syncSeedCards() {
        for (SeedCard seedCard : SEED_CARDS) {
            Integer updated = jdbcTemplate.update(
                    """
                    UPDATE t_post
                    SET accessory_cards = ?
                    WHERE id = ?
                      AND (accessory_cards IS NULL OR accessory_cards = '')
                    """,
                    seedCard.payload(),
                    seedCard.postId()
            );

            if (updated != null && updated > 0) {
                log.info("Backfilled accessory card seed data for post {}.", seedCard.postId());
            }
        }
    }

    private record SeedCard(Long postId, String payload) {
    }
}
