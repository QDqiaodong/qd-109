<template>
  <transition name="viewer-fade">
    <div v-if="visible" class="before-after-viewer" @keydown.esc.prevent="close" tabindex="-1">
      <div class="viewer-overlay" @click="close"></div>

      <div class="viewer-container">
        <div class="viewer-topbar">
          <div class="topbar-left">
            <span class="viewer-icon">🔍</span>
            <span class="viewer-title">前后对照</span>
          </div>

          <div class="topbar-center">
            <div class="group-selector">
              <div class="selector-group left-group">
                <span class="selector-label">前</span>
                <el-select
                  v-model="leftGroupKey"
                  size="small"
                  class="group-select"
                  @change="handleGroupChange"
                >
                  <el-option
                    v-for="g in availableGroups"
                    :key="g.key"
                    :label="g.icon + ' ' + g.label"
                    :value="g.key"
                    :disabled="g.key === rightGroupKey"
                  />
                </el-select>
              </div>

              <div class="selector-divider">
                <span class="divider-icon">⇄</span>
              </div>

              <div class="selector-group right-group">
                <span class="selector-label">后</span>
                <el-select
                  v-model="rightGroupKey"
                  size="small"
                  class="group-select"
                  @change="handleGroupChange"
                >
                  <el-option
                    v-for="g in availableGroups"
                    :key="g.key"
                    :label="g.icon + ' ' + g.label"
                    :value="g.key"
                    :disabled="g.key === leftGroupKey"
                  />
                </el-select>
              </div>
            </div>
          </div>

          <div class="topbar-right">
            <div class="mode-toggle">
              <button
                :class="['mode-btn', { active: viewMode === 'slider' }]"
                @click="viewMode = 'slider'"
                title="滑块对照"
              >
                <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="3" y="3" width="18" height="18" rx="2" />
                  <line x1="12" y1="3" x2="12" y2="21" />
                  <polyline points="9 10 12 7 15 10" />
                  <polyline points="9 14 12 17 15 14" />
                </svg>
                <span>滑块</span>
              </button>
              <button
                :class="['mode-btn', { active: viewMode === 'side' }]"
                @click="viewMode = 'side'"
                title="并排对照"
              >
                <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="2" y="3" width="8" height="18" rx="2" />
                  <rect x="14" y="3" width="8" height="18" rx="2" />
                </svg>
                <span>并排</span>
              </button>
            </div>

            <button class="close-btn" @click="close" title="关闭">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="6" x2="6" y2="18" />
                <line x1="6" y1="6" x2="18" y2="18" />
              </svg>
            </button>
          </div>
        </div>

        <div class="viewer-body">
          <div v-if="viewMode === 'slider'" class="slider-view">
            <div class="slider-container" ref="sliderContainerRef"
              @mousemove="handleSliderMove"
              @mousedown="handleSliderDown"
              @mouseup="handleSliderUp"
              @mouseleave="handleSliderUp"
            >
              <div class="slider-img-wrap slider-right-wrap">
                <img
                  :src="currentRightImage"
                  :alt="rightGroup?.label || '后'"
                  class="slider-img"
                  @load="handleImageLoad('right', $event)"
                />
                <div class="slider-caption right-caption">
                  <span class="caption-icon">{{ rightGroup?.icon }}</span>
                  <span class="caption-label">{{ rightGroup?.label }}</span>
                  <span class="caption-desc" v-if="rightGroupMeta?.desc">{{ rightGroupMeta.desc }}</span>
                </div>
              </div>

              <div class="slider-img-wrap slider-left-wrap" :style="{ clipPath: clipPathStyle }">
                <img
                  :src="currentLeftImage"
                  :alt="leftGroup?.label || '前'"
                  class="slider-img"
                  @load="handleImageLoad('left', $event)"
                />
                <div class="slider-caption left-caption">
                  <span class="caption-icon">{{ leftGroup?.icon }}</span>
                  <span class="caption-label">{{ leftGroup?.label }}</span>
                  <span class="caption-desc" v-if="leftGroupMeta?.desc">{{ leftGroupMeta.desc }}</span>
                </div>
              </div>

              <div class="slider-handle" :style="{ left: sliderPos + '%' }">
                <div class="handle-line"></div>
                <div class="handle-dot">
                  <svg viewBox="0 0 24 24" width="20" height="20" fill="white" stroke="#333" stroke-width="1.5">
                    <polyline points="15 18 9 12 15 6" />
                    <polyline points="9 6 15 12 9 18" />
                  </svg>
                </div>
                <div class="handle-line"></div>
              </div>

              <div class="slider-label slider-label-left">前</div>
              <div class="slider-label slider-label-right">后</div>
            </div>
          </div>

          <div v-else class="side-view">
            <div class="side-panel left-panel">
              <div class="side-img-wrap">
                <img
                  :src="currentLeftImage"
                  :alt="leftGroup?.label || '前'"
                  class="side-img"
                />
              </div>
              <div class="side-info">
                <div class="side-header" :style="{ borderLeftColor: leftGroupMeta?.color || '#722ed1' }">
                  <span class="side-icon">{{ leftGroup?.icon }}</span>
                  <div class="side-title-area">
                    <h3 class="side-name" :style="{ color: leftGroupMeta?.color || '#722ed1' }">{{ leftGroup?.label }}</h3>
                    <p class="side-desc" v-if="leftGroupMeta?.desc">{{ leftGroupMeta.desc }}</p>
                  </div>
                </div>
              </div>
            </div>

            <div class="side-divider">
              <div class="divider-line"></div>
              <div class="divider-badge">VS</div>
              <div class="divider-line"></div>
            </div>

            <div class="side-panel right-panel">
              <div class="side-img-wrap">
                <img
                  :src="currentRightImage"
                  :alt="rightGroup?.label || '后'"
                  class="side-img"
                />
              </div>
              <div class="side-info">
                <div class="side-header" :style="{ borderLeftColor: rightGroupMeta?.color || '#1890ff' }">
                  <span class="side-icon">{{ rightGroup?.icon }}</span>
                  <div class="side-title-area">
                    <h3 class="side-name" :style="{ color: rightGroupMeta?.color || '#1890ff' }">{{ rightGroup?.label }}</h3>
                    <p class="side-desc" v-if="rightGroupMeta?.desc">{{ rightGroupMeta.desc }}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="viewer-footer">
          <button class="nav-btn prev-btn" @click="prevPair" :disabled="!canGoPrev">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="15 18 9 12 15 6" />
            </svg>
            <span>上一组</span>
          </button>

          <div class="pair-indicator">
            <span class="pair-current">{{ currentPairIndex + 1 }}</span>
            <span class="pair-sep">/</span>
            <span class="pair-total">{{ totalPairs }}</span>
            <span class="pair-label">组对照</span>
          </div>

          <div class="image-counter">
            <span class="counter-left">
              {{ leftGroup?.icon }} {{ leftGroup?.label }} · 第 {{ leftImageIndex + 1 }}/{{ leftGroup?.images?.length || 0 }} 张
            </span>
            <span class="counter-sep">|</span>
            <span class="counter-right">
              {{ rightGroup?.icon }} {{ rightGroup?.label }} · 第 {{ rightImageIndex + 1 }}/{{ rightGroup?.images?.length || 0 }} 张
            </span>
          </div>

          <button class="nav-btn next-btn" @click="nextPair" :disabled="!canGoNext">
            <span>下一组</span>
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="9 6 15 12 9 18" />
            </svg>
          </button>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { ref, computed, watch, nextTick, onMounted, onUnmounted } from 'vue'

