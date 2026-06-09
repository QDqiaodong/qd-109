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

      <div class="header-right">
        <router-link to="/create" class="btn-create">
          <span>✏️ 发布帖子</span>
        </router-link>
        <div class="user-area" v-if="userStore.userInfo">
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { getCategories } from '@/api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const showLogin = ref(false)
const activeTab = ref('login')
const categories = ref([])
const loginForm = ref({ username: '', password: '' })
const registerForm = ref({ username: '', nickname: '', password: '' })

onMounted(() => {
  userStore.init()
  loadCategories()
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
