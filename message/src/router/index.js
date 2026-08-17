import Vue from 'vue'
import VueRouter from 'vue-router'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    name: 'MessageList',
    component: () => import('../views/MessageList.vue')
  },
  {
    path: '/detail/:id',
    name: 'MessageDetail',
    component: () => import('../views/MessageDetail.vue')
  }
]

const router = new VueRouter({
  mode: 'hash',
  base: '/message/',
  routes
})

export default router
