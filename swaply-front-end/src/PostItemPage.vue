<template>
  <div class="post-item-container">
    <!-- Hero Section -->
    <section class="hero-section">
      <div class="hero-content">
        <h1 class="hero-title">📦 Sell Your Item</h1>
        <p class="hero-subtitle">List your item and reach thousands of buyers</p>
      </div>
    </section>

    <!-- Main Content -->
    <main class="main-content">
      <div class="post-form-container">
        <div class="post-form-card">
          <h1 class="form-title">Post Item</h1>

          <form @submit.prevent="handleSubmit" class="post-form">
            <!-- Title Field -->
            <div class="form-group">
              <label for="title" class="form-label">Title</label>
              <input
                id="title"
                v-model="form.title"
                type="text"
                class="form-input"
                placeholder="Enter item title"
                required
              />
            </div>

            <!-- Price Field -->
            <div class="form-group">
              <label for="price" class="form-label">Price</label>
              <input
                id="price"
                v-model="form.price"
                type="number"
                step="0.01"
                min="0"
                class="form-input"
                placeholder="Enter price (e.g., 49.99)"
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
                <button type="button" @click="triggerFileUpload" class="upload-button">
                  Upload picture
                </button>
                <div v-if="uploadedImages.length > 0" class="uploaded-images">
                  <div v-for="(image, index) in uploadedImages" :key="index" class="image-preview">
                    <img :src="image.preview" :alt="image.fileName" />
                    <button type="button" @click="removeImage(index)" class="remove-image">×</button>
                  </div>
                </div>
              </div>
            </div>

            <!-- Condition -->
            <div class="form-group">
              <label for="condition" class="form-label">Condition</label>
              <select
                id="condition"
                v-model="form.condition"
                class="form-select"
                required
              >
                <option value="">Select condition</option>
                <option v-for="c in conditions" :key="c.value" :value="c.value">{{ c.label }}</option>
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
                <option v-for="c in categories" :key="c.value" :value="c.value">{{ c.label }}</option>
              </select>
            </div>

            <!-- Description -->
            <div class="form-group">
              <label for="description" class="form-label">Description</label>
              <textarea
                id="description"
                v-model="form.description"
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
import listingApi from './services/listingApi.js'

const emit = defineEmits(['navigate'])

const loading = ref(false)
const fileInput = ref(null)
const uploadedImages = ref([]) // { fileName, url, preview }

const categories = [
  { value: 'ELECTRONICS', label: 'Electronics' },
  { value: 'BOOKS', label: 'Books' },
  { value: 'FURNITURE', label: 'Furniture' },
  { value: 'CLOTHING', label: 'Clothing' },
  { value: 'SPORTS', label: 'Sports' },
  { value: 'NECESSITIES', label: 'Necessities' },
  { value: 'TOYS_GAMES', label: 'Toys & Games' },
  { value: 'OTHER', label: 'Other' }
]

const conditions = [
  { value: 'NEW', label: 'New' },
  { value: 'LIKE_NEW', label: 'Like New' },
  { value: 'GOOD', label: 'Good' },
  { value: 'FAIR', label: 'Fair' },
  { value: 'POOR', label: 'Poor' }
]

const form = reactive({
  title: '',
  price: '',
  category: '',
  condition: '',
  description: ''
})

const triggerFileUpload = () => {
  fileInput.value?.click()
}

const handleFileUpload = async (event) => {
  const files = Array.from(event.target.files)
  for (const file of files) {
    if (file && file.type.startsWith('image/')) {
      try {
        const { data } = await listingApi.uploadImage(file)
        const url = typeof data.url === 'string' ? (data.url.startsWith('http') ? data.url : (data.url.startsWith('/') ? data.url : '/' + data.url)) : ''
        uploadedImages.value.push({
          fileName: data.fileName || file.name,
          url: url, // normalized server path '/uploads/...'
          preview: url // use proxied relative URL for preview to avoid CORS
        })
      } catch (e) {
        console.error('Upload failed for', file.name, e)
        alert('Failed to upload ' + file.name + ': ' + (e.response?.data?.message || e.message))
      }
    }
  }
  // reset input so same file can be reselected if removed
  event.target.value = ''
}

const removeImage = (index) => {
  uploadedImages.value.splice(index, 1)
}

const handleSubmit = async () => {
  if (!form.title || !form.price || !form.category || !form.condition || !form.description) return
  loading.value = true
  try {
    const userRaw = localStorage.getItem('user')
    if (!userRaw) throw new Error('User not authenticated')
    const user = JSON.parse(userRaw)
    const userId = user.userId || user.id

    const imageUrl = uploadedImages.value[0]?.url || null // store relative path; backend expects string

    const payload = {
      title: form.title,
      description: form.description,
      price: parseFloat(form.price),
      category: form.category,
      condition: form.condition,
      imageUrl
    }

    const { data } = await listingApi.createListing(userId, payload)

    form.title = ''
    form.price = ''
    form.category = ''
    form.condition = ''
    form.description = ''
    uploadedImages.value = []

    alert('Listing created successfully (ID: ' + data.listingId + ')')
    goToHome()
  } catch (e) {
    console.error('Create listing failed', e)
    alert('Failed to create listing: ' + (e.response?.data?.message || e.message))
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  background-attachment: fixed;
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

/* Hero Section */
.hero-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 3rem 2rem;
  text-align: center;
}

.hero-content {
  max-width: 800px;
  margin: 0 auto;
}

.hero-title {
  font-size: 2.5rem;
  font-weight: 800;
  color: white;
  margin: 0 0 0.5rem 0;
  text-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.hero-subtitle {
  font-size: 1.1rem;
  color: rgba(255, 255, 255, 0.95);
  margin: 0;
}

/* Main Content */
.main-content {
  width: 100%;
  max-width: none;
  margin: 0;
  padding: 3rem 2rem 4rem 2rem;
  box-sizing: border-box;
  display: flex;
  justify-content: center;
  background: #f8fafc;
  border-radius: 32px 32px 0 0;
  margin-top: -2rem;
  position: relative;
  z-index: 2;
}

.post-form-container {
  width: 100%;
  max-width: 800px;
}

.post-form-card {
  background: white;
  border-radius: 20px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  padding: 3rem;
  border: 1px solid #e2e8f0;
}

.form-title {
  font-size: 2rem;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-align: center;
  margin-bottom: 2.5rem;
}

.post-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-label {
  font-weight: 600;
  color: #475569;
  font-size: 0.9rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.form-input,
.form-select {
  padding: 0.875rem 1rem;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  font-size: 1rem;
  transition: all 0.3s ease;
  background: white;
}

.form-input:focus,
.form-select:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
  transform: translateY(-1px);
}

.form-textarea {
  padding: 0.875rem 1rem;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  font-size: 1rem;
  transition: all 0.3s ease;
  background: white;
  resize: vertical;
  font-family: inherit;
}

.form-textarea:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
  transform: translateY(-1px);
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
  gap: 1rem;
  margin-top: 1.5rem;
}

.cancel-button {
  flex: 1;
  padding: 1rem 2rem;
  border: 2px solid #e2e8f0;
  background: white;
  color: #64748b;
  border-radius: 12px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.cancel-button:hover {
  background: #f8fafc;
  border-color: #cbd5e1;
  transform: translateY(-1px);
}

.submit-button {
  flex: 1;
  padding: 1rem 2rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 1rem;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.submit-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.submit-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

/* Responsive */
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