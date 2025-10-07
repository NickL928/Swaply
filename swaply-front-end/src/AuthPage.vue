<template>
  <div class="auth-container">
    <!-- Left side - Branding -->
    <div class="auth-branding">
      <div class="branding-content">
        <img alt="Swaply logo" class="brand-logo" src="./assets/logo.svg" width="120" height="120" />
        <h1 class="brand-title">Swaply</h1>
        <p class="brand-subtitle">Your trusted second-hand trading platform</p>
        <div class="brand-features">
          <div class="feature">
            <span class="feature-icon">🔄</span>
            <span>Easy Trading</span>
          </div>
          <div class="feature">
            <span class="feature-icon">🛡️</span>
            <span>Secure Transactions</span>
          </div>
          <div class="feature">
            <span class="feature-icon">🌍</span>
            <span>Global Community</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Right side - Auth Form -->
    <div class="auth-form-section">
      <div class="auth-card">
        <div class="auth-header">
          <h1>{{ isLogin ? 'Welcome Back' : 'Create Account' }}</h1>
          <p>{{ isLogin ? 'Sign in to your account' : 'Join Swaply today' }}</p>
        </div>

        <div v-if="errorMessage" class="alert error">{{ errorMessage }}</div>
        <div v-if="successMessage" class="alert success">{{ successMessage }}</div>

        <div class="auth-tabs">
          <button
              :class="{ active: isLogin }"
              @click="isLogin = true"
              class="tab-button"
          >
            Login
          </button>
          <button
              :class="{ active: !isLogin }"
              @click="isLogin = false"
              class="tab-button"
          >
            Register
          </button>
        </div>

        <form @submit.prevent="handleSubmit" class="auth-form">
          <!-- Login identifier (username or email) -->
          <div v-if="isLogin" class="form-group">
            <label for="identifier">Username or Email</label>
            <input
              id="identifier"
              v-model="form.identifier"
              type="text"
              required
              placeholder="Enter username or email"
              autocomplete="username"
            />
          </div>

          <!-- Registration only fields -->
          <div v-if="!isLogin" class="form-group">
            <label for="userName">Username</label>
            <input
                id="userName"
                v-model="form.userName"
                type="text"
                required
                placeholder="Choose a username"
                autocomplete="username"
            />
          </div>

          <div v-if="!isLogin" class="form-group">
            <label for="email">Email</label>
            <input
                id="email"
                v-model="form.email"
                type="email"
                required
                placeholder="Enter your email"
                autocomplete="email"
            />
          </div>

          <!-- Password field -->
          <div class="form-group">
            <label for="password">Password</label>
            <div class="password-input">
              <input
                  id="password"
                  v-model="form.password"
                  :type="showPassword ? 'text' : 'password'"
                  required
                  :placeholder="isLogin ? 'Enter your password' : 'Create a password'"
                  autocomplete="current-password"
              />
              <button
                  type="button"
                  @click="showPassword = !showPassword"
                  class="password-toggle"
              >
                {{ showPassword ? '👁️' : '🙈' }}
              </button>
            </div>
          </div>

          <!-- Confirm password for registration -->
          <div v-if="!isLogin" class="form-group">
            <label for="confirmPassword">Confirm Password</label>
            <input
                id="confirmPassword"
                v-model="form.confirmPassword"
                type="password"
                required
                placeholder="Confirm your password"
                autocomplete="new-password"
            />
          </div>

          <!-- Remember me for login -->
          <div v-if="isLogin" class="form-options">
            <label class="checkbox-label">
              <input v-model="form.rememberMe" type="checkbox" />
              Remember me
            </label>
            <a href="#" class="forgot-link">Forgot password?</a>
          </div>

          <!-- Terms for registration -->
          <div v-if="!isLogin" class="form-options">
            <label class="checkbox-label">
              <input v-model="form.acceptTerms" type="checkbox" required />
              I agree to the <a href="#" class="terms-link">Terms of Service</a> and <a href="#" class="terms-link">Privacy Policy</a>
            </label>
          </div>

          <!-- Submit button -->
          <button type="submit" class="submit-button" :disabled="loading">
            {{ loading ? 'Please wait...' : (isLogin ? 'Sign In' : 'Create Account') }}
          </button>
        </form>

        <!-- Switch mode -->
        <div class="auth-switch">
          <p v-if="isLogin">
            Don't have an account?
            <button @click="isLogin = false" class="switch-button">Sign up</button>
          </p>
          <p v-else>
            Already have an account?
            <button @click="isLogin = true" class="switch-button">Sign in</button>
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import authApi from './services/authApi.js'
const emit = defineEmits(['login-success'])

