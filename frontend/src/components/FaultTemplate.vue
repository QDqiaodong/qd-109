<template>
  <div class="fault-template">
    <div class="template-header">
      <span class="header-title">📋 故障现象模板</span>
      <el-radio-group v-model="mode" size="small">
        <el-radio-button :label="'form'">表单填写</el-radio-button>
        <el-radio-button :label="'preview'">预览效果</el-radio-button>
      </el-radio-group>
    </div>

    <div class="template-body" v-if="mode === 'form'">
      <el-form :model="formData" label-width="100px" size="default">
        <el-form-item label="设备型号" :required="isRequired('deviceModel')">
          <el-input
            v-model="formData.deviceModel"
            placeholder="如：iPhone 15 Pro、MacBook Pro 14寸 M3"
            maxlength="100"
            clearable
          />
        </el-form-item>

        <el-form-item label="配件型号" :required="isRequired('accessoryModel')">
          <el-input
            v-model="formData.accessoryModel"
            placeholder="如：AirPods Pro 2、罗技MX Master 3S"
            maxlength="100"
            clearable
          />
        </el-form-item>

        <el-form-item v-if="formData.deviceModel || formData.accessoryModel">
          <div style="width: 100%; margin-left: 0;">
            <ModelFaultInsight
              :model="formData.deviceModel || formData.accessoryModel"
              :category-id="categoryId"
            />
          </div>
        </el-form-item>

        <el-form-item label="连接方式" :required="isRequired('connectionType')" v-if="isFieldVisible('connectionType')">
          <el-select
            v-model="formData.connectionType"
            placeholder="请选择连接方式"
            style="width: 100%"
            clearable
            filterable
            allow-create
          >
            <el-option v-for="item in connectionOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>

        <el-form-item label="使用平台" :required="isRequired('platform')" v-if="isFieldVisible('platform')">
          <el-select
            v-model="formData.platform"
            placeholder="请选择使用平台"
            style="width: 100%"
            clearable
            filterable
            allow-create
          >
            <el-option v-for="item in platformOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>

        <el-form-item label="读写环境" :required="isRequired('environment')" v-if="isFieldVisible('environment')">
          <el-select
            v-model="formData.environment"
            placeholder="请选择读写环境"
            style="width: 100%"
            clearable
            filterable
            allow-create
          >
            <el-option v-for="item in environmentOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>

        <el-form-item label="出现症状" :required="isRequired('symptoms')">
          <el-input
            v-model="formData.symptoms"
            type="textarea"
            :rows="4"
            placeholder="请详细描述遇到的问题现象，例如：连接后没有声音、频繁断连、充电异常等"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="已尝试动作" :required="isRequired('triedActions')">
          <el-input
            v-model="formData.triedActions"
            type="textarea"
            :rows="4"
            placeholder="请列出已经尝试过的解决方法，例如：重启设备、重新配对、更换数据线等"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <div class="template-tip" v-if="requiredFields.length > 0">
        <el-icon><InfoFilled /></el-icon>
        <span>当前分类要求必填：{{ requiredFieldLabels }}，填写越完整，他人越容易帮你定位问题</span>
      </div>
      <div class="template-tip" v-else>
        <el-icon><InfoFilled /></el-icon>
        <span>填写完成后，系统会自动整理成清晰的提问格式，方便他人快速理解问题背景</span>
      </div>
    </div>

    <div class="template-preview" v-else>
      <div class="preview-title">📝 结构化提问预览</div>
      <div class="preview-content" v-html="generatedContentHtml || emptyPreviewHtml"></div>
      <div class="preview-actions">
        <el-button type="primary" size="small" @click="insertToContent" :disabled="!hasContent">
          应用到帖子内容
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { InfoFilled } from '@element-plus/icons-vue'
import ModelFaultInsight from './ModelFaultInsight.vue'

const FIELD_LABEL_MAP = {
  deviceModel: '设备型号',
  accessoryModel: '配件型号',
  connectionType: '连接方式',
  platform: '使用平台',
  environment: '读写环境',
  symptoms: '出现症状',
  triedActions: '已尝试动作'
}

const CATEGORY_SPECIFIC_FIELDS = {
  platform: [8],
  environment: [8]
}

