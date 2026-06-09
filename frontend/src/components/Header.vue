<template>
  <header class="header">
    <div class="container header-inner">
      <router-link to="/" class="logo">
        <span class="logo-icon">🔧</span>
        <span class="logo-text">数码配件社区</span>
      </router-link>

      <nav class="nav">
        <router-link to="/" class="nav-item">首页</router-link>
        <router-link v-for="cat in categories" :key="cat.id" :to="`/category/${cat.id}`" class="nav-item">
          {{ cat.icon }} {{ cat.name }}
        </router-link>
      </nav>

      <div class="search-box" ref="searchBoxRef">
        <el-input
          v-model="searchKeyword"
          class="search-input"
          placeholder="搜索帖子、内容、作者..."
          clearable
          size="default"
          @input="handleSearchInput"
          @keyup.enter="handleSearchSubmit"
          @focus="handleSearchFocus"
          @clear="handleSearchClear"
        >
          <template #prefix>
            <span class="search-prefix-icon">🔍</span>
          </template>
        </el-input>

        <div class="search-dropdown" v-show="showDropdown && searchKeyword">
          <div class="dropdown-header" v-if="suggestions.length > 0">
            <span class="dropdown-title">🔍 相关帖子</span>
            <span class="view-all" @click="handleSearchSubmit">查看全部结果 →</span>
          </div>
          <div class="suggestion-list" v-if="suggestions.length > 0">
            <div
              v-for="post in suggestions"
              :key="post.id"
              class="suggestion-item"
              @click="handleSuggestionClick(post)"
              @mouseenter="activeIndex = suggestions.indexOf(post)"
              :class="{ active: activeIndex === suggestions.indexOf(post) }"
            >
              <div class="suggestion-thumb" v-if="postImages(post)[0]">
                <img :src="postImages(post)[0]" alt="" />
              </div>
              <div class="suggestion-thumb default-thumb" v-else>
                <span>📄</span>
              </div>
              <div class="suggestion-content">
                <div class="suggestion-title" v-html="highlightText(post.title, searchKeyword)"></div>
                <div class="suggestion-meta">
                  <span class="suggestion-author">{{ post.nickname }}</span>
                  <span class="suggestion-category">{{ post.categoryName }}</span>
                  <span :class="['suggestion-type', post.type === 1 ? 'type-experience' : 'type-question']">
                    {{ post.type === 1 ? '体验' : '求助' }}
                  </span>
                </div>
              </div>
            </div>
          </div>
          <div class="empty-suggestion" v-else-if="!loadingSuggestions">
            <span>暂无匹配结果</span>
          </div>
          <div class="loading-suggestion" v-else>
            <el-icon class="is-loading"><Loading /></el-icon>
            <span>搜索中...</span>
          </div>
        </div>
      </div>

      <div class="header-right">
        <router-link to="/create" class="btn-create">
          <span>✏️ 发布帖子</span>
        </router-link>
        <div class="user-area" v-if="userStore.isLoggedIn">
          <el-avatar :src="userStore.userInfo.avatar" :size="32" />
          <span class="nickname">{{ userStore.userInfo.nickname }}</span>
          <el-button type="text" @click="userStore.logout()">退出</el-button>
        </div>
        <el-button v-else type="primary" plain @click="showLogin = true">登录</el-button>
      </div>
    </div>

    <el-dialog v-model="showLogin" title="登录 / 注册" width="400px">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="登录" name="login">
          <el-form :model="loginForm" label-width="80px">
            <el-form-item label="用户名">
              <el-input v-model="loginForm.username" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="loginForm.password" type="password" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleLogin" style="width: 100%">登录</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="注册" name="register">
          <el-form :model="registerForm" label-width="80px">
            <el-form-item label="用户名">
              <el-input v-model="registerForm.username" />
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="registerForm.nickname" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="registerForm.password" type="password" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleRegister" style="width: 100%">注册</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </header>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { getCategories, getSearchSuggestions } from '@/api'
import { highlightKeyword } from '@/utils/highlight'
import { ElMessage, ElIcon } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const showLogin = ref(false)
const activeTab = ref('login')
const categories = ref([])
const loginForm = ref({ username: '', password: '' })
const registerForm = ref({ username: '', nickname: '', password: '' })

const searchKeyword = ref('')
const suggestions = ref([])
const showDropdown = ref(false)
const loadingSuggestions = ref(false)
const activeIndex = ref(-1)
const searchBoxRef = ref(null)
let searchTimer = null

const highlightText = (text, keyword) => {
  return highlightKeyword(text, keyword)
}

const postImages = (post) => {
  if (!post.images) return []
  return Array.isArray(post.images) ? post.images : post.images.split(',')
}

const handleSearchInput = () => {
  activeIndex.value = -1
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  if (!searchKeyword.value.trim()) {
    suggestions.value = []
    return
  }
  searchTimer = setTimeout(() => {
    fetchSuggestions()
  }, 300)
}

const fetchSuggestions = async () => {
  if (!searchKeyword.value.trim()) return
  loadingSuggestions.value = true
  try {
    suggestions.value = await getSearchSuggestions(searchKeyword.value.trim())
  } catch (e) {
    suggestions.value = []
  } finally {
    loadingSuggestions.value = false
  }
}

const handleSearchFocus = () => {
  showDropdown.value = true
  if (searchKeyword.value.trim() && suggestions.value.length === 0) {
    fetchSuggestions()
  }
}

const handleSearchClear = () => {
  suggestions.value = []
  showDropdown.value = false
}

const handleSearchSubmit = () => {
  if (!searchKeyword.value.trim()) return
  showDropdown.value = false
  router.push({ path: '/search', query: { q: searchKeyword.value.trim() } })
}

