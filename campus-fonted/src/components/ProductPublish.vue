<template>
  <div class="publish-container">
    <div class="publish-header">
      <h1>发布商品</h1>
      <button class="back-btn" @click="goBack">返回</button>
    </div>
    
    <div class="publish-form-container">
      <form @submit.prevent="handleSubmit" class="publish-form">
        <!-- 商品基本信息 -->
        <div class="form-section">
          <h2>商品基本信息</h2>
          
          <div class="form-group">
            <label for="name">商品名称 <span class="required">*</span></label>
            <input 
              id="name"
              v-model="form.name" 
              type="text" 
              placeholder="请输入商品名称"
              maxlength="50"
              required
            />
            <span class="char-count">{{ form.name.length }}/50</span>
          </div>
          
          <div class="form-group">
            <label for="category">商品分类 <span class="required">*</span></label>
            <select id="category" v-model="form.category" required>
              <option value="">请选择分类</option>
              <option value="electronics">电子产品</option>
              <option value="books">图书教材</option>
              <option value="clothing">服装配饰</option>
              <option value="sports">运动用品</option>
              <option value="daily">生活用品</option>
              <option value="other">其他</option>
            </select>
          </div>
          
          <div class="form-group">
            <label for="price">商品价格 <span class="required">*</span></label>
            <div class="price-input">
              <span class="currency">¥</span>
              <input 
                id="price"
                v-model="form.price" 
                type="number" 
                placeholder="0.00"
                min="0"
                step="0.01"
                required
              />
            </div>
          </div>
          
          <div class="form-group">
            <label for="originalPrice">原价（可选）</label>
            <div class="price-input">
              <span class="currency">¥</span>
              <input 
                id="originalPrice"
                v-model="form.originalPrice" 
                type="number" 
                placeholder="0.00"
                min="0"
                step="0.01"
              />
            </div>
          </div>
          
          <div class="form-group">
            <label for="description">商品描述</label>
            <textarea 
              id="description"
              v-model="form.description" 
              placeholder="请详细描述商品的特点、使用情况等"
              maxlength="500"
              rows="5"
            ></textarea>
            <span class="char-count">{{ form.description.length }}/500</span>
          </div>
        </div>
        
        <!-- 商品图片 -->
        <div class="form-section">
          <h2>商品图片</h2>
          <p class="section-desc">最多上传5张图片，第一张将作为封面图片</p>
          
          <div class="image-upload-area">
            <div class="image-list">
              <div 
                v-for="(image, index) in form.images" 
                :key="index"
                class="image-item"
              >
                <img :src="image.preview" :alt="`商品图片 ${index + 1}`" />
                <div class="image-overlay">
                  <button type="button" @click="removeImage(index)" class="remove-btn">
                    删除
                  </button>
                  <span v-if="index === 0" class="cover-badge">封面</span>
                </div>
              </div>
              
              <div 
                v-if="form.images.length < 5"
                class="upload-placeholder"
                @click="triggerFileInput"
              >
                <div class="upload-icon">+</div>
                <span>添加图片</span>
              </div>
            </div>
            
            <input 
              ref="fileInput"
              type="file" 
              accept="image/*" 
              multiple
              @change="handleImageUpload"
              style="display: none"
            />
          </div>
        </div>
        
        <!-- 联系方式 -->
        <div class="form-section">
          <h2>联系方式</h2>
          
          <div class="form-group">
            <label for="contactPhone">联系电话</label>
            <input 
              id="contactPhone"
              v-model="form.contactPhone" 
              type="tel" 
              placeholder="请输入联系电话"
            />
          </div>
          
          <div class="form-group">
            <label for="contactEmail">联系邮箱</label>
            <input 
              id="contactEmail"
              v-model="form.contactEmail" 
              type="email" 
              placeholder="请输入联系邮箱"
            />
          </div>
        </div>
        
        <!-- 提交按钮 -->
        <div class="form-actions">
          <button type="button" @click="saveDraft" class="draft-btn" :disabled="loading">
            保存草稿
          </button>
          <button type="submit" class="submit-btn" :disabled="loading">
            {{ loading ? '发布中...' : '立即发布' }}
          </button>
        </div>
      </form>
    </div>
    
    <!-- 错误提示 -->
    <div v-if="errorMessage" class="error-message">
      {{ errorMessage }}
    </div>
    
    <!-- 成功提示 -->
    <div v-if="successMessage" class="success-message">
      {{ successMessage }}
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { post } from '../api/request'