const GROUP_META = {
  appearance: { icon: '🎨', color: '#722ed1', desc: '产品整体外观、设计语言、做工细节' },
  interface:  { icon: '🔌', color: '#13c2c2', desc: '接口类型、线材、按键、连接方式' },
  usage:      { icon: '💻', color: '#1890ff', desc: '装在设备上的实际效果、使用场景' },
  desk:       { icon: '🖼️', color: '#fa8c16', desc: '整体桌面环境、氛围灯光、搭配组合' }
}

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  imageGroups: {
    type: Array,
    default: () => []
  },
  defaultLeftKey: {
    type: String,
    default: ''
  },
  defaultRightKey: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:visible', 'close'])

const viewMode = ref('slider')
const leftGroupKey = ref('')
const rightGroupKey = ref('')
const leftImageIndex = ref(0)
const rightImageIndex = ref(0)
const sliderPos = ref(50)
const isDragging = ref(false)
const sliderContainerRef = ref(null)

const availableGroups = computed(() => {
  if (!props.imageGroups || props.imageGroups.length === 0) return []
  return props.imageGroups.filter(g => g.images && g.images.length > 0)
})

const leftGroup = computed(() => {
  return availableGroups.value.find(g => g.key === leftGroupKey.value) || null
})

const rightGroup = computed(() => {
  return availableGroups.value.find(g => g.key === rightGroupKey.value) || null
})

