<template>
  <div>
    <el-breadcrumb separator-icon="ArrowRight" style="margin: 16px">
      <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>房间管理</el-breadcrumb-item>
      <el-breadcrumb-item>床位可视化</el-breadcrumb-item>
    </el-breadcrumb>
    <el-card style="margin: 15px; min-height: calc(100vh - 111px)">
      <div>
        <!-- 房间选择区 -->
        <div style="margin: 10px 0">
          <el-input 
            v-model="searchRoomId" 
            clearable 
            placeholder="请输入房间号" 
            prefix-icon="Search" 
            style="width: 20%"
          />
          <el-button icon="Search" style="margin-left: 5px" type="primary" @click="loadRoom"></el-button>
          <el-button icon="refresh-left" style="margin-left: 10px" type="default" @click="reset"></el-button>
        </div>

        <!-- 房间信息卡片 -->
        <el-card v-if="currentRoom" class="room-info-card" shadow="hover">
          <template #header>
            <div class="room-header">
              <span class="room-title">房间 {{ currentRoom.centerRoomId }}</span>
              <el-tag type="info">楼栋 {{ currentRoom.centerBuildingId }}</el-tag>
              <el-tag type="warning" style="margin-left: 10px">{{ currentRoom.floorNum }} 楼</el-tag>
              <el-tag :type="currentRoom.currentCapacity >= currentRoom.maxCapacity ? 'danger' : 'success'" style="margin-left: 10px">
                {{ currentRoom.currentCapacity }}/{{ currentRoom.maxCapacity }} 人
              </el-tag>
            </div>
          </template>

          <!-- 床位可视化区域 -->
          <div class="bed-visualization-container">
            <div class="bed-layout">
              <!-- 床位1 (上下铺) -->
              <div class="bunk-bed" @click="handleBedClick(1, 'upper')">
                <div 
                  :class="['bed-unit', 'upper-bed', getBedStatus(1, 'upper')]"
                  @mouseenter="showBedTooltip(1, 'upper')"
                  @mouseleave="hideTooltip"
                >
                  <div class="bed-number">1号床 (上铺)</div>
                  <div v-if="currentRoom.firstBed" class="bed-student">
                    <el-icon><User /></el-icon>
                    {{ currentRoom.firstBed }}
                  </div>
                  <div v-else class="bed-empty">
                    <el-icon><Plus /></el-icon>
                    空床位
                  </div>
                </div>
                <div 
                  :class="['bed-unit', 'lower-bed', getBedStatus(1, 'lower')]"
                  @click.stop="handleBedClick(1, 'lower')"
                  @mouseenter="showBedTooltip(1, 'lower')"
                  @mouseleave="hideTooltip"
                >
                  <div class="bed-number">1号床 (下铺)</div>
                  <div v-if="lowerBeds[0]" class="bed-student">
                    <el-icon><User /></el-icon>
                    {{ lowerBeds[0] }}
                  </div>
                  <div v-else class="bed-empty">
                    <el-icon><Plus /></el-icon>
                    空床位
                  </div>
                </div>
                <div class="bed-ladder"></div>
              </div>

              <!-- 床位2 (上下铺) -->
              <div class="bunk-bed" @click="handleBedClick(2, 'upper')">
                <div 
                  :class="['bed-unit', 'upper-bed', getBedStatus(2, 'upper')]"
                  @mouseenter="showBedTooltip(2, 'upper')"
                  @mouseleave="hideTooltip"
                >
                  <div class="bed-number">2号床 (上铺)</div>
                  <div v-if="currentRoom.secondBed" class="bed-student">
                    <el-icon><User /></el-icon>
                    {{ currentRoom.secondBed }}
                  </div>
                  <div v-else class="bed-empty">
                    <el-icon><Plus /></el-icon>
                    空床位
                  </div>
                </div>
                <div 
                  :class="['bed-unit', 'lower-bed', getBedStatus(2, 'lower')]"
                  @click.stop="handleBedClick(2, 'lower')"
                  @mouseenter="showBedTooltip(2, 'lower')"
                  @mouseleave="hideTooltip"
                >
                  <div class="bed-number">2号床 (下铺)</div>
                  <div v-if="lowerBeds[1]" class="bed-student">
                    <el-icon><User /></el-icon>
                    {{ lowerBeds[1] }}
                  </div>
                  <div v-else class="bed-empty">
                    <el-icon><Plus /></el-icon>
                    空床位
                  </div>
                </div>
                <div class="bed-ladder"></div>
              </div>

              <!-- 床位3 (上下铺) -->
              <div class="bunk-bed" @click="handleBedClick(3, 'upper')">
                <div 
                  :class="['bed-unit', 'upper-bed', getBedStatus(3, 'upper')]"
                  @mouseenter="showBedTooltip(3, 'upper')"
                  @mouseleave="hideTooltip"
                >
                  <div class="bed-number">3号床 (上铺)</div>
                  <div v-if="currentRoom.thirdBed" class="bed-student">
                    <el-icon><User /></el-icon>
                    {{ currentRoom.thirdBed }}
                  </div>
                  <div v-else class="bed-empty">
                    <el-icon><Plus /></el-icon>
                    空床位
                  </div>
                </div>
                <div 
                  :class="['bed-unit', 'lower-bed', getBedStatus(3, 'lower')]"
                  @click.stop="handleBedClick(3, 'lower')"
                  @mouseenter="showBedTooltip(3, 'lower')"
                  @mouseleave="hideTooltip"
                >
                  <div class="bed-number">3号床 (下铺)</div>
                  <div v-if="lowerBeds[2]" class="bed-student">
                    <el-icon><User /></el-icon>
                    {{ lowerBeds[2] }}
                  </div>
                  <div v-else class="bed-empty">
                    <el-icon><Plus /></el-icon>
                    空床位
                  </div>
                </div>
                <div class="bed-ladder"></div>
              </div>

              <!-- 床位4 (上下铺，如果maxCapacity > 6) -->
              <div v-if="currentRoom.maxCapacity > 6" class="bunk-bed" @click="handleBedClick(4, 'upper')">
                <div 
                  :class="['bed-unit', 'upper-bed', getBedStatus(4, 'upper')]"
                  @mouseenter="showBedTooltip(4, 'upper')"
                  @mouseleave="hideTooltip"
                >
                  <div class="bed-number">4号床 (上铺)</div>
                  <div v-if="currentRoom.fourthBed" class="bed-student">
                    <el-icon><User /></el-icon>
                    {{ currentRoom.fourthBed }}
                  </div>
                  <div v-else class="bed-empty">
                    <el-icon><Plus /></el-icon>
                    空床位
                  </div>
                </div>
                <div 
                  :class="['bed-unit', 'lower-bed', getBedStatus(4, 'lower')]"
                  @click.stop="handleBedClick(4, 'lower')"
                  @mouseenter="showBedTooltip(4, 'lower')"
                  @mouseleave="hideTooltip"
                >
                  <div class="bed-number">4号床 (下铺)</div>
                  <div v-if="lowerBeds[3]" class="bed-student">
                    <el-icon><User /></el-icon>
                    {{ lowerBeds[3] }}
                  </div>
                  <div v-else class="bed-empty">
                    <el-icon><Plus /></el-icon>
                    空床位
                  </div>
                </div>
                <div class="bed-ladder"></div>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 空状态 -->
        <el-empty v-else description="请输入房间号查询床位信息" />

        <!-- 床位操作对话框 -->
        <el-dialog v-model="bedDialog" :title="dialogTitle" width="30%" @close="cancelBedDialog">
          <el-form ref="bedForm" :model="bedForm" :rules="bedRules" label-width="120px">
            <el-form-item label="房间号">
              <el-input v-model="bedForm.centerRoomId" disabled></el-input>
            </el-form-item>
            <el-form-item label="床位号">
              <el-input v-model="bedForm.bedDisplay" disabled></el-input>
            </el-form-item>
            <el-form-item v-if="!isViewMode" label="学号" prop="studentId">
              <el-input v-model="bedForm.studentId" placeholder="请输入学号"></el-input>
            </el-form-item>
            <el-form-item v-if="isViewMode && studentInfo" label="姓名">
              <span>{{ studentInfo.name }}</span>
            </el-form-item>
            <el-form-item v-if="isViewMode && studentInfo" label="性别">
              <span>{{ studentInfo.gender }}</span>
            </el-form-item>
            <el-form-item v-if="isViewMode && studentInfo" label="年龄">
              <span>{{ studentInfo.age }}</span>
            </el-form-item>
            <el-form-item v-if="isViewMode && studentInfo" label="手机号">
              <span>{{ studentInfo.phoneNum }}</span>
            </el-form-item>
            <el-form-item v-if="isViewMode && studentInfo" label="邮箱">
              <span>{{ studentInfo.email }}</span>
            </el-form-item>
          </el-form>
          <template #footer>
            <span class="dialog-footer">
              <el-button @click="cancelBedDialog">取消</el-button>
              <el-button v-if="!isViewMode && !currentBedStudent" type="primary" @click="assignBed">分配床位</el-button>
              <el-button v-if="!isViewMode && currentBedStudent" type="primary" @click="updateBed">更新床位</el-button>
              <el-button v-if="!isViewMode && currentBedStudent" type="danger" @click="removeBed">移除学生</el-button>
            </span>
          </template>
        </el-dialog>
      </div>
    </el-card>
  </div>
</template>

<script src="@/assets/js/BedVisualization.js"></script>
<style scoped>@import '../assets/css/BedVisualization.css';</style>