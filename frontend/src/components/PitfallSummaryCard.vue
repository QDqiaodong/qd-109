<template>
  <div class="pitfall-summary-card" v-if="hasContent">
    <div class="pitfall-card-header" @click="expanded = !expanded">
      <div class="header-left">
        <span class="pitfall-icon">⚠️</span>
        <span class="pitfall-title">避坑摘要</span>
        <span class="pitfall-count">{{ totalCount }} 条提醒</span>
      </div>
      <el-icon class="expand-icon" :class="{ expanded: expanded }">
        <ArrowDown />
      </el-icon>
    </div>

    <div class="pitfall-card-body" v-show="expanded">
      <div class="pitfall-section" v-if="postPitfalls.length > 0">
        <div class="section-label">
          <span class="label-icon">✍️</span>
          <span class="label-text">作者提醒</span>
        </div>
        <div class="pitfall-list">
          <div
            v-for="(item, idx) in postPitfalls"
            :key="'post-' + idx"
            class="pitfall-item"
          >
            <span class="item-bullet">●</span>
            <span class="item-text">{{ item }}</span>
          </div>
        </div>
      </div>

      <div class="pitfall-section" v-if="commentReminders.length > 0">
        <div class="section-label">
          <span class="label-icon">💬</span>
          <span class="label-text">评论区热评提醒</span>
        </div>
        <div class="pitfall-list">
          <div
            v-for="(item, idx) in commentReminders"
            :key="'comment-' + idx"
            class="pitfall-item comment-item"
          >
            <span class="item-bullet">●</span>
            <div class="item-content">
              <span class="item-text">{{ item.text }}</span>
              <span class="item-source" v-if="item.nickname">—— @{{ item.nickname }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="pitfall-footer" v-if="showMore">
        <span class="footer-hint">滚动查看完整内容获取更多细节 →</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ArrowDown } from '@element-plus/icons-vue'

const props = defineProps({
  postPitfalls: {
    type: Array,
    default: () => []
  },
  commentReminders: {
    type: Array,
    default: () => []
  },
  maxVisible: {
    type: Number,
    default: 6
  }
})

const expanded = ref(true)

const totalCount = computed(() => {
  return props.postPitfalls.length + props.commentReminders.length
})

const hasContent = computed(() => totalCount.value > 0)

const showMore = computed(() => totalCount.value > props.maxVisible)
</script>

<style lang="scss" scoped>
.pitfall-summary-card {
  margin-bottom: 20px;
  border: 1px solid #ffd591;
  border-radius: 12px;
  background: linear-gradient(135deg, #fffbe6 0%, #fff7e6 100%);
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(250, 140, 22, 0.08);
  transition: box-shadow 0.3s ease;

  &:hover {
    box-shadow: 0 4px 16px rgba(250, 140, 22, 0.15);
  }
}

.pitfall-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  background: linear-gradient(135deg, #fa8c16 0%, #faad14 100%);
  color: white;
  cursor: pointer;
  user-select: none;
  transition: background 0.3s ease;

  &:hover {
    background: linear-gradient(135deg, #ffa940 0%, #ffc53d 100%);
  }

  .header-left {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  .pitfall-icon {
    font-size: 18px;
  }

  .pitfall-title {
    font-size: 15px;
    font-weight: 600;
  }

  .pitfall-count {
    font-size: 12px;
    background: rgba(255, 255, 255, 0.25);
    padding: 2px 10px;
    border-radius: 10px;
    font-weight: 500;
  }

  .expand-icon {
    font-size: 16px;
    transition: transform 0.3s ease;

    &.expanded {
      transform: rotate(180deg);
    }
  }
}

.pitfall-card-body {
  padding: 16px 20px;
}

.pitfall-section {
  & + .pitfall-section {
    margin-top: 16px;
    padding-top: 16px;
    border-top: 1px dashed #ffd591;
  }
}

.section-label {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;

  .label-icon {
    font-size: 14px;
  }

  .label-text {
    font-size: 13px;
    font-weight: 600;
    color: #d46b08;
  }
}

.pitfall-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.pitfall-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 8px 12px;
  background: #fff;
  border-radius: 8px;
  border-left: 3px solid #faad14;
  transition: all 0.2s ease;

  &:hover {
    background: #fff7e6;
    transform: translateX(2px);
  }

  .item-bullet {
    flex-shrink: 0;
    color: #fa8c16;
    font-size: 8px;
    margin-top: 7px;
  }

  .item-text {
    flex: 1;
    font-size: 13px;
    line-height: 1.6;
    color: #333;
  }

  .item-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  .item-source {
    font-size: 11px;
    color: #999;
    font-style: italic;
    align-self: flex-end;
  }

  &.comment-item {
    border-left-color: #1890ff;
    background: linear-gradient(135deg, #fff 0%, #f0f7ff 100%);

    .item-bullet {
      color: #1890ff;
    }

    &:hover {
      background: #e6f7ff;
    }
  }
}

.pitfall-footer {
  margin-top: 14px;
  text-align: center;

  .footer-hint {
    font-size: 12px;
    color: #faad14;
    cursor: pointer;
    transition: color 0.2s;

    &:hover {
      color: #fa8c16;
    }
  }
}
</style>
