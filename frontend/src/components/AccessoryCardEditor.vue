<template>
  <div class="accessory-card-editor">
    <div class="editor-header">
      <span class="header-title">🔧 设备组合卡片</span>
      <el-button type="primary" size="small" @click="addCard" :disabled="cards.length >= 8">
        + 添加设备
      </el-button>
    </div>

    <div class="category-hint">
      <el-tag type="primary" effect="plain">🖥️ 主设备</el-tag>
      <el-tag type="success" effect="plain">⚡ 核心配件</el-tag>
      <el-tag type="warning" effect="plain">🔌 补充外设</el-tag>
      <span class="hint-text">分类后可在详情页自动生成组合卡片</span>
    </div>

    <div class="cards-list" v-if="cards.length > 0">
      <div v-for="(card, index) in cards" :key="index" class="card-item">
        <div class="card-header">
          <div class="card-header-left">
            <el-tag :type="getCategoryTagType(card.category)" size="small">
              {{ getCategoryLabel(card.category) }}
            </el-tag>
            <span class="card-index">设备 {{ index + 1 }}</span>
          </div>
          <el-button type="danger" link size="small" @click="removeCard(index)">
            删除
          </el-button>
        </div>

        <div class="card-body">
          <el-form :model="card" label-width="90px" size="small">
            <el-form-item label="设备分类">
              <el-radio-group v-model="card.category">
                <el-radio label="main">🖥️ 主设备</el-radio>
                <el-radio label="core">⚡ 核心配件</el-radio>
                <el-radio label="peripheral">🔌 补充外设</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="作用说明">
              <el-input
                v-model="card.role"
                placeholder="描述该设备在整套方案中的作用，如：提供计算性能、输出画面、扩展接口等"
                maxlength="100"
                show-word-limit
              />
            </el-form-item>

            <el-form-item label="型号">
              <el-input v-model="card.model" placeholder="如：MacBook Pro 14寸 M3" maxlength="50" />
            </el-form-item>

            <el-form-item label="接口类型">
              <el-input v-model="card.interfaceType" placeholder="如：Thunderbolt 4、Type-C、蓝牙5.3" maxlength="50" />
            </el-form-item>

            <el-form-item label="适配设备">
              <el-select
                v-model="card.compatibleDevices"
                multiple
                filterable
                allow-create
                placeholder="选择或输入设备，回车添加"
                style="width: 100%"
              >
                <el-option v-for="device in deviceOptions" :key="device" :label="device" :value="device" />
              </el-select>
            </el-form-item>

            <el-form-item label="使用场景">
              <el-select
                v-model="card.usageScenarios"
                multiple
                filterable
                allow-create
                placeholder="选择或输入场景，回车添加"
                style="width: 100%"
              >
                <el-option v-for="scene in scenarioOptions" :key="scene" :label="scene" :value="scene" />
              </el-select>
            </el-form-item>

            <el-form-item label="核心优点">
              <el-select
                v-model="card.pros"
                multiple
                filterable
                allow-create
                placeholder="输入优点，回车添加"
                style="width: 100%"
              >
                <el-option v-for="pro in proOptions" :key="pro" :label="pro" :value="pro" />
              </el-select>
            </el-form-item>

            <el-form-item label="核心缺点">
              <el-select
                v-model="card.cons"
                multiple
                filterable
                allow-create
                placeholder="输入缺点，回车添加"
                style="width: 100%"
              >
                <el-option v-for="con in conOptions" :key="con" :label="con" :value="con" />
              </el-select>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </div>

    <div class="empty-tip" v-else>
      <el-empty description="暂无设备，点击上方按钮添加主设备、核心配件和补充外设" :image-size="80" />
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  modelValue: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue'])

const cards = ref([])

const deviceOptions = [
  'iPhone', 'iPad', 'MacBook', 'Android手机', 'Windows电脑',
  'Apple Watch', 'AirPods', 'Switch', 'PS5', 'Xbox'
]

const scenarioOptions = [
  '通勤出行', '办公学习', '运动健身', '游戏娱乐',
  '摄影创作', '户外旅行', '居家使用', '商务会议'
]

const proOptions = [
  '音质出色', '降噪效果好', '续航持久', '佩戴舒适',
  '做工精良', '颜值高', '性价比高', '连接稳定',
  '功能丰富', '防水防尘', '充电快', '轻量化'
]

const conOptions = [
  '价格偏高', '续航一般', '佩戴不舒适', '音质一般',
  '降噪效果差', '做工一般', '连接不稳定', '重量大',
  '功能单一', '不防水', '充电慢', '易沾指纹'
]

const getCategoryLabel = (category) => {
  const map = {
    main: '🖥️ 主设备',
    core: '⚡ 核心配件',
    peripheral: '🔌 补充外设'
  }
  return map[category] || '未分类'
}

const getCategoryTagType = (category) => {
  const map = {
    main: 'primary',
    core: 'success',
    peripheral: 'warning'
  }
  return map[category] || 'info'
}

const createEmptyCard = () => ({
  category: 'core',
  role: '',
  model: '',
  interfaceType: '',
  compatibleDevices: [],
  usageScenarios: [],
  pros: [],
  cons: []
})

const addCard = () => {
  cards.value.push(createEmptyCard())
}

const removeCard = (index) => {
  cards.value.splice(index, 1)
}

watch(
  () => props.modelValue,
  (val) => {
    if (val && val.length > 0) {
      cards.value = JSON.parse(JSON.stringify(val))
    } else {
      cards.value = []
    }
  },
  { immediate: true, deep: true }
)

watch(
  cards,
  (val) => {
    emit('update:modelValue', val)
  },
  { deep: true }
)
</script>

<style lang="scss" scoped>
.accessory-card-editor {
  .editor-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;

    .header-title {
      font-size: 15px;
      font-weight: 600;
      color: #333;
    }
  }

  .category-hint {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 16px;
    padding: 10px 14px;
    background: #f5f7fa;
    border-radius: 8px;

    .hint-text {
      margin-left: auto;
      font-size: 12px;
      color: #909399;
    }
  }

  .cards-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .card-item {
    border: 1px solid #e8e8e8;
    border-radius: 10px;
    background: #fafafa;
    overflow: hidden;
    transition: all 0.2s;

    &:hover {
      border-color: #d0d0d0;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
    }

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 10px 16px;
      background: linear-gradient(135deg, #f0f0f0 0%, #e8e8e8 100%);
      border-bottom: 1px solid #e8e8e8;

      .card-header-left {
        display: flex;
        align-items: center;
        gap: 10px;
      }

      .card-index {
        font-size: 13px;
        font-weight: 600;
        color: #666;
      }
    }

    .card-body {
      padding: 16px;
      background: #fff;
    }
  }

  .empty-tip {
    padding: 20px 0;
  }
}
</style>
