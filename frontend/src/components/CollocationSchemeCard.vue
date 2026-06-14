<template>
  <div class="collocation-scheme-card">
    <div class="scheme-header">
      <div class="scheme-title-row">
        <span class="scheme-icon">🛠️</span>
        <span class="scheme-title">设备组合方案</span>
        <el-tag type="info" size="small" effect="plain" class="device-count">
          共 {{ totalCount }} 件设备
        </el-tag>
      </div>
      <div class="scheme-desc">
        作者精心搭配的整套方案，一眼看清设备分工与连接逻辑
      </div>
    </div>

    <div class="scheme-body">
      <div v-if="mainDevices.length > 0" class="category-section main-section">
        <div class="category-header main-header">
          <div class="category-label">
            <span class="category-icon">🖥️</span>
            <span class="category-name">主设备</span>
          </div>
          <span class="category-count">{{ mainDevices.length }} 件</span>
        </div>
        <div class="device-list">
          <div v-for="(device, idx) in mainDevices" :key="'m-'+idx" class="device-card main-card">
            <div class="device-connector" v-if="idx === 0"></div>
            <div class="device-card-body">
              <div class="device-top">
                <h4 class="device-model">{{ device.model || '未命名设备' }}</h4>
              </div>
              <div class="device-role" v-if="device.role">
                <span class="role-label">🎯 作用</span>
                <span class="role-text">{{ device.role }}</span>
              </div>
              <div class="device-params">
                <div class="param-item" v-if="device.interfaceType">
                  <span class="param-k">接口</span>
                  <span class="param-v">{{ device.interfaceType }}</span>
                </div>
              </div>
              <div class="device-bottom" v-if="device.pros?.length > 0">
                <div class="mini-pros">
                  <span class="pro-tag" v-for="(pro, pi) in device.pros.slice(0, 3)" :key="pi">
                    ✨ {{ pro }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-if="coreDevices.length > 0" class="category-section core-section">
        <div class="category-header core-header">
          <div class="category-label">
            <span class="category-icon">⚡</span>
            <span class="category-name">核心配件</span>
          </div>
          <span class="category-count">{{ coreDevices.length }} 件</span>
        </div>
        <div class="device-list grid-list">
          <div v-for="(device, idx) in coreDevices" :key="'c-'+idx" class="device-card core-card">
            <div class="device-card-body">
              <div class="device-top">
                <h4 class="device-model">{{ device.model || '未命名设备' }}</h4>
              </div>
              <div class="device-role" v-if="device.role">
                <span class="role-label">🎯 作用</span>
                <span class="role-text">{{ device.role }}</span>
              </div>
              <div class="device-params">
                <div class="param-item" v-if="device.interfaceType">
                  <span class="param-k">接口</span>
                  <span class="param-v">{{ device.interfaceType }}</span>
                </div>
                <div class="param-item" v-if="device.compatibleDevices?.length > 0">
                  <span class="param-k">适配</span>
                  <span class="param-v tags-inline">
                    <span class="mini-tag" v-for="(d, di) in device.compatibleDevices.slice(0, 3)" :key="di">
                      {{ d }}
                    </span>
                    <span class="more-tag" v-if="device.compatibleDevices.length > 3">
                      +{{ device.compatibleDevices.length - 3 }}
                    </span>
                  </span>
                </div>
              </div>
              <div class="device-bottom" v-if="device.pros?.length > 0">
                <div class="mini-pros">
                  <span class="pro-tag" v-for="(pro, pi) in device.pros.slice(0, 2)" :key="pi">
                    ✨ {{ pro }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-if="peripheralDevices.length > 0" class="category-section peripheral-section">
        <div class="category-header peripheral-header">
          <div class="category-label">
            <span class="category-icon">🔌</span>
            <span class="category-name">补充外设</span>
          </div>
          <span class="category-count">{{ peripheralDevices.length }} 件</span>
        </div>
        <div class="device-list grid-list dense">
          <div v-for="(device, idx) in peripheralDevices" :key="'p-'+idx" class="device-card peripheral-card">
            <div class="device-card-body">
              <div class="device-top">
                <h4 class="device-model">{{ device.model || '未命名设备' }}</h4>
              </div>
              <div class="device-role" v-if="device.role">
                <span class="role-label">🎯</span>
                <span class="role-text">{{ device.role }}</span>
              </div>
              <div class="device-params">
                <div class="param-item" v-if="device.interfaceType">
                  <span class="param-k">接口</span>
                  <span class="param-v">{{ device.interfaceType }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-if="uncategorizedDevices.length > 0" class="category-section others-section">
        <div class="category-header others-header">
          <div class="category-label">
            <span class="category-icon">📦</span>
            <span class="category-name">其他设备</span>
          </div>
          <span class="category-count">{{ uncategorizedDevices.length }} 件</span>
        </div>
        <div class="device-list grid-list dense">
          <div v-for="(device, idx) in uncategorizedDevices" :key="'u-'+idx" class="device-card others-card">
            <div class="device-card-body">
              <div class="device-top">
                <h4 class="device-model">{{ device.model || '未命名设备' }}</h4>
              </div>
              <div class="device-params">
                <div class="param-item" v-if="device.interfaceType">
                  <span class="param-k">接口</span>
                  <span class="param-v">{{ device.interfaceType }}</span>
                </div>
              </div>
              <div class="device-bottom" v-if="device.pros?.length > 0">
                <div class="mini-pros">
                  <span class="pro-tag" v-for="(pro, pi) in device.pros.slice(0, 2)" :key="pi">
                    ✨ {{ pro }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="scheme-footer" v-if="showConnectionHint">
      <div class="footer-hint">
        <span class="hint-icon">💡</span>
        <span class="hint-text">
          整套方案通过 {{ mainInterface }} 串联，{{ connectionAdvice }}
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  accessoryCards: {
    type: Array,
    required: true
  }
})

const mainDevices = computed(() =>
  props.accessoryCards.filter(c => c.category === 'main' && c.model)
)

const coreDevices = computed(() =>
  props.accessoryCards.filter(c => c.category === 'core' && c.model)
)

const peripheralDevices = computed(() =>
  props.accessoryCards.filter(c => c.category === 'peripheral' && c.model)
)

const uncategorizedDevices = computed(() =>
  props.accessoryCards.filter(c => !c.category && c.model)
)

const hasCategories = computed(() =>
  mainDevices.value.length > 0 || coreDevices.value.length > 0 || peripheralDevices.value.length > 0
)

const totalCount = computed(() =>
  mainDevices.value.length + coreDevices.value.length + peripheralDevices.value.length + uncategorizedDevices.value.length
)

const mainInterface = computed(() => {
  const allInterfaces = []
  props.accessoryCards.forEach(c => {
    if (c.interfaceType) {
      const parts = c.interfaceType.split(/[\/、,，\s]+/).map(s => s.trim()).filter(Boolean)
      allInterfaces.push(...parts)
    }
  })
  const counts = {}
  allInterfaces.forEach(i => {
    const normalized = i.toLowerCase().replace(/[\d.\s]/g, '')
    counts[normalized] = (counts[normalized] || 0) + 1
  })
  const sorted = Object.entries(counts).sort((a, b) => b[1] - a[1])
  if (sorted.length > 0 && sorted[0][1] >= 2) {
    const key = sorted[0][0]
    const original = allInterfaces.find(i => i.toLowerCase().replace(/[\d.\s]/g, '') === key)
    return original || key
  }
  return '统一接口'
})

const connectionAdvice = computed(() => {
  if (mainDevices.value.length > 0 && coreDevices.value.length > 0) {
    return '主设备为核心，配件围绕其扩展'
  }
  if (coreDevices.value.length > 1) {
    return '核心配件之间需注意兼容性'
  }
  return '选购时建议优先确认接口匹配'
})

const showConnectionHint = computed(() =>
  hasCategories.value && totalCount.value >= 3
)
</script>

<style lang="scss" scoped>
.collocation-scheme-card {
  border-radius: 16px;
  overflow: hidden;
  background: linear-gradient(135deg, #fefefe 0%, #f8fafc 100%);
  border: 1px solid #ebeef5;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  margin-bottom: 24px;

  .scheme-header {
    padding: 20px 24px 16px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #fff;

    .scheme-title-row {
      display: flex;
      align-items: center;
      gap: 10px;
      margin-bottom: 8px;

      .scheme-icon {
        font-size: 22px;
      }

      .scheme-title {
        font-size: 18px;
        font-weight: 700;
      }

      .device-count {
        margin-left: auto;
        background: rgba(255, 255, 255, 0.2);
        border-color: rgba(255, 255, 255, 0.3);
        color: #fff;
      }
    }

    .scheme-desc {
      font-size: 13px;
      color: rgba(255, 255, 255, 0.85);
      padding-left: 32px;
    }
  }

  .scheme-body {
    padding: 20px 24px;
  }

  .category-section {
    margin-bottom: 20px;

    &:last-child {
      margin-bottom: 0;
    }

    .category-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 14px;
      padding: 10px 14px;
      border-radius: 10px;

      .category-label {
        display: flex;
        align-items: center;
        gap: 8px;

        .category-icon {
          font-size: 18px;
        }

        .category-name {
          font-size: 15px;
          font-weight: 700;
        }
      }

      .category-count {
        font-size: 12px;
        font-weight: 600;
        padding: 3px 10px;
        border-radius: 10px;
      }
    }

    .main-header {
      background: linear-gradient(135deg, #e6f7ff 0%, #d9ecff 100%);
      border: 1px solid #bae7ff;

      .category-name { color: #0958d9; }
      .category-count {
        background: #1890ff;
        color: #fff;
      }
    }

    .core-header {
      background: linear-gradient(135deg, #f6ffed 0%, #eaffdc 100%);
      border: 1px solid #b7eb8f;

      .category-name { color: #389e0d; }
      .category-count {
        background: #52c41a;
        color: #fff;
      }
    }

    .peripheral-header {
      background: linear-gradient(135deg, #fff7e6 0%, #ffe7ba 100%);
      border: 1px solid #ffd591;

      .category-name { color: #d46b08; }
      .category-count {
        background: #fa8c16;
        color: #fff;
      }
    }

    .others-header {
      background: linear-gradient(135deg, #f5f5f5 0%, #eeeeee 100%);
      border: 1px solid #e0e0e0;

      .category-name { color: #595959; }
      .category-count {
        background: #8c8c8c;
        color: #fff;
      }
    }
  }

  .device-list {
    display: flex;
    flex-direction: column;
    gap: 12px;

    &.grid-list {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
      gap: 12px;

      &.dense {
        grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
      }
    }
  }

  .device-card {
    border-radius: 12px;
    overflow: hidden;
    transition: all 0.25s;

    &:hover {
      transform: translateY(-2px);

      .device-card-body {
        box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
      }
    }

    .device-card-body {
      background: #fff;
      padding: 14px 16px;
      border-radius: 12px;
      border: 1px solid #f0f0f0;
      transition: all 0.25s;
    }

    &.main-card {
      position: relative;

      .device-card-body {
        border: 2px solid #91caff;
        background: linear-gradient(135deg, #ffffff 0%, #f0f7ff 100%);
      }
    }

    &.core-card .device-card-body {
      border: 1px solid #b7eb8f;
      background: linear-gradient(135deg, #ffffff 0%, #f6ffed 100%);
    }

    &.peripheral-card .device-card-body {
      border: 1px solid #ffd591;
      background: linear-gradient(135deg, #ffffff 0%, #fffbe6 100%);
      padding: 12px 14px;
    }

    &.others-card .device-card-body {
      border: 1px solid #e0e0e0;
      padding: 12px 14px;
    }

    .device-top {
      margin-bottom: 10px;

      .device-model {
        font-size: 16px;
        font-weight: 700;
        color: #1a1a1a;
        margin: 0;
        line-height: 1.3;
      }
    }

    .device-role {
      display: flex;
      gap: 6px;
      padding: 8px 10px;
      margin-bottom: 10px;
      background: #fafafa;
      border-radius: 8px;
      border-left: 3px solid #faad14;

      .role-label {
        flex-shrink: 0;
        font-size: 12px;
        font-weight: 600;
        color: #d46b08;
        padding-top: 1px;
      }

      .role-text {
        font-size: 13px;
        color: #555;
        line-height: 1.5;
      }
    }

    .device-params {
      display: flex;
      flex-direction: column;
      gap: 6px;
      margin-bottom: 10px;

      .param-item {
        display: flex;
        gap: 8px;
        font-size: 12px;

        .param-k {
          flex-shrink: 0;
          color: #999;
          font-weight: 500;
          min-width: 36px;
        }

        .param-v {
          flex: 1;
          color: #555;
          font-weight: 500;

          &.tags-inline {
            display: flex;
            flex-wrap: wrap;
            gap: 4px;

            .mini-tag {
              display: inline-block;
              padding: 1px 6px;
              background: #e6f7ff;
              color: #1890ff;
              border-radius: 4px;
              font-size: 11px;
            }

            .more-tag {
              display: inline-block;
              padding: 1px 6px;
              background: #f5f5f5;
              color: #999;
              border-radius: 4px;
              font-size: 11px;
            }
          }
        }
      }
    }

    .device-bottom {
      .mini-pros {
        display: flex;
        flex-wrap: wrap;
        gap: 6px;

        .pro-tag {
          display: inline-block;
          padding: 3px 8px;
          font-size: 11px;
          background: linear-gradient(135deg, #f6ffed 0%, #eaffdc 100%);
          color: #389e0d;
          border-radius: 6px;
          font-weight: 500;
        }
      }
    }
  }

  .scheme-footer {
    padding: 14px 24px;
    border-top: 1px solid #f0f0f0;
    background: #fafbfc;

    .footer-hint {
      display: flex;
      align-items: flex-start;
      gap: 8px;

      .hint-icon {
        font-size: 16px;
        flex-shrink: 0;
      }

      .hint-text {
        font-size: 13px;
        color: #666;
        line-height: 1.6;
      }
    }
  }
}
</style>
