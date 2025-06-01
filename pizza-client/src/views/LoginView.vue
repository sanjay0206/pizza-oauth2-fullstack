<script setup>
import { useAuth } from "../composables/useAuth";

const { login, loading, error } = useAuth();
</script>

<template>
  <div class="login">
    <h1>Login</h1>
    <button @click="login" :disabled="loading">
      {{ loading ? "Logging in..." : "Login with OAuth2" }}
    </button>
    <p v-if="error" class="error">{{ error }}</p>
  </div>
</template>

<style scoped>
.error {
  color: red;
}
</style>