const router = useRouter()
const fileInput = ref(null)

// 表单数据
const form = reactive({
  name: '',
  category: '',
  price: '',
  originalPrice: '',
  description: '',
  images: [],
  contactPhone: '',
  contactEmail: ''
})

// 状态管理
const loading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

// 方法
const goBack = () => {
  router.push('/products')
}

const triggerFileInput = () => {
  fileInput.value?.click()
}

const handleImageUpload = (event) => {
  const files = Array.from(event.target.files)
  const remainingSlots = 5 - form.images.length
  const filesToProcess = files.slice(0, remainingSlots)
  
  filesToProcess.forEach(file => {
    // 验证文件类型
    if (!file.type.startsWith('image/')) {
      errorMessage.value = '请选择图片文件'
      return
    }
    
    // 验证文件大小（5MB）
    if (file.size > 5 * 1024 * 1024) {
      errorMessage.value = '图片大小不能超过5MB'
      return
    }
    
    // 创建预览
    const reader = new FileReader()
    reader.onload = (e) => {
      form.images.push({
        file: file,
        preview: e.target.result,
        name: file.name
      })
    }
    reader.readAsDataURL(file)
  })
  
  // 清空input
  event.target.value = ''
}

const removeImage = (index) => {
  form.images.splice(index, 1)
}

const validateForm = () => {
  if (!form.name.trim()) {
    errorMessage.value = '请输入商品名称'
    return false
  }
  
  if (!form.category) {
    errorMessage.value = '请选择商品分类'
    return false
  }
  
  if (!form.price || form.price <= 0) {
    errorMessage.value = '请输入有效的商品价格'
    return false
  }
  
  if (form.originalPrice && form.originalPrice <= form.price) {
    errorMessage.value = '原价应该大于现价'
    return false
  }
  
  if (form.images.length === 0) {
    errorMessage.value = '请至少上传一张商品图片'
    return false
  }
  
  return true
}

const uploadImages = async () => {
  const uploadedImages = []
  
  for (const imageData of form.images) {
    const formData = new FormData()
    formData.append('file', imageData.file)
    
    try {
      const response = await post('/upload/image', formData, {
        'Content-Type': 'multipart/form-data'
      })
      
      if (response.success) {
        uploadedImages.push(response.data.url)
      } else {
        throw new Error(response.message || '图片上传失败')
      }
    } catch (error) {
      throw new Error(`图片上传失败: ${error.message}`)
    }
  }
  
  return uploadedImages
}

const handleSubmit = async () => {
  errorMessage.value = ''
  successMessage.value = ''
  
  if (!validateForm()) {
    return
  }
  
  loading.value = true
  
  try {
    console.log('开始发布商品，表单数据：', form)
    
    // 上传图片
    const imageUrls = await uploadImages()
    
    // 准备提交数据
    const submitData = {
      name: form.name.trim(),
      category: form.category,
      price: parseFloat(form.price),
      originalPrice: form.originalPrice ? parseFloat(form.originalPrice) : null,
      description: form.description.trim(),
      images: imageUrls,
      contactPhone: form.contactPhone.trim(),
      contactEmail: form.contactEmail.trim(),
      status: 'available'
    }
    
    console.log('提交商品数据：', submitData)
    
    const response = await post('/products', submitData)
    
    if (response.success) {
      successMessage.value = '商品发布成功！'
      console.log('商品发布成功：', response.data)
      
      // 2秒后跳转到商品列表
      setTimeout(() => {
        router.push('/products')
      }, 2000)
    } else {
      errorMessage.value = response.message || '发布失败，请重试'
      console.error('商品发布失败：', response.message)
    }
  } catch (error) {
    console.error('发布商品出错：', error)
    errorMessage.value = error.message || '发布失败，请检查网络连接'
  } finally {
    loading.value = false
  }
}

