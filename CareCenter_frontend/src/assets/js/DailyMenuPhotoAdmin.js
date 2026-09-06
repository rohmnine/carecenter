import request from "@/utils/request";
import {ElMessage, ElMessageBox} from "element-plus";
import { getCurrentIdentity, getCurrentUser } from "@/utils/sessionHelper";

export default {
    name: "DailyMenuPhotoAdmin",
    data() {
        return {
            identity: '',
            selectedDate: new Date(),
            todayPhotos: [],
            uploadDialogVisible: false,
            uploadForm: {
                photoDate: '',
                mealType: 'lunch',
                description: ''
            },
            fileList: [],
            uploading: false,
            mealTypes: [
                {label: '午餐', value: 'lunch'},
                {label: '晚餐', value: 'dinner'}
            ]
        }
    },
    created() {
        this.identity = getCurrentIdentity();
        this.loadPhotos();
    },
    methods: {
        formatDate(date) {
            const d = date instanceof Date ? date : new Date(date);
            const year = d.getFullYear();
            const month = String(d.getMonth() + 1).padStart(2, '0');
            const day = String(d.getDate()).padStart(2, '0');
            return `${year}-${month}-${day}`;
        },
        loadPhotos() {
            const dateStr = this.formatDate(this.selectedDate);
            request.get("/dailyMenuPhoto/byDate", {
                params: { photoDate: dateStr }
            }).then(res => {
                if (res.code === '0') {
                    this.todayPhotos = res.data || [];
                } else {
                    ElMessage.error(res.msg);
                }
            });
        },
        onDateChange() {
            this.loadPhotos();
        },
        getPhotosByMealType(mealType) {
            return this.todayPhotos.filter(p => p.mealType === mealType);
        },
        getMealTypeLabel(type) {
            const found = this.mealTypes.find(m => m.value === type);
            return found ? found.label : type;
        },
        getPhotoUrl(filename) {
            return '/api/dailyMenuPhoto/file/' + filename;
        },
        openUploadDialog() {
            this.uploadForm.photoDate = this.formatDate(this.selectedDate);
            this.uploadForm.mealType = 'lunch';
            this.uploadForm.description = '';
            this.fileList = [];
            this.uploadDialogVisible = true;
        },
        handleFileChange(file) {
            this.fileList.push(file);
        },
        handleFileRemove(file) {
            const index = this.fileList.findIndex(f => f.uid === file.uid);
            if (index > -1) {
                this.fileList.splice(index, 1);
            }
        },
        beforeUpload(file) {
            const isImage = file.type.startsWith('image/');
            const isLt5M = file.size / 1024 / 1024 < 5;
            if (!isImage) {
                ElMessage.error('只能上传图片文件！');
                return false;
            }
            if (!isLt5M) {
                ElMessage.error('图片大小不能超过5MB！');
                return false;
            }
            return true;
        },
        submitUpload() {
            // 过滤出有效的文件（必须有 raw 属性）
            const validFiles = this.fileList.filter(f => f && f.raw instanceof File);
            if (validFiles.length === 0) {
                ElMessage.warning('请选择要上传的图片');
                return;
            }

            this.uploading = true;
            const formData = new FormData();
            // 将所有选中的文件以 files 参数发送
            validFiles.forEach(file => {
                formData.append('files', file.raw, file.name);
            });
            formData.append('photoDate', this.uploadForm.photoDate);
            formData.append('mealType', this.uploadForm.mealType);
            formData.append('description', this.uploadForm.description);

            const user = getCurrentUser();
            if (user) {
                formData.append('uploader', user.username || '');
            }

            request.post("/dailyMenuPhoto/upload", formData, {
                headers: { 'Content-Type': 'multipart/form-data' }
            }).then(res => {
                this.uploading = false;
                if (res.code === '0') {
                    ElMessage.success('上传成功');
                    this.uploadDialogVisible = false;
                    this.loadPhotos();
                } else {
                    ElMessage.error(res.msg || '上传失败');
                }
            }).catch(() => {
                this.uploading = false;
                ElMessage.error('上传失败');
            });
        },
        deletePhoto(id) {
            ElMessageBox.confirm('确认删除该菜品图片吗？', '警告', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                request.delete("/dailyMenuPhoto/delete/" + id).then(res => {
                    if (res.code === '0') {
                        ElMessage.success('删除成功');
                        this.loadPhotos();
                    } else {
                        ElMessage.error(res.msg || '删除失败');
                    }
                });
            }).catch(() => {
                ElMessage.info('已取消');
            });
        }
    }
}