export default {
    name: "ParentMealReservation",
    data() {
        return {
            // Current user
            user: null,
            // Reservations list
            reservations: [],
            loading: false,
            // Month picker
            currentMonth: '',
            selectedStudentUsername: '',
            studentOptions: [],
            // New reservation form
            reserveDialogVisible: false,
            reserveForm: {
                mealType: 'lunch',
                studentUsername: '',
                dates: [],
                dateInput: ''
            },
            // Calendar data
            calendarReservations: {},
            // Statistics
            monthStats: {
                lunchCount: 0,
                dinnerCount: 0,
                totalCount: 0
            }
        }
    },
    created() {
        const currentIdentity = sessionStorage.getItem('currentIdentity');
        const userStr = currentIdentity ? sessionStorage.getItem('user_' + currentIdentity) : sessionStorage.getItem('user');
        this.user = userStr ? JSON.parse(userStr) : null;
        if (!this.user) {
            this.$message.error('请先登录');
            return;
        }
        // Default to current month
        const now = new Date();
        this.currentMonth = now.getFullYear() + '-' + String(now.getMonth() + 1).padStart(2, '0');
        const bound = Array.isArray(this.user.studentUsernames)
            ? this.user.studentUsernames
            : ((this.user.studentUsername || '').split(',').map(v => v.trim()).filter(Boolean));
        this.studentOptions = bound;
        this.selectedStudentUsername = bound.length ? bound[0] : '';
        this.loadReservations();
    },
    methods: {
        loadReservations() {
            this.loading = true;
            const currentIdentity = sessionStorage.getItem('currentIdentity');
            const userStr = currentIdentity ? sessionStorage.getItem('user_' + currentIdentity) : sessionStorage.getItem('user');
            const user = userStr ? JSON.parse(userStr) : null;
            if (!user) {
                this.loading = false;
                return;
            }
            const selectedStudent = this.selectedStudentUsername || (this.studentOptions[0] || '');
            this.$http.get('/mealReservation/parent/' + user.username, {
                params: {
                    month: this.currentMonth,
                    studentUsername: selectedStudent
                }
            }).then(res => {
                if (res.code === '0' || res.code === 0) {
                    // Transform backend data structure to frontend format
                    this.reservations = (res.data || []).map(item => {
                        return {
                            id: item.id,
                            reservationDate: item.requestData?.reservation_date || '',
                            mealType: item.requestData?.meal_type || '',
                            studentName: item.studentName || '',
                            status: item.status || '',
                            createTime: item.createTime || '',
                            requestData: item.requestData,
                            mealConfirmed: item.requestData?.mealConfirmed || false
                        };
                    });
                    this.buildCalendarData();
                    this.calculateStats();
                }
                this.loading = false;
            }).catch(() => {
                this.loading = false;
            });
        },
        buildCalendarData() {
            const map = {};
            this.reservations.forEach(r => {
                if (r.status === 'pending' || r.status === 'approved' || r.status === 'reserved') {
                    if (!map[r.reservationDate]) {
                        map[r.reservationDate] = [];
                    }
                    map[r.reservationDate].push(r.mealType);
                }
            });
            this.calendarReservations = map;
        },
        calculateStats() {
            let lunch = 0, dinner = 0;
            this.reservations.forEach(r => {
                if (r.status === 'pending' || r.status === 'approved' || r.status === 'reserved') {
                    if (r.mealType === 'lunch') lunch++;
                    if (r.mealType === 'dinner') dinner++;
                }
            });
            this.monthStats = {
                lunchCount: lunch,
                dinnerCount: dinner,
                totalCount: lunch + dinner
            };
        },
        openReserveDialog() {
            this.reserveForm = {
                mealType: 'lunch',
                studentUsername: this.selectedStudentUsername || (this.studentOptions[0] || ''),
                dates: [],
                dateInput: ''
            };
            this.reserveDialogVisible = true;
        },
        submitReservation() {
            if (!this.reserveForm.dates || this.reserveForm.dates.length === 0) {
                this.$message.warning('请选择预约日期');
                return;
            }
            const currentIdentity = sessionStorage.getItem('currentIdentity');
            const userStr = currentIdentity ? sessionStorage.getItem('user_' + currentIdentity) : sessionStorage.getItem('user');
            const user = userStr ? JSON.parse(userStr) : null;
            if (!user) {
                this.$message.error('请先登录');
                return;
            }

            // Format dates
            const formattedDates = this.reserveForm.dates.map(d => {
                if (typeof d === 'string') return d;
                const date = new Date(d);
                return date.getFullYear() + '-' + String(date.getMonth() + 1).padStart(2, '0') + '-' + String(date.getDate()).padStart(2, '0');
            });

            const studentUsername = this.reserveForm.studentUsername || this.selectedStudentUsername || (this.studentOptions[0] || '');
            this.$http.post('/mealReservation/batchReserve', {
                parentUsername: user.username,
                studentUsername,
                mealType: this.reserveForm.mealType,
                dates: formattedDates,
                studentName: user.studentName || ''
            }).then(res => {
                if (res.code === '0' || res.code === 0) {
                    this.$message.success(res.data || '预约成功');
                    this.reserveDialogVisible = false;
                    this.loadReservations();
                } else {
                    this.$message.error(res.msg || '预约失败');
                }
            });
        },
        cancelReservation(row) {
            const currentIdentity = sessionStorage.getItem('currentIdentity');
            const userStr = currentIdentity ? sessionStorage.getItem('user_' + currentIdentity) : sessionStorage.getItem('user');
            const user = userStr ? JSON.parse(userStr) : null;
            this.$confirm('确认取消该预约?', '提示', { type: 'warning' }).then(() => {
                this.$http.put('/mealReservation/cancel/' + row.id, null, {
                    params: { parentUsername: user.username }
                }).then(res => {
                    if (res.code === '0' || res.code === 0) {
                        this.$message.success('取消成功');
                        this.loadReservations();
                    } else {
                        this.$message.error(res.msg || '取消失败');
                    }
                });
            }).catch(() => {});
        },
        monthChanged() {
            this.loadReservations();
        },
        studentChanged() {
            this.loadReservations();
        },
        getMealTypeLabel(type) {
            return type === 'lunch' ? '午餐' : '晚餐';
        },
        getMealTypeTag(type) {
            return type === 'lunch' ? '' : 'warning';
        },
        getStatusLabel(status) {
            if (status === 'pending') return '待审核';
            if (status === 'approved' || status === 'reserved') return '已通过';
            if (status === 'rejected') return '已驳回';
            return '已取消';
        },
        getStatusTag(status) {
            if (status === 'pending') return 'warning';
            if (status === 'approved' || status === 'reserved') return 'success';
            if (status === 'rejected') return 'danger';
            return 'info';
        },
        // Check if meal is confirmed
        isMealConfirmed(row) {
            return row.mealConfirmed === true || (row.requestData && row.requestData.mealConfirmed === true);
        },
        canCancel(row) {
            // Cannot cancel if status is not pending/approved/reserved
            if (!(row.status === 'pending' || row.status === 'approved' || row.status === 'reserved')) return false;
            // Cannot cancel if meal has been confirmed
            if (this.isMealConfirmed(row)) return false;
            // Cannot cancel past date reservations
            const today = new Date();
            today.setHours(0, 0, 0, 0);
            const resDate = new Date(row.reservationDate);
            return resDate >= today;
        },
        formatDate(date) {
            if (!date) return '';
            return date;
        },
        getDateReservations(date) {
            return this.calendarReservations[date] || [];
        },
        addReserveDate(val) {
            if (!val) return;
            if (!this.reserveForm.dates.includes(val)) {
                this.reserveForm.dates.push(val);
            }
            this.reserveForm.dateInput = '';
        },
        removeReserveDate(date) {
            this.reserveForm.dates = this.reserveForm.dates.filter(d => d !== date);
        },
        // Disable past dates in date picker
        disablePastDates(time) {
            const today = new Date();
            today.setHours(0, 0, 0, 0);
            return time.getTime() < today.getTime();
        }
    }
}