<template>
  <div>
    <el-breadcrumb separator-icon="ArrowRight" style="margin: 16px">
      <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>门店管理</el-breadcrumb-item>
      <el-breadcrumb-item>房间信息</el-breadcrumb-item>
    </el-breadcrumb>
    <el-card style="margin: 15px; min-height: calc(100vh - 111px)">
      <div>
        <!--    功能区-->
        <div style="margin: 10px 0">
          <!--    搜索区-->
          <div style="margin: 10px 0">
            <el-input v-model="search" clearable placeholder="请输入房间号" prefix-icon="Search" style="width: 20%"/>
            <el-button icon="Search" style="margin-left: 5px" type="primary" @click="load"></el-button>
            <el-button icon="refresh-left" style="margin-left: 10px" type="default" @click="reset"></el-button>
            <div style="float: right; display: flex; gap: 8px;">
              <el-tooltip content="智能推荐床位" placement="top">
                <el-button icon="MagicStick" type="success" @click="openRecommendDialog">智能推荐</el-button>
              </el-tooltip>
              <el-tooltip content="添加" placement="top">
                <el-button icon="plus" style="width: 50px" type="primary" @click="add"></el-button>
              </el-tooltip>
            </div>
          </div>
        </div>
        <!--    表格-->
        <el-table v-loading="loading" :data="tableData" :expand-row-keys="expandedRowKeys" row-key="centerRoomId" border max-height="705" style="width: 100%" @expand-change="onExpandChange">
          <el-table-column label="#" type="index"/>
          <!-- 床位展开-->
          <el-table-column type="expand">
            <template #default="props">
              <el-form inline label-position="left">
                <el-form-item v-for="bed in (props.row.beds || [])" :key="bed.bedNo" :label="`床位${bed.bedNo}`" class="item">
                  <template #default="scope">
                  <el-tag v-if="bed.occupantUsername" disable-transitions type="primary"
                  >{{ formatOccupantLabel(bed) }}
                  </el-tag>
                  <div class="el-form--inline-icon">
                      <el-icon v-if="!bed.occupantUsername" @click="plusIcon(bed, props.row)">
                        <plus/>
                      </el-icon>
                      <div v-if="bed.occupantUsername" class="el-form--inline-icon">
                        <el-icon @click="detailIcon(bed, props.row)">
                          <more-filled/>
                        </el-icon>
                        <el-icon @click="editIcon(bed, props.row)">
                          <edit/>
                        </el-icon>
                        <el-popconfirm title="确认删除？" @confirm="deleteStuBed(bed, props.row)">
                          <template #reference>
                            <el-icon>
                              <delete/>
                            </el-icon>
                          </template>
                        </el-popconfirm>
                      </div>
                    </div>
                  </template>
                </el-form-item>
              </el-form>
            </template>
          </el-table-column>
          <el-table-column label="房间号" prop="centerRoomId" sortable/>
          <el-table-column label="分店名称" prop="centerBuildingId" sortable :formatter="formatBuildingName"/>
          <el-table-column label="楼层" prop="floorNum" sortable/>
          <el-table-column label="最多可住人数" prop="maxCapacity"/>
          <el-table-column
              :filter-method="filterTag"
              :filters="capacityFilters"
              filter-placement="bottom-end"
              label="已住人数"
              prop="currentCapacity"
              sortable
          />
          <!--      操作栏-->
          <el-table-column label="操作" width="130px">
            <template #default="scope">
              <el-button icon="Edit" type="primary" @click="handleEdit(scope.row)"
              ></el-button>
              <el-popconfirm title="确认删除？" @confirm="handleDelete(scope.row.centerRoomId)">
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
          <el-dialog v-model="dialogVisible" title="操作" width="30%" @close="cancel">
            <el-form ref="form" :model="form" :rules="rules" label-width="120px">
              <el-form-item label="分店名称" prop="centerBuildingId">
                <el-select v-model="form.centerBuildingId" placeholder="请选择分店" style="width: 80%">
                  <el-option
                    v-for="item in buildingList"
                    :key="item.centerBuildingId"
                    :label="item.centerBuildingName"
                    :value="item.centerBuildingId">
                  </el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="楼层数" prop="floorNum">
                <el-input v-model.number="form.floorNum" style="width: 80%"></el-input>
              </el-form-item>
              <el-form-item label="房间号" prop="centerRoomId">
                <el-input v-model="form.centerRoomId" :disabled="disabled" style="width: 80%"></el-input>
              </el-form-item>
              <el-form-item label="最多可住人数" prop="maxCapacity">
                <el-input v-model.number="form.maxCapacity" style="width: 80%"></el-input>
              </el-form-item>
              <el-form-item label="已住人数" prop="currentCapacity">
                <el-input v-model.number="form.currentCapacity" style="width: 80%"></el-input>
              </el-form-item>
            </el-form>
            <template #footer>
              <span class="dialog-footer">
                <el-button @click="cancel">取 消</el-button>
                <el-button type="primary" @click="save">确 定</el-button>
              </span>
            </template>
          </el-dialog>
          <!-- 床位 弹窗-->
          <el-dialog v-model="bedDialog" title="操作" width="30%" @close="cancel">
            <el-form ref="form" :model="form" :rules="rules" label-width="120px">
              <el-form-item label="分店名称" prop="centerBuildingId">
                <el-select v-model="form.centerBuildingId" disabled placeholder="请选择分店" style="width: 80%">
                  <el-option
                    v-for="item in buildingList"
                    :key="item.centerBuildingId"
                    :label="item.centerBuildingName"
                    :value="item.centerBuildingId">
                  </el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="楼层数" prop="floorNum">
                <el-input v-model.number="form.floorNum" disabled="true" style="width: 80%"></el-input>
              </el-form-item>
              <el-form-item label="房间号" prop="centerRoomId">
                <el-input v-model="form.centerRoomId" disabled="true" style="width: 80%"></el-input>
              </el-form-item>
              <el-form-item label="床位号" prop="bedNo">
                <el-input v-model.number="form.bedNo" disabled="true" style="width: 80%"></el-input>
              </el-form-item>
              <el-form-item label="床位(学号)" prop="bedStudentUsername">
                <el-input v-model="form.bedStudentUsername" placeholder="请输入学号" style="width: 80%"></el-input>
              </el-form-item>
            </el-form>
            <template #footer>
              <span class="dialog-footer">
                <el-button @click="cancel">取 消</el-button>
                <el-button v-if="this.judge === false" type="primary" @click="addStuBed">确 定</el-button>
                <el-button v-if="this.judge === true" type="primary" @click="editStuBed">确 定</el-button>
              </span>
            </template>
          </el-dialog>
          <!-- 学生信息弹窗-->
          <el-dialog v-model="stuInfoDialog" title="学生信息" width="20%" @close="cancel">
            <el-form ref="form" :model="form" label-width="120px">
              <el-form-item label="学号：" prop="username">
                <template #default="scope">
                  <span>{{ form.username }}</span>
                </template>
              </el-form-item>
              <el-form-item label="姓名：" prop="name">
                <template #default="scope">
                  <span>{{ form.name }}</span>
                </template>
              </el-form-item>
              <el-form-item label="年龄：" prop="age">
                <template #default="scope">
                  <span>{{ form.age }}</span>
                </template>
              </el-form-item>
              <el-form-item label="性别：" prop="gender">
                <template #default="scope">
                  <span>{{ form.gender }}</span>
                </template>
              </el-form-item>
              <el-form-item label="手机号：" prop="phoneNum">
                <template #default="scope">
                  <span>{{ form.phoneNum }}</span>
                </template>
              </el-form-item>
              <el-form-item label="邮箱地址：" prop="email">
                <template #default="scope">
                  <span>{{ form.email }}</span>
                </template>
              </el-form-item>
            </el-form>
          </el-dialog>
          <!-- 智能排床推荐弹窗 -->
          <el-dialog v-model="recommendDialog" title="智能排床/调宿推荐" width="55%" @close="cancel">
            <el-form :model="recommendForm" label-width="110px" style="margin-bottom: 12px;">
              <el-form-item label="学生学号">
                <el-input v-model="recommendForm.studentUsername" placeholder="请输入学生学号" style="width: 260px"></el-input>
              </el-form-item>
              <el-form-item label="分店" required>
                <el-select v-model="recommendForm.centerBuildingId" placeholder="请选择分店" style="width: 260px">
                  <el-option
                    v-for="item in buildingList"
                    :key="item.centerBuildingId"
                    :label="item.centerBuildingName"
                    :value="item.centerBuildingId">
                  </el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="性别(可选)">
                <el-select v-model="recommendForm.gender" clearable placeholder="自动读取或手动选择" style="width: 260px">
                  <el-option label="男" value="男"></el-option>
                  <el-option label="女" value="女"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="年级(可选)">
                <el-input v-model="recommendForm.grade" placeholder="如：2023级" style="width: 260px"></el-input>
              </el-form-item>
              <el-form-item label="作息偏好(可选)">
                <el-input v-model="recommendForm.scheduleType" placeholder="如：早睡早起" style="width: 260px"></el-input>
              </el-form-item>
              <el-form-item label="过敏偏好(可选)">
                <el-input v-model="recommendForm.allergyPreference" placeholder="如：花粉过敏" style="width: 260px"></el-input>
              </el-form-item>
              <el-form-item label="饮食偏好(可选)">
                <el-input v-model="recommendForm.dietPreference" placeholder="如：清淡" style="width: 260px"></el-input>
              </el-form-item>
            </el-form>

            <div style="margin-bottom: 12px;">
              <el-button type="primary" :loading="recommendLoading" @click="fetchRecommendBeds">生成推荐</el-button>
            </div>

            <el-table :data="recommendList" border max-height="320" v-loading="recommendLoading">
              <el-table-column prop="centerRoomId" label="房间号" width="130"></el-table-column>
              <el-table-column prop="centerBuildingId" label="分店" width="120" :formatter="formatBuildingName"></el-table-column>
              <el-table-column prop="floorNum" label="楼层" width="80"></el-table-column>
              <el-table-column prop="bedNum" label="床位号" width="90"></el-table-column>
              <el-table-column prop="score" label="匹配分" width="90"></el-table-column>
              <el-table-column label="推荐理由">
                <template #default="scope">
                  <el-tag
                    v-for="(reason, idx) in (scope.row.reasons || [])"
                    :key="idx"
                    size="small"
                    style="margin-right: 6px; margin-bottom: 4px;">
                    {{ reason }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="120" fixed="right">
                <template #default="scope">
                  <el-button type="success" size="small" @click="applyRecommendBed(scope.row)">套用</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-dialog>
        </div>
      </div>
    </el-card>
  </div>
</template>
<script src="@/assets/js/RoomInfo.js"></script>
<style scoped>@import '../assets/css/RoomInfo.css';</style>