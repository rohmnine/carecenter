// 跨域配置
module.exports = {

    // dev 环境必须使用绝对路径，避免路由跳转后懒加载与 hot-update 资源路径错误
    // build 产物保留相对路径，兼容后端静态目录部署
    publicPath: process.env.NODE_ENV === 'production' ? './' : '/',

    devServer: {                //记住，别写错了devServer//设置本地默认端口  选填
        // port: 9876,
        proxy: {                 //设置代理，必须填
            '/api': {              //设置拦截器  拦截器格式   斜杠+拦截器名字，名字可以自己定
                target: 'http://localhost:9090',     //代理的目标地址
                changeOrigin: true,              //是否设置同源，输入是的
                pathRewrite: {                   //路径重写
                    '^/api': ''                     //选择忽略拦截器里面的内容
                }
            }
        }
    },

    chainWebpack: config => {
        // 添加对 .mjs 文件的支持，并通过 babel-loader 处理
        config.module
            .rule('mjs')
            .test(/\.mjs$/)
            .include.add(/node_modules/)
            .end()
            .type('javascript/auto')
            .use('babel-loader')
            .loader('babel-loader')
            .end();
    }
}

