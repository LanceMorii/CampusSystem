<template>
  <div class="publish-product">
    <div class="page-header">
      <h1>发布商品</h1>
      <p>发布你的闲置物品，让它们重新焕发价值</p>
    </div>

    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="120px"
      class="publish-form"
    >
      <!-- 基本信息 -->
      <el-card class="form-section">
        <template #header>
          <span class="section-title">基本信息</span>
        </template>

        <el-form-item label="商品标题" prop="title">
          <el-input
            v-model="form.title"
            placeholder="请输入商品标题（最多100字）"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="商品分类" prop="categoryId">
          <el-select
            v-model="form.categoryId"
            placeholder="请选择商品分类"
            style="width: 100%"
          >
            <el-option
              v-for="category in categories"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="商品描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="6"
            placeholder="请详细描述商品的特点、使用情况等（最多2000字）"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="商品图片" prop="images">
          <el-upload
            v-model:file-list="fileList"
            :action="uploadUrl"
            :headers="uploadHeaders"
            list-type="picture-card"
            :on-success="handleUploadSuccess"
            :on-remove="handleRemove"
            :before-upload="beforeUpload"
            :limit="9"
            accept="image/*"
          >
            <el-icon><Plus /></el-icon>
            <template #tip>
              <div class="el-upload__tip">
                支持jpg/png格式，单张图片不超过5MB，最多上传9张
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-card>

      <!-- 价格信息 -->
      <el-card class="form-section">
        <template #header>
          <span class="section-title">价格信息</span>
        </template>

        <el-form-item label="出售价格" prop="price">
          <el-input-number
            v-model="form.price"
            :min="0.01"
            :max="99999999.99"
            :precision="2"
            :step="1"
            placeholder="请输入出售价格"
            style="width: 200px"
          />
          <span class="price-unit">元</span>
        </el-form-item>

        <el-form-item label="原价" prop="originalPrice">
          <el-input-number
            v-model="form.originalPrice"
            :min="0.01"
            :max="99999999.99"
            :precision="2"
            :step="1"
            placeholder="请输入原价（可选）"
            style="width: 200px"
          />
          <span class="price-unit">元</span>
        </el-form-item>
      </el-card>

      <!-- 商品详情 -->
      <el-card class="form-section">
        <template #header>
          <span class="section-title">商品详情</span>
        </template>

        <el-form-item label="商品状况" prop="condition">
          <el-select v-model="form.condition" placeholder="请选择商品状况">
            <el-option label="全新" value="NEW" />
            <el-option label="几乎全新" value="LIKE_NEW" />
            <el-option label="良好" value="GOOD" />
            <el-option label="一般" value="FAIR" />
            <el-option label="较差" value="POOR" />
          </el-select>
        </el-form-item>

        <el-form-item label="品牌" prop="brand">
          <el-input
            v-model="form.brand"
            placeholder="请输入品牌（可选）"
            maxlength="50"
          />
        </el-form-item>

        <el-form-item label="型号" prop="model">
          <el-input
            v-model="form.model"
            placeholder="请输入型号（可选）"
            maxlength="50"
          />
        </el-form-item>

        <el-form-item label="颜色" prop="color">
          <el-input
            v-model="form.color"
            placeholder="请输入颜色（可选）"
            maxlength="20"
          />
        </el-form-item>

        <el-form-item label="尺寸" prop="size">
          <el-input
            v-model="form.size"
            placeholder="请输入尺寸（可选）"
            maxlength="50"
          />
        </el-form-item>

        <el-form-item label="购买地点" prop="purchaseLocation">
          <el-input
            v-model="form.purchaseLocation"
            placeholder="请输入购买地点（可选）"
            maxlength="100"
          />
        </el-form-item>

        <el-form-item label="购买时间" prop="purchaseDate">
          <el-date-picker
            v-model="form.purchaseDate"
            type="date"
            placeholder="请选择购买时间（可选）"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <el-form-item label="使用时长" prop="usageDuration">
          <el-input
            v-model="form.usageDuration"
            placeholder="例如：使用1年、几乎未用等（可选）"
            maxlength="50"
          />
        </el-form-item>

        <el-form-item label="商品标签" prop="tags">
          <el-input
            v-model="form.tags"
            placeholder="用逗号分隔多个标签，如：学习用品,电子产品"
            maxlength="200"
          />
        </el-form-item>
      </el-card>

      <!-- 交易信息 -->
      <el-card class="form-section">
        <template #header>
          <span class="section-title">交易信息</span>
        </template>

        <el-form-item label="交易方式" prop="tradeMethods">
          <el-checkbox-group v-model="form.tradeMethods">
            <el-checkbox label="FACE_TO_FACE">面交</el-checkbox>
            <el-checkbox label="MAIL">邮寄</el-checkbox>
            <el-checkbox label="EXPRESS">快递</el-checkbox>
          </el-checkbox-group>
        </el-form-item>

        <el-form-item label="交易地点" prop="location">
          <el-input
            v-model="form.location"
            placeholder="请输入交易地点（可选）"
            maxlength="100"
          />
        </el-form-item>

        <el-form-item label="联系方式" prop="contactInfo">
          <el-input
            v-model="form.contactInfo"
            placeholder="请输入联系方式（可选）"
            maxlength="100"
          />
        </el-form-item>
      </el-card>

      <!-- 提交按钮 -->
      <div class="form-actions">
        <el-button size="large" @click="resetForm">重置</el-button>
        <el-button
          type="primary"
          size="large"
          :loading="submitting"
          @click="submitForm"
        >
          {{ submitting ? '发布中...' : '发布商品' }}
        </el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import api from '@/utils/api'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

