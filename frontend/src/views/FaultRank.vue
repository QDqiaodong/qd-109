<template>
  <div class="fault-rank container">
    <div class="page-header card">
      <div class="header-left">
        <h1 class="page-title">🔧 同型号高频问题聚合</h1>
        <p class="page-desc">
          基于社区历史求助帖数据分析，汇总各设备/配件型号的常见故障方向，助您快速定位问题
        </p>
      </div>
      <div class="header-right">
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
        <div class="stat-num">{{ overviewStats.totalModels }}</div>
        <div class="stat-label">覆盖型号数</div>
      </div>
      <div class="stat-divider"></div>
      <div class="stat-item">
        <div class="stat-num">{{ overviewStats.totalPosts }}</div>
        <div class="stat-label">历史求助帖</div>
      </div>
      <div class="stat-divider"></div>
      <div class="stat-item">
        <div class="stat-num">{{ overviewStats.totalComments }}</div>
        <div class="stat-label">社区互动</div>
      </div>
      <div class="stat-divider"></div>
      <div class="stat-item highlight">
        <div class="stat-num">{{ overviewStats.topThemeName }}</div>
        <div class="stat-label">最高频故障</div>
      </div>
    </div>

    <div class="model-list" v-loading="loading">
      <el-empty
        v-if="!loading && modelList.length === 0"
        description="暂无故障数据，当用户发布求助帖后这里会自动聚合统计"
        :image-size="100"
      />

      <div
        class="model-card card"
        v-for="(model, idx) in modelList"
        :key="model.model"
      >
        <div class="model-header">
          <div class="rank-badge" :class="'rank-' + (idx + 1)">
            {{ idx + 1 }}
          </div>
          <div class="model-main">
            <div class="model-name-row">
              <h3 class="model-name">{{ model.model }}</h3>
              <el-tag size="small" effect="plain" v-if="model.modelType === 'deviceModel'">
                设备型号
              </el-tag>
              <el-tag size="small" type="success" effect="plain" v-else>
                配件型号
              </el-tag>
              <el-tag
                size="small"
                type="info"
                effect="light"
                v-if="model.categoryName"
              >
                {{ model.categoryName }}
              </el-tag>
            </div>
            <div class="model-meta-row">
              <span class="meta-tag">
                <el-icon><ChatDotRound /></el-icon>
                {{ model.totalHelpPosts }} 求助帖
              </span>
              <span class="meta-tag">
                <el-icon><View /></el-icon>
                {{ model.totalViews }} 浏览
              </span>
              <span class="meta-tag">
                <el-icon><ChatLineSquare /></el-icon>
                {{ model.totalComments }} 评论
              </span>
            </div>
          </div>
        </div>

        <div class="themes-section" v-if="model.topThemes && model.topThemes.length > 0">
          <div class="section-title">📊 常见故障方向</div>
          <div class="themes-grid">
            <div
              class="theme-block"
              v-for="theme in model.topThemes"
              :key="theme.themeKey"
              @click="viewRelated(model, theme)"
            >
              <div class="theme-top">
                <span class="theme-icon">{{ theme.themeIcon }}</span>
                <span class="theme-name">{{ theme.themeName }}</span>
                <el-tag
                  size="small"
                  :type="getThemeTagType(theme.percentage)"
                  effect="dark"
                >
                  {{ theme.count }} 次 · {{ theme.percentage }}%
                </el-tag>
              </div>
              <div class="theme-bar">
                <div
                  class="theme-bar-fill"
                  :style="{ width: Math.min(theme.percentage, 100) + '%' }"
                  :class="getThemeBarClass(theme.percentage)"
                ></div>
              </div>
              <div class="theme-symptom" v-if="theme.sampleSymptoms">
                <span class="symptom-label">典型症状：</span>
                {{ theme.sampleSymptoms }}
              </div>
            </div>
          </div>
        </div>

        <div class="related-section" v-if="model.relatedPosts && model.relatedPosts.length > 0">
          <div class="section-title">📝 相关求助帖</div>
          <div class="related-list">
            <router-link
              class="related-item"
              v-for="post in model.relatedPosts.slice(0, 3)"
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
import { ChatDotRound, View, ChatLineSquare } from '@element-plus/icons-vue'
import { getFaultHotModels, getCategories } from '@/api'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()
const loading = ref(false)
const modelList = ref([])
const categories = ref([])
const filterCategoryId = ref(null)

