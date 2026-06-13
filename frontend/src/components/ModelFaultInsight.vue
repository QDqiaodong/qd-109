<template>
  <div class="model-fault-insight" v-if="visible">
    <div class="insight-header" @click="collapsed = !collapsed">
      <div class="header-left">
        <span class="header-icon">📊</span>
        <span class="header-title">同型号高频问题洞察</span>
        <el-tag size="small" type="warning" effect="light" v-if="loading">
          已匹配 {{ matchedCount }} 条历史数据
        </el-tag>
      </div>
      <el-icon class="expand-icon" :class="{ rotated: !collapsed }">
        <ArrowDown />
      </el-icon>
    </div>

    <div class="insight-body" v-show="!collapsed" v-loading="loading">
      <el-empty
        v-if="!loading && (!suggestion || !suggestion.commonThemes || suggestion.commonThemes.length === 0)"
        description="暂无该型号的历史故障数据，您是第一个反馈此型号问题的用户！"
        :image-size="80"
      />

      <template v-else-if="suggestion">
        <div class="section" v-if="suggestion.commonThemes && suggestion.commonThemes.length > 0">
          <div class="section-title">🔥 高频故障方向</div>
          <div class="theme-list">
            <div
              class="theme-item"
              v-for="theme in suggestion.commonThemes.slice(0, 5)"
              :key="theme.themeKey"
            >
              <div class="theme-header">
                <span class="theme-icon">{{ theme.themeIcon }}</span>
                <span class="theme-name">{{ theme.themeName }}</span>
                <el-tag
                  size="small"
                  :type="getThemeTagType(theme.percentage)"
                  effect="dark"
                >
                  {{ theme.percentage }}%
                </el-tag>
              </div>
              <div class="theme-bar">
                <div
                  class="theme-bar-fill"
                  :style="{ width: Math.min(theme.percentage, 100) + '%' }"
                  :class="getThemeBarClass(theme.percentage)"
                ></div>
              </div>
              <div class="theme-sample" v-if="theme.sampleSymptoms">
                <span class="sample-label">典型症状：</span>
                <span class="sample-text">{{ theme.sampleSymptoms }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="section" v-if="suggestion.troubleshootingTips">
          <div class="section-title">💡 智能排障建议</div>
          <div class="tips-box">
            <pre class="tips-content">{{ suggestion.troubleshootingTips }}</pre>
          </div>
        </div>

        <div class="section" v-if="suggestion.matchedModels && suggestion.matchedModels.length > 0">
          <div class="section-title">📋 相似型号故障榜</div>
          <div class="model-rank-list">
            <div
              class="model-rank-item"
              v-for="(m, idx) in suggestion.matchedModels.slice(0, 5)"
              :key="m.model"
            >
              <div class="rank-num" :class="'rank-' + (idx + 1)">
                {{ idx + 1 }}
              </div>
              <div class="rank-info">
                <div class="model-name">{{ m.model }}</div>
                <div class="model-meta">
                  <span class="meta-item">
                    <el-icon><ChatDotRound /></el-icon>
                    {{ m.totalHelpPosts }} 求助帖
                  </span>
                  <span class="meta-item" v-if="m.categoryName">
                    {{ m.categoryName }}
                  </span>
                </div>
                <div class="model-themes">
                  <el-tag
                    v-for="t in m.topThemes ? m.topThemes.slice(0, 3) : []"
                    :key="t.themeKey"
                    size="small"
                    effect="plain"
                  >
                    {{ t.themeIcon }} {{ t.themeName }}
                  </el-tag>
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { ArrowDown, ChatDotRound } from '@element-plus/icons-vue'
import { getFaultSuggestions } from '@/api'

const props = defineProps({
  model: {
    type: String,
    default: ''
  },
  categoryId: {
    type: [Number, String],
    default: null
  },
  visible: {
    type: Boolean,
    default: true
  }
})

const loading = ref(false)
const collapsed = ref(false)
const suggestion = ref(null)
const matchedCount = computed(() => {
  if (suggestion.value && suggestion.value.matchedModels) {
    return suggestion.value.matchedModels.reduce((sum, m) => sum + (m.totalHelpPosts || 0), 0)
  }
  return 0
})

let fetchTimer = null

const fetchData = async () => {
  const keyword = (props.model || '').trim()
  if (!keyword) {
    suggestion.value = null
    return
  }
  if (fetchTimer) clearTimeout(fetchTimer)
  fetchTimer = setTimeout(async () => {
    loading.value = true
    try {
      const params = { model: keyword }
      if (props.categoryId) {
        params.categoryId = props.categoryId
      }
      const res = await getFaultSuggestions(params)
      suggestion.value = res
    } finally {
      loading.value = false
    }
  }, 400)
}

watch(
  () => [props.model, props.categoryId],
  () => {
    fetchData()
  },
  { immediate: true }
)

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
</script>

<style lang="scss" scoped>
.model-fault-insight {
  margin-top: 16px;
  border: 1px solid #f0e3d9;
  border-radius: 10px;
  background: linear-gradient(135deg, #fffaf3 0%, #fff 60%);
  overflow: hidden;
  transition: all 0.25s;

  &:hover {
    border-color: #e6d5b8;
    box-shadow: 0 2px 12px rgba(255, 153, 51, 0.08);
  }
}

.insight-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: linear-gradient(90deg, #fff7e8 0%, #fffaf3 100%);
  cursor: pointer;
  user-select: none;

  .header-left {
    display: flex;
    align-items: center;
    gap: 10px;

    .header-icon {
      font-size: 18px;
    }

    .header-title {
      font-weight: 600;
      color: #6b4e22;
      font-size: 15px;
    }
  }

  .expand-icon {
    color: #c49a5a;
    transition: transform 0.2s;

    &.rotated {
      transform: rotate(-90deg);
    }
  }
}

.insight-body {
  padding: 16px;
  border-top: 1px solid #f7ecd9;
}

.section {
  margin-bottom: 20px;

  &:last-child {
    margin-bottom: 0;
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

.theme-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.theme-item {
  padding: 12px;
  background: #fff;
  border: 1px solid #f5ead6;
  border-radius: 8px;

  .theme-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 8px;

    .theme-icon {
      font-size: 16px;
    }

    .theme-name {
      flex: 1;
      font-weight: 500;
      color: #333;
      font-size: 14px;
    }
  }

  .theme-bar {
    height: 6px;
    background: #faf0dc;
    border-radius: 3px;
    overflow: hidden;
    margin-bottom: 8px;

    .theme-bar-fill {
      height: 100%;
      border-radius: 3px;
      transition: width 0.5s ease;
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

  .theme-sample {
    font-size: 12px;
    color: #888;
    line-height: 1.5;

    .sample-label {
      color: #aaa;
    }

    .sample-text {
      color: #666;
    }
  }
}

.tips-box {
  background: #f6faff;
  border: 1px solid #e0edff;
  border-radius: 8px;
  padding: 12px 14px;

  .tips-content {
    margin: 0;
    white-space: pre-wrap;
    font-size: 13px;
    line-height: 1.7;
    color: #555;
    font-family: inherit;
  }
}

.model-rank-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.model-rank-item {
  display: flex;
  gap: 12px;
  padding: 10px 12px;
  background: #fff;
  border: 1px solid #f3ead6;
  border-radius: 8px;

  .rank-num {
    width: 24px;
    height: 24px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 12px;
    font-weight: 700;
    flex-shrink: 0;
    background: #f0f0f0;
    color: #999;

    &.rank-1 {
      background: linear-gradient(135deg, #ff6b6b, #ffa8a8);
      color: #fff;
    }

    &.rank-2 {
      background: linear-gradient(135deg, #ffa94d, #ffd591);
      color: #fff;
    }

    &.rank-3 {
      background: linear-gradient(135deg, #ffd43b, #fff3bf);
      color: #fff;
    }
  }

  .rank-info {
    flex: 1;
    min-width: 0;

    .model-name {
      font-weight: 600;
      color: #333;
      font-size: 14px;
      margin-bottom: 4px;
    }

    .model-meta {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 6px;
      font-size: 12px;
      color: #999;

      .meta-item {
        display: flex;
        align-items: center;
        gap: 4px;

        .el-icon {
          font-size: 14px;
        }
      }
    }

    .model-themes {
      display: flex;
      flex-wrap: wrap;
      gap: 6px;
    }
  }
}
</style>
