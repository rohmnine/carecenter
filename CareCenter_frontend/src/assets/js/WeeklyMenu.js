import request from "@/utils/request";
import {ElMessage, ElMessageBox} from "element-plus";
import { getCurrentIdentity } from "@/utils/sessionHelper";

export default {
    name: "WeeklyMenu",
    data() {
        return {
            selectedWeek: null,
            weekMenuData: {},
            setupDialogVisible: false,
            allMenus: [],
            activeDay: 'Monday',
            identity: '',
            setupForm: {
                weekStartDate: '',
                weekEndDate: '',
                menus: {
                    'Monday': {breakfast: [], lunch: [], dinner: []},
                    'Tuesday': {breakfast: [], lunch: [], dinner: []},
                    'Wednesday': {breakfast: [], lunch: [], dinner: []},
                    'Thursday': {breakfast: [], lunch: [], dinner: []},
                    'Friday': {breakfast: [], lunch: [], dinner: []},
                    'Saturday': {breakfast: [], lunch: [], dinner: []},
                    'Sunday': {breakfast: [], lunch: [], dinner: []}
                }
            },
            weekDays: [
                {label: '星期一', value: 'Monday'},
                {label: '星期二', value: 'Tuesday'},
                {label: '星期三', value: 'Wednesday'},
                {label: '星期四', value: 'Thursday'},
                {label: '星期五', value: 'Friday'},
                {label: '星期六', value: 'Saturday'},
                {label: '星期日', value: 'Sunday'}
            ]
        }
    },
    computed: {
        filteredMenus() {
            // Filter out "主食" (staple food) category
            return this.allMenus.filter(menu => menu.category !== '主食')
        }
    },
    created() {
        this.identity = getCurrentIdentity()
        this.selectedWeek = new Date()
        this.loadWeekMenu()
        this.loadAllMenus()
    },
    methods: {
        loadWeekMenu() {
            if (!this.selectedWeek) return
            
            const weekDates = this.getWeekDates(this.selectedWeek)
            
            request.get("/weeklyMenu/byDateRange", {
                params: {
                    startDate: weekDates.start,
                    endDate: weekDates.end
                }
            }).then(res => {
                if (res.code === '0') {
                    this.processWeekMenuData(res.data)
                } else {
                    ElMessage.error(res.msg)
                }
            })
        },
        processWeekMenuData(data) {
            this.weekMenuData = {}
            // Filter out "主食" category when displaying
            data.forEach(item => {
                if (item.menu && item.menu.category === '主食') {
                    return // Skip staple food items
                }
                if (!this.weekMenuData[item.weekDay]) {
                    this.weekMenuData[item.weekDay] = {}
                }
                if (!this.weekMenuData[item.weekDay][item.mealType]) {
                    this.weekMenuData[item.weekDay][item.mealType] = []
                }
                this.weekMenuData[item.weekDay][item.mealType].push(item)
            })
        },
        getWeekDates(date) {
            const current = new Date(date)
            const day = current.getDay()
            const diff = current.getDate() - day + (day === 0 ? -6 : 1)
            
            const monday = new Date(current.setDate(diff))
            const sunday = new Date(monday)
            sunday.setDate(monday.getDate() + 6)
            
            return {
                start: this.formatDate(monday),
                end: this.formatDate(sunday)
            }
        },
        formatDate(date) {
            const year = date.getFullYear()
            const month = String(date.getMonth() + 1).padStart(2, '0')
            const day = String(date.getDate()).padStart(2, '0')
            return `${year}-${month}-${day}`
        },
        loadAllMenus() {
            request.get("/menu/all").then(res => {
                if (res.code === '0') {
                    this.allMenus = res.data
                } else {
                    ElMessage.error(res.msg)
                }
            })
        },
        openSetupDialog() {
            const weekDates = this.getWeekDates(this.selectedWeek)
            this.setupForm.weekStartDate = weekDates.start
            this.setupForm.weekEndDate = weekDates.end
            
            // Reset menus
            Object.keys(this.setupForm.menus).forEach(day => {
                this.setupForm.menus[day] = {breakfast: [], lunch: [], dinner: []}
            })
            
            // Load existing data if any
            Object.keys(this.weekMenuData).forEach(day => {
                Object.keys(this.weekMenuData[day]).forEach(mealType => {
                    this.setupForm.menus[day][mealType] = this.weekMenuData[day][mealType].map(item => item.menuId)
                })
            })
            
            this.setupDialogVisible = true
        },
        closeSetupDialog() {
            this.setupDialogVisible = false
        },
        saveWeekMenu() {
            const weeklyMenuList = []
            
            Object.keys(this.setupForm.menus).forEach(day => {
                Object.keys(this.setupForm.menus[day]).forEach(mealType => {
                    const menuIds = this.setupForm.menus[day][mealType]
                    menuIds.forEach(menuId => {
                        weeklyMenuList.push({
                            menuId: menuId,
                            weekDay: day,
                            mealType: mealType,
                            weekStartDate: this.setupForm.weekStartDate,
                            weekEndDate: this.setupForm.weekEndDate
                        })
                    })
                })
            })
            
            if (weeklyMenuList.length === 0) {
                ElMessage.warning("请至少选择一个菜品")
                return
            }
            
            // First delete existing menu for this week
            request.delete("/weeklyMenu/deleteByWeek", {
                params: {
                    weekStartDate: this.setupForm.weekStartDate,
                    weekEndDate: this.setupForm.weekEndDate
                }
            }).then(() => {
                // Then add new menu
                request.post("/weeklyMenu/batchAdd", weeklyMenuList).then(res => {
                    if (res.code === '0') {
                        ElMessage.success("保存成功")
                        this.setupDialogVisible = false
                        this.loadWeekMenu()
                    } else {
                        ElMessage.error(res.msg)
                    }
                })
            })
        },
        clearWeekMenu() {
            ElMessageBox.confirm('确认清空本周菜谱吗？', '警告', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                const weekDates = this.getWeekDates(this.selectedWeek)
                request.delete("/weeklyMenu/deleteByWeek", {
                    params: {
                        weekStartDate: weekDates.start,
                        weekEndDate: weekDates.end
                    }
                }).then(res => {
                    if (res.code === '0') {
                        ElMessage.success("清空成功")
                        this.loadWeekMenu()
                    } else {
                        ElMessage.error(res.msg)
                    }
                })
            }).catch(() => {
                ElMessage.info("已取消")
            })
        },
        formatWeekRange() {
            if (!this.selectedWeek) {
                return '未选择'
            }
            const weekDates = this.getWeekDates(this.selectedWeek)
            return `${weekDates.start} 至 ${weekDates.end}`
        }
    }
}