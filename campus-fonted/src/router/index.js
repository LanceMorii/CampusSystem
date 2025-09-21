import { createRouter, createWebHistory } from 'vue-router'
import Login from '../components/Login.vue'
import Register from '../components/Register.vue'
import Home from '../components/Home.vue'
import ProductList from '../components/ProductList.vue'
import ProductDetail from '../components/ProductDetail.vue'
import MessageCenter from '../components/MessageCenter.vue'
import ProductPublish from '../components/ProductPublish.vue'
import MyProducts from '../components/MyProducts.vue'
import OrderManagement from '../components/OrderManagement.vue'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/register',
    name: 'Register',
    component: Register
  },
  {
    path: '/home',
    name: 'Home',
    component: Home
  },
  {
    path: '/products',
    name: 'ProductList',
    component: ProductList
  },
  {
    path: '/product/:id',
    name: 'ProductDetail',
    component: ProductDetail
  },
  {
    path: '/messages',
    name: 'MessageCenter',
    component: MessageCenter
  },
  {
    path: '/publish',
    name: 'ProductPublish',
    component: ProductPublish
  },
  {
    path: '/my-products',
    name: 'MyProducts',
    component: MyProducts
  },
  {
    path: '/orders',
    name: 'OrderManagement',
    component: OrderManagement
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router