const isLogin = ref(true)
const showPassword = ref(false)
const loading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

const form = reactive({
  identifier: '', // used only for login (username OR email)
  userName: '',
  email: '',
  password: '',
  confirmPassword: '',
  rememberMe: false,
  acceptTerms: false
})

function resetMessages() {
  errorMessage.value = ''
  successMessage.value = ''
}

function resetForm() {
  form.identifier = ''
  form.userName = ''
  form.email = ''
  form.password = ''
  form.confirmPassword = ''
  form.rememberMe = false
  form.acceptTerms = false
}

const handleSubmit = async () => {
  resetMessages()
  loading.value = true
  try {
    if (isLogin.value) {
      const identifier = form.identifier?.trim()
      if (!identifier) {
        errorMessage.value = 'Please enter username or email'
        return
      }
      const { data } = await authApi.login({ userName: identifier, password: form.password })
      localStorage.setItem('token', data.token)
      localStorage.setItem('user', JSON.stringify(data.user))
      successMessage.value = 'Login successful!'
      emit('login-success', data.user)
      if (!form.rememberMe) {
        // Optionally could move token to sessionStorage; keeping localStorage for simplicity
      }
    } else {
      // registration path
      if (form.password !== form.confirmPassword) {
        errorMessage.value = 'Passwords do not match'
        return
      }
      if (!form.acceptTerms) {
        errorMessage.value = 'You must accept the terms to continue'
        return
      }
      const payload = { userName: form.userName, email: form.email, password: form.password }
      await authApi.register(payload)
      successMessage.value = 'Registration successful! You can now sign in.'
      setTimeout(() => {
        isLogin.value = true
        resetForm()
        resetMessages()
      }, 1200)
    }
  } catch (e) {
    if (e.response) {
      if (e.response.status === 401) {
        errorMessage.value = 'Invalid credentials'
      } else if (e.response.status === 400) {
        errorMessage.value = (e.response.data?.message) ? e.response.data.message : 'Request failed'
      } else {
        errorMessage.value = 'Server error (' + e.response.status + ')'
      }
    } else {
      errorMessage.value = 'Network error. Please try again.'
    }
  } finally {
    loading.value = false
  }
}

</script>

<style scoped>
.auth-container {
  min-height: 100vh;
  height: 100vh;
  display: flex;
  background: #f8fafc;
  width: 100vw;
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  position: fixed;
  top: 0;
  left: 0;
}

/* Left side - Branding */
.auth-branding {
  flex: 1;
  width: 50vw;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 3rem;
  position: relative;
  overflow: hidden;
}

.auth-branding::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="grid" width="10" height="10" patternUnits="userSpaceOnUse"><path d="M 10 0 L 0 0 0 10" fill="none" stroke="rgba(255,255,255,0.1)" stroke-width="0.5"/></pattern></defs><rect width="100" height="100" fill="url(%23grid)" /></svg>');
  opacity: 0.3;
}

.branding-content {
  text-align: center;
  color: white;
  z-index: 1;
  position: relative;
}

.brand-logo {
  margin-bottom: 2rem;
  filter: brightness(0) invert(1);
}

.brand-title {
  font-size: 4rem;
  font-weight: 800;
  margin-bottom: 1rem;
  text-shadow: 0 4px 8px rgba(0, 0, 0, 0.3);
}

.brand-subtitle {
  font-size: 1.5rem;
  margin-bottom: 3rem;
  opacity: 0.9;
  font-weight: 300;
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  max-width: 300px;
}

.feature {
  display: flex;
  align-items: center;
  gap: 1rem;
  font-size: 1.1rem;
  font-weight: 500;
}

