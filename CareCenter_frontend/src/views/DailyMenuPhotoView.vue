<template>
  <div>
    <el-breadcrumb separator-icon="ArrowRight" style="margin: 16px">
      <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>今日菜品</el-breadcrumb-item>
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
            <el-button type="primary" @click="loadToday">查看今日</el-button>
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
                    <div v-if="photo.description" style="margin-top: 8px; padding: 8px; background: #f0f9eb; border-radius: 4px; color: #606266; font-size: 13px;">
                      <el-icon><ChatDotRound /></el-icon> {{ photo.description }}
                    </div>
                  </el-card>
                </el-col>
              </el-row>
            </div>
            <el-empty v-else :description="mealType.label + '暂无图片'" :image-size="60"></el-empty>
          </div>
        </div>
        <el-empty v-else description="今日暂无菜品图片" :image-size="100"></el-empty>
      </div>
    </el-card>
  </div>
</template>
<script src="@/assets/js/DailyMenuPhotoView.js"></script>
<style scoped>
</style>