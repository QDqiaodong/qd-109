<template>
  <div class="collocation-rank container">
    <div class="page-header card">
      <div class="header-left">
        <h1 class="page-title">🔗 主流搭配方案排行</h1>
        <p class="page-desc">
          基于社区体验帖数据分析，汇总高频出现的配件组合方案，助您发现热门搭配参考
        </p>
      </div>
      <div class="header-right">
        <el-select
          v-model="filterItemCount"
          placeholder="搭配件数"
          style="width: 140px; margin-right: 12px"
          @change="loadData"
        >
          <el-option label="全部件数" :value="null" />
          <el-option label="2件搭配" :value="2" />
          <el-option label="3件搭配" :value="3" />
          <el-option label="4件搭配" :value="4" />
          <el-option label="5件及以上" :value="5" />
        </el-select>
        <el-select
          v-model="filterCategoryId"
          placeholder="按分类筛选"
          clearable
          style="width: 180px"
          @change="loadData"
        >
          <el-option
            v-for="cat in categories"
            :key="cat.id"
            :label="cat.icon + ' ' + cat.name"
            :value="cat.id"
          />
        </el-select>
      </div>
    </div>

    <div class="stats-overview card" v-if="overviewStats">
      <div class="stat-item">
        <div class="stat-num">{{ overviewStats.totalSchemes }}</div>
        <div class="stat-label">搭配方案数</div>
      </div>
      <div class="stat-divider"></div>
      <div class="stat-item">
        <div class="stat-num">{{ overviewStats.totalPosts }}</div>
        <div class="stat-label">体验帖总数</div>
      </div>
      <div class="stat-divider"></div>
      <div class="stat-item">
        <div class="stat-num">{{ overviewStats.totalViews }}</div>
        <div class="stat-label">总浏览量</div>
      </div>
      <div class="stat-divider"></div>
      <div class="stat-item highlight">
        <div class="stat-num">{{ overviewStats.topSchemeName }}</div>
        <div class="stat-label">最热门搭配</div>
      </div>
    </div>

    <div class="scheme-list" v-loading="loading">
      <el-empty
        v-if="!loading && schemeList.length === 0"
        description="暂无搭配数据，当用户发布含配件卡片的体验帖后这里会自动聚合统计"
        :image-size="100"
      />

      <div
        class="scheme-card card"
        v-for="(scheme, idx) in schemeList"
        :key="scheme.schemeKey"
      >
        <div class="scheme-header">
          <div class="rank-badge" :class="'rank-' + (idx + 1)">
            {{ idx + 1 }}
          </div>
          <div class="scheme-main">
            <div class="scheme-models-row">
              <div class="models-chain">
                <template v-for="(model, mIdx) in scheme.accessoryModels" :key="mIdx">
                  <span class="model-tag">{{ model }}</span>
                  <span class="plus-icon" v-if="mIdx < scheme.accessoryModels.length - 1">+</span>
                </template>
              </div>
              <el-tag size="small" type="primary" effect="plain" class="item-count-tag">
                {{ scheme.itemCount }}件套
              </el-tag>
              <el-tag
                size="small"
                type="info"
                effect="light"
                v-if="scheme.categoryName"
              >
                {{ scheme.categoryName }}
              </el-tag>
            </div>
            <div class="scheme-meta-row">
              <span class="meta-tag">
                <el-icon><Document /></el-icon>
                {{ scheme.postCount }} 篇体验帖
              </span>
              <span class="meta-tag">
                <el-icon><View /></el-icon>
                {{ scheme.totalViews }} 浏览
              </span>
              <span class="meta-tag">
                <el-icon><ChatLineSquare /></el-icon>
                {{ scheme.totalComments }} 评论
              </span>
              <span class="meta-tag percentage">
                占比 {{ scheme.percentage }}%
              </span>
            </div>
          </div>
        </div>

        <div class="percentage-bar">
          <div
            class="percentage-bar-fill"
            :style="{ width: Math.min(scheme.percentage * 2, 100) + '%' }"
          ></div>
        </div>

        <div class="related-section" v-if="scheme.relatedPosts && scheme.relatedPosts.length > 0">
          <div class="section-title">📝 相关体验帖</div>
          <div class="related-list">
            <router-link
              class="related-item"
              v-for="post in scheme.relatedPosts.slice(0, 3)"
              :key="post.id"
              :to="`/post/${post.id}`"
            >
              <span class="related-title">{{ post.title }}</span>
              <span class="related-meta">
                <el-icon><ChatDotRound /></el-icon>
                {{ post.commentCount }}
              </span>
            </router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Document, View, ChatLineSquare, ChatDotRound } from '@element-plus/icons-vue'
import { getCollocationSchemes, getCategories } from '@/api'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const schemeList = ref([])
const categories = ref([])
const filterCategoryId = ref(null)
const filterItemCount = ref(null)

const overviewStats = computed(() => {
  if (!schemeList.value || schemeList.value.length === 0) return null
  const totalSchemes = schemeList.value.length
  const totalPosts = schemeList.value.reduce((s, m) => s + (m.postCount || 0), 0)
  const totalViews = schemeList.value.reduce((s, m) => s + (m.totalViews || 0), 0)

  const topScheme = schemeList.value[0]
  const topSchemeName = topScheme && topScheme.accessoryModels && topScheme.accessoryModels.length > 0
    ? topScheme.accessoryModels[0] + (topScheme.accessoryModels.length > 1 ? ' 等' : '')
    : '-'

  return {
    totalSchemes,
    totalPosts,
    totalViews,
    topSchemeName
  }
})

