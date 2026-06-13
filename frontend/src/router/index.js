import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue')
  },
  {
    path: '/post/:id',
    name: 'PostDetail',
    component: () => import('@/views/PostDetail.vue')
  },
  {
    path: '/create',
    name: 'CreatePost',
    component: () => import('@/views/CreatePost.vue')
  },
  {
    path: '/category/:id',
    name: 'Category',
    component: () => import('@/views/Category.vue')
  },
  {
    path: '/search',
    name: 'Search',
    component: () => import('@/views/SearchResult.vue')
  },
  {
    path: '/compare',
    name: 'Compare',
    component: () => import('@/views/ComparePanel.vue')
  },
  {
    path: '/fault-rank',
    name: 'FaultRank',
    component: () => import('@/views/FaultRank.vue')
  },
  {
    path: '/collocation-rank',
    name: 'CollocationRank',
    component: () => import('@/views/CollocationRank.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
