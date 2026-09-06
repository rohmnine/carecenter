<template>
  <div>
    <el-breadcrumb separator-icon="ArrowRight" style="margin: 16px">
      <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>菜谱管理</el-breadcrumb-item>
      <el-breadcrumb-item>每周菜谱</el-breadcrumb-item>
    </el-breadcrumb>
    <el-card style="margin: 15px; min-height: calc(100vh - 111px)">
      <div>
        <!--    功能区-->
        <div style="margin: 10px 0">
          <div style="margin: 10px 0; display: flex; justify-content: space-between; align-items: center;">
          <div>
            <span style="margin-right: 10px; font-weight: bold;">选择周:</span>
            <el-date-picker
                v-model="selectedWeek"
                type="week"
                format="YYYY 第 ww 周"
                placeholder="选择日期"
                @change="loadWeekMenu"
                :default-value="new Date()"
                style="width: 250px;"
                :teleported="true">
            </el-date-picker>
            <el-tag type="info" style="margin-left: 10px;">当前显示: {{ formatWeekRange() }}</el-tag>
          </div>
          <div v-if="identity === 'admin'">
            <el-button icon="Plus" type="primary" @click="openSetupDialog">设置本周菜谱</el-button>
            <el-button icon="Delete" type="danger" @click="clearWeekMenu">清空本周菜谱</el-button>
          </div>
        </div>
        </div>

        <!-- 每周菜谱展示 -->
        <div v-if="weekMenuData && Object.keys(weekMenuData).length > 0">
          <el-row :gutter="15">
            <el-col :span="8" v-for="day in weekDays" :key="day.value" style="margin-bottom: 15px;">
              <el-card shadow="hover">
                <template #header>
                  <div style="text-align: center; font-weight: bold; font-size: 16px;">
                    {{ day.label }}
                  </div>
                </template>
                <div>
                  <!-- 早餐 -->
                  <div v-if="weekMenuData[day.value] && weekMenuData[day.value]['breakfast']">
                    <h4 style="color: #409EFF; margin: 10px 0;">🌅 早餐</h4>
                    <el-card v-for="item in weekMenuData[day.value]['breakfast']" :key="item.id" class="menu-item-card">
                      <div style="font-weight: bold;">{{ item.menu.dishName }}</div>
                      <div style="font-size: 12px; color: #909399; margin-top: 5px;">
                        {{ item.menu.category }}
                      </div>
                    </el-card>
                    <div style="font-size: 12px; color: #67C23A; margin: 6px 0 10px 0;">主食：米饭（固定）</div>
                  </div>

                  <!-- 午餐 -->
                  <div v-if="weekMenuData[day.value] && weekMenuData[day.value]['lunch']">
                    <h4 style="color: #67C23A; margin: 10px 0;">🌞 午餐</h4>
                    <el-card v-for="item in weekMenuData[day.value]['lunch']" :key="item.id" class="menu-item-card">
                      <div style="font-weight: bold;">{{ item.menu.dishName }}</div>
                      <div style="font-size: 12px; color: #909399; margin-top: 5px;">
                        {{ item.menu.category }}
                      </div>
                    </el-card>
                    <div style="font-size: 12px; color: #67C23A; margin: 6px 0 10px 0;">主食：米饭（固定）</div>
                  </div>

                  <!-- 晚餐 -->
                  <div v-if="weekMenuData[day.value] && weekMenuData[day.value]['dinner']">
                    <h4 style="color: #E6A23C; margin: 10px 0;">🌙 晚餐</h4>
                    <el-card v-for="item in weekMenuData[day.value]['dinner']" :key="item.id" class="menu-item-card">
                      <div style="font-weight: bold;">{{ item.menu.dishName }}</div>
                      <div style="font-size: 12px; color: #909399; margin-top: 5px;">
                        {{ item.menu.category }}
                      </div>
                    </el-card>
                    <div style="font-size: 12px; color: #67C23A; margin: 6px 0 10px 0;">主食：米饭（固定）</div>
                  </div>

                  <el-empty v-if="!weekMenuData[day.value] || Object.keys(weekMenuData[day.value]).length === 0"
                           description="暂无菜谱" :image-size="60"></el-empty>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>
        <el-empty v-else description="暂无本周菜谱，请管理员设置" :image-size="100"></el-empty>

        <!-- 设置菜谱对话框 -->
        <el-dialog v-model="setupDialogVisible" title="设置每周菜谱" width="80%" @close="closeSetupDialog">
          <div>
            <el-form :model="setupForm" label-width="100px">
              <el-form-item label="周开始日期">
                <el-date-picker
                    v-model="setupForm.weekStartDate"
                    type="date"
                    format="YYYY-MM-DD"
                    placeholder="选择日期"
                    value-format="YYYY-MM-DD"
                    style="width: 100%;"
                    :teleported="true"
                    disabled>
                </el-date-picker>
              </el-form-item>
              <el-form-item label="周结束日期">
                <el-date-picker
                    v-model="setupForm.weekEndDate"
                    type="date"
                    format="YYYY-MM-DD"
                    placeholder="选择日期"
                    value-format="YYYY-MM-DD"
                    style="width: 100%;"
                    :teleported="true"
                    disabled>
                </el-date-picker>
              </el-form-item>
            </el-form>

            <el-tabs v-model="activeDay" type="card">
              <el-tab-pane v-for="day in weekDays" :key="day.value" :label="day.label" :name="day.value">
                <div style="margin: 15px 0;">
                  <h4>早餐</h4>
                  <el-select v-model="setupForm.menus[day.value]['breakfast']" multiple placeholder="请选择早餐菜品" style="width: 100%;">
                    <el-option v-for="menu in filteredMenus" :key="menu.id" :label="menu.dishName" :value="menu.id">
                      <span>{{ menu.dishName }}</span>
                      <span style="float: right; color: #8492a6; font-size: 13px;">{{ menu.category }}</span>
                    </el-option>
                  </el-select>
                </div>
                <div style="margin: 15px 0;">
                  <h4>午餐</h4>
                  <el-select v-model="setupForm.menus[day.value]['lunch']" multiple placeholder="请选择午餐菜品" style="width: 100%;">
                    <el-option v-for="menu in filteredMenus" :key="menu.id" :label="menu.dishName" :value="menu.id">
                      <span>{{ menu.dishName }}</span>
                      <span style="float: right; color: #8492a6; font-size: 13px;">{{ menu.category }}</span>
                    </el-option>
                  </el-select>
                </div>
                <div style="margin: 15px 0;">
                  <h4>晚餐</h4>
                  <el-select v-model="setupForm.menus[day.value]['dinner']" multiple placeholder="请选择晚餐菜品" style="width: 100%;">
                    <el-option v-for="menu in filteredMenus" :key="menu.id" :label="menu.dishName" :value="menu.id">
                      <span>{{ menu.dishName }}</span>
                      <span style="float: right; color: #8492a6; font-size: 13px;">{{ menu.category }}</span>
                    </el-option>
                  </el-select>
                </div>
              </el-tab-pane>
            </el-tabs>
          </div>
          <template #footer>
            <span class="dialog-footer">
              <el-button @click="closeSetupDialog">取 消</el-button>
              <el-button type="primary" @click="saveWeekMenu">保存菜谱</el-button>
            </span>
          </template>
        </el-dialog>
      </div>
    </el-card>
  </div>
</template>
<script src="@/assets/js/WeeklyMenu.js"></script>
<style scoped>
.menu-item-card {
  margin: 5px 0;
  padding: 8px;
}
</style>