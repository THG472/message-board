<template>
  <div id="app">
    <el-container class="app-container">
      <el-header class="app-header">
        <div class="header-left">
          <h1>留言板系统</h1>
          <span class="header-subtitle">产品问题 & 需求反馈</span>
        </div>
        <div class="header-right">
          <span v-if="currentUserName" class="user-info">
            <i class="el-icon-user-solid"></i>
            {{ currentUserName }}
            <span v-if="currentOrgName" class="user-org">（{{ currentOrgName }}）</span>
          </span>
        </div>
      </el-header>
      <el-main :class="['app-main', { 'is-detail': isDetailPage }]">
        <router-view />
      </el-main>
    </el-container>
    <!-- 全局页面水印：姓名 / 身份证号 / 访问时间（按服务器时间校准），浅色不遮挡内容 -->
    <watermark :user-name="currentUserName" :sfzh="currentSfzh" :time-offset="serverTimeOffset" />
  </div>
</template>

<script>
import { mapState } from 'vuex'
import { getCurrentUser, reportPageView, getServerTime } from './api'
import Watermark from './components/Watermark.vue'

export default {
  name: 'App',
  components: { Watermark },
  data() {
    return {
      isDetailPage: false,
      // 服务器时间与本地时间的偏移量（毫秒），用于校准水印时间
      serverTimeOffset: 0
    }
  },
  computed: {
    ...mapState(['currentUid', 'currentUserName', 'currentSfzh', 'currentOrgName'])
  },
  watch: {
    '$route.name'(val) {
      this.isDetailPage = val === 'MessageDetail'
    },
    currentUid(val) {
      if (val && !this.currentUserName) {
        this.fetchUserInfo()
      }
    }
  },
  created() {
    const isDetailPageUrl = this.isDetailPageUrl()
    this.isDetailPage = isDetailPageUrl
    const uid = this.getUidFromUrl()
    if (uid) {
      this.$store.commit('SET_UID', uid)
    }
    // 仅整页加载进入主页（留言列表页）时上报一次访问日志（LOGIN）
    // 点击留言会新开标签页加载详情页（#/detail/...），那不算"登录访问主页"，不重复上报
    // 注意：不能在 SET_UID 之前调用，否则请求头带不上 uid
    if (!isDetailPageUrl) {
      reportPageView().catch(() => {})
    }
    // 校准水印时间：获取服务器时间，计算与本地时钟的偏移量（减去一半往返耗时以修正网络延迟）
    this.syncServerTime()
  },
  methods: {
    /** 拉取服务器时间并计算偏移量，失败时保持偏移为 0（水印退化为本地时间） */
    async syncServerTime() {
      const t0 = Date.now()
      try {
        const serverTime = await getServerTime()
        if (typeof serverTime === 'number') {
          const rtt = Date.now() - t0
          // 假设请求/响应耗时各占一半，服务器时间对应本地 t0 + rtt/2 时刻
          this.serverTimeOffset = serverTime - (t0 + rtt / 2)
        }
      } catch (e) {
        // 获取失败不阻塞页面，水印使用本地时间
      }
    },
    // 判断当前 URL 是否是详情页，基于 window.location 解析（hash 路由 #/detail/...）
    // 不依赖 this.$route：App created 执行时 Vue Router 首次导航可能尚未完成
    isDetailPageUrl() {
      try {
        const hash = window.location.hash || ''
        const path = (hash.split('?')[0] || '').replace(/^#/, '')
        return path.startsWith('/detail')
      } catch (e) {
        return false
      }
    },
    // 从 URL 解析 uid，兼容 hash 内（#/?uid=xxx、#/detail/1?uid=xxx）与 hash 外（?uid=xxx）两种形式
    // 不依赖 this.$route：App created 执行时 Vue Router 首次导航可能尚未完成，route.query 会取不到
    getUidFromUrl() {
      try {
        const hash = window.location.hash
        const qIndex = hash.indexOf('?')
        if (qIndex >= 0) {
          const hashUid = new URLSearchParams(hash.slice(qIndex + 1)).get('uid')
          if (hashUid) return hashUid
        }
        return new URLSearchParams(window.location.search).get('uid') || ''
      } catch (e) {
        return ''
      }
    },
    async fetchUserInfo() {
      try {
        const userInfo = await getCurrentUser()
        this.$store.commit('SET_USER_INFO', userInfo)
      } catch (e) {
        // 用户信息获取失败不阻塞页面
      }
    }
  }
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
body {
  font-family: 'Microsoft YaHei', 'PingFang SC', sans-serif;
  background-color: #f0f2f5;
}
.app-container {
  min-height: 100vh;
}
.app-header {
  background: linear-gradient(135deg, #409EFF 0%, #337ECC 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 30px;
  height: 60px !important;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
}
.header-left {
  display: flex;
  align-items: baseline;
  gap: 15px;
}
.header-left h1 {
  font-size: 22px;
  font-weight: 600;
}
.header-subtitle {
  font-size: 13px;
  opacity: 0.85;
}
.header-right {
  font-size: 14px;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 6px;
  background: rgba(255,255,255,0.2);
  padding: 6px 16px;
  border-radius: 20px;
}
.user-org {
  font-size: 12px;
  opacity: 0.8;
}
.app-main {
  padding: 24px 40px;
}
/* 详情页：去掉宽度限制和左右padding */
.app-main.is-detail {
  padding: 0;
}
</style>
