<template>
  <div>
    <el-breadcrumb separator-icon="ArrowRight" style="margin: 16px">
      <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>菜谱管理</el-breadcrumb-item>
      <el-breadcrumb-item>随机菜谱</el-breadcrumb-item>
    </el-breadcrumb>
    <el-card style="margin: 15px; min-height: calc(100vh - 111px)">
      <div>
        <!--    功能区-->
        <div style="margin: 10px 0">
          <!--    搜索区-->
          <div style="margin: 10px 0">
            <el-input v-model="search" clearable placeholder="请输入菜品名称" prefix-icon="Search" style="width: 20%"/>
            <el-button icon="Search" style="margin-left: 5px" type="primary" @click="load"></el-button>
            <el-button icon="refresh-left" style="margin-left: 10px" type="default" @click="reset"></el-button>
            <div style="float: right">
              <el-tooltip content="添加菜品" placement="top">
                <el-button icon="plus" style="width: 50px; margin-right: 10px" type="primary" @click="add"></el-button>
              </el-tooltip>
              <el-tooltip content="随机生成菜谱" placement="top">
                <el-button icon="MagicStick" type="success" @click="generateRandom">随机生成</el-button>
              </el-tooltip>
            </div>
          </div>
        </div>
        <!--    表格-->
        <el-table v-loading="loading" :data="tableData" border max-height="705" style="width: 100%">
          <el-table-column label="#" type="index"/>
          <el-table-column label="菜品名称" prop="dishName" :show-overflow-tooltip="true"/>
          <el-table-column label="分类" prop="category" width="100px"/>
          <el-table-column label="描述" prop="description" :show-overflow-tooltip="true"/>
          <el-table-column label="营养信息" prop="nutritionInfo" :show-overflow-tooltip="true"/>
          <!--      操作栏-->
          <el-table-column label="操作" width="150px">
            <template #default="scope">
              <el-button icon="Edit" type="primary" @click="handleEdit(scope.row)"></el-button>
              <el-popconfirm title="确认删除？" @confirm="handleDelete(scope.row.id)">
                <template #reference>
                  <el-button icon="Delete" type="danger"></el-button>
                </template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>
        <!--分页-->
        <div style="margin: 10px 0">
          <el-pagination
              v-model:currentPage="currentPage"
              :page-size="pageSize"
              :page-sizes="[10, 20]"
              :total="total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
          >
          </el-pagination>
        </div>
        <!--      弹窗-->
        <div>
          <el-dialog v-model="dialogVisible" title="菜品信息" width="50%" @close="cancel">
            <el-form ref="form" :model="form" :rules="rules" label-width="120px">
              <el-form-item label="菜品名称" prop="dishName">
                <el-input v-model="form.dishName" clearable style="width: 50%"></el-input>
              </el-form-item>
              <el-form-item label="分类" prop="category">
                <el-select v-model="form.category" clearable placeholder="请选择分类" style="width: 50%">
                  <el-option label="荤菜" value="荤菜"></el-option>
                  <el-option label="素菜" value="素菜"></el-option>
                  <el-option label="汤品" value="汤品"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="描述" prop="description">
                <el-input v-model="form.description" type="textarea" :rows="3" style="width: 70%"></el-input>
              </el-form-item>
              <el-form-item label="营养信息" prop="nutritionInfo">
                <el-input v-model="form.nutritionInfo" type="textarea" :rows="2" style="width: 70%"></el-input>
              </el-form-item>
            </el-form>
            <template #footer>
              <span class="dialog-footer">
                <el-button @click="cancel">取 消</el-button>
                <el-button type="primary" @click="save">确 定</el-button>
              </span>
            </template>
          </el-dialog>
        </div>
        
        <!-- 随机菜谱展示与保存 -->
        <el-dialog v-model="randomDialogVisible" title="随机菜谱管理" width="80%">
          <div v-if="randomMenus.length > 0">
            <!-- 操作提示和添加按钮 -->
            <div style="margin-bottom: 20px;">
              <el-alert title="提示：您可以编辑、删除菜品，或添加新菜品，然后保存到某一天的菜谱" type="info" show-icon :closable="false" style="margin-bottom: 15px;"/>
              <el-button icon="Plus" type="primary" @click="showAddMenuDialog">添加菜品</el-button>
            </div>
            
            <!-- 可编辑的菜品列表 -->
            <div style="margin-bottom: 20px;">
              <el-row :gutter="15">
                <el-col :span="6" v-for="(menu, index) in randomMenus" :key="index" style="margin-bottom: 15px">
                  <el-card shadow="hover">
                    <template #header>
                      <div style="display: flex; justify-content: space-between; align-items: center;">
                        <el-tag :type="getCategoryType(menu.category)" size="small">{{ menu.category }}</el-tag>
                        <div>
                          <el-button icon="Edit" size="small" type="text" @click="editRandomMenu(index)"></el-button>
                          <el-button icon="Delete" size="small" type="text" style="color: #f56c6c;" @click="removeFromRandom(index)"></el-button>
                        </div>
                      </div>
                    </template>
                    <div>
                      <div style="font-weight: bold; margin-bottom: 8px; font-size: 14px;">{{ menu.dishName }}</div>
                      <p style="font-size: 12px; color: #606266; margin: 5px 0;"><strong>描述：</strong>{{ menu.description }}</p>
                      <p style="font-size: 12px; color: #909399; margin: 5px 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">{{ menu.nutritionInfo }}</p>
                    </div>
                  </el-card>
                </el-col>
              </el-row>
            </div>
            
            <!-- 保存到每周菜谱 -->
            <el-divider></el-divider>
            <div style="margin-top: 20px;">
              <h4 style="margin-bottom: 15px;">保存到每周菜谱</h4>
              <el-form :model="saveToWeekForm" label-width="100px">
                <el-form-item label="选择周">
                  <el-date-picker
                      v-model="saveToWeekForm.selectedWeek"
                      type="week"
                      format="YYYY 第 ww 周"
                      placeholder="选择日期"
                      style="width: 250px;"
                      @change="onWeekChange"
                      :teleported="true">
                  </el-date-picker>
                </el-form-item>
                <el-form-item label="选择星期">
                  <el-select v-model="saveToWeekForm.weekDay" placeholder="请选择星期" style="width: 250px;">
                    <el-option label="周一" :value="1"></el-option>
                    <el-option label="周二" :value="2"></el-option>
                    <el-option label="周三" :value="3"></el-option>
                    <el-option label="周四" :value="4"></el-option>
                    <el-option label="周五" :value="5"></el-option>
                    <el-option label="周六" :value="6"></el-option>
                    <el-option label="周日" :value="7"></el-option>
                  </el-select>
                </el-form-item>
                <el-form-item label="餐次">
                  <el-select v-model="saveToWeekForm.mealType" placeholder="请选择餐次" style="width: 250px;">
                    <el-option label="早餐" value="breakfast"></el-option>
                    <el-option label="午餐" value="lunch"></el-option>
                    <el-option label="晚餐" value="dinner"></el-option>
                  </el-select>
                </el-form-item>
              </el-form>
            </div>
          </div>
          <el-empty v-else description="暂无数据"></el-empty>
          <template #footer>
            <span class="dialog-footer">
              <el-button type="primary" @click="generateRandom">重新生成</el-button>
              <el-button type="success" @click="saveRandomToWeek" :disabled="!canSaveToWeek">保存到每周菜谱</el-button>
              <el-button @click="randomDialogVisible = false">关 闭</el-button>
            </span>
          </template>
        </el-dialog>
        
        <!-- 编辑随机菜品对话框 -->
        <el-dialog v-model="editRandomDialogVisible" title="编辑菜品" width="40%">
          <el-form :model="editingRandomMenu" label-width="100px">
            <el-form-item label="菜品名称">
              <el-input v-model="editingRandomMenu.dishName"></el-input>
            </el-form-item>
            <el-form-item label="分类">
              <el-select v-model="editingRandomMenu.category" style="width: 100%;">
                <el-option label="荤菜" value="荤菜"></el-option>
                <el-option label="素菜" value="素菜"></el-option>
                <el-option label="汤品" value="汤品"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="描述">
              <el-input v-model="editingRandomMenu.description" type="textarea" :rows="3"></el-input>
            </el-form-item>
            <el-form-item label="营养信息">
              <el-input v-model="editingRandomMenu.nutritionInfo" type="textarea" :rows="2"></el-input>
            </el-form-item>
          </el-form>
          <template #footer>
            <span class="dialog-footer">
              <el-button @click="editRandomDialogVisible = false">取 消</el-button>
              <el-button type="primary" @click="saveEditedRandomMenu">确 定</el-button>
            </span>
          </template>
        </el-dialog>
        
        <!-- 添加菜品到随机菜谱对话框 -->
        <el-dialog v-model="addMenuDialogVisible" title="添加菜品到当前菜谱" width="50%">
          <div>
            <el-input v-model="addMenuSearch" placeholder="搜索菜品名称" prefix-icon="Search" style="margin-bottom: 15px;" clearable/>
            <div style="max-height: 400px; overflow-y: auto;">
              <el-row :gutter="10">
                <el-col :span="12" v-for="menu in filteredAvailableMenus" :key="menu.id" style="margin-bottom: 10px;">
                  <el-card shadow="hover" :body-style="{padding: '12px'}" style="cursor: pointer;" @click="addToRandom(menu)">
                    <div style="display: flex; justify-content: space-between; align-items: center;">
                      <div style="flex: 1;">
                        <div style="font-weight: bold; margin-bottom: 5px;">{{ menu.dishName }}</div>
                        <el-tag :type="getCategoryType(menu.category)" size="small">{{ menu.category }}</el-tag>
                      </div>
                      <el-button icon="Plus" type="primary" size="small" circle></el-button>
                    </div>
                  </el-card>
                </el-col>
              </el-row>
            </div>
          </div>
          <template #footer>
            <span class="dialog-footer">
              <el-button @click="addMenuDialogVisible = false">关 闭</el-button>
            </span>
          </template>
        </el-dialog>
      </div>
    </el-card>
  </div>
</template>
<script src="@/assets/js/RandomMenu.js"></script>