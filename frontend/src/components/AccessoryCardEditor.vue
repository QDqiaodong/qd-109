<template>
  <div class="accessory-card-editor">
    <div class="editor-header">
      <span class="header-title">🔧 配件参数卡片</span>
      <el-button type="primary" size="small" @click="addCard" :disabled="cards.length >= 5">
        + 添加卡片
      </el-button>
    </div>

    <div class="cards-list" v-if="cards.length > 0">
      <div v-for="(card, index) in cards" :key="index" class="card-item">
        <div class="card-header">
          <span class="card-index">卡片 {{ index + 1 }}</span>
          <el-button type="danger" link size="small" @click="removeCard(index)">
            删除
          </el-button>
        </div>

        <div class="card-body">
          <el-form :model="card" label-width="90px" size="small">
            <el-form-item label="型号">
              <el-input v-model="card.model" placeholder="如：AirPods Pro 2" maxlength="50" />
            </el-form-item>

            <el-form-item label="接口类型">
              <el-input v-model="card.interfaceType" placeholder="如：Type-C、Lightning、蓝牙5.3" maxlength="50" />
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
      <el-empty description="暂无配件卡片，点击上方按钮添加" :image-size="80" />
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

const createEmptyCard = () => ({
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
    margin-bottom: 16px;

    .header-title {
      font-size: 15px;
      font-weight: 600;
      color: #333;
    }
  }

  .cards-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .card-item {
    border: 1px solid #e8e8e8;
    border-radius: 8px;
    background: #fafafa;
    overflow: hidden;

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 10px 16px;
      background: #f0f0f0;
      border-bottom: 1px solid #e8e8e8;

      .card-index {
        font-size: 13px;
        font-weight: 600;
        color: #666;
      }
    }

    .card-body {
      padding: 16px;
    }
  }

  .empty-tip {
    padding: 20px 0;
  }
}
</style>
