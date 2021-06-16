module.exports = {
    // 打包app时放开该配置
    configureWebpack: config => {
        // 生产环境取消 console.log
        if (process.env.NODE_ENV === 'production') {
            config.optimization.minimizer[0].options.terserOptions.compress.drop_console = true
        }
    },
    productionSourceMap: false,
    devServer: {
        port: 8088,
        open: true
    }
}
