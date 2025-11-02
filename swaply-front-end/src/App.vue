<script setup>
import { ref, onMounted } from 'vue'
import HomePage from './HomePage.vue'
import PostItemPage from './PostItemPage.vue'
import AuthPage from './AuthPage.vue'
import ProfilePage from './ProfilePage.vue'
import ListingDetail from './ListingDetail.vue'
import CartPage from './CartPage.vue'
import CommunityPage from './CommunityPage.vue'
import BiddingPage from './BiddingPage.vue'
import AuctionDetail from './AuctionDetail.vue'
import CreateAuctionPage from './CreateAuctionPage.vue'
import AdminPage from './AdminPage.vue'

// track current internal page AFTER authentication
const currentPage = ref('home')
// authentication state
const isAuthenticated = ref(false)
// store user info
const currentUser = ref(null)
// selected listing id for detail page
const selectedListingId = ref(null)
// selected auction id for detail page
const selectedAuctionId = ref(null)

onMounted(() => {
  const token = localStorage.getItem('token')
  if (token) {
    isAuthenticated.value = true
    try {
      currentUser.value = JSON.parse(localStorage.getItem('user'))
      if (currentUser.value?.role === 'ADMIN') currentPage.value = 'admin'
    } catch (e) { /* ignore parse error */ }
  }
})

const handleNavigation = (page, payload) => {
  if (page === 'listing-detail') {
    selectedListingId.value = payload?.listingId ?? null
  }
  if (page === 'auction-detail') {
    selectedAuctionId.value = payload?.auctionId ?? null
  }
  currentPage.value = page
}

const handleLoginSuccess = (user) => {
  currentUser.value = user
  isAuthenticated.value = true
  currentPage.value = user?.role === 'ADMIN' ? 'admin' : 'home'
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
      <AdminPage
        v-if="currentPage === 'admin'"
        @navigate="handleNavigation"
        @logout="handleLogout"
      />
      <HomePage
        v-else-if="currentPage === 'home'"
        @navigate="handleNavigation"
        @logout="handleLogout"
      />
      <PostItemPage
        v-else-if="currentPage === 'post-item'"
        @navigate="handleNavigation"
      />
      <ProfilePage
        v-else-if="currentPage === 'profile'"
        :user="currentUser"
        @navigate="handleNavigation"
        @logout="handleLogout"
        @user-updated="u => currentUser = u"
      />
      <ListingDetail
        v-else-if="currentPage === 'listing-detail' && selectedListingId != null"
        :listingId="selectedListingId"
        @navigate="handleNavigation"
      />
      <CartPage
        v-else-if="currentPage === 'cart'"
        @navigate="handleNavigation"
      />
      <CommunityPage
        v-else-if="currentPage === 'community'"
        @navigate="handleNavigation"
      />
      <BiddingPage
        v-else-if="currentPage === 'bidding'"
        @navigate="handleNavigation"
      />
      <AuctionDetail
        v-else-if="currentPage === 'auction-detail' && selectedAuctionId != null"
        :auctionId="selectedAuctionId"
        @navigate="handleNavigation"
      />
      <CreateAuctionPage
        v-else-if="currentPage === 'create-auction'"
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
