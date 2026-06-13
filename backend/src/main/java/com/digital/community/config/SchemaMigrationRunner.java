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
    private static final String FAULT_INFO_COLUMN = "fault_info";
    private static final String REQUIRED_FIELDS_COLUMN = "required_fields";
    private static final String COMMENT_ROOT_ID_COLUMN = "root_id";
    private static final String COMMENT_DEPTH_COLUMN = "depth";
    private static final String COMMENT_IS_COLLAPSED_COLUMN = "is_collapsed";

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

    private static final List<RequiredFieldsSeed> REQUIRED_FIELDS_SEEDS = List.of(
            new RequiredFieldsSeed(1L, "[\"deviceModel\",\"connectionType\",\"symptoms\"]"),
            new RequiredFieldsSeed(2L, "[\"deviceModel\",\"connectionType\",\"symptoms\"]"),
            new RequiredFieldsSeed(3L, "[\"deviceModel\",\"connectionType\",\"symptoms\"]"),
            new RequiredFieldsSeed(4L, "[\"deviceModel\",\"connectionType\",\"symptoms\"]"),
            new RequiredFieldsSeed(5L, "[\"deviceModel\",\"symptoms\"]"),
            new RequiredFieldsSeed(6L, "[\"deviceModel\",\"connectionType\",\"symptoms\"]"),
            new RequiredFieldsSeed(7L, "[\"deviceModel\",\"connectionType\",\"symptoms\"]"),
            new RequiredFieldsSeed(8L, "[\"deviceModel\",\"platform\",\"environment\",\"symptoms\"]")
    );

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        ensureAccessoryCardsColumn();
        syncSeedCards();
        ensureFaultInfoColumn();
        ensureRequiredFieldsColumn();
        syncRequiredFieldsSeed();
        ensureCommentRootIdColumn();
        ensureCommentDepthColumn();
        ensureCommentIsCollapsedColumn();
        backfillCommentDepthAndRootId();
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

    private void ensureFaultInfoColumn() {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 't_post'
                  AND COLUMN_NAME = ?
                """,
                Integer.class,
                FAULT_INFO_COLUMN
        );

        if (count != null && count > 0) {
            return;
        }

        jdbcTemplate.execute(
                "ALTER TABLE t_post ADD COLUMN fault_info TEXT NULL COMMENT '求助帖故障信息JSON' AFTER accessory_cards"
        );
        log.info("Added missing column t_post.{} for fault info support.", FAULT_INFO_COLUMN);
    }

    private void ensureRequiredFieldsColumn() {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 't_category'
                  AND COLUMN_NAME = ?
                """,
                Integer.class,
                REQUIRED_FIELDS_COLUMN
        );

        if (count != null && count > 0) {
            return;
        }

        jdbcTemplate.execute(
                "ALTER TABLE t_category ADD COLUMN required_fields VARCHAR(500) NULL COMMENT '求助帖必填字段JSON数组' AFTER icon"
        );
        log.info("Added missing column t_category.{} for help post validation support.", REQUIRED_FIELDS_COLUMN);
    }

    private void syncRequiredFieldsSeed() {
        for (RequiredFieldsSeed seed : REQUIRED_FIELDS_SEEDS) {
            Integer updated = jdbcTemplate.update(
                    """
                    UPDATE t_category
                    SET required_fields = ?
                    WHERE id = ?
                      AND (required_fields IS NULL OR required_fields = '')
                    """,
                    seed.payload(),
                    seed.categoryId()
            );

            if (updated != null && updated > 0) {
                log.info("Backfilled required_fields seed data for category {}.", seed.categoryId());
            }
        }
    }

    private record SeedCard(Long postId, String payload) {
    }

    private record RequiredFieldsSeed(Long categoryId, String payload) {
    }

    private void ensureCommentRootIdColumn() {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 't_comment'
                  AND COLUMN_NAME = ?
                """,
                Integer.class,
                COMMENT_ROOT_ID_COLUMN
        );

        if (count != null && count > 0) {
            return;
        }

        jdbcTemplate.execute(
                "ALTER TABLE t_comment ADD COLUMN root_id BIGINT NULL COMMENT '根评论ID（主评论）' AFTER parent_id, ADD INDEX idx_root_id (root_id)"
        );
        log.info("Added missing column t_comment.{} for comment depth protection.", COMMENT_ROOT_ID_COLUMN);
    }

    private void ensureCommentDepthColumn() {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 't_comment'
                  AND COLUMN_NAME = ?
                """,
                Integer.class,
                COMMENT_DEPTH_COLUMN
        );

        if (count != null && count > 0) {
            return;
        }

        jdbcTemplate.execute(
                "ALTER TABLE t_comment ADD COLUMN depth INT DEFAULT 1 COMMENT '评论深度，1为主评论，2为直接回复，以此类推' AFTER content"
        );
        log.info("Added missing column t_comment.{} for comment depth protection.", COMMENT_DEPTH_COLUMN);
    }

    private void ensureCommentIsCollapsedColumn() {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 't_comment'
                  AND COLUMN_NAME = ?
                """,
                Integer.class,
                COMMENT_IS_COLLAPSED_COLUMN
        );

        if (count != null && count > 0) {
            return;
        }

        jdbcTemplate.execute(
                "ALTER TABLE t_comment ADD COLUMN is_collapsed TINYINT DEFAULT 0 COMMENT '是否因深度超限被自动收束 0:否 1:是' AFTER depth"
        );
        log.info("Added missing column t_comment.{} for comment depth protection.", COMMENT_IS_COLLAPSED_COLUMN);
    }

    private void backfillCommentDepthAndRootId() {
        Integer nullCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM t_comment WHERE depth IS NULL OR root_id IS NULL",
                Integer.class
        );

        if (nullCount == null || nullCount == 0) {
            return;
        }

        jdbcTemplate.execute(
                """
                UPDATE t_comment c1
                SET c1.depth = 1, c1.root_id = c1.id
                WHERE c1.parent_id IS NULL
                  AND (c1.depth IS NULL OR c1.root_id IS NULL)
                """
        );

        int updated;
        int maxIterations = 10;
        do {
            updated = jdbcTemplate.update(
                    """
                    UPDATE t_comment child
                    INNER JOIN t_comment parent ON child.parent_id = parent.id
                    SET child.depth = parent.depth + 1,
                        child.root_id = COALESCE(parent.root_id, parent.id)
                    WHERE child.parent_id IS NOT NULL
                      AND (child.depth IS NULL OR child.root_id IS NULL)
                    """
            );
            maxIterations--;
        } while (updated > 0 && maxIterations > 0);

        jdbcTemplate.execute(
                "UPDATE t_comment SET depth = 1 WHERE depth IS NULL OR depth < 1"
        );
        jdbcTemplate.execute(
                "UPDATE t_comment SET root_id = id WHERE root_id IS NULL"
        );

        log.info("Backfilled depth and root_id for {} existing comments.", nullCount);
    }
}
