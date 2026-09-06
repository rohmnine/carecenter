import {createApp} from 'vue'
import App from './App.vue'
import router from './router'
import './assets/css/global.css'
import request from './utils/request'
// element-plus
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ELIcons from '@element-plus/icons-vue'
import zhCn from 'element-plus/es/locale/lang/zh-cn'

const app = createApp(App)

app.use(ElementPlus, {
    locale: zhCn
})

// 兼容原有 Options API 页面中 this.$http 的调用方式
app.config.globalProperties.$http = request

for (let iconName in ELIcons) {
    app.component(iconName, ELIcons[iconName])
}
app.use(router)
app.mount('#app')

