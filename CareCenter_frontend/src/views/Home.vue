<template>
    <el-card style="margin: 15px; min-height: calc(100vh - 80px)">
        <!--    头部数据-->
        <div>
            <el-row :gutter="20" class="topInfo">
                <el-col :span="5">
                    <div id="stuNumDiv" class="el-colDiv">
                        <div id="ssv1-main-text" class="nowDiv">实时</div>
                        <span class="title">学生统计</span><br/>
                        <span class="digital">{{ this.studentNum }}</span><br/>
                        <span class="last-span">当前分类总记录数</span>
                    </div>
                </el-col>
                <el-col :span="5">
                    <div id="haveRoomDiv" class="el-colDiv">
                        <div id="ssv2-main-text" class="nowDiv">实时</div>
                        <span class="title">住宿人数</span><br/>
                        <span class="digital">{{ this.haveRoomStudentNum }}</span><br/>
                        <span class="last-span">当前分类总记录数</span>
                    </div>
                </el-col>
                <el-col :span="5">
                    <div id="emptyRoom" class="el-colDiv">
                        <div id="ssv4-main-text" class="nowDiv">实时</div>
                        <span class="title">空床位统计</span><br/>
                        <span class="digital">{{ this.noFullRoomNum }}</span><br/>
                        <span class="last-span">当前分类总记录数</span>
                    </div>
                </el-col>
                <el-col :span="5">
                    <div id="mealReservationDiv" class="el-colDiv">
                        <div class="nowDiv">实时</div>
                        <span class="title">今日就餐预约</span><br/>
                        <span class="digital">{{ this.todayMealReservation }}</span><br/>
                        <span class="last-span">家长预约总数</span>
                    </div>
                </el-col>
                <el-col :span="4">
                    <div id="mealConfirmDiv" class="el-colDiv">
                        <div class="nowDiv">实时</div>
                        <span class="title">今日已就餐</span><br/>
                        <span class="digital">{{ this.todayMealConfirmed }}</span><br/>
                        <span class="last-span">已确认就餐数</span>
                    </div>
                </el-col>
            </el-row>
        </div>
        <!-- 下部-->
        <div style="display: flex;width: 100%;margin-top: 40px;align-items: center;justify-content: center;">
            <!--   左侧 分店通告-->
            <div style="margin-right: 5%">
                <span style="font-size: 22px;display: block;margin-bottom: 30px;margin-left: 10px;">公告</span>
                <el-timeline>
                    <el-timeline-item v-for="(activity, index) in activities.slice(0, 8)" :key="index"
                                      :timestamp="activity.releaseTime">
                        <span style="font-size: 15px">{{ activity.title }}</span><br/>
<!--                        <span style="font-size: 10px">{{ activity.content }}</span>-->
                    </el-timeline-item>
                </el-timeline>
            </div>
            <!--   中部-->
            <div style="height: 588px">
        <span style="
            font-size: 22px;
            display: block;
            margin-bottom: 30px;
            margin-left: 10px;
          ">学生人数分布</span>
                <home_echarts/>
            </div>
            <!--  右侧-->
            <div style="margin-left: 5%">
                <!--    日历组件-->
                <el-card style="width: 380px; max-height: 440px; margin-top: 17px">
                    <Calender/>
                </el-card>
            </div>
        </div>
    </el-card>
</template>

<script src="@/assets/js/Home.js"></script>
<style scoped>@import '../assets/css/Home.css';</style>