<template>
  <div class="compare-panel container">
    <div class="panel-header card">
      <div class="header-left">
        <h1 class="panel-title">
          <span class="title-icon">⚖️</span>
          对照清单板
        </h1>
        <p class="panel-desc">并排对比多篇帖子的分类、类型、标题摘要和关键参数</p>
      </div>
      <div class="header-actions">
        <el-button
          type="danger"
          plain
          :disabled="compareStore.isEmpty"
          @click="handleClearAll"
        >
          清空对照
        </el-button>
        <el-button
          type="primary"
          @click="goBack"
        >
          返回列表
        </el-button>
      </div>
    </div>

    <div class="compare-content" v-if="!compareStore.isEmpty">
      <div class="compare-grid" :style="{ gridTemplateColumns: `repeat(${compareStore.count}, 1fr)` }">
        <div
          v-for="post in compareStore.compareList"
          :key="post.id"
          class="compare-column card"
        >
          <div class="column-header">
            <div class="author-row">
              <img :src="post.avatar" alt="" class="avatar" />
              <div class="author-meta">
                <span class="nickname">{{ post.nickname }}</span>
                <span class="post-time">{{ formatTime(post.createTime) }}</span>
              </div>
              <button
                class="remove-btn"
                @click="handleRemove(post.id)"
                title="移出对照"
              >
                ✕
              </button>
            </div>

            <div class="type-row">
              <span
                :class="['type-tag', post.type === 1 ? 'type-experience' : 'type-question']"
              >
                {{ post.type === 1 ? '体验分享' : '问题求助' }}
              </span>
              <span class="category-tag">{{ post.categoryName }}</span>
            </div>

            <router-link :to="`/post/${post.id}`" class="post-title">
              {{ post.title }}
            </router-link>

            <p class="post-excerpt">{{ getExcerpt(post.content) }}</p>

            <div class="stats-row">
              <span>👁️ {{ post.viewCount }}</span>
              <span>💬 {{ post.commentCount }}</span>
              <span>👍 {{ post.likeCount }}</span>
            </div>
          </div>

          <div class="column-body">
            <div class="section-title">
              <span class="section-icon">🔧</span>
              关键参数
            </div>

            <div class="accessory-cards" v-if="post.accessoryCards && post.accessoryCards.length">
              <div
                v-for="(card, idx) in post.accessoryCards"
                :key="idx"
                class="mini-accessory-card"
              >
                <div class="mini-card-header">
                  <span class="mini-card-title">配件参数 {{ idx + 1 }}</span>
                </div>

                <div class="mini-card-body">
                  <div class="param-item" v-if="card.model">
                    <span class="param-label">型号</span>
                    <span class="param-value model">{{ card.model }}</span>
                  </div>

                  <div class="param-item" v-if="card.interfaceType">
                    <span class="param-label">接口类型</span>
                    <span class="param-value">{{ card.interfaceType }}</span>
                  </div>

                  <div class="param-item" v-if="card.compatibleDevices && card.compatibleDevices.length">
                    <span class="param-label">适配设备</span>
                    <div class="param-tags">
                      <span
                        class="tag tag-blue"
                        v-for="(device, dIdx) in card.compatibleDevices"
                        :key="dIdx"
                      >
                        {{ device }}
                      </span>
                    </div>
                  </div>

                  <div class="param-item" v-if="card.usageScenarios && card.usageScenarios.length">
                    <span class="param-label">使用场景</span>
                    <div class="param-tags">
                      <span
                        class="tag tag-green"
                        v-for="(scene, sIdx) in card.usageScenarios"
                        :key="sIdx"
                      >
                        {{ scene }}
                      </span>
                    </div>
                  </div>

                  <div class="param-item pros-item" v-if="card.pros && card.pros.length">
                    <span class="param-label pros-label">✨ 核心优点</span>
                    <ul class="pros-cons-list">
                      <li v-for="(pro, pIdx) in card.pros" :key="pIdx">{{ pro }}</li>
                    </ul>
                  </div>

                  <div class="param-item cons-item" v-if="card.cons && card.cons.length">
                    <span class="param-label cons-label">⚠️ 核心缺点</span>
                    <ul class="pros-cons-list">
                      <li v-for="(con, cIdx) in card.cons" :key="cIdx">{{ con }}</li>
                    </ul>
                  </div>
                </div>
              </div>
            </div>

            <div class="empty-params" v-else>
              <span class="empty-icon">📋</span>
              <span class="empty-text">暂无参数卡</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="empty-state card" v-else>
      <div class="empty-icon-large">📭</div>
      <h2 class="empty-title">对照清单是空的</h2>
      <p class="empty-desc">去列表页选择你感兴趣的帖子，点击「对照」按钮加入清单</p>
      <el-button type="primary" size="large" @click="goBack">
        去逛逛
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCompareStore } from '@/store/compare'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const compareStore = useCompareStore()

