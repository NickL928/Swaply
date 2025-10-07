<script setup>
import { ref, onMounted } from 'vue'
import HomePage from './HomePage.vue'
import PostItemPage from './PostItemPage.vue'
import AuthPage from './AuthPage.vue'

// track current internal page AFTER authentication
const currentPage = ref('home')
// authentication state
const isAuthenticated = ref(false)
// store user info
const currentUser = ref(null)

onMounted(() => {
  const token = localStorage.getItem('token')
  if (token) {
    isAuthenticated.value = true
    try {
      currentUser.value = JSON.parse(localStorage.getItem('user'))
    } catch (e) { /* ignore parse error */ }
  }
})

const handleNavigation = (page) => {
  currentPage.value = page
}

const handleLoginSuccess = (user) => {
  currentUser.value = user
  isAuthenticated.value = true
  currentPage.value = 'home'
}

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  isAuthenticated.value = false
  currentUser.value = null
  currentPage.value = 'home'
}
</script>

<template>
  <div id="app">
    <!-- Show auth screen until logged in -->
    <AuthPage v-if="!isAuthenticated" @login-success="handleLoginSuccess" />

    <!-- Main app after auth -->
    <template v-else>
      <HomePage
        v-if="currentPage === 'home'"
        @navigate="handleNavigation"
        @logout="handleLogout"
      />
      <PostItemPage
        v-if="currentPage === 'post-item'"
        @navigate="handleNavigation"
      />
    </template>
  </div>
</template>

<style scoped>
header {
  line-height: 1.5;
}

.logo {
  display: block;
  margin: 0 auto 2rem;
}

@media (min-width: 1024px) {
  header {
    display: flex;
    place-items: center;
    padding-right: calc(var(--section-gap) / 2);
  }

  .logo {
    margin: 0 2rem 0 0;
  }

  header .wrapper {
    display: flex;
    place-items: flex-start;
    flex-wrap: wrap;
  }
}
</style>
