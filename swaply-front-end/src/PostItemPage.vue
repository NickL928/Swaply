<template>
  <div class="post-item-container">
    <!-- Header/Navigation -->
    <header class="header">
      <div class="header-content">
        <!-- Logo -->
        <div class="logo-section">
          <img alt="Swaply logo" class="logo" src="./assets/logo.svg" width="40" height="40" />
          <span class="logo-text">Swaply</span>
        </div>

        <!-- Navigation -->
        <nav class="nav-menu">
          <a href="#" class="nav-link" @click="goToHome">Home</a>
          <a href="#" class="nav-link">Community</a>
          <a href="#" class="nav-link">Buy Requests</a>
          <a href="#" class="nav-link">System Announcements</a>
          <a href="#" class="nav-link">Message & Feedback</a>
        </nav>

        <!-- User Profile & DM -->
        <div class="header-actions">
          <button class="dm-button">
            <span class="dm-icon">💬</span>
            DM
          </button>
          <div class="profile-section">
            <img alt="Profile" class="profile-picture" src="./assets/logo.svg" width="40" height="40" />
          </div>
        </div>
      </div>
    </header>

    <!-- Main Content -->
    <main class="main-content">
      <div class="post-form-container">
        <div class="post-form-card">
          <h1 class="form-title">Post Item</h1>

          <form @submit.prevent="handleSubmit" class="post-form">
            <!-- Name Field -->
            <div class="form-group">
              <label for="name" class="form-label">Name</label>
              <input
                id="name"
                v-model="form.name"
                type="text"
                class="form-input"
                placeholder="Enter item name"
                required
              />
            </div>

            <!-- Price Field -->
            <div class="form-group">
              <label for="price" class="form-label">Price</label>
              <input
                id="price"
                v-model="form.price"
                type="text"
                class="form-input"
                placeholder="Enter price (e.g., $50)"
                required
              />
            </div>

            <!-- Picture Upload -->
            <div class="form-group">
              <label for="picture" class="form-label">Picture</label>
              <div class="picture-upload">
                <input
                  id="picture"
                  ref="fileInput"
                  type="file"
                  accept="image/*"
                  @change="handleFileUpload"
                  class="file-input"
                  multiple
                />
                <button
                  type="button"
                  @click="triggerFileUpload"
                  class="upload-button"
                >
                  Upload picture
                </button>
                <div v-if="uploadedImages.length > 0" class="uploaded-images">
                  <div
                    v-for="(image, index) in uploadedImages"
                    :key="index"
                    class="image-preview"
                  >
                    <img :src="image.preview" :alt="image.name" />
                    <button
                      type="button"
                      @click="removeImage(index)"
                      class="remove-image"
                    >
                      ×
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <!-- Listing Status -->
            <div class="form-group">
              <label for="listingStatus" class="form-label">Listing Status</label>
              <select
                id="listingStatus"
                v-model="form.listingStatus"
                class="form-select"
                required
              >
                <option value="">Select listing status</option>
                <option value="available">Available</option>
                <option value="pending">Pending</option>
                <option value="sold">Sold</option>
              </select>
            </div>

            <!-- Category -->
            <div class="form-group">
              <label for="category" class="form-label">Category</label>
              <select
                id="category"
                v-model="form.category"
                class="form-select"
                required
              >
                <option value="">Select category</option>
                <option value="electronics">Electronics</option>
                <option value="fashion">Fashion</option>
                <option value="sports">Sports</option>
                <option value="books">Books</option>
                <option value="appliances">Appliances</option>
                <option value="music">Music</option>
                <option value="other">Other</option>
              </select>
            </div>

            <!-- Details -->
            <div class="form-group">
              <label for="details" class="form-label">Details</label>
              <textarea
                id="details"
                v-model="form.details"
                class="form-textarea"
                placeholder="Describe your item in detail..."
                rows="6"
                required
              ></textarea>
            </div>

            <!-- Submit Buttons -->
            <div class="form-actions">
              <button
                type="button"
                @click="goToHome"
                class="cancel-button"
              >
                Cancel
              </button>
              <button
                type="submit"
                class="submit-button"
                :disabled="loading"
              >
                {{ loading ? 'Posting...' : 'Post Item' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'

const emit = defineEmits(['navigate'])

const loading = ref(false)
const fileInput = ref(null)
const uploadedImages = ref([])

const form = reactive({
  name: '',
  price: '',
  listingStatus: '',
  category: '',
  details: ''
})

const triggerFileUpload = () => {
  fileInput.value?.click()
}

const handleFileUpload = (event) => {
  const files = Array.from(event.target.files)

  files.forEach(file => {
    if (file && file.type.startsWith('image/')) {
      const reader = new FileReader()
      reader.onload = (e) => {
        uploadedImages.value.push({
          file: file,
          name: file.name,
          preview: e.target.result
        })
      }
      reader.readAsDataURL(file)
    }
  })
}

const removeImage = (index) => {
  uploadedImages.value.splice(index, 1)
}

const handleSubmit = async () => {
  loading.value = true

  try {
    // Create form data for submission
    const formData = new FormData()
    formData.append('name', form.name)
    formData.append('price', form.price)
    formData.append('listingStatus', form.listingStatus)
    formData.append('category', form.category)
    formData.append('details', form.details)

    // Add images to form data
    uploadedImages.value.forEach((image, index) => {
      formData.append(`image_${index}`, image.file)
    })

    console.log('Submitting item:', {
      name: form.name,
      price: form.price,
      listingStatus: form.listingStatus,
      category: form.category,
      details: form.details,
      images: uploadedImages.value.length
    })

    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 2000))

    alert('Item posted successfully!')

    // Reset form
    Object.keys(form).forEach(key => {
      form[key] = ''
    })
    uploadedImages.value = []

    // Navigate back to home
    goToHome()

  } catch (error) {
    console.error('Error posting item:', error)
    alert('Error posting item. Please try again.')
  } finally {
    loading.value = false
  }
}

