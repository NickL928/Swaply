<template>
  <div class="post-item-container">
    <!-- Top bar removed per request -->

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
  background: #f8fafc;
  margin: 0;
  padding: 0;
  box-sizing: border-box;
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