// 表单引用
const formRef = ref()

// 表单数据
const form = reactive({
  title: '',
  categoryId: null,
  description: '',
  price: null,
  originalPrice: null,
  condition: '',
  brand: '',
  model: '',
  color: '',
  size: '',
  purchaseLocation: '',
  purchaseDate: '',
  usageDuration: '',
  tags: '',
  tradeMethods: ['FACE_TO_FACE'],
  location: '',
  contactInfo: '',
  images: []
})

// 表单验证规则
const rules = {
  title: [
    { required: true, message: '请输入商品标题', trigger: 'blur' },
    { min: 1, max: 100, message: '标题长度在1到100个字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择商品分类', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入商品描述', trigger: 'blur' },
    { min: 10, max: 2000, message: '描述长度在10到2000个字符', trigger: 'blur' }
  ],
  price: [
    { required: true, message: '请输入出售价格', trigger: 'blur' },
    { type: 'number', min: 0.01, max: 99999999.99, message: '价格必须在0.01到99999999.99之间', trigger: 'blur' }
  ],
  condition: [
    { required: true, message: '请选择商品状况', trigger: 'change' }
  ],
  tradeMethods: [
    { type: 'array', required: true, message: '请选择至少一种交易方式', trigger: 'change' }
  ]
}

// 其他数据
const categories = ref([])
const fileList = ref([])
const submitting = ref(false)

// 上传配置
const uploadUrl = `${import.meta.env.VITE_API_BASE_URL}/files/upload`
const uploadHeaders = {
  'Authorization': `Bearer ${authStore.token}`
}

// 获取商品分类
const fetchCategories = async () => {
  try {
    const response = await api.get('/categories')
    categories.value = response.data.data || []
  } catch (error) {
    console.error('获取分类失败:', error)
    ElMessage.error('获取分类失败')
  }
}

// 上传成功处理
const handleUploadSuccess = (response, file) => {
  if (response.success) {
    form.images.push(response.data.filename)
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(response.message || '图片上传失败')
    // 移除失败的文件
    const index = fileList.value.findIndex(item => item.uid === file.uid)
    if (index > -1) {
      fileList.value.splice(index, 1)
    }
  }
}

// 移除图片处理
const handleRemove = (file) => {
  if (file.response && file.response.success) {
    const filename = file.response.data.filename
    const index = form.images.indexOf(filename)
    if (index > -1) {
      form.images.splice(index, 1)
    }
  }
}

// 上传前检查
const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过5MB!')
    return false
  }
  return true
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    
    if (form.images.length === 0) {
      ElMessage.warning('请至少上传一张商品图片')
      return
    }

    submitting.value = true

    // 准备提交数据
    const submitData = {
      ...form,
      tradeMethods: form.tradeMethods.join(','),
      images: form.images
    }

    const response = await api.post('/products', submitData)
    
    if (response.data.success) {
      ElMessage.success('商品发布成功!')
      
      // 询问是否查看商品详情
      try {
        await ElMessageBox.confirm('商品发布成功！是否查看商品详情？', '发布成功', {
          confirmButtonText: '查看详情',
          cancelButtonText: '继续发布',
          type: 'success'
        })
        
        // 跳转到商品详情页
        router.push(`/products/${response.data.data.id}`)
      } catch {
        // 用户选择继续发布，重置表单
        resetForm()
      }
    } else {
      ElMessage.error(response.data.message || '发布失败')
    }
  } catch (error) {
    console.error('发布商品失败:', error)
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('发布失败，请重试')
    }
  } finally {
    submitting.value = false
  }
}

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  fileList.value = []
  form.images = []
  form.tradeMethods = ['FACE_TO_FACE']
}

// 组件挂载时获取分类数据
onMounted(() => {
  fetchCategories()
})
</script>

<style scoped>
.publish-product {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 28px;
  color: #303133;
  margin-bottom: 10px;
}

.page-header p {
  color: #909399;
  font-size: 16px;
}

.publish-form {
  margin-bottom: 30px;
}

.form-section {
  margin-bottom: 20px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.price-unit {
  margin-left: 10px;
  color: #909399;
}

.form-actions {
  text-align: center;
  padding: 30px 0;
  border-top: 1px solid #ebeef5;
}

.form-actions .el-button {
  margin: 0 10px;
  min-width: 120px;
}

/* 上传组件样式调整 */
:deep(.el-upload--picture-card) {
  width: 100px;
  height: 100px;
}

:deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 100px;
  height: 100px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .publish-product {
    padding: 10px;
  }
  
  .page-header h1 {
    font-size: 24px;
  }
  
  .publish-form {
    :deep(.el-form-item__label) {
      width: 100px !important;
    }
  }
  
  .form-actions .el-button {
    margin: 5px;
    min-width: 100px;
  }
}
</style>