const overviewStats = computed(() => {
  if (!modelList.value || modelList.value.length === 0) return null
  const totalModels = modelList.value.length
  const totalPosts = modelList.value.reduce((s, m) => s + (m.totalHelpPosts || 0), 0)
  const totalComments = modelList.value.reduce((s, m) => s + (m.totalComments || 0), 0)

  const themeMap = {}
  for (const m of modelList.value) {
    for (const t of (m.topThemes || [])) {
      themeMap[t.themeKey] = (themeMap[t.themeKey] || 0) + (t.count || 0)
    }
  }
  const themeNameMap = {
    disconnection: '掉连/断连',
    noise: '底噪/杂音',
    compatibility: '兼容异常',
    power: '供电不足',
    sound: '音质/无声',
    lag: '延迟/卡顿',
    bluetooth: '蓝牙连接',
    keyboard: '按键/输入',
    display: '显示/画面',
    transfer: '传输/读写'
  }
  let topTheme = ''
  let topCount = 0
  for (const [k, v] of Object.entries(themeMap)) {
    if (v > topCount) {
      topCount = v
      topTheme = themeNameMap[k] || k
    }
  }

  return {
    totalModels,
    totalPosts,
    totalComments,
    topThemeName: topTheme || '-'
  }
})

const loadData = async () => {
  loading.value = true
  try {
    const params = {}
    if (filterCategoryId.value) {
      params.categoryId = filterCategoryId.value
    }
    modelList.value = await getFaultHotModels(params)
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

const getThemeTagType = (percentage) => {
  if (percentage >= 40) return 'danger'
  if (percentage >= 20) return 'warning'
  return 'info'
}

const getThemeBarClass = (percentage) => {
  if (percentage >= 40) return 'bar-danger'
  if (percentage >= 20) return 'bar-warning'
  return 'bar-info'
}

const viewRelated = (model, theme) => {
  const keyword = model.model + ' ' + theme.themeName
  router.push({ path: '/search', query: { keyword, type: 2 } })
}

onMounted(() => {
  loadCategories()
  loadData()
})
</script>

<style lang="scss" scoped>
.fault-rank {
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
        background: linear-gradient(135deg, #ff6b6b, #ffa94d);
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

  .model-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .model-card {
    padding: 20px 24px;
    transition: all 0.2s;

    &:hover {
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
      transform: translateY(-1px);
    }
  }

  .model-header {
    display: flex;
    gap: 16px;
    margin-bottom: 20px;
    padding-bottom: 16px;
    border-bottom: 1px solid #f5f5f5;

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
        background: linear-gradient(135deg, #ff6b6b, #ffa8a8);
        color: #fff;
        box-shadow: 0 2px 8px rgba(255, 107, 107, 0.3);
      }

      &.rank-2 {
        background: linear-gradient(135deg, #ffa94d, #ffd591);
        color: #fff;
        box-shadow: 0 2px 8px rgba(255, 169, 77, 0.3);
      }

      &.rank-3 {
        background: linear-gradient(135deg, #ffd43b, #fff3bf);
        color: #fff;
        box-shadow: 0 2px 8px rgba(255, 212, 59, 0.3);
      }
    }

    .model-main {
      flex: 1;
      min-width: 0;

      .model-name-row {
        display: flex;
        align-items: center;
        gap: 10px;
        margin-bottom: 8px;
        flex-wrap: wrap;

        .model-name {
          font-size: 18px;
          font-weight: 700;
          color: #222;
          margin: 0;
        }
      }

      .model-meta-row {
        display: flex;
        gap: 16px;
        flex-wrap: wrap;

        .meta-tag {
          display: flex;
          align-items: center;
          gap: 4px;
          font-size: 13px;
          color: #888;

          .el-icon {
            font-size: 14px;
          }
        }
      }
    }
  }

  .section-title {
    font-size: 14px;
    font-weight: 600;
    color: #333;
    margin-bottom: 12px;
    padding-left: 8px;
    border-left: 3px solid #ff9933;
  }

  .themes-section {
    margin-bottom: 18px;

    .themes-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
      gap: 12px;
    }

    .theme-block {
      padding: 14px;
      background: linear-gradient(135deg, #fefaf5 0%, #fff 100%);
      border: 1px solid #f5ead6;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.2s;

      &:hover {
        border-color: #e6d0a8;
        background: linear-gradient(135deg, #fdf3e0 0%, #fff 100%);
        transform: translateY(-2px);
        box-shadow: 0 3px 12px rgba(255, 153, 51, 0.1);
      }

      .theme-top {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 10px;

        .theme-icon {
          font-size: 18px;
        }

        .theme-name {
          flex: 1;
          font-weight: 600;
          color: #333;
          font-size: 14px;
        }
      }

      .theme-bar {
        height: 5px;
        background: #faf0dc;
        border-radius: 3px;
        overflow: hidden;
        margin-bottom: 10px;

        .theme-bar-fill {
          height: 100%;
          border-radius: 3px;
          transition: width 0.6s ease;
        }

        .bar-danger {
          background: linear-gradient(90deg, #f56c6c 0%, #ff8787 100%);
        }

        .bar-warning {
          background: linear-gradient(90deg, #e6a23c 0%, #f3c07f 100%);
        }

        .bar-info {
          background: linear-gradient(90deg, #409eff 0%, #79bbff 100%);
        }
      }

      .theme-symptom {
        font-size: 12px;
        color: #888;
        line-height: 1.5;

        .symptom-label {
          color: #aaa;
        }
      }
    }
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
          color: #409eff;
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
