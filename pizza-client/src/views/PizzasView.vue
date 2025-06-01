<script setup>
import { ref, onMounted } from "vue";
import { useAuth } from "../composables/useAuth";
import axios from "axios";

const { logout } = useAuth();

const pizzas = ref([]);
const userInfo = ref(null);
const loading = ref(false);
const error = ref(null);

const fetchPizzas = async () => {
  try {
    loading.value = true;
    error.value = null;

    const response = await axios.get("http://localhost:8080/api/pizzas");
    pizzas.value = response.data;

    console.log(pizzas.value);
  } catch (err) {
    error.value = err.response?.data?.message || err.message;
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchPizzas();
  fetchUserInfo();
});

const fetchUserInfo = async () => {
  try {
    const response = await axios.get("http://localhost:9000/userinfo");

    console.log("userInfo: ", response.data);
  } catch (error) {
    console.error("Failed to fetch user info:", error);
    return null;
  }
};
</script>

<template>
  <div class="pizzas">
    <h1>Our Pizzas</h1>
    <button @click="logout" class="logout-btn">Logout</button>

    <div v-if="loading" class="loading-msg">Loading pizzas...</div>
    <div v-else-if="error" class="error-msg">{{ error }}</div>
    <ul v-else class="pizza-list">
      <li v-for="pizza in pizzas" :key="pizza.id">
        {{ pizza.name }}
      </li>
    </ul>
  </div>
</template>

<style scoped>
.loading-msg {
  padding-top: 20px;
}

.error-msg {
  color: red;
  padding-top: 20px;
}

.pizza-list {
  list-style-type: disc;
  padding-left: 20px;
}
</style>
