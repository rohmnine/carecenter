import request from "@/utils/request";
import {ElMessage} from "element-plus";

export default {
    name: "RandomMenu",
    data() {
        return {
            search: '',
            currentPage: 1,
            pageSize: 10,
            total: 0,
            tableData: [],
            dialogVisible: false,
            loading: false,
            form: {},
            rules: {
                dishName: [
                    {required: true, message: '请输入菜品名称', trigger: 'blur'}
                ],
                category: [
                    {required: true, message: '请选择分类', trigger: 'change'}
                ],
            },
            randomMenus: [],
            randomDialogVisible: false,
            editRandomDialogVisible: false,
            editingRandomMenu: {},
            editingRandomMenuIndex: -1,
            addMenuDialogVisible: false,
            addMenuSearch: '',
            allAvailableMenus: [],
            saveToWeekForm: {
                selectedWeek: null,
                weekDay: null,
                mealType: null,
                weekStartDate: '',
                weekEndDate: ''
            }
        }
    },
    created() {
        this.load()
    },
    computed: {
        canSaveToWeek() {
            return this.saveToWeekForm.selectedWeek &&
                   this.saveToWeekForm.weekDay &&
                   this.saveToWeekForm.mealType &&
                   this.randomMenus.length > 0
        },
        filteredAvailableMenus() {
            let menus = this.allAvailableMenus.filter(menu => menu.category !== '主食')
            
            // Filter by search text
            if (this.addMenuSearch) {
                menus = menus.filter(menu =>
                    menu.dishName.toLowerCase().includes(this.addMenuSearch.toLowerCase())
                )
            }
            
            // Exclude already added menus
            const addedIds = this.randomMenus.map(m => m.id)
            menus = menus.filter(menu => !addedIds.includes(menu.id))
            
            return menus
        }
    },
    methods: {
        load() {
            this.loading = true
            request.get("/menu/find", {
                params: {
                    pageNum: this.currentPage,
                    pageSize: this.pageSize,
                    search: this.search
                }
            }).then(res => {
                this.loading = false
                if (res.code === '0') {
                    this.tableData = res.data.records
                    this.total = res.data.total
                } else {
                    ElMessage.error(res.msg)
                }
            })
        },
        add() {
            this.dialogVisible = true
            this.form = {}
        },
        save() {
            this.$refs.form.validate((valid) => {
                if (valid) {
                    if (this.form.id) {
                        request.put("/menu/update", this.form).then(res => {
                            if (res.code === '0') {
                                ElMessage.success("更新成功")
                                this.load()
                                this.dialogVisible = false
                            } else {
                                ElMessage.error(res.msg)
                            }
                        })
                    } else {
                        request.post("/menu/add", this.form).then(res => {
                            if (res.code === '0') {
                                ElMessage.success("添加成功")
                                this.load()
                                this.dialogVisible = false
                            } else {
                                ElMessage.error(res.msg)
                            }
                        })
                    }
                }
            })
        },
        handleEdit(row) {
            this.form = JSON.parse(JSON.stringify(row))
            this.dialogVisible = true
        },
        handleDelete(id) {
            request.delete("/menu/delete/" + id).then(res => {
                if (res.code === '0') {
                    ElMessage.success("删除成功")
                    this.load()
                } else {
                    ElMessage.error(res.msg)
                }
            })
        },
        reset() {
            this.search = ''
            this.currentPage = 1
            this.load()
        },
        cancel() {
            this.dialogVisible = false
            this.form = {}
        },
        handleSizeChange(pageSize) {
            this.pageSize = pageSize
            this.load()
        },
        handleCurrentChange(pageNum) {
            this.currentPage = pageNum
            this.load()
        },
        generateRandom() {
            request.get("/menu/random").then(res => {
                if (res.code === '0') {
                    this.randomMenus = res.data
                    this.randomDialogVisible = true
                    // Load all available menus for adding
                    this.loadAllAvailableMenus()
                    // Reset save form
                    this.saveToWeekForm = {
                        selectedWeek: null,
                        weekDay: null,
                        mealType: null,
                        weekStartDate: '',
                        weekEndDate: ''
                    }
                } else {
                    ElMessage.error(res.msg)
                }
            })
        },
        loadAllAvailableMenus() {
            request.get("/menu/all").then(res => {
                if (res.code === '0') {
                    this.allAvailableMenus = res.data
                } else {
                    ElMessage.error(res.msg)
                }
            })
        },
        getCategoryType(category) {
            const typeMap = {
                '荤菜': 'danger',
                '素菜': 'success',
                '汤品': 'warning'
            }
            return typeMap[category] || ''
        },
        editRandomMenu(index) {
            this.editingRandomMenuIndex = index
            this.editingRandomMenu = JSON.parse(JSON.stringify(this.randomMenus[index]))
            this.editRandomDialogVisible = true
        },
        saveEditedRandomMenu() {
            if (this.editingRandomMenuIndex >= 0) {
                this.randomMenus[this.editingRandomMenuIndex] = JSON.parse(JSON.stringify(this.editingRandomMenu))
                this.editRandomDialogVisible = false
                ElMessage.success('修改成功')
            }
        },
        showAddMenuDialog() {
            this.addMenuSearch = ''
            this.addMenuDialogVisible = true
            if (this.allAvailableMenus.length === 0) {
                this.loadAllAvailableMenus()
            }
        },
        addToRandom(menu) {
            // Check if already added
            if (this.randomMenus.some(m => m.id === menu.id)) {
                ElMessage.warning('该菜品已在列表中')
                return
            }
            this.randomMenus.push(JSON.parse(JSON.stringify(menu)))
            ElMessage.success(`已添加 ${menu.dishName}`)
        },
        removeFromRandom(index) {
            const dishName = this.randomMenus[index].dishName
            this.randomMenus.splice(index, 1)
            ElMessage.success(`已移除 ${dishName}`)
        },
        onWeekChange(value) {
            if (value) {
                const dates = this.getWeekDates(value)
                this.saveToWeekForm.weekStartDate = dates.start
                this.saveToWeekForm.weekEndDate = dates.end
            }
        },
        getWeekDates(date) {
            const d = new Date(date)
            const day = d.getDay()
            const diff = d.getDate() - day + (day === 0 ? -6 : 1)
            const monday = new Date(d.setDate(diff))
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
        saveRandomToWeek() {
            if (!this.canSaveToWeek) {
                ElMessage.warning('请完整填写周、星期和餐次信息')
                return
            }
            
            if (this.randomMenus.length === 0) {
                ElMessage.warning('请至少选择一个菜品')
                return
            }
            
            // Convert weekDay number to English day name
            const weekDayMap = {
                1: 'Monday',
                2: 'Tuesday',
                3: 'Wednesday',
                4: 'Thursday',
                5: 'Friday',
                6: 'Saturday',
                7: 'Sunday'
            }
            
            const weekDayString = weekDayMap[this.saveToWeekForm.weekDay]
            
            if (!weekDayString) {
                ElMessage.error('无效的星期选择')
                return
            }
            
            // Prepare batch data
            const weeklyMenuItems = this.randomMenus.map(menu => ({
                menuId: menu.id,
                weekDay: weekDayString,
                mealType: this.saveToWeekForm.mealType,
                weekStartDate: this.saveToWeekForm.weekStartDate,
                weekEndDate: this.saveToWeekForm.weekEndDate
            }))
            
            // Call batch add API
            request.post("/weeklyMenu/batchAdd", weeklyMenuItems).then(res => {
                if (res.code === '0') {
                    ElMessage.success('已成功添加到每周菜谱，正在跳转...')
                    this.randomDialogVisible = false
                    this.randomMenus = []
                                        // Wait for dialog to fullyclose before navigating
                    this.$nextTick(() => {
                        // Clean up any remainingdialog overlays
                        const overlays = document.querySelectorAll('.el-overlay')
                        overlays.forEach(overlay => {
                            if (overlay.style.display !== 'none') {
                                overlay.style.display = 'none'
                            }
                        })
                        this.$router.push('/weeklyMenu')
                    })
                } else {
                    ElMessage.error(res.msg)
                }
            })
        }
    }
}