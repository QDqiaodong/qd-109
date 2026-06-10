<template>
  <div class="accessory-card">
    <div class="card-header">
      <span class="card-icon">🔧</span>
      <span class="card-title">配件参数</span>
    </div>

    <div class="card-body">
      <div class="param-row" v-if="card.model">
        <span class="param-label">型号</span>
        <span class="param-value model">{{ card.model }}</span>
      </div>

      <div class="param-row" v-if="card.interfaceType">
        <span class="param-label">接口类型</span>
        <span class="param-value">{{ card.interfaceType }}</span>
      </div>

      <div class="param-row" v-if="card.compatibleDevices && card.compatibleDevices.length">
        <span class="param-label">适配设备</span>
        <div class="param-tags">
          <span class="tag tag-blue" v-for="(device, idx) in card.compatibleDevices" :key="idx">
            {{ device }}
          </span>
        </div>
      </div>

      <div class="param-row" v-if="card.usageScenarios && card.usageScenarios.length">
        <span class="param-label">使用场景</span>
        <div class="param-tags">
          <span class="tag tag-green" v-for="(scene, idx) in card.usageScenarios" :key="idx">
            {{ scene }}
          </span>
        </div>
      </div>

      <div class="param-row pros-row" v-if="card.pros && card.pros.length">
        <span class="param-label pros-label">✨ 核心优点</span>
        <ul class="pros-list">
          <li v-for="(pro, idx) in card.pros" :key="idx">{{ pro }}</li>
        </ul>
      </div>

      <div class="param-row cons-row" v-if="card.cons && card.cons.length">
        <span class="param-label cons-label">⚠️ 核心缺点</span>
        <ul class="cons-list">
          <li v-for="(con, idx) in card.cons" :key="idx">{{ con }}</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  card: {
    type: Object,
    required: true
  }
})
</script>

<style lang="scss" scoped>
.accessory-card {
  border: 1px solid #e8e8e8;
  border-radius: 12px;
  background: linear-gradient(135deg, #f8f9ff 0%, #f0f5ff 100%);
  overflow: hidden;
  margin-bottom: 20px;

  .card-header {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 14px 20px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;

    .card-icon {
      font-size: 18px;
    }

    .card-title {
      font-size: 15px;
      font-weight: 600;
    }
  }

  .card-body {
    padding: 16px 20px;
  }

  .param-row {
    display: flex;
    align-items: flex-start;
    gap: 16px;
    padding: 10px 0;
    border-bottom: 1px dashed #e0e0e0;

    &:last-child {
      border-bottom: none;
    }

    .param-label {
      flex-shrink: 0;
      width: 80px;
      font-size: 13px;
      color: #888;
      font-weight: 500;
      padding-top: 2px;
    }

    .param-value {
      flex: 1;
      font-size: 14px;
      color: #333;
      font-weight: 500;

      &.model {
        font-size: 16px;
        color: #1a1a1a;
        font-weight: 600;
      }
    }

    .param-tags {
      flex: 1;
      display: flex;
      flex-wrap: wrap;
      gap: 6px;

      .tag {
        display: inline-block;
        padding: 4px 10px;
        font-size: 12px;
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

  .pros-row, .cons-row {
    flex-direction: column;
    gap: 8px;

    .param-label {
      width: auto;
      font-size: 14px;
    }

    .pros-label {
      color: #52c41a;
    }

    .cons-label {
      color: #ff4d4f;
    }
  }

  .pros-list, .cons-list {
    margin: 0;
    padding-left: 20px;

    li {
      font-size: 13px;
      line-height: 1.8;
      color: #555;
    }
  }

  .pros-list li {
    color: #389e0d;
  }

  .cons-list li {
    color: #cf1322;
  }
}
</style>