.feature-icon {
  font-size: 1.5rem;
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* Right side - Form */
.auth-form-section {
  flex: 1;
  width: 50vw;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 3rem;
  background: white;
}

.auth-card {
  width: 100%;
  max-width: 500px;
  min-width: 400px;
  background: white;
  border-radius: 24px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  padding: 3rem;
  border: 1px solid #e5e7eb;
  box-sizing: border-box;
}

.auth-header {
  text-align: center;
  margin-bottom: 2.5rem;
}

.auth-header h1 {
  font-size: 2.5rem;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 0.75rem;
}

.auth-header p {
  color: #6b7280;
  font-size: 1.1rem;
}

.auth-tabs {
  display: flex;
  background: #f8fafc;
  border-radius: 16px;
  padding: 8px;
  margin-bottom: 2.5rem;
  border: 1px solid #e5e7eb;
}

.tab-button {
  flex: 1;
  padding: 1.25rem 1.5rem;
  border: none;
  background: transparent;
  border-radius: 12px;
  font-weight: 600;
  font-size: 1.1rem;
  cursor: pointer;
  transition: all 0.3s;
  color: #6b7280;
}

.tab-button.active {
  background: white;
  color: #4f46e5;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.auth-form {
  margin-bottom: 2rem;
}

.form-group {
  margin-bottom: 2rem;
}

.form-group label {
  display: block;
  font-weight: 600;
  color: #374151;
  margin-bottom: 0.75rem;
  font-size: 1.1rem;
}

.form-group input {
  width: 100%;
  padding: 1.25rem 1.5rem;
  border: 2px solid #e5e7eb;
  border-radius: 16px;
  font-size: 1.1rem;
  transition: all 0.3s;
  box-sizing: border-box;
  background: #f8fafc;
}

.form-group input:focus {
  outline: none;
  border-color: #4f46e5;
  box-shadow: 0 0 0 4px rgba(79, 70, 229, 0.1);
  background: white;
}

.password-input {
  position: relative;
}

.password-toggle {
  position: absolute;
  right: 1.25rem;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  cursor: pointer;
  padding: 0.5rem;
  font-size: 1.3rem;
  color: #6b7280;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2.5rem;
  font-size: 1rem;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  cursor: pointer;
  font-weight: 500;
  color: #374151;
}

.checkbox-label input[type="checkbox"] {
  width: auto;
  margin: 0;
  transform: scale(1.3);
  background: white;
}

.forgot-link, .terms-link {
  color: #4f46e5;
  text-decoration: none;
  font-weight: 500;
}

.forgot-link:hover, .terms-link:hover {
  text-decoration: underline;
}

.submit-button {
  width: 100%;
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  color: white;
  border: none;
  padding: 1.5rem;
  border-radius: 16px;
  font-size: 1.2rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 8px 20px rgba(79, 70, 229, 0.3);
}

.submit-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 12px 25px rgba(79, 70, 229, 0.4);
}

.submit-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.auth-switch {
  text-align: center;
  margin-top: 2rem;
}

.auth-switch p {
  color: #6b7280;
  font-size: 1.1rem;
}

.switch-button {
  background: none;
  border: none;
  color: #4f46e5;
  cursor: pointer;
  font-weight: 600;
  font-size: 1.1rem;
  text-decoration: underline;
}

.switch-button:hover {
  color: #4338ca;
}

/* Mobile responsiveness */
@media (max-width: 1024px) {
  .auth-container {
    flex-direction: column;
    width: 100vw;
    position: relative;
    height: auto;
    min-height: 100vh;
  }

  .auth-branding {
    flex: none;
    width: 100vw;
    min-height: 40vh;
    padding: 2rem;
  }

  .brand-title {
    font-size: 2.5rem;
  }

  .brand-features {
    flex-direction: row;
    justify-content: center;
    max-width: none;
  }

  .auth-form-section {
    flex: none;
    width: 100vw;
    padding: 2rem;
  }

  .auth-card {
    min-width: auto;
  }
}

@media (max-width: 768px) {
  .auth-branding {
    min-height: 30vh;
    padding: 1.5rem;
  }

  .brand-title {
    font-size: 2rem;
  }

  .brand-subtitle {
    font-size: 1.2rem;
  }

  .brand-features {
    flex-direction: column;
    gap: 1rem;
  }

  .auth-card {
    padding: 2rem;
    max-width: 400px;
  }

  .auth-header h1 {
    font-size: 2rem;
  }

  .form-group input {
    padding: 1rem 1.25rem;
    font-size: 1rem;
  }

  .submit-button {
    padding: 1.25rem;
    font-size: 1.1rem;
  }
}

@media (max-width: 480px) {
  .auth-card {
    padding: 1.5rem;
  }

  .auth-header h1 {
    font-size: 1.75rem;
  }

  .brand-features {
    gap: 0.75rem;
  }

  .feature {
    font-size: 1rem;
  }
}

.alert {
  padding: 1rem 1.25rem;
  border-radius: 12px;
  margin-bottom: 1.25rem;
  font-weight: 500;
  font-size: 0.95rem;
  border: 1px solid transparent;
  text-align: center;
}
.alert.error {
  background: #fee2e2;
  color: #b91c1c;
  border-color: #fecaca;
}
.alert.success {
  background: #dcfce7;
  color: #166534;
  border-color: #bbf7d0;
}
</style>