const goToHome = () => {
  emit('navigate', 'home')
}
</script>

<style scoped>
.post-item-container {
  min-height: 100vh;
  width: 100%;
  background: #f8fafc;
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

/* Header Styles - Same as HomePage */
.header {
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  border-bottom: none;
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.3);
  position: sticky;
  top: 0;
  z-index: 100;
  width: 100%;
}

.header-content {
  width: 100%;
  max-width: none;
  margin: 0;
  padding: 1.25rem 2rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 2rem;
  box-sizing: border-box;
}

.logo-section {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-shrink: 0;
}

.logo {
  border-radius: 8px;
  filter: brightness(0) invert(1);
}

.logo-text {
  font-size: 1.75rem;
  font-weight: 800;
  color: white;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.nav-menu {
  display: flex;
  gap: 0.5rem;
  flex-grow: 1;
  justify-content: center;
}

.nav-link {
  text-decoration: none;
  color: rgba(255, 255, 255, 0.9);
  font-weight: 600;
  padding: 0.75rem 1.25rem;
  border-radius: 12px;
  transition: all 0.3s;
  white-space: nowrap;
  font-size: 0.95rem;
  cursor: pointer;
}

.nav-link:hover {
  color: white;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  transform: translateY(-1px);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 1rem;
  flex-shrink: 0;
}

.dm-button {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: 2px solid rgba(255, 255, 255, 0.3);
  padding: 0.75rem 1.25rem;
  border-radius: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  backdrop-filter: blur(10px);
}

.dm-button:hover {
  background: rgba(255, 255, 255, 0.3);
  border-color: rgba(255, 255, 255, 0.5);
  transform: translateY(-2px);
}

.dm-icon {
  font-size: 1.1rem;
}

.profile-section {
  position: relative;
}

.profile-picture {
  width: 45px;
  height: 45px;
  border-radius: 50%;
  cursor: pointer;
  border: 3px solid rgba(255, 255, 255, 0.3);
  transition: all 0.3s;
  filter: brightness(0) invert(1);
  background: rgba(255, 255, 255, 0.1);
  padding: 5px;
}

.profile-picture:hover {
  border-color: rgba(255, 255, 255, 0.6);
  transform: scale(1.05);
}

/* Main Content */
.main-content {
  width: 100%;
  max-width: none;
  margin: 0;
  padding: 2.5rem 2rem;
  box-sizing: border-box;
  display: flex;
  justify-content: center;
}

.post-form-container {
  width: 100%;
  max-width: 800px;
}

.post-form-card {
  background: white;
  border-radius: 24px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  padding: 3rem;
  border: 1px solid #e5e7eb;
}

.form-title {
  font-size: 2.5rem;
  font-weight: 700;
  color: #1f2937;
  text-align: center;
  margin-bottom: 2.5rem;
}

.post-form {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.form-label {
  font-weight: 600;
  color: #374151;
  font-size: 1.1rem;
}

.form-input,
.form-select {
  padding: 1.25rem 1.5rem;
  border: 2px solid #e5e7eb;
  border-radius: 16px;
  font-size: 1.1rem;
  transition: all 0.3s;
  background: #f8fafc;
}

.form-input:focus,
.form-select:focus {
  outline: none;
  border-color: #4f46e5;
  box-shadow: 0 0 0 4px rgba(79, 70, 229, 0.1);
  background: white;
}

.form-textarea {
  padding: 1.25rem 1.5rem;
  border: 2px solid #e5e7eb;
  border-radius: 16px;
  font-size: 1.1rem;
  transition: all 0.3s;
  background: #f8fafc;
  resize: vertical;
  font-family: inherit;
}

.form-textarea:focus {
  outline: none;
  border-color: #4f46e5;
  box-shadow: 0 0 0 4px rgba(79, 70, 229, 0.1);
  background: white;
}

/* Picture Upload */
.picture-upload {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.file-input {
  display: none;
}

.upload-button {
  padding: 1rem 2rem;
  border: 2px dashed #cbd5e1;
  background: #f8fafc;
  border-radius: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  color: #6b7280;
  font-size: 1rem;
  text-align: center;
}

.upload-button:hover {
  border-color: #4f46e5;
  color: #4f46e5;
  background: #eef2ff;
}

.uploaded-images {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
}

.image-preview {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: 12px;
  overflow: hidden;
  border: 2px solid #e5e7eb;
}

.image-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.remove-image {
  position: absolute;
  top: 5px;
  right: 5px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: rgba(239, 68, 68, 0.9);
  color: white;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  line-height: 1;
}

.remove-image:hover {
  background: rgb(239, 68, 68);
}

/* Form Actions */
.form-actions {
  display: flex;
  gap: 1.5rem;
  margin-top: 1rem;
}

.cancel-button {
  flex: 1;
  padding: 1.5rem;
  border: 2px solid #e5e7eb;
  background: white;
  color: #6b7280;
  border-radius: 16px;
  font-size: 1.2rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.cancel-button:hover {
  border-color: #d1d5db;
  color: #374151;
}

.submit-button {
  flex: 1;
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

/* Mobile Responsiveness */
@media (max-width: 1024px) {
  .nav-menu {
    display: none;
  }

  .header-content {
    justify-content: space-between;
  }
}

@media (max-width: 768px) {
  .header-content {
    flex-wrap: wrap;
    gap: 1rem;
    padding: 1rem;
  }

  .main-content {
    padding: 1.5rem 1rem;
  }

  .post-form-card {
    padding: 2rem;
  }

  .form-title {
    font-size: 2rem;
  }

  .form-actions {
    flex-direction: column;
  }
}

@media (max-width: 480px) {
  .post-form-card {
    padding: 1.5rem;
  }

  .form-title {
    font-size: 1.75rem;
  }

  .form-input,
  .form-select,
  .form-textarea {
    padding: 1rem 1.25rem;
    font-size: 1rem;
  }

  .submit-button,
  .cancel-button {
    padding: 1.25rem;
    font-size: 1.1rem;
  }
}
</style>