<template>
  <div class="infinite-load-more">
    <div v-if="loading" class="loading-state">
      <el-icon class="loading-icon"><Loading /></el-icon>
      <span>加载中...</span>
    </div>
    <div v-else-if="error" class="error-state" @click="$emit('retry')">
      <span class="error-text">加载失败，点击重试</span>
    </div>
    <div v-else-if="finished" class="finished-state">
      <span class="finished-text">没有更多了</span>
    </div>
  </div>
</template>

<script setup>
import { Loading } from '@element-plus/icons-vue'

defineProps({
  loading: {
    type: Boolean,
    default: false
  },
  error: {
    type: null,
    default: null
  },
  finished: {
    type: Boolean,
    default: false
  }
})

defineEmits(['retry'])
</script>

<style lang="scss" scoped>
.infinite-load-more {
  padding: 20px 0;
  text-align: center;
  font-size: 14px;
  color: #999;

  .loading-state {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;

    .loading-icon {
      animation: rotate 1s linear infinite;
      font-size: 16px;
      color: #1890ff;
    }
  }

  .error-state {
    cursor: pointer;
    color: #ff7a45;

    &:hover {
      color: #ff9c6e;
    }
  }

  .finished-state {
    .finished-text {
      position: relative;
      padding: 0 20px;

      &::before,
      &::after {
        content: '';
        position: absolute;
        top: 50%;
        width: 60px;
        height: 1px;
        background: #f0f0f0;
      }

      &::before {
        left: 0;
      }

      &::after {
        right: 0;
      }
    }
  }
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
