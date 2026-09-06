<template>
  <div>
    <el-breadcrumb separator-icon="ArrowRight" style="margin: 16px">
      <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>菜品管理</el-breadcrumb-item>
      <el-breadcrumb-item>每日菜品图片</el-breadcrumb-item>
    </el-breadcrumb>
    <el-card style="margin: 15px; min-height: calc(100vh - 111px)">
      <div>
        <!-- 功能区 -->
        <div style="margin: 10px 0; display: flex; justify-content: space-between; align-items: center;">
          <div>
            <span style="margin-right: 10px; font-weight: bold;">选择日期:</span>
            <el-date-picker
                v-model="selectedDate"
                type="date"
                format="YYYY-MM-DD"
                placeholder="选择日期"
                @change="onDateChange"
                :teleported="true"
                style="width: 200px;">
            </el-date-picker>
          </div>
          <div>
            <el-button icon="Upload" type="primary" @click="openUploadDialog">上传菜品图片</el-button>
          </div>
        </div>

        <!-- 菜品图片展示（按餐次分组） -->
        <div v-if="todayPhotos.length > 0">
          <div v-for="mealType in mealTypes" :key="mealType.value" style="margin-bottom: 30px;">
            <h3 style="margin-bottom: 15px; padding-bottom: 8px; border-bottom: 2px solid #409EFF; color: #409EFF;">
              🍽️ {{ mealType.label }}
            </h3>
            <div v-if="getPhotosByMealType(mealType.value).length > 0">
              <el-row :gutter="20">
                <el-col :span="8" v-for="photo in getPhotosByMealType(mealType.value)" :key="photo.id" style="margin-bottom: 20px;">
                  <el-card shadow="hover">
                    <div style="text-align: center;">
                      <el-image
                          :src="getPhotoUrl(photo.photoUrl)"
                          fit="cover"
                          style="height: 200px; width: 100%;"
                          :preview-src-list="getPhotosByMealType(mealType.value).map(p => getPhotoUrl(p.photoUrl))">
                        <template #error>
                          <div style="display: flex; justify-content: center; align-items: center; width: 100%; height: 200px; background: #f5f7fa; color: #909399;">
                            <el-icon size="40"><Picture /></el-icon>
                          </div>
                        </template>
                      </el-image>
                    </div>
                    <div v-if="photo.description" style="margin-top: 8px; color: #606266; font-size: 13px;">
                      <el-icon><ChatDotRound /></el-icon> {{ photo.description }}
                    </div>
                    <div style="margin-top: 8px; display: flex; justify-content: space-between; align-items: center;">
                      <span style="color: #909399; font-size: 12px;">{{ photo.uploader || '未知' }} | {{ photo.createTime }}</span>
                      <el-button type="danger" size="small" icon="Delete" @click="deletePhoto(photo.id)">删除</el-button>
                    </div>
                  </el-card>
                </el-col>
              </el-row>
            </div>
            <el-empty v-else :description="mealType.label + '暂无图片'" :image-size="60"></el-empty>
          </div>
        </div>
        <el-empty v-else description="该日期暂无菜品图片，请上传" :image-size="100"></el-empty>

        <!-- 上传对话框 -->
        <el-dialog v-model="uploadDialogVisible" title="上传菜品图片" width="500px">
          <el-form :model="uploadForm" label-width="100px">
            <el-form-item label="日期">
              <el-input v-model="uploadForm.photoDate" disabled></el-input>
            </el-form-item>
            <el-form-item label="餐次">
              <el-select v-model="uploadForm.mealType" placeholder="请选择餐次" style="width: 100%;">
                <el-option v-for="meal in mealTypes" :key="meal.value" :label="meal.label" :value="meal.value"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="描述">
              <el-input v-model="uploadForm.description" type="textarea" :rows="3" placeholder="请输入菜品描述（可选）"></el-input>
            </el-form-item>
            <el-form-item label="菜品图片">
              <el-upload
                  :auto-upload="false"
                  multiple
                  accept="image/*"
                  :on-change="handleFileChange"
                  :on-remove="handleFileRemove"
                  :before-upload="beforeUpload"
                  :file-list="fileList"
                  list-type="picture-card">
                <el-icon><Plus /></el-icon>
                <template #tip>
                  <div class="el-upload__tip">可上传多张图片，每张不超过5MB</div>
                </template>
              </el-upload>
            </el-form-item>
          </el-form>
          <template #footer>
            <span class="dialog-footer">
              <el-button @click="uploadDialogVisible = false">取 消</el-button>
              <el-button type="primary" @click="submitUpload" :loading="uploading">上 传</el-button>
            </span>
          </template>
        </el-dialog>
      </div>
    </el-card>
  </div>
</template>
<script src="@/assets/js/DailyMenuPhotoAdmin.js"></script>
<style scoped>
</style>