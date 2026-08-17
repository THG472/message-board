<template>
  <div class="hot-rank-page">
    <el-button icon="el-icon-back" @click="$router.push('/')" class="back-btn">返回列表</el-button>

    <div class="rank-container">
      <!-- 点赞热榜 -->
      <el-card class="rank-card" shadow="never">
        <div slot="header" class="rank-header">
          <i class="el-icon-star-on rank-icon like-icon"></i>
          <span>点赞热榜 TOP 10</span>
        </div>
        <div v-loading="loadingLike" class="rank-list">
          <el-empty v-if="likeList.length === 0 && !loadingLike" description="暂无数据" :image-size="60" />
          <div
            v-for="item in likeList"
            :key="'like-' + item.id"
            class="rank-item"
            @click="goDetail(item.id)"
          >
            <div class="rank-num" :class="getRankClass(item.rank)">
              {{ item.rank }}
            </div>
            <div class="rank-info">
              <div class="rank-title" :title="item.title">{{ item.title }}</div>
              <div class="rank-meta">{{ item.userName }}</div>
            </div>
            <div class="rank-count like-count">
              <i class="el-icon-star-on"></i> {{ item.likeCount }}
            </div>
          </div>
        </div>
      </el-card>

      <!-- 评论热榜 -->
      <el-card class="rank-card" shadow="never">
        <div slot="header" class="rank-header">
          <i class="el-icon-chat-dot-round rank-icon comment-icon"></i>
          <span>评论热榜 TOP 10</span>
        </div>
        <div v-loading="loadingComment" class="rank-list">
          <el-empty v-if="commentList.length === 0 && !loadingComment" description="暂无数据" :image-size="60" />
          <div
            v-for="item in commentList"
            :key="'comment-' + item.id"
            class="rank-item"
            @click="goDetail(item.id)"
          >
            <div class="rank-num" :class="getRankClass(item.rank)">
              {{ item.rank }}
            </div>
            <div class="rank-info">
              <div class="rank-title" :title="item.title">{{ item.title }}</div>
              <div class="rank-meta">{{ item.userName }}</div>
            </div>
            <div class="rank-count comment-count">
              <i class="el-icon-chat-dot-round"></i> {{ item.commentCount }}
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script>
import { getHotByLike, getHotByComment } from '../api'

export default {
  name: 'HotRank',
  data() {
    return {
      likeList: [],
      commentList: [],
      loadingLike: false,
      loadingComment: false
    }
  },
  created() {
    this.fetchHotByLike()
    this.fetchHotByComment()
  },
  methods: {
    async fetchHotByLike() {
      this.loadingLike = true
      try {
        this.likeList = await getHotByLike(10)
      } catch (e) {
        this.$message.error('加载点赞热榜失败')
      } finally {
        this.loadingLike = false
      }
    },
    async fetchHotByComment() {
      this.loadingComment = true
      try {
        this.commentList = await getHotByComment(10)
      } catch (e) {
        this.$message.error('加载评论热榜失败')
      } finally {
        this.loadingComment = false
      }
    },
    getRankClass(rank) {
      if (rank === 1) return 'rank-1'
      if (rank === 2) return 'rank-2'
      if (rank === 3) return 'rank-3'
      return ''
    },
    goDetail(id) {
      this.$router.push({ path: `/detail/${id}`, query: { uid: this.$store.state.currentUid } })
    }
  }
}
</script>

<style scoped>
.hot-rank-page {
  max-width: 380px;
  margin: 0;
}
.back-btn {
  margin-bottom: 16px;
}
.rank-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.rank-card {
  border-radius: 8px;
}
.rank-header {
  font-size: 15px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}
.rank-icon {
  font-size: 18px;
}
.like-icon {
  color: #f56c6c;
}
.comment-icon {
  color: #409EFF;
}
.rank-list {
  max-height: 460px;
  overflow-y: auto;
}
.rank-item {
  display: flex;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f5f5f5;
  cursor: pointer;
  transition: background 0.2s;
}
.rank-item:hover {
  background: #fafafa;
}
.rank-item:last-child {
  border-bottom: none;
}
.rank-num {
  width: 32px;
  height: 24px;
  line-height: 24px;
  text-align: center;
  font-size: 14px;
  font-weight: bold;
  color: #909399;
  flex-shrink: 0;
  border-radius: 4px;
  background: #f5f5f5;
}
.rank-num.rank-1 {
  color: #fff;
  background: #f56c6c;
}
.rank-num.rank-2 {
  color: #fff;
  background: #e6a23c;
}
.rank-num.rank-3 {
  color: #fff;
  background: #67c23a;
}
.rank-info {
  flex: 1;
  padding: 0 10px;
  min-width: 0;
}
.rank-title {
  font-size: 13px;
  color: #303133;
  margin-bottom: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.rank-meta {
  font-size: 11px;
  color: #C0C4CC;
}
.rank-count {
  font-size: 14px;
  font-weight: 600;
  flex-shrink: 0;
  min-width: 48px;
  text-align: right;
}
.like-count {
  color: #f56c6c;
}
.comment-count {
  color: #409EFF;
}
</style>
