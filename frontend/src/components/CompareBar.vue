<template>
  <transition name="slide-up">
    <div class="compare-bar" v-show="!compareStore.isEmpty">
      <div class="bar-content">
        <div class="bar-left">
          <span class="bar-icon">⚖️</span>
          <span class="bar-label">对照清单</span>
          <span class="bar-count">{{ compareStore.count }} / {{ compareStore.MAX_COMPARE_ITEMS }}</span>
        </div>

        <div class="bar-posts">
          <div
            v-for="post in compareStore.compareList"
            :key="post.id"
            class="bar-post-item"
            :title="post.title"
          >
            <img :src="post.avatar" alt="" class="bar-post-avatar" />
            <span class="bar-post-title">{{ post.title }}</span>
            <button
              class="bar-post-remove"
              @click.stop="handleRemove(post.id)"
              title="移出"
            >
              ✕
            </button>
          </div>
        </div>

        <div class="bar-actions">
          <el-button
            size="small"
            plain
            @click="handleClear"
          >
            清空
          </el-button>
          <el-button
            type="primary"
            size="small"
            @click="goToCompare"
          >
            开始对照
          </el-button>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useCompareStore } from '@/store/compare'
import { ElMessageBox, ElMessage } from 'element-plus'

const router = useRouter()
const compareStore = useCompareStore()

const handleRemove = (postId) => {
  compareStore.removeFromCompare(postId)
  ElMessage.info('已移出对照清单')
}

const handleClear = () => {
  ElMessageBox.confirm(
    '确定要清空对照清单吗？',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    compareStore.clearCompare()
    ElMessage.success('已清空对照清单')
  }).catch(() => {})
}

const goToCompare = () => {
  router.push('/compare')
}
</script>

<style lang="scss" scoped>
.compare-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background: #fff;
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.1);
  border-top: 1px solid #f0f0f0;

  .bar-content {
    max-width: 1200px;
    margin: 0 auto;
    padding: 12px 24px;
    display: flex;
    align-items: center;
    gap: 20px;
  }

  .bar-left {
    display: flex;
    align-items: center;
    gap: 8px;
    flex-shrink: 0;

    .bar-icon {
      font-size: 20px;
    }

    .bar-label {
      font-size: 14px;
      font-weight: 600;
      color: #333;
    }

    .bar-count {
      font-size: 12px;
      color: #999;
      background: #f5f7fa;
      padding: 2px 8px;
      border-radius: 10px;
    }
  }

  .bar-posts {
    flex: 1;
    display: flex;
    align-items: center;
    gap: 10px;
    overflow-x: auto;
    padding: 4px 0;

    &::-webkit-scrollbar {
      height: 4px;
    }

    &::-webkit-scrollbar-thumb {
      background: #ddd;
      border-radius: 2px;
    }

    .bar-post-item {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 6px 10px;
      background: #f5f7fa;
      border-radius: 20px;
      flex-shrink: 0;
      max-width: 200px;
      transition: all 0.2s;

      &:hover {
        background: #e6f7ff;
      }

      .bar-post-avatar {
        width: 24px;
        height: 24px;
        border-radius: 50%;
        object-fit: cover;
        flex-shrink: 0;
      }

      .bar-post-title {
        font-size: 12px;
        color: #333;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        flex: 1;
        min-width: 0;
      }

      .bar-post-remove {
        width: 18px;
        height: 18px;
        display: flex;
        align-items: center;
        justify-content: center;
        border: none;
        background: transparent;
        color: #999;
        font-size: 10px;
        cursor: pointer;
        border-radius: 50%;
        flex-shrink: 0;
        transition: all 0.2s;

        &:hover {
          background: #ff4d4f;
          color: #fff;
        }
      }
    }
  }

  .bar-actions {
    display: flex;
    gap: 8px;
    flex-shrink: 0;
  }
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s ease;
}

.slide-up-enter-from,
.slide-up-leave-to {
  transform: translateY(100%);
  opacity: 0;
}
</style>