const props = defineProps({
  modelValue: {
    type: Object,
    default: () => ({})
  },
  requiredFields: {
    type: Array,
    default: () => []
  },
  categoryId: {
    type: Number,
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'apply'])

const mode = ref('form')

const connectionOptions = [
  '蓝牙', 'Wi-Fi', '有线（Type-C）', '有线（Lightning）', '有线（USB-A）',
  'HDMI', '3.5mm音频', '磁吸', 'AirDrop', '其他'
]

const platformOptions = [
  'Windows 11', 'Windows 10', 'macOS Sonoma', 'macOS Ventura',
  'iOS 17', 'iOS 16', 'Android 14', 'Android 13',
  'Linux', 'iPadOS', '其他'
]

const environmentOptions = [
  'USB 3.0', 'USB 3.1', 'USB 3.2', 'USB-C', 'Thunderbolt 3',
  'Thunderbolt 4', 'PCIe 3.0', 'PCIe 4.0', 'PCIe 5.0',
  'NVMe', 'SATA III', 'SD卡槽', '其他'
]

const createEmptyForm = () => ({
  deviceModel: '',
  accessoryModel: '',
  connectionType: '',
  platform: '',
  environment: '',
  symptoms: '',
  triedActions: ''
})

const formData = reactive(createEmptyForm())

const isRequired = (field) => {
  return props.requiredFields.includes(field)
}

const isFieldVisible = (field) => {
  const categoryIds = CATEGORY_SPECIFIC_FIELDS[field]
  if (!categoryIds) return true
  if (props.categoryId == null) return true
  return categoryIds.includes(props.categoryId) || props.requiredFields.includes(field)
}

const requiredFieldLabels = computed(() => {
  return props.requiredFields
    .map(f => FIELD_LABEL_MAP[f] || f)
    .join('、')
})

const hasContent = computed(() => {
  return formData.deviceModel || formData.accessoryModel ||
         formData.connectionType || formData.platform ||
         formData.environment || formData.symptoms ||
         formData.triedActions
})

const generatedContent = computed(() => {
  const lines = []

  if (formData.deviceModel) {
    lines.push(`📱 **设备型号**：${formData.deviceModel}`)
  }
  if (formData.accessoryModel) {
    lines.push(`🔧 **配件型号**：${formData.accessoryModel}`)
  }
  if (formData.connectionType) {
    lines.push(`🔌 **连接方式**：${formData.connectionType}`)
  }
  if (formData.platform) {
    lines.push(`💻 **使用平台**：${formData.platform}`)
  }
  if (formData.environment) {
    lines.push(`💾 **读写环境**：${formData.environment}`)
  }
  if (formData.symptoms) {
    lines.push('')
    lines.push(`❓ **出现症状**`)
    lines.push('')
    lines.push(formData.symptoms)
  }
  if (formData.triedActions) {
    lines.push('')
    lines.push(`🔄 **已尝试动作**`)
    lines.push('')
    lines.push(formData.triedActions)
  }

  return lines.join('\n')
})

const generatedContentHtml = computed(() => {
  if (!hasContent.value) return ''

  const htmlLines = []

  if (formData.deviceModel) {
    htmlLines.push(`<p><strong>📱 设备型号：</strong>${escapeHtml(formData.deviceModel)}</p>`)
  }
  if (formData.accessoryModel) {
    htmlLines.push(`<p><strong>🔧 配件型号：</strong>${escapeHtml(formData.accessoryModel)}</p>`)
  }
  if (formData.connectionType) {
    htmlLines.push(`<p><strong>🔌 连接方式：</strong>${escapeHtml(formData.connectionType)}</p>`)
  }
  if (formData.platform) {
    htmlLines.push(`<p><strong>💻 使用平台：</strong>${escapeHtml(formData.platform)}</p>`)
  }
  if (formData.environment) {
    htmlLines.push(`<p><strong>💾 读写环境：</strong>${escapeHtml(formData.environment)}</p>`)
  }
  if (formData.symptoms) {
    htmlLines.push(`<p style="margin-top: 12px;"><strong>❓ 出现症状</strong></p>`)
    htmlLines.push(`<p style="white-space: pre-wrap; color: #333;">${escapeHtml(formData.symptoms)}</p>`)
  }
  if (formData.triedActions) {
    htmlLines.push(`<p style="margin-top: 12px;"><strong>🔄 已尝试动作</strong></p>`)
    htmlLines.push(`<p style="white-space: pre-wrap; color: #333;">${escapeHtml(formData.triedActions)}</p>`)
  }

  return htmlLines.join('')
})

const emptyPreviewHtml = computed(() => {
  return `<p style="color: #999; text-align: center; padding: 40px 0;">请先在表单中填写故障信息</p>`
})

const escapeHtml = (text) => {
  const div = document.createElement('div')
  div.textContent = text
  return div.innerHTML
}

const validate = () => {
  const missing = []
  for (const field of props.requiredFields) {
    const value = formData[field]
    if (!value || !value.trim()) {
      missing.push(FIELD_LABEL_MAP[field] || field)
    }
  }
  return missing
}

const insertToContent = () => {
  emit('apply', generatedContent.value)
}

watch(
  () => props.modelValue,
  (val) => {
    if (val && typeof val === 'object') {
      Object.assign(formData, createEmptyForm(), val)
    } else {
      Object.assign(formData, createEmptyForm())
    }
  },
  { immediate: true, deep: true }
)

watch(
  formData,
  (val) => {
    emit('update:modelValue', { ...val })
  },
  { deep: true }
)

defineExpose({ validate, formData })
</script>

<style lang="scss" scoped>
.fault-template {
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  background: #fafafa;
  overflow: hidden;

  .template-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;
    background: #f0f0f0;
    border-bottom: 1px solid #e8e8e8;

    .header-title {
      font-size: 15px;
      font-weight: 600;
      color: #333;
    }
  }

  .template-body {
    padding: 20px 16px;
  }

  .template-tip {
    display: flex;
    align-items: flex-start;
    gap: 8px;
    padding: 12px;
    margin-top: 16px;
    background: #ecf5ff;
    border-radius: 6px;
    font-size: 13px;
    color: #409eff;
    line-height: 1.5;

    .el-icon {
      margin-top: 2px;
      flex-shrink: 0;
    }
  }

  .template-preview {
    padding: 20px 16px;

    .preview-title {
      font-size: 14px;
      font-weight: 600;
      color: #333;
      margin-bottom: 16px;
      padding-bottom: 10px;
      border-bottom: 1px dashed #e0e0e0;
    }

    .preview-content {
      background: #fff;
      border: 1px solid #e8e8e8;
      border-radius: 6px;
      padding: 16px;
      font-size: 14px;
      line-height: 1.8;
      color: #333;
      min-height: 120px;

      :deep(p) {
        margin: 4px 0;
      }

      :deep(strong) {
        color: #333;
      }
    }

    .preview-actions {
      margin-top: 16px;
      text-align: right;
    }
  }
}
</style>
