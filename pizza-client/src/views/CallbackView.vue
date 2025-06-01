<script setup>
import { onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useAuth } from "../composables/useAuth";

const route = useRoute();
const router = useRouter();
const { handleCallback } = useAuth();

onMounted(async () => {
  const code = route.query.code;
  console.log("code: ", code);

  if (code) {
    await handleCallback(code);
  } else {
    router.push("/");
  }
});
</script>

<template>
  <div class="callback">
    <p>Processing authentication...</p>
  </div>
</template>