onMounted(() => {
  compareStore.init()
  compareStore.syncFromStorage()
})

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return date.toLocaleDateString()
}

const getExcerpt = (content, maxLen = 120) => {
  if (!content) return ''
  const clean = content.replace(/\n/g, ' ').trim()
  if (clean.length <= maxLen) return clean
  return clean.slice(0, maxLen) + '...'
}

const handleRemove = (postId) => {
  compareStore.removeFromCompare(postId)
  ElMessage.info('已移出对照清单')
}

const handleClearAll = () => {
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

const goBack = () => {
  router.push('/')
}
</script>

<style lang="scss" scoped>
.compare-panel {
  padding-bottom: 40px;

  .panel-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding: 24px 28px;

    .header-left {
      .panel-title {
        font-size: 24px;
        font-weight: 600;
        margin: 0 0 8px 0;
        color: #1a1a1a;
        display: flex;
        align-items: center;
        gap: 10px;

        .title-icon {
          font-size: 28px;
        }
      }

      .panel-desc {
        font-size: 14px;
        color: #999;
        margin: 0;
      }
    }

    .header-actions {
      display: flex;
      gap: 12px;
    }
  }

  .compare-content {
    .compare-grid {
      display: grid;
      gap: 16px;
      min-height: 400px;
    }

    .compare-column {
      display: flex;
      flex-direction: column;
      padding: 0;
      overflow: hidden;
      transition: all 0.2s;

      &:hover {
        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
      }

      .column-header {
        padding: 20px;
        border-bottom: 1px solid #f0f0f0;
        background: linear-gradient(135deg, #fafbfc 0%, #f5f7fa 100%);

        .author-row {
          display: flex;
          align-items: center;
          gap: 10px;
          margin-bottom: 12px;

          .avatar {
            width: 40px;
            height: 40px;
            border-radius: 50%;
            object-fit: cover;
          }

          .author-meta {
            flex: 1;
            display: flex;
            flex-direction: column;
            gap: 2px;
            min-width: 0;

            .nickname {
              font-size: 14px;
              font-weight: 500;
              color: #333;
              overflow: hidden;
              text-overflow: ellipsis;
              white-space: nowrap;
            }

            .post-time {
              font-size: 12px;
              color: #999;
            }
          }

          .remove-btn {
            width: 24px;
            height: 24px;
            display: flex;
            align-items: center;
            justify-content: center;
            border: none;
            background: transparent;
            color: #999;
            font-size: 14px;
            cursor: pointer;
            border-radius: 50%;
            transition: all 0.2s;

            &:hover {
              background: #ff4d4f;
              color: #fff;
            }
          }
        }

        .type-row {
          display: flex;
          gap: 8px;
          margin-bottom: 12px;

          .type-tag {
            font-size: 12px;
            padding: 2px 10px;
            border-radius: 12px;
            font-weight: 500;

            &.type-experience {
              background: #e6f7ff;
              color: #1890ff;
            }

            &.type-question {
              background: #fff7e6;
              color: #fa8c16;
            }
          }

          .category-tag {
            font-size: 12px;
            color: #722ed1;
            background: #f9f0ff;
            padding: 2px 10px;
            border-radius: 12px;
          }
        }

        .post-title {
          display: block;
          font-size: 16px;
          font-weight: 600;
          color: #1a1a1a;
          margin-bottom: 10px;
          line-height: 1.5;
          transition: color 0.2s;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
          overflow: hidden;

          &:hover {
            color: #722ed1;
          }
        }

        .post-excerpt {
          font-size: 13px;
          color: #666;
          line-height: 1.6;
          margin-bottom: 12px;
          display: -webkit-box;
          -webkit-line-clamp: 3;
          -webkit-box-orient: vertical;
          overflow: hidden;
        }

        .stats-row {
          display: flex;
          gap: 16px;
          font-size: 12px;
          color: #999;
        }
      }

      .column-body {
        flex: 1;
        padding: 16px 20px;
        overflow-y: auto;
        max-height: 500px;

        .section-title {
          display: flex;
          align-items: center;
          gap: 6px;
          font-size: 14px;
          font-weight: 600;
          color: #333;
          margin-bottom: 12px;
          padding-bottom: 8px;
          border-bottom: 1px dashed #e8e8e8;

          .section-icon {
            font-size: 16px;
          }
        }

        .accessory-cards {
          display: flex;
          flex-direction: column;
          gap: 12px;

          .mini-accessory-card {
            border: 1px solid #e8e8e8;
            border-radius: 10px;
            overflow: hidden;
            background: #fafbff;

            .mini-card-header {
              padding: 10px 14px;
              background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
              color: white;

              .mini-card-title {
                font-size: 13px;
                font-weight: 600;
              }
            }

            .mini-card-body {
              padding: 12px 14px;

              .param-item {
                display: flex;
                align-items: flex-start;
                gap: 10px;
                padding: 6px 0;
                border-bottom: 1px dashed #e8e8e8;

                &:last-child {
                  border-bottom: none;
                }

                .param-label {
                  flex-shrink: 0;
                  width: 60px;
                  font-size: 12px;
                  color: #888;
                  padding-top: 1px;
                }

                .param-value {
                  flex: 1;
                  font-size: 12px;
                  color: #333;
                  word-break: break-all;

                  &.model {
                    font-size: 13px;
                    font-weight: 600;
                    color: #1a1a1a;
                  }
                }

                .param-tags {
                  flex: 1;
                  display: flex;
                  flex-wrap: wrap;
                  gap: 4px;

                  .tag {
                    display: inline-block;
                    padding: 2px 8px;
                    font-size: 11px;
                    border-radius: 4px;

                    &.tag-blue {
                      background: #e6f7ff;
                      color: #1890ff;
                    }

                    &.tag-green {
                      background: #f6ffed;
                      color: #52c41a;
                    }
                  }
                }
              }

              .pros-item, .cons-item {
                flex-direction: column;
                gap: 6px;

                .param-label {
                  width: auto;
                  font-size: 12px;
                }

                .pros-label {
                  color: #52c41a;
                }

                .cons-label {
                  color: #ff4d4f;
                }
              }

              .pros-cons-list {
                margin: 0;
                padding-left: 16px;

                li {
                  font-size: 12px;
                  line-height: 1.6;
                  color: #555;
                }
              }

              .pros-cons-list li {
                &:first-child {
                  color: #389e0d;
                }
              }
            }
          }
        }

        .empty-params {
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          padding: 30px 0;
          color: #bbb;

          .empty-icon {
            font-size: 32px;
            margin-bottom: 8px;
            opacity: 0.5;
          }

          .empty-text {
            font-size: 13px;
          }
        }
      }
    }
  }

  .empty-state {
    text-align: center;
    padding: 60px 40px;

    .empty-icon-large {
      font-size: 64px;
      margin-bottom: 16px;
    }

    .empty-title {
      font-size: 20px;
      font-weight: 600;
      color: #333;
      margin: 0 0 8px 0;
    }

    .empty-desc {
      font-size: 14px;
      color: #999;
      margin: 0 0 24px 0;
    }
  }
}
</style>
