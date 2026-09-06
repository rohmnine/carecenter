export default {
    name: "MealReservationAudit",
    data() {
        return {
            reservations: [],
            loading: false,
            search: '',
            month: '',
            status: '',
            pageNum: 1,
            pageSize: 10,
            total: 0,
            stats: {
                total: 0,
                pending: 0,
                approved: 0,
                rejected: 0
            },
            selectedIds: []
        }
    },
    created() {
        this.loadReservations();
    },
    methods: {
        loadReservations() {
            this.loading = true;
            this.$http.get('/mealReservation/page', {
                params: {
                    pageNum: this.pageNum,
                    pageSize: this.pageSize,
                    search: this.search,
                    status: this.status || undefined
                }
            }).then(res => {
                if (res.code === '0' || res.code === 0) {
                    let list = (res.data && res.data.records) ? res.data.records : [];

                    // Map backend ApprovalRequest structure to frontend table fields
                    list = list.map(item => ({
                        ...item,
                        parentUsername: item.requesterUsername || '',
                        reservationDate: item.requestData?.reservation_date || '',
                        mealType: item.requestData?.meal_type || ''
                    }));

                    if (this.month) {
                        list = list.filter(r => r.reservationDate && String(r.reservationDate).startsWith(this.month));
                    }
                    this.reservations = list;
                    this.total = (res.data && typeof res.data.total !== 'undefined') ? res.data.total : 0;
                    this.calculateStats();
                    this.selectedIds = [];
                }
            }).finally(() => {
                this.loading = false;
            });
        },
        calculateStats() {
            this.stats = {
                total: this.reservations.length,
                pending: this.reservations.filter(r => r.status === 'pending').length,
                approved: this.reservations.filter(r => r.status === 'approved' || r.status === 'reserved').length,
                rejected: this.reservations.filter(r => r.status === 'rejected').length
            };
        },
        searchReservations() {
            this.pageNum = 1;
            this.loadReservations();
        },
        resetSearch() {
            this.search = '';
            this.month = '';
            this.status = '';
            this.pageNum = 1;
            this.loadReservations();
        },
        handleCurrentChange(val) {
            this.pageNum = val;
            this.loadReservations();
        },
        handleSelectionChange(selection) {
            this.selectedIds = (selection || []).map(item => item.id).filter(id => id);
        },
        batchApprove() {
            if (!this.selectedIds || this.selectedIds.length === 0) {
                this.$message.warning('请选择需要通过的预约');
                return;
            }
            this.$confirm(`确认批量通过已选 ${this.selectedIds.length} 条预约申请?`, '提示', { type: 'warning' }).then(() => {
                this.$http.post('/mealReservation/batchApprove', {
                    ids: this.selectedIds
                }).then(res => {
                    if (res.code === '0' || res.code === 0) {
                        this.$message.success(res.data || '批量通过成功');
                        this.loadReservations();
                    } else {
                        this.$message.error(res.msg || '批量通过失败');
                    }
                });
            }).catch(() => {});
        },
        approveReservation(row) {
            this.$confirm('确认通过该预约申请?', '提示', { type: 'warning' }).then(() => {
                this.$http.put('/mealReservation/approve/' + row.id).then(res => {
                    if (res.code === '0' || res.code === 0) {
                        this.$message.success('审核通过成功');
                        this.loadReservations();
                    } else {
                        this.$message.error(res.msg || '审核失败');
                    }
                });
            }).catch(() => {});
        },
        rejectReservation(row) {
            this.$confirm('确认驳回该预约申请?', '提示', { type: 'warning' }).then(() => {
                this.$http.put('/mealReservation/reject/' + row.id).then(res => {
                    if (res.code === '0' || res.code === 0) {
                        this.$message.success('已驳回');
                        this.loadReservations();
                    } else {
                        this.$message.error(res.msg || '操作失败');
                    }
                });
            }).catch(() => {});
        },
        // Confirm meal (record actual dining)
        confirmMeal(row) {
            this.$confirm('确认该家长已就餐?', '提示', { type: 'warning' }).then(() => {
                this.$http.put('/mealReservation/confirmMeal/' + row.id).then(res => {
                    if (res.code === '0' || res.code === 0) {
                        this.$message.success('确认就餐成功');
                        this.loadReservations();
                    } else {
                        this.$message.error(res.msg || '操作失败');
                    }
                });
            }).catch(() => {});
        },
        // Unconfirm meal (cancel dining confirmation)
        unconfirmMeal(row) {
            this.$confirm('取消该就餐确认?', '提示', { type: 'warning' }).then(() => {
                this.$http.put('/mealReservation/unconfirmMeal/' + row.id).then(res => {
                    if (res.code === '0' || res.code === 0) {
                        this.$message.success('取消确认成功');
                        this.loadReservations();
                    } else {
                        this.$message.error(res.msg || '操作失败');
                    }
                });
            }).catch(() => {});
        },
        // Check if meal is confirmed
        isMealConfirmed(row) {
            return row.requestData && row.requestData.mealConfirmed === true;
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
        // Check if can confirm meal (only approved/reserved status)
        canConfirmMeal(row) {
            return row.status === 'approved' || row.status === 'reserved';
        },
        formatDate(date) {
            if (!date) return '';
            return new Date(date).toLocaleString('zh-CN');
        }
    }
}
