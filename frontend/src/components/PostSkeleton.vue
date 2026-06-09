<template>
  <div class="post-skeleton card">
    <div class="post-header">
      <el-skeleton class="avatar-skeleton" :rows="0" animated>
        <template #default>
          <div class="skeleton-avatar"></div>
        </template>
      </el-skeleton>
      <div class="post-meta">
        <el-skeleton class="meta-line" :rows="1" animated />
        <el-skeleton class="meta-line small" :rows="1" animated />
      </div>
      <el-skeleton class="tag-skeleton" :rows="1" animated />
    </div>
    <el-skeleton class="title-skeleton" :rows="1" animated />
    <el-skeleton class="excerpt-skeleton" :rows="2" animated />
    <div class="skeleton-images" v-if="showImages">
      <div v-for="i in 3" :key="i" class="skeleton-image"></div>
    </div>
    <div class="post-footer">
      <el-skeleton class="footer-tag" :rows="1" animated />
      <div class="post-stats">
        <el-skeleton class="stat-item" :rows="1" animated />
        <el-skeleton class="stat-item" :rows="1" animated />
        <el-skeleton class="stat-item" :rows="1" animated />
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  showImages: {
    type: Boolean,
    default: true
  }
})
</script>

<style lang="scss" scoped>
.post-skeleton {
  margin-bottom: 16px;

  .post-header {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 12px;

    .avatar-skeleton {
      width: 40px;
      height: 40px;

      :deep(.el-skeleton__item) {
        border-radius: 50%;
      }
    }

    .skeleton-avatar {
      width: 40px;
      height: 40px;
    }

    .post-meta {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 6px;

      .meta-line {
        width: 100px;

        &.small {
          width: 60px;
        }
      }
    }

    .tag-skeleton {
      width: 60px;
    }
  }

  .title-skeleton {
    margin-bottom: 8px;
  }

  .excerpt-skeleton {
    margin-bottom: 12px;
  }

  .skeleton-images {
    display: flex;
    gap: 8px;
    margin-bottom: 12px;

    .skeleton-image {
      width: 160px;
      height: 120px;
      background: linear-gradient(90deg, #f2f2f2 25%, #e6e6e6 37%, #f2f2f2 63%);
      background-size: 400% 100%;
      animation: skeleton-loading 1.4s ease infinite;
      border-radius: 6px;
    }
  }

  .post-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 12px;
    border-top: 1px solid #f0f0f0;

    .footer-tag {
      width: 50px;
    }

    .post-stats {
      display: flex;
      gap: 20px;

      .stat-item {
        width: 40px;
      }
    }
  }
}

@keyframes skeleton-loading {
  0% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0 50%;
  }
}
</style>
