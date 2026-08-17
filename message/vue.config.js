module.exports = {
  publicPath: '/message',
  outputDir: 'message',
  devServer: {
    port: 8080,
    proxy: {
      '/message-board': {
        target: 'http://localhost:8088',
        changeOrigin: true
      }
    }
  },
  lintOnSave: false
}
