import { ref } from "vue";
import { useRouter } from "vue-router";
import axios from "axios";

export const useAuth = () => {
  const token = ref(localStorage.getItem("token"));
  const refreshToken = ref(localStorage.getItem("refreshToken"));
  const error = ref(null);
  const loading = ref(false);
  const router = useRouter();

  // Add token to request headers
  axios.interceptors.request.use(
    async (config) => {
      // Simulate delay for 2 secs
      await new Promise((resolve) => setTimeout(resolve, 2000));

      if (token.value) {
        config.headers.Authorization = `Bearer ${token.value}`;
      }
      return config;
    },
    (error) => Promise.reject(error)
  );

  // Handle 401 errors and attempt token refresh
  axios.interceptors.response.use(
    (response) => response,
    async (error) => {
      const originalRequest = error.config;

      if (
        error.response?.status === 401 &&
        refreshToken.value &&
        !originalRequest._retry
      ) {
        originalRequest._retry = true;

        try {
          // Refresh token and retry original request
          const newTokens = await refreshAccessToken();
          originalRequest.headers.Authorization = `Bearer ${newTokens.access_token}`;
          return axios(originalRequest);
        } catch (refreshError) {
          // Logout if refresh fails
          logout();
          return Promise.reject(refreshError);
        }
      }

      if (error.response?.status === 401) {
        logout();
      }

      return Promise.reject(error);
    }
  );

  const login = async () => {
    loading.value = true;
    error.value = null;

    const authServerUrl = "http://localhost:9000/oauth2/authorize";
    const clientId = "pizza-client";
    const redirectUri = encodeURIComponent("http://localhost:5173/callback");
    const responseType = "code";
    const scope = "api.read openid";

    window.location.href = `${authServerUrl}?response_type=${responseType}&client_id=${clientId}&redirect_uri=${redirectUri}&scope=${scope}`;
  };

  const handleCallback = async (code) => {
    try {
      loading.value = true;
      error.value = null;

      const response = await axios.post(
        "http://localhost:9000/oauth2/token",
        {
          grant_type: "authorization_code",
          code,
          redirect_uri: "http://localhost:5173/callback",
        },
        {
          headers: {
            "Content-Type": "application/x-www-form-urlencoded",
            Authorization: "Basic " + btoa("pizza-client:secret"),
          },
        }
      );

      token.value = response.data.access_token;
      refreshToken.value = response.data.refresh_token;
      localStorage.setItem("token", response.data.access_token);
      localStorage.setItem("refreshToken", response.data.refresh_token);

      router.push("/pizzas");
    } catch (err) {
      error.value = err.response?.data?.error || err.message;
    } finally {
      loading.value = false;
    }
  };

  const refreshAccessToken = async () => {
    try {
      const response = await axios.post(
        "http://localhost:9000/oauth2/token",
        {
          grant_type: "refresh_token",
          refresh_token: refreshToken.value,
        },
        {
          headers: {
            "Content-Type": "application/json",
            Authorization: "Basic " + btoa("pizza-client:secret"),
          },
        }
      );

      token.value = response.data.access_token;
      refreshToken.value = response.data.refresh_token;
      localStorage.setItem("token", response.data.access_token);
      localStorage.setItem("refreshToken", response.data.refresh_token);

      return response.data;
    } catch (err) {
      error.value = err.response?.data?.error || err.message;
      throw err;
    }
  };

  const logout = () => {
    token.value = null;
    refreshToken.value = null;
    localStorage.removeItem("token");
    localStorage.removeItem("refreshToken");
    router.push("/");
  };

  return {
    token,
    refreshToken,
    error,
    loading,
    login,
    handleCallback,
    logout,
  };
};
