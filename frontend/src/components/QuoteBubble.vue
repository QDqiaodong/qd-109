<template>
  <div class="quote-bubble" :class="{ 'clickable': clickable }" @click="handleClick">
    <div class="quote-header">
      <span class="quote-floor">#{{ floor }}</span>
      <span class="quote-user">@{{ nickname }}</span>
    </div>
    <div class="quote-content">
      {{ displayContent }}
    </div>
    <div v-if="showCancel" class="quote-cancel" @click.stop="$emit('cancel')">
      <el-icon><Close /></el-icon>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Close } from '@element-plus/icons-vue'

const props = defineProps({
  floor: {
    type: [Number, String],
    default: ''
  },
  nickname: {
    type: String,
    default: ''
  },
  content: {
    type: String,
    default: ''
  },
  maxLength: {
    type: Number,
    default: 80
  },
  clickable: {
    type: Boolean,
    default: false
  },
  showCancel: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['click', 'cancel'])

const displayContent = computed(() => {
  if (!props.content) return ''
  const cleaned = props.content.replace(/\n/g, ' ').trim()
  if (cleaned.length <= props.maxLength) return cleaned
  return cleaned.slice(0, props.maxLength) + '...'
})

const handleClick = () => {
  if (props.clickable) {
    emit('click')
  }
}
</script>

<style lang="scss" scoped>
.quote-bubble {
  position: relative;
  padding: 10px 14px;
  background: linear-gradient(135deg, #f6f8fb 0%, #eef2f7 100%);
  border-left: 3px solid #1890ff;
  border-radius: 6px;
  margin-bottom: 10px;
  transition: all 0.2s ease;

  &.clickable {
    cursor: pointer;

    &:hover {
      background: linear-gradient(135deg, #e6f7ff 0%, #d9ecff 100%);
      border-left-color: #40a9ff;
    }
  }

  .quote-header {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 4px;

    .quote-floor {
      font-size: 11px;
      font-weight: 600;
      color: #1890ff;
      background: #e6f7ff;
      padding: 1px 8px;
      border-radius: 10px;
    }

    .quote-user {
      font-size: 12px;
      font-weight: 500;
      color: #1890ff;
    }
  }

  .quote-content {
    font-size: 12px;
    line-height: 1.5;
    color: #606266;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
  }

  .quote-cancel {
    position: absolute;
    top: 8px;
    right: 10px;
    color: #909399;
    font-size: 14px;
    cursor: pointer;
    transition: color 0.2s;

    &:hover {
      color: #f56c6c;
    }
  }
}
</style>
