import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'
import './assets/main.scss'
import { useCompareStore } from '@/store/compare'
import { useUserStore } from '@/store/user'

const app = createApp(App)
const pinia = createPinia()
app.use(pinia)
app.use(router)
app.use(ElementPlus)

const compareStore = useCompareStore()
compareStore.init()

const userStore = useUserStore()
userStore.init()

app.mount('#app')
