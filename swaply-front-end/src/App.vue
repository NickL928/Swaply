<script setup>
import { ref } from 'vue'
import HomePage from './HomePage.vue'
import PostItemPage from './PostItemPage.vue'

const currentPage = ref('home')

const handleNavigation = (page) => {
  currentPage.value = page
}
</script>

<template>
  <div id="app">
    <HomePage
      v-if="currentPage === 'home'"
      @navigate="handleNavigation"
    />
    <PostItemPage
      v-if="currentPage === 'post-item'"
      @navigate="handleNavigation"
    />
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
