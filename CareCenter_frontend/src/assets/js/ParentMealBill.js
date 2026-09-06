export default {
    name: "ParentMealBill",
    data() {
        return {
            bills: [],
            billDetailVisible: false,
            billDetail: null,
            billLeaveRecords: [],
            loading: false,
            user: null
        }
    },
    computed: {
        paidCount() {
            return this.bills.filter(b => b.status === 'paid').length;
        },
        unpaidCount() {
            return this.bills.filter(b => b.status !== 'paid').length;
        }
    },
    created() {
        const currentIdentity = sessionStorage.getItem('currentIdentity');
        const userStr = currentIdentity ? sessionStorage.getItem('user_' + currentIdentity) : sessionStorage.getItem('user');
        this.user = userStr ? JSON.parse(userStr) : null;
        this.loadBills();
    },
    methods: {
        loadBills() {
            this.loading = true;
            const currentIdentity = sessionStorage.getItem('currentIdentity');
            const userStr = currentIdentity ? sessionStorage.getItem('user_' + currentIdentity) : sessionStorage.getItem('user');
            const user = userStr ? JSON.parse(userStr) : null;
            if (!user) {
                this.$message.error('请先登录');
                this.loading = false;
                return;
            }
            this.$http.get('/mealBill/parent/' + user.username).then(res => {
                if (res.code === '0' || res.code === 0) {
                    this.bills = res.data || [];
                }
                this.loading = false;
            }).catch(() => {
                this.loading = false;
            });
        },
        getPageTitle() {
            // 无孩子家长显示“家长就餐账单”，有孩子家长显示“孩子就餐账单”
            if (!this.user) return '就餐账单';
            return this.user.studentUsername ? '孩子就餐账单' : '家长就餐账单';
        },
        getBoardingTypeLabel(type) {
            const labels = {
                'meal_only': '仅午餐(走读)',
                'meal_and_rest': '午餐+晚餐+午休(半寄宿)'
            };
            return labels[type] || '未设置';
        },
        getStatusLabel(status) {
            return status === 'paid' ? '已缴费' : '未缴费';
        },
        getStatusType(status) {
            return status === 'paid' ? 'success' : 'danger';
        },
        viewDetail(bill) {
            this.$http.get('/mealBill/detail/' + bill.id).then(res => {
                if (res.code === '0' || res.code === 0) {
                    this.billDetail = res.data.bill;
                    this.billLeaveRecords = res.data.leaveRecords || [];
                    this.billDetailVisible = true;
                }
            });
        },
        formatDate(date) {
            if (!date) return '';
            return new Date(date).toLocaleString('zh-CN');
        }
    }
}