export default {
    name: "MealBillingAdmin",
    data() {
        return {
            // Bills management
            bills: [],
            selectedBillRows: [],
            allSelectedIds: [],       // IDs of all items selected across pages
            isAllSelected: false,     // Whether "select all across pages" is active
            isReapplying: false,      // Flag to prevent selection-change handler during programmatic reapply
            billSearch: '',
            billMonth: '',
            billStatus: '',
            pageNum: 1,
            pageSize: 10,
            total: 0,

            // Bill detail dialog
            billDetailVisible: false,
            billDetail: null,
            billLeaveRecords: [],

            // Statistics
            statistics: {},

            // Generate bills dialog
            generateDialogVisible: false,
            generateMonth: '',

            // Edit bill dialog
            editDialogVisible: false,
            editSaving: false,
            editForm: {
                id: null,
                studentUsername: '',
                studentName: '',
                billMonth: '',
                boardingType: 'meal_and_rest',
                lunchDays: 0,
                dinnerDays: 0,
                leaveDeductDays: 0,
                lunchPrice: 15,
                dinnerPrice: 20,
                boardingFee: 500,
                totalAmount: null,
                remark: ''
            }

        }
    },
    created() {
        this.loadBills();
        this.loadStatistics();
    },
    methods: {
        // ========== Bill Methods ==========
        loadBills() {
            this.$http.get('/mealBill/page', {
                params: {
                    pageNum: this.pageNum,
                    pageSize: this.pageSize,
                    search: this.billSearch,
                    month: this.billMonth,
                    status: this.billStatus
                }
            }).then(res => {
                if (res.code === '0' || res.code === 0) {
                    this.bills = (res.data && res.data.records) ? res.data.records : [];
                    this.total = (res.data && typeof res.data.total !== 'undefined') ? res.data.total : 0;
                    // After loading new page data, re-apply cross-page selections
                    this.$nextTick(() => {
                        this.reapplySelection();
                    });
                }
            });
        },
        searchBills() {
            this.pageNum = 1;
            this.isAllSelected = false;
            this.allSelectedIds = [];
            this.loadBills();
            this.loadStatistics();
        },
        resetSearch() {
            this.billSearch = '';
            this.billMonth = '';
            this.billStatus = '';
            this.pageNum = 1;
            this.isAllSelected = false;
            this.allSelectedIds = [];
            this.loadBills();
            this.loadStatistics();
        },
        handleSizeChange(val) {
            this.pageSize = val;
            this.pageNum = 1;
            this.loadBills();
        },
        handleCurrentChange(val) {
            this.pageNum = val;
            this.loadBills();
        },
        handleBillSelectionChange(rows) {
            // Skip processing when we are programmatically reapplying selections
            if (this.isReapplying) return;

            this.selectedBillRows = rows || [];

            // Detect if the "select all" header checkbox was checked
            // When all rows on current page are selected and total > current page rows,
            // it means the user clicked the header checkbox to select all on this page
            const currentPageAllSelected = this.bills.length > 0 && rows.length === this.bills.length;

            if (currentPageAllSelected && !this.isAllSelected && this.total > this.bills.length) {
                // User just checked "select all" on current page, and there are more pages
                // Auto-trigger cross-page select-all
                this.selectAllAcrossPages();
            } else if (!currentPageAllSelected && this.isAllSelected) {
                // User unchecked some items while in all-selected mode
                this.isAllSelected = false;
                this.allSelectedIds = rows.map(r => r.id);
            } else if (!this.isAllSelected) {
                // Normal selection on current page
                this.allSelectedIds = rows.map(r => r.id);
            }
        },
        selectAllAcrossPages() {
            // Fetch all bill IDs matching current filter from backend
            this.$http.get('/mealBill/allIds', {
                params: {
                    search: this.billSearch,
                    month: this.billMonth,
                    status: this.billStatus
                }
            }).then(res => {
                if (res.code === '0' || res.code === 0) {
                    this.allSelectedIds = res.data || [];
                    this.isAllSelected = true;
                    // Ensure all rows on current page are checked
                    this.$nextTick(() => {
                        this.reapplySelection();
                    });
                }
            });
        },
        clearAllSelection() {
            this.isAllSelected = false;
            this.allSelectedIds = [];
            this.selectedBillRows = [];
            this.isReapplying = true;
            this.$refs.billTable.clearSelection();
            this.$nextTick(() => {
                this.isReapplying = false;
            });
        },
        reapplySelection() {
            if (!this.$refs.billTable) return;
            const table = this.$refs.billTable;
            // Set flag to prevent handleBillSelectionChange from processing during programmatic reapply
            this.isReapplying = true;
            // Clear current visual selection first
            table.clearSelection();
            if (this.isAllSelected && this.allSelectedIds.length > 0) {
                // In all-selected mode: check all rows on current page
                this.bills.forEach(row => {
                    table.toggleRowSelection(row, true);
                });
            } else if (this.allSelectedIds.length > 0) {
                // Partial selection: only check rows whose IDs are in allSelectedIds
                this.bills.forEach(row => {
                    if (this.allSelectedIds.includes(row.id)) {
                        table.toggleRowSelection(row, true);
                    }
                });
            }
            // Reset flag after a tick so the selection-change events finish first
            this.$nextTick(() => {
                this.isReapplying = false;
            });
        },
        getSelectedIds() {
            // Return the effective selected IDs for batch operations
            if (this.isAllSelected) {
                return this.allSelectedIds;
            }
            return this.selectedBillRows.map(item => item.id);
        },
        getStatusLabel(status) {
            return status === 'paid' ? '已缴费' : '未缴费';
        },
        getStatusType(status) {
            return status === 'paid' ? 'success' : 'danger';
        },
        normalizeNumber(val, defaultValue = 0) {
            if (val === null || val === '' || typeof val === 'undefined') return defaultValue;
            const n = Number(val);
            return Number.isNaN(n) ? defaultValue : n;
        },
        getBoardingTypeLabel(type) {
            const labels = {
                'meal_only': '仅午餐(走读)',
                'meal_and_rest': '午餐+午休(月度固定)'
            };
            return labels[type] || '未设置';
        },
        calculateEditTotalAmount() {
            const lunchDays = this.normalizeNumber(this.editForm.lunchDays, 0);
            const dinnerDays = this.normalizeNumber(this.editForm.dinnerDays, 0);
            const leaveDays = this.normalizeNumber(this.editForm.leaveDeductDays, 0);
            const lunchPrice = this.normalizeNumber(this.editForm.lunchPrice, 0);
            const dinnerPrice = this.normalizeNumber(this.editForm.dinnerPrice, 0);
            const boardingFee = this.normalizeNumber(this.editForm.boardingFee, 0);

            const mealTotal = lunchDays * lunchPrice + dinnerDays * dinnerPrice;
            const leaveRefund = leaveDays * 20;
            const total = Math.max(0, boardingFee + mealTotal - leaveRefund);
            return Number(total.toFixed(2));
        },
        recalculateEditTotalAmount() {
            this.editForm.totalAmount = this.calculateEditTotalAmount();
        },
        getBoardingTypeTag(type) {
            if (type === 'meal_and_rest') return 'warning';
            return 'info';
        },

        // Generate bills
        showGenerateDialog() {
            this.generateMonth = '';
            this.generateDialogVisible = true;
        },
        generateBills() {
            if (!this.generateMonth) {
                this.$message.warning('请选择账单月份');
                return;
            }
            this.$http.post('/mealBill/generate', null, {
                params: { month: this.generateMonth }
            }).then(res => {
                if (res.code === '0' || res.code === 0) {
                    this.$message.success('账单生成成功');
                    this.generateDialogVisible = false;
                    this.billMonth = this.generateMonth;
                    this.searchBills();
                } else {
                    this.$message.error('生成失败: ' + (res.msg || '未知错误'));
                }
            });
        },

        // Single mark as paid
        markAsPaid(bill) {
            this.$confirm('确认将该账单标记为已缴费?', '提示', {
                type: 'warning'
            }).then(() => {
                this.$http.put('/mealBill/pay/' + bill.id).then(res => {
                    if (res.code === '0' || res.code === 0) {
                        this.$message.success('操作成功');
                        this.loadBills();
                        this.loadStatistics();
                    }
                });
            }).catch(() => {});
        },

        // Batch actions in meal bill page
        batchMarkPaid() {
            const ids = this.getSelectedIds();
            if (!ids.length) {
                this.$message.warning('请先选择账单');
                return;
            }
            this.$http.put('/mealBill/batchPay', { ids }).then(res => {
                if (res.code === '0' || res.code === 0) {
                    this.$message.success(res.data || '批量标记成功');
                    this.isAllSelected = false;
                    this.allSelectedIds = [];
                    this.loadBills();
                    this.loadStatistics();
                } else {
                    this.$message.error(res.msg || '批量标记失败');
                }
            });
        },
        batchSendReminder() {
            const ids = this.getSelectedIds();
            if (!ids.length) {
                this.$message.warning('请先选择账单');
                return;
            }
            this.$http.post('/mealBill/batchSendReminder', { ids }).then(res => {
                if (res.code === '0' || res.code === 0) {
                    this.$message.success(res.data || '批量提醒成功');
                } else {
                    this.$message.error(res.msg || '批量提醒失败');
                }
            });
        },
        exportMealExcel() {
            const month = encodeURIComponent(this.billMonth || '');
            const search = encodeURIComponent(this.billSearch || '');
            const status = encodeURIComponent(this.billStatus || '');
            window.open(`/api/mealBill/export?month=${month}&search=${search}&status=${status}`);
        },
        handleMealFileChange(file) {
            const formData = new FormData();
            formData.append('file', file.raw);
            this.$http.post('/mealBill/import', formData, {
                headers: { 'Content-Type': 'multipart/form-data' }
            }).then(res => {
                if (res.code === '0' || res.code === 0) {
                    this.$message.success(res.data || '导入成功');
                    this.loadBills();
                    this.loadStatistics();
                } else {
                    this.$message.error(res.msg || '导入失败');
                }
            });
        },

        // View bill detail
        viewDetail(bill) {
            this.$http.get('/mealBill/detail/' + bill.id).then(res => {
                if (res.code === '0' || res.code === 0) {
                    this.billDetail = res.data ? res.data.bill : null;
                    this.billLeaveRecords = (res.data && res.data.leaveRecords) ? res.data.leaveRecords : [];
                    this.billDetailVisible = true;
                }
            });
        },

        // Edit bill
        openEditDialog(bill) {
            this.editForm = {
                id: bill.id,
                studentUsername: bill.studentUsername,
                studentName: bill.studentName,
                billMonth: bill.billMonth,
                boardingType: bill.boardingType || 'meal_and_rest',
                lunchDays: this.normalizeNumber(bill.lunchDays, 0),
                dinnerDays: this.normalizeNumber(bill.dinnerDays, 0),
                leaveDeductDays: this.normalizeNumber(bill.leaveDeductDays, 0),
                lunchPrice: this.normalizeNumber(bill.lunchPrice, 15),
                dinnerPrice: this.normalizeNumber(bill.dinnerPrice, 20),
                boardingFee: this.normalizeNumber(bill.boardingFee, 500),
                totalAmount: bill.totalAmount === null || typeof bill.totalAmount === 'undefined' ? null : Number(bill.totalAmount),
                remark: bill.remark || ''
            };
            this.recalculateEditTotalAmount();
            this.editDialogVisible = true;
        },
        submitEdit() {
            if (!this.editForm.id) {
                this.$message.error('账单ID不能为空');
                return;
            }
            this.editSaving = true;

            this.$http.put('/mealBill/studentBoardingType', null, {
                params: {
                    studentUsername: this.editForm.studentUsername,
                    boardingType: this.editForm.boardingType
                }
            }).then(typeRes => {
                if (!(typeRes.code === '0' || typeRes.code === 0)) {
                    throw new Error(typeRes.msg || '修改就餐类型失败');
                }

                this.recalculateEditTotalAmount();

                const payload = {
                    id: this.editForm.id,
                    boardingType: this.editForm.boardingType,
                    lunchDays: this.normalizeNumber(this.editForm.lunchDays, 0),
                    dinnerDays: this.normalizeNumber(this.editForm.dinnerDays, 0),
                    leaveDeductDays: this.normalizeNumber(this.editForm.leaveDeductDays, 0),
                    lunchPrice: this.normalizeNumber(this.editForm.lunchPrice, 0),
                    dinnerPrice: this.normalizeNumber(this.editForm.dinnerPrice, 0),
                    boardingFee: this.normalizeNumber(this.editForm.boardingFee, 0),
                    remark: this.editForm.remark || null,
                    totalAmount: Number(this.editForm.totalAmount)
                };

                return this.$http.put('/mealBill/updateRecord', payload);
            }).then(res => {
                if (res.code === '0' || res.code === 0) {
                    this.$message.success('账单修改成功');
                    this.editDialogVisible = false;
                    this.loadBills();
                    this.loadStatistics();
                } else {
                    this.$message.error(res.msg || '账单修改失败');
                }
            }).catch(err => {
                this.$message.error((err && err.message) ? err.message : '账单修改失败');
            }).finally(() => {
                this.editSaving = false;
            });
        },

        // Statistics
        loadStatistics() {
            this.$http.get('/mealBill/statistics', {
                params: { month: this.billMonth }
            }).then(res => {
                if (res.code === '0' || res.code === 0) {
                    this.statistics = res.data || {};
                }
            });
        },

        formatDate(date) {
            if (!date) return '';
            return new Date(date).toLocaleString('zh-CN');
        },

    }
}