const saveDraft = async () => {
  errorMessage.value = ''
  successMessage.value = ''
  
  if (!form.name.trim()) {
    errorMessage.value = '请至少输入商品名称'
    return
  }
  
  loading.value = true
  
  try {
    // 上传已选择的图片
    let imageUrls = []
    if (form.images.length > 0) {
      imageUrls = await uploadImages()
    }
    
    const draftData = {
      name: form.name.trim(),
      category: form.category,
      price: form.price ? parseFloat(form.price) : null,
      originalPrice: form.originalPrice ? parseFloat(form.originalPrice) : null,
      description: form.description.trim(),
      images: imageUrls,
      contactPhone: form.contactPhone.trim(),
      contactEmail: form.contactEmail.trim(),
      status: 'draft'
    }
    
    const response = await post('/products/draft', draftData)
    
    if (response.success) {
      successMessage.value = '草稿保存成功！'
      console.log('草稿保存成功：', response.data)
    } else {
      errorMessage.value = response.message || '保存草稿失败'
    }
  } catch (error) {
    console.error('保存草稿出错：', error)
    errorMessage.value = '保存草稿失败，请重试'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.publish-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.publish-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.publish-header h1 {
  color: #333;
  font-size: 28px;
  margin: 0;
}

.back-btn {
  background: #f5f5f5;
  color: #333;
  border: 1px solid #ddd;
  padding: 10px 20px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.2s;
}

.back-btn:hover {
  background: #e5e5e5;
}

.publish-form-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.publish-form {
  padding: 30px;
}

.form-section {
  margin-bottom: 40px;
}

.form-section:last-child {
  margin-bottom: 0;
}

.form-section h2 {
  color: #333;
  font-size: 20px;
  margin: 0 0 20px 0;
  padding-bottom: 10px;
  border-bottom: 2px solid #f0f0f0;
}

.section-desc {
  color: #666;
  font-size: 14px;
  margin: -10px 0 20px 0;
}

.form-group {
  margin-bottom: 20px;
  position: relative;
}

.form-group label {
  display: block;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
  font-size: 14px;
}

.required {
  color: #e53e3e;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  transition: border-color 0.2s;
  box-sizing: border-box;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #667eea;
}

.price-input {
  position: relative;
  display: flex;
  align-items: center;
}

.currency {
  position: absolute;
  left: 16px;
  color: #666;
  font-weight: 500;
  z-index: 1;
}

.price-input input {
  padding-left: 40px;
}

.char-count {
  position: absolute;
  right: 12px;
  bottom: 12px;
  font-size: 12px;
  color: #999;
}

.image-upload-area {
  border: 2px dashed #ddd;
  border-radius: 8px;
  padding: 20px;
  background: #fafafa;
}

.image-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 15px;
}

.image-item {
  position: relative;
  aspect-ratio: 1;
  border-radius: 8px;
  overflow: hidden;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.image-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s;
}

.image-item:hover .image-overlay {
  opacity: 1;
}

.remove-btn {
  background: #e53e3e;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.cover-badge {
  position: absolute;
  top: 8px;
  left: 8px;
  background: #667eea;
  color: white;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 10px;
}

.upload-placeholder {
  aspect-ratio: 1;
  border: 2px dashed #ccc;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: border-color 0.2s;
  background: white;
}

.upload-placeholder:hover {
  border-color: #667eea;
}

.upload-icon {
  font-size: 24px;
  color: #999;
  margin-bottom: 8px;
}

.upload-placeholder span {
  font-size: 12px;
  color: #666;
}

.form-actions {
  display: flex;
  gap: 15px;
  justify-content: flex-end;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.draft-btn,
.submit-btn {
  padding: 12px 24px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.draft-btn {
  background: white;
  color: #666;
  border: 1px solid #ddd;
}

.draft-btn:hover:not(:disabled) {
  background: #f5f5f5;
}

.submit-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
}

.draft-btn:disabled,
.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

.error-message {
  margin-top: 20px;
  padding: 12px 16px;
  background-color: #fee;
  color: #c53030;
  border: 1px solid #fed7d7;
  border-radius: 8px;
  text-align: center;
  font-size: 14px;
}

.success-message {
  margin-top: 20px;
  padding: 12px 16px;
  background-color: #f0fff4;
  color: #22543d;
  border: 1px solid #9ae6b4;
  border-radius: 8px;
  text-align: center;
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .publish-container {
    padding: 16px;
  }
  
  .publish-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .publish-form {
    padding: 20px;
  }
  
  .image-list {
    grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
    gap: 10px;
  }
  
  .form-actions {
    flex-direction: column;
  }
}
</style>