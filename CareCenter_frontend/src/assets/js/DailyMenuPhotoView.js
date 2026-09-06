import request from "@/utils/request";
import {ElMessage} from "element-plus";

export default {
    name: "DailyMenuPhotoView",
    data() {
        return {
            selectedDate: new Date(),
            todayPhotos: [],
            mealTypes: [
                {label: '午餐', value: 'lunch'},
                {label: '晚餐', value: 'dinner'}
            ]
        }
    },
    created() {
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
        loadToday() {
            this.selectedDate = new Date();
            this.loadPhotos();
        }
    }
}