const leftGroupMeta = computed(() => {
  if (!leftGroupKey.value) return null
  return GROUP_META[leftGroupKey.value] || null
})

const rightGroupMeta = computed(() => {
  if (!rightGroupKey.value) return null
  return GROUP_META[rightGroupKey.value] || null
})

const currentLeftImage = computed(() => {
  if (!leftGroup.value || !leftGroup.value.images?.length) return ''
  return leftGroup.value.images[leftImageIndex.value] || ''
})

const currentRightImage = computed(() => {
  if (!rightGroup.value || !rightGroup.value.images?.length) return ''
  return rightGroup.value.images[rightImageIndex.value] || ''
})

const totalPairs = computed(() => {
  const leftLen = leftGroup.value?.images?.length || 0
  const rightLen = rightGroup.value?.images?.length || 0
  return Math.max(leftLen, rightLen)
})

const currentPairIndex = computed(() => {
  return Math.min(leftImageIndex.value, rightImageIndex.value)
})

const canGoPrev = computed(() => {
  return leftImageIndex.value > 0 || rightImageIndex.value > 0
})

const canGoNext = computed(() => {
  const leftLen = leftGroup.value?.images?.length || 0
  const rightLen = rightGroup.value?.images?.length || 0
  return leftImageIndex.value < leftLen - 1 || rightImageIndex.value < rightLen - 1
})

const clipPathStyle = computed(() => {
  return `inset(0 ${100 - sliderPos.value}% 0 0)`
})

const handleGroupChange = () => {
  leftImageIndex.value = 0
  rightImageIndex.value = 0
  sliderPos.value = 50
}

const prevPair = () => {
  if (leftImageIndex.value > 0) leftImageIndex.value--
  if (rightImageIndex.value > 0) rightImageIndex.value--
}

const nextPair = () => {
  const leftLen = leftGroup.value?.images?.length || 0
  const rightLen = rightGroup.value?.images?.length || 0
  if (leftImageIndex.value < leftLen - 1) leftImageIndex.value++
  if (rightImageIndex.value < rightLen - 1) rightImageIndex.value++
}

const handleSliderDown = (e) => {
  isDragging.value = true
  updateSliderPos(e)
}

const handleSliderMove = (e) => {
  if (!isDragging.value) return
  updateSliderPos(e)
}

const handleSliderUp = () => {
  isDragging.value = false
}

const updateSliderPos = (e) => {
  if (!sliderContainerRef.value) return
  const rect = sliderContainerRef.value.getBoundingClientRect()
  const x = e.clientX - rect.left
  const percent = Math.max(0, Math.min(100, (x / rect.width) * 100))
  sliderPos.value = percent
}

const handleImageLoad = (side, e) => {
  //
}

const close = () => {
  emit('update:visible', false)
  emit('close')
}

const handleKeydown = (e) => {
  if (!props.visible) return
  if (e.key === 'Escape') {
    close()
  } else if (e.key === 'ArrowLeft') {
    prevPair()
  } else if (e.key === 'ArrowRight') {
    nextPair()
  }
}

