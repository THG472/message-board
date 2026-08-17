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
  </div>
</template>

<script>
import { mapState } from 'vuex'
import { getCurrentUser } from './api'

export default {
  name: 'App',
  data() {
    return {
      isDetailPage: false
    }
  },
  computed: {
    ...mapState(['currentUid', 'currentUserName', 'currentOrgName'])
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
    this.isDetailPage = this.$route.name === 'MessageDetail'
    const urlUid = this.$route.query.uid
    if (urlUid) {
      this.$store.commit('SET_UID', urlUid)
      // SET_UID 后立即获取用户信息
      this.fetchUserInfo()
    }
  },
  methods: {
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