const loadData = async () => {
  loading.value = true
  try {
    const params = {}
    if (filterCategoryId.value) {
      params.categoryId = filterCategoryId.value
    }
    if (filterItemCount.value) {
      params.minItems = filterItemCount.value
      params.maxItems = filterItemCount.value === 5 ? 10 : filterItemCount.value
    }
    params.limit = 30
    schemeList.value = await getCollocationSchemes(params)
  } catch (e) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const loadCategories = async () => {
  try {
    categories.value = await getCategories()
  } catch (e) {}
}

onMounted(() => {
  loadCategories()
  loadData()
})
</script>

<style lang="scss" scoped>
.collocation-rank {
  padding-bottom: 40px;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
    padding: 24px;
    margin-bottom: 20px;

    .header-left {
      .page-title {
        font-size: 24px;
        font-weight: 700;
        color: #333;
        margin: 0 0 8px 0;
      }

      .page-desc {
        font-size: 14px;
        color: #888;
        margin: 0;
        line-height: 1.6;
      }
    }

    .header-right {
      display: flex;
      align-items: center;
    }
  }

  .stats-overview {
    display: flex;
    align-items: center;
    padding: 24px;
    margin-bottom: 20px;

    .stat-item {
      flex: 1;
      text-align: center;

      .stat-num {
        font-size: 28px;
        font-weight: 700;
        color: #333;
        margin-bottom: 6px;
      }

      .stat-label {
        font-size: 13px;
        color: #999;
      }

      &.highlight .stat-num {
        font-size: 20px;
        background: linear-gradient(135deg, #667eea, #764ba2);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        background-clip: text;
      }
    }

    .stat-divider {
      width: 1px;
      height: 48px;
      background: #f0f0f0;
    }
  }

  .scheme-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .scheme-card {
    padding: 20px 24px;
    transition: all 0.2s;

    &:hover {
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
      transform: translateY(-1px);
    }
  }

  .scheme-header {
    display: flex;
    gap: 16px;
    margin-bottom: 14px;

    .rank-badge {
      width: 36px;
      height: 36px;
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: 800;
      font-size: 16px;
      flex-shrink: 0;
      background: #f0f0f0;
      color: #999;

      &.rank-1 {
        background: linear-gradient(135deg, #667eea, #764ba2);
        color: #fff;
        box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
      }

      &.rank-2 {
        background: linear-gradient(135deg, #409eff, #79bbff);
        color: #fff;
        box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
      }

      &.rank-3 {
        background: linear-gradient(135deg, #13c2c2, #5cdbd3);
        color: #fff;
        box-shadow: 0 2px 8px rgba(19, 194, 194, 0.3);
      }
    }

    .scheme-main {
      flex: 1;
      min-width: 0;

      .scheme-models-row {
        display: flex;
        align-items: center;
        gap: 10px;
        margin-bottom: 10px;
        flex-wrap: wrap;

        .models-chain {
          display: flex;
          align-items: center;
          gap: 8px;
          flex-wrap: wrap;
          flex: 1;
          min-width: 0;

          .model-tag {
            display: inline-block;
            padding: 6px 14px;
            background: linear-gradient(135deg, #f0f5ff 0%, #e6f0ff 100%);
            border: 1px solid #d6e4ff;
            border-radius: 6px;
            font-size: 14px;
            font-weight: 600;
            color: #1d39c4;
          }

          .plus-icon {
            font-size: 16px;
            font-weight: 700;
            color: #8c8c8c;
          }
        }

        .item-count-tag {
          flex-shrink: 0;
        }
      }

      .scheme-meta-row {
        display: flex;
        gap: 16px;
        flex-wrap: wrap;
        align-items: center;

        .meta-tag {
          display: flex;
          align-items: center;
          gap: 4px;
          font-size: 13px;
          color: #888;

          .el-icon {
            font-size: 14px;
          }

          &.percentage {
            color: #667eea;
            font-weight: 600;
          }
        }
      }
    }
  }

  .percentage-bar {
    height: 6px;
    background: #f0f0f0;
    border-radius: 4px;
    overflow: hidden;
    margin-bottom: 16px;

    .percentage-bar-fill {
      height: 100%;
      border-radius: 4px;
      background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
      transition: width 0.6s ease;
    }
  }

  .section-title {
    font-size: 14px;
    font-weight: 600;
    color: #333;
    margin-bottom: 12px;
    padding-left: 8px;
    border-left: 3px solid #667eea;
  }

  .related-section {
    .related-list {
      display: flex;
      flex-direction: column;
      gap: 6px;
    }

    .related-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 10px 14px;
      background: #fafafa;
      border-radius: 6px;
      text-decoration: none;
      transition: all 0.15s;

      &:hover {
        background: #f0f0f0;

        .related-title {
          color: #667eea;
        }
      }

      .related-title {
        font-size: 14px;
        color: #444;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        flex: 1;
        margin-right: 12px;
        transition: color 0.15s;
      }

      .related-meta {
        display: flex;
        align-items: center;
        gap: 4px;
        font-size: 12px;
        color: #999;
        flex-shrink: 0;

        .el-icon {
          font-size: 13px;
        }
      }
    }
  }
}
</style>