const handleSuggestionClick = (post) => {
  showDropdown.value = false
  router.push(`/post/${post.id}`)
}

const handleClickOutside = (e) => {
  if (searchBoxRef.value && !searchBoxRef.value.contains(e.target)) {
    showDropdown.value = false
  }
}

onMounted(() => {
  userStore.init()
  loadCategories()
  document.addEventListener('click', handleClickOutside)

  if (route.name === 'Search' && route.query.q) {
    searchKeyword.value = route.query.q
  }
})

watch(() => route.query.q, (newQ) => {
  if (route.name === 'Search' && newQ) {
    searchKeyword.value = newQ
  }
})

watch(() => route.name, (newName) => {
  if (newName !== 'Search') {
    showDropdown.value = false
  }
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
})

const loadCategories = async () => {
  categories.value = await getCategories()
}

const handleLogin = async () => {
  try {
    await userStore.doLogin(loginForm.value.username, loginForm.value.password)
    ElMessage.success('登录成功')
    showLogin.value = false
  } catch (e) {}
}

const handleRegister = async () => {
  try {
    await userStore.doRegister(registerForm.value.username, registerForm.value.password, registerForm.value.nickname)
    ElMessage.success('注册成功')
    showLogin.value = false
  } catch (e) {}
}
</script>

<style lang="scss" scoped>
.header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  position: sticky;
  top: 0;
  z-index: 100;

  .header-inner {
    display: flex;
    align-items: center;
    height: 64px;
  }

  .logo {
    display: flex;
    align-items: center;
    font-size: 20px;
    font-weight: 600;
    color: #1890ff;
    margin-right: 40px;

    .logo-icon {
      margin-right: 8px;
      font-size: 24px;
    }
  }

  .nav {
    flex: 1;
    display: flex;
    gap: 24px;

    .nav-item {
      color: #666;
      font-size: 14px;
      transition: color 0.2s;

      &:hover, &.router-link-active {
        color: #1890ff;
      }
    }
  }

  .search-box {
    position: relative;
    width: 320px;
    margin: 0 24px;

    .search-input {
      :deep(.el-input__wrapper) {
        border-radius: 20px;
        box-shadow: 0 0 0 1px #e0e0e0 inset;
        transition: all 0.2s;

        &:hover {
          box-shadow: 0 0 0 1px #1890ff inset;
        }

        &.is-focus {
          box-shadow: 0 0 0 1px #1890ff inset;
        }
      }

      :deep(.el-input__inner) {
        height: 36px;
        font-size: 14px;
      }

      .search-prefix-icon {
        font-size: 14px;
        color: #999;
      }
    }

    .search-dropdown {
      position: absolute;
      top: calc(100% + 8px);
      left: 0;
      right: 0;
      background: #fff;
      border-radius: 8px;
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
      z-index: 1000;
      overflow: hidden;

      .dropdown-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 12px 16px;
        background: #f5f7fa;
        border-bottom: 1px solid #eee;

        .dropdown-title {
          font-size: 13px;
          font-weight: 500;
          color: #333;
        }

        .view-all {
          font-size: 12px;
          color: #1890ff;
          cursor: pointer;
          transition: color 0.2s;

          &:hover {
            color: #096dd9;
          }
        }
      }

      .suggestion-list {
        max-height: 400px;
        overflow-y: auto;
      }

      .suggestion-item {
        display: flex;
        gap: 12px;
        padding: 12px 16px;
        cursor: pointer;
        transition: background 0.2s;
        border-bottom: 1px solid #f0f0f0;

        &:last-child {
          border-bottom: none;
        }

        &:hover, &.active {
          background: #f0f7ff;
        }

        .suggestion-thumb {
          width: 56px;
          height: 56px;
          border-radius: 6px;
          overflow: hidden;
          flex-shrink: 0;
          background: #f5f7fa;

          img {
            width: 100%;
            height: 100%;
            object-fit: cover;
          }

          &.default-thumb {
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 24px;
          }
        }

        .suggestion-content {
          flex: 1;
          min-width: 0;

          .suggestion-title {
            font-size: 14px;
            font-weight: 500;
            color: #333;
            margin-bottom: 6px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            line-height: 1.4;

            :deep(.highlight) {
              color: #1890ff;
              font-style: normal;
              background: #e6f7ff;
              padding: 0 2px;
              border-radius: 2px;
              font-weight: 600;
            }
          }

          .suggestion-meta {
            display: flex;
            align-items: center;
            gap: 10px;
            font-size: 12px;
            color: #999;

            .suggestion-author {
              color: #666;
            }

            .suggestion-category {
              color: #1890ff;
            }

            .suggestion-type {
              padding: 1px 6px;
              border-radius: 3px;
              font-size: 11px;

              &.type-experience {
                background: #e6f7ff;
                color: #1890ff;
              }

              &.type-question {
                background: #fff7e6;
                color: #fa8c16;
              }
            }
          }
        }
      }

      .empty-suggestion,
      .loading-suggestion {
        padding: 32px 16px;
        text-align: center;
        color: #999;
        font-size: 14px;

        .is-loading {
          margin-right: 8px;
        }
      }
    }
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 16px;

    .btn-create {
      background: linear-gradient(135deg, #1890ff, #096dd9);
      color: #fff;
      padding: 8px 20px;
      border-radius: 20px;
      font-size: 14px;
      transition: all 0.2s;

      &:hover {
        transform: translateY(-1px);
        box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
      }
    }

    .user-area {
      display: flex;
      align-items: center;
      gap: 8px;

      .nickname {
        font-size: 14px;
        color: #333;
      }
    }
  }
}
</style>
