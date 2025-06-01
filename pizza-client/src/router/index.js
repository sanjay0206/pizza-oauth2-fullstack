import { createRouter, createWebHistory } from "vue-router";
import LoginView from "../views/LoginView.vue";
import PizzasView from "../views/PizzasView.vue";
import CallbackView from "../views/CallbackView.vue";

const routes = [
  { path: "/", component: LoginView },
  { path: "/pizzas", component: PizzasView, meta: { requiresAuth: true } },
  { path: "/callback", component: CallbackView },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem("token");
  if (to.meta.requiresAuth && !token) {
    next("/");
  } else {
    next();
  }
});

export default router;
