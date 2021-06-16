import Vue from 'vue'
import Router from 'vue-router'
import room from '@/view/room'
import index from '@/view/index'

Vue.use(Router)

export default new Router({
  mode: 'history',
  base: process.env.BASE_URL,
  routes: [{
      path: '/',
      name: 'index',
      component: index
    },
    {
      path: '/room',
      name: 'room',
      component: room
    }
  ]
})