watch(() => props.visible, (val) => {
  if (val) {
    if (props.defaultLeftKey && !leftGroupKey.value) {
      leftGroupKey.value = props.defaultLeftKey
    }
    if (props.defaultRightKey && !rightGroupKey.value) {
      rightGroupKey.value = props.defaultRightKey
    }

    if (!leftGroupKey.value && availableGroups.value.length >= 1) {
      leftGroupKey.value = availableGroups.value[0].key
    }
    if (!rightGroupKey.value && availableGroups.value.length >= 2) {
      rightGroupKey.value = availableGroups.value[1].key
    } else if (!rightGroupKey.value && availableGroups.value.length === 1) {
      rightGroupKey.value = availableGroups.value[0].key
    }

    leftImageIndex.value = 0
    rightImageIndex.value = 0
    sliderPos.value = 50
    document.body.style.overflow = 'hidden'
  } else {
    document.body.style.overflow = ''
  }
})

onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
  document.body.style.overflow = ''
})
</script>

<style lang="scss" scoped>
.before-after-viewer {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 2000;
  display: flex;
  flex-direction: column;
}

.viewer-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.75);
  backdrop-filter: blur(4px);
}

.viewer-container {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #1a1a1a;
}

.viewer-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 24px;
  background: linear-gradient(180deg, #2a2a2a 0%, #1f1f1f 100%);
  border-bottom: 1px solid #333;
  flex-shrink: 0;
}

.topbar-left {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;

  .viewer-icon {
    font-size: 22px;
  }

  .viewer-title {
    font-size: 16px;
    font-weight: 600;
    color: #fff;
    letter-spacing: 0.5px;
  }
}

.topbar-center {
  flex: 1;
  display: flex;
  justify-content: center;
  padding: 0 32px;
}

.group-selector {
  display: flex;
  align-items: center;
  gap: 16px;
  background: rgba(255, 255, 255, 0.06);
  border-radius: 12px;
  padding: 8px 20px;
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.selector-group {
  display: flex;
  align-items: center;
  gap: 8px;

  .selector-label {
    font-size: 13px;
    font-weight: 700;
    color: #fff;
    padding: 2px 10px;
    border-radius: 4px;
    letter-spacing: 1px;
  }

  &.left-group .selector-label {
    background: linear-gradient(135deg, #722ed1, #9254de);
  }

  &.right-group .selector-label {
    background: linear-gradient(135deg, #1890ff, #40a9ff);
  }
}

.group-select {
  width: 160px;

  :deep(.el-input__wrapper) {
    background: rgba(255, 255, 255, 0.08);
    border: 1px solid rgba(255, 255, 255, 0.12);
    box-shadow: none;

    &:hover {
      border-color: rgba(255, 255, 255, 0.25);
    }
  }

  :deep(.el-input__inner) {
    color: #e0e0e0;
    font-size: 13px;
  }

  :deep(.el-input__suffix) {
    color: #999;
  }
}

.selector-divider {
  .divider-icon {
    font-size: 20px;
    color: rgba(255, 255, 255, 0.4);
  }
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.mode-toggle {
  display: flex;
  gap: 4px;
  background: rgba(255, 255, 255, 0.06);
  border-radius: 8px;
  padding: 3px;
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.mode-btn {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 6px 12px;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: #999;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;

  svg {
    width: 16px;
    height: 16px;
  }

  &:hover {
    color: #ccc;
    background: rgba(255, 255, 255, 0.06);
  }

  &.active {
    background: rgba(64, 158, 255, 0.2);
    color: #409eff;
  }
}

.close-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.06);
  color: #999;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: rgba(255, 77, 79, 0.2);
    color: #ff4d4f;
  }
}

.viewer-body {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  padding: 20px;
}

.slider-view {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.slider-container {
  position: relative;
  width: 100%;
  max-width: 1000px;
  aspect-ratio: 16 / 10;
  max-height: 100%;
  border-radius: 12px;
  overflow: hidden;
  cursor: ew-resize;
  box-shadow: 0 12px 48px rgba(0, 0, 0, 0.5);
}

.slider-img-wrap {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;

  &.slider-left-wrap {
    z-index: 2;
  }

  &.slider-right-wrap {
    z-index: 1;
  }
}

.slider-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  display: block;
  background: #111;
}

.slider-caption {
  position: absolute;
  bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border-radius: 8px;
  backdrop-filter: blur(12px);
  font-size: 13px;
  font-weight: 500;
  z-index: 3;

  &.left-caption {
    left: 16px;
    background: rgba(114, 46, 209, 0.75);
    color: #fff;
  }

  &.right-caption {
    right: 16px;
    background: rgba(24, 144, 255, 0.75);
    color: #fff;
  }

  .caption-icon {
    font-size: 16px;
  }

  .caption-label {
    font-weight: 600;
  }

  .caption-desc {
    font-size: 11px;
    opacity: 0.85;
    margin-left: 4px;
    padding-left: 8px;
    border-left: 1px solid rgba(255, 255, 255, 0.3);
  }
}

.slider-handle {
  position: absolute;
  top: 0;
  bottom: 0;
  z-index: 10;
  display: flex;
  flex-direction: column;
  align-items: center;
  transform: translateX(-50%);
  pointer-events: none;
}

.handle-line {
  flex: 1;
  width: 2px;
  background: rgba(255, 255, 255, 0.8);
}

.handle-dot {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.4);
  flex-shrink: 0;
  pointer-events: auto;
  cursor: ew-resize;

  svg {
    width: 20px;
    height: 20px;
  }
}

.slider-label {
  position: absolute;
  top: 16px;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 2px;
  z-index: 3;
  pointer-events: none;

  &.slider-label-left {
    left: 16px;
    background: rgba(114, 46, 209, 0.8);
    color: #fff;
  }

  &.slider-label-right {
    right: 16px;
    background: rgba(24, 144, 255, 0.8);
    color: #fff;
  }
}

.side-view {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: stretch;
  gap: 0;
}

.side-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  background: #222;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4);
}

.side-img-wrap {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 0;
  padding: 16px;
  background: #1a1a1a;
}

.side-img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  border-radius: 6px;
}

.side-info {
  flex-shrink: 0;
  padding: 16px 20px;
  background: linear-gradient(180deg, #262626 0%, #222 100%);
  border-top: 1px solid #333;
}

.side-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-left: 12px;
  border-left: 4px solid;

  .side-icon {
    font-size: 22px;
  }

  .side-title-area {
    flex: 1;
    min-width: 0;

    .side-name {
      font-size: 16px;
      font-weight: 700;
      margin: 0 0 2px 0;
      line-height: 1.3;
    }

    .side-desc {
      font-size: 12px;
      color: #999;
      margin: 0;
      line-height: 1.4;
    }
  }
}

.side-divider {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 0 16px;
  flex-shrink: 0;

  .divider-line {
    width: 1px;
    height: 60px;
    background: linear-gradient(180deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  }

  .divider-badge {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    background: linear-gradient(135deg, #722ed1, #1890ff);
    color: #fff;
    font-size: 13px;
    font-weight: 700;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.3);
    letter-spacing: 1px;
  }
}

.viewer-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 24px;
  background: linear-gradient(180deg, #1f1f1f 0%, #2a2a2a 100%);
  border-top: 1px solid #333;
  flex-shrink: 0;
}

.nav-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 18px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.06);
  color: #ccc;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;

  &:hover:not(:disabled) {
    background: rgba(255, 255, 255, 0.12);
    color: #fff;
    border-color: rgba(255, 255, 255, 0.2);
  }

  &:disabled {
    opacity: 0.3;
    cursor: not-allowed;
  }

  svg {
    width: 16px;
    height: 16px;
  }
}

.pair-indicator {
  display: flex;
  align-items: baseline;
  gap: 4px;

  .pair-current {
    font-size: 24px;
    font-weight: 700;
    color: #409eff;
  }

  .pair-sep {
    font-size: 16px;
    color: #555;
    margin: 0 2px;
  }

  .pair-total {
    font-size: 16px;
    font-weight: 600;
    color: #999;
  }

  .pair-label {
    font-size: 12px;
    color: #666;
    margin-left: 6px;
  }
}

.image-counter {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: #777;

  .counter-sep {
    color: #444;
  }
}

.viewer-fade-enter-active {
  transition: opacity 0.3s ease;

  .viewer-container {
    transition: transform 0.3s ease;
    transform: translateY(0);
  }
}

.viewer-fade-enter-from {
  opacity: 0;

  .viewer-container {
    transform: translateY(20px);
  }
}

.viewer-fade-leave-active {
  transition: opacity 0.2s ease;
}

.viewer-fade-leave-to {
  opacity: 0;
}
</style>
