<template>
  <div class="detail-page">
    <div v-loading="loading" element-loading-text="加载中...">
      <!-- 顶部导航条 -->
      <div class="top-bar">
        <div></div>
        <div class="top-bar-right">
          <span class="top-bar-category" :class="message.category">
            {{ message.category === 'question' ? '问题反馈' : '需求建议' }}
          </span>
          <span class="top-bar-stat">
            <i class="el-icon-star-on" :class="{ liked: message.liked }"></i> {{ message.likeCount }}
          </span>
          <span class="top-bar-stat">
            <i class="el-icon-chat-dot-round"></i> {{ message.commentCount }}
          </span>
        </div>
      </div>

      <!-- 文章正文区 -->
      <div class="article-wrapper">
        <article class="article">
          <h1 class="article-title">{{ message.title }}</h1>
          <div class="article-meta">
            <div class="meta-author">
              <span class="meta-avatar">{{ message.userName ? message.userName.charAt(0) : '?' }}</span>
              <span class="meta-name">{{ message.userName }}</span>
              <span v-if="message.orgName" class="meta-dot">·</span>
              <span v-if="message.orgName" class="meta-org">{{ message.orgName }}</span>
            </div>
            <span class="meta-time">{{ message.createTime }}</span>
          </div>
          <div class="article-content" v-html="renderedContent"></div>
          <!-- 手机号 -->
          <div v-if="message.phone" class="article-phone">
            <i class="el-icon-phone"></i> 联系手机：{{ message.phone }}
          </div>
          <!-- 附件列表 -->
          <div v-if="filesForDisplay.length > 0" class="article-files">
            <div class="files-title"><i class="el-icon-paperclip"></i> 附件 ({{ filesForDisplay.length }})</div>
            <div v-for="(file, idx) in filesForDisplay" :key="idx" class="file-item">
              <i class="el-icon-document"></i>
              <a :href="getFileUrl(file.path)" target="_blank" download>{{ file.name }}</a>
            </div>
          </div>
        </article>
      </div>

      <!-- 底部操作栏 -->
      <div class="action-bar">
        <div class="action-bar-inner">
          <div class="action-left">
            <div class="action-btn" :class="{ active: message.liked }" @click="handleLike">
              <i :class="message.liked ? 'el-icon-star-on' : 'el-icon-star-off'"></i>
              <span>点赞 {{ message.likeCount }}</span>
            </div>
          </div>
          <div class="action-right">
            <el-button type="primary" icon="el-icon-edit" size="medium" @click="scrollToComment">
              评论 ({{ message.commentCount }})
            </el-button>
          </div>
        </div>
      </div>

      <!-- 评论区 -->
      <div class="comment-wrapper" ref="commentBox">
        <div class="comment-section">
          <div class="comment-section-title">
            评论 <span class="comment-count-badge">{{ message.commentCount }}</span>
          </div>

          <!-- 评论输入 -->
          <div class="comment-editor">
            <div class="editor-avatar">{{ message.userName ? message.userName.charAt(0) : '?' }}</div>
            <div class="editor-body">
              <el-input
                v-model="commentContent"
                type="textarea"
                :rows="3"
                placeholder="写下你的想法..."
                resize="none"
              />
              <div class="editor-footer">
                <span class="editor-hint">支持友善交流，文明发言</span>
                <el-button type="primary" @click="submitComment" :loading="submitting" :disabled="!commentContent.trim()">发表评论</el-button>
              </div>
            </div>
          </div>

          <!-- 评论列表 -->
          <el-empty v-if="!loading && (!message.comments || message.comments.length === 0)" description="还没有评论，来发表第一条吧" :image-size="80" />
          <div v-for="comment in message.comments" :key="comment.id" class="comment-item">
            <div class="cmt-avatar">{{ comment.userName ? comment.userName.charAt(0) : '?' }}</div>
            <div class="cmt-main">
              <div class="cmt-header">
                <div class="cmt-user">
                  <span class="cmt-name">{{ comment.userName }}</span>
                  <span v-if="comment.orgName" class="cmt-org">{{ comment.orgName }}</span>
                </div>
                <span class="cmt-time">{{ comment.createTime }}</span>
              </div>
              <div class="cmt-content">{{ comment.content }}</div>
              <div class="cmt-actions">
                <span class="cmt-action" @click="openReply(comment)">
                  <i class="el-icon-chat-line-square"></i> 回复
                </span>
              </div>

              <!-- 回复编辑器 -->
              <div v-if="replyTarget && replyTarget.id === comment.id" class="reply-editor">
                <el-input v-model="replyContent" type="textarea" :rows="2" :placeholder="'回复 @' + comment.userName" resize="none" />
                <div class="reply-editor-footer">
                  <span class="reply-editor-hint">@{{ comment.userName }}</span>
                  <div>
                    <el-button size="small" @click="replyTarget = null">取消</el-button>
                    <el-button type="primary" size="small" @click="submitReply(comment)" :loading="submitting">回复</el-button>
                  </div>
                </div>
              </div>

              <!-- 子回复 -->
              <div v-if="comment.replies && comment.replies.length > 0" class="sub-replies">
                <div v-for="reply in comment.replies" :key="reply.id" class="sub-reply-item">
                  <span class="sub-avatar">{{ reply.userName ? reply.userName.charAt(0) : '?' }}</span>
                  <div class="sub-reply-body">
                    <div class="sub-reply-header">
                      <span class="sub-name">{{ reply.userName }}</span>
                      <template v-if="reply.replyToName">
                        <i class="el-icon-d-arrow-right sub-arrow"></i>
                        <span class="sub-reply-to">{{ reply.replyToName }}</span>
                      </template>
                      <span class="sub-time">{{ reply.createTime }}</span>
                    </div>
                    <div class="sub-text">{{ reply.content }}</div>
                    <span class="cmt-action sub-action" @click="openReply(comment, reply)">
                      <i class="el-icon-chat-line-square"></i> 回复
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getMessageDetail, toggleLike, publishComment } from '../api'
import { mapState } from 'vuex'

export default {
  name: 'MessageDetail',
  data() {
    return {
      message: {},
      loading: false,
      commentContent: '',
      replyTarget: null,
      replyContent: '',
      submitting: false
    }
  },
  computed: {
    ...mapState(['currentUid']),
    renderedContent() {
      if (!this.message.content) return ''
      return this.message.content.replace(/\n/g, '<br>')
    },
    filesForDisplay() {
      if (!this.message.filePaths || !this.message.fileNames) return []
      const paths = this.message.filePaths.split(',')
      const names = this.message.fileNames.split(',')
      return paths.map((path, i) => ({ path: path.trim(), name: names[i] ? names[i].trim() : '' }))
    }
  },
  created() {
    const urlUid = this.$route.query.uid
    if (urlUid) {
      this.$store.commit('SET_UID', urlUid)
    }
    this.fetchDetail()
  },
  methods: {
    getFileUrl(filePath) {
      return '/message-board/uploads/' + filePath
    },
    async fetchDetail() {
      this.loading = true
      try {
        this.message = await getMessageDetail(this.$route.params.id)
      } catch (e) {
        this.$message.error('加载详情失败')
      } finally {
        this.loading = false
      }
    },
    scrollToComment() {
      const box = this.$refs.commentBox
      if (box) {
        box.scrollIntoView({ behavior: 'smooth', block: 'start' })
        this.$nextTick(() => {
          const textarea = box.querySelector('textarea')
          if (textarea) textarea.focus()
        })
      }
    },
    async handleLike() {
      if (!this.currentUid) {
        this.$message.warning('请通过正确的链接访问系统')
        return
      }
      try {
        const liked = await toggleLike(this.message.id)
        this.message.liked = liked
        this.message.likeCount += liked ? 1 : -1
      } catch (e) {
        this.$message.error('操作失败')
      }
    },
    async submitComment() {
      if (!this.commentContent.trim()) return
      this.submitting = true
      try {
        await publishComment({ messageId: this.message.id, content: this.commentContent, parentId: null })
        this.$message.success('评论成功')
        this.commentContent = ''
        this.fetchDetail()
      } catch (e) {
        this.$message.error(e.message || '评论失败')
      } finally {
        this.submitting = false
      }
    },
    openReply(comment, reply) {
      this.replyTarget = reply || comment
      this.replyContent = ''
      this.$nextTick(() => {
        const box = document.querySelector('.reply-editor textarea')
        if (box) box.focus()
      })
    },
    async submitReply(comment) {
      if (!this.replyContent.trim()) return
      this.submitting = true
      try {
        await publishComment({
          messageId: this.message.id,
          content: this.replyContent,
          parentId: comment.id,
          replyToUid: this.replyTarget ? this.replyTarget.uid : comment.uid
        })
        this.$message.success('回复成功')
        this.replyTarget = null
        this.replyContent = ''
        this.fetchDetail()
      } catch (e) {
        this.$message.error(e.message || '回复失败')
      } finally {
        this.submitting = false
      }
    }
  }
}
</script>

<style scoped>
.detail-page {
  width: 100%;
  padding: 24px 60px;
}

/* ========== 顶部导航 ========== */
.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 0;
  margin-bottom: 8px;
}
.back-btn {
  font-size: 14px;
}
.top-bar-right {
  display: flex;
  align-items: center;
  gap: 20px;
  font-size: 14px;
  color: #86909c;
}
.top-bar-category {
  padding: 4px 14px;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 600;
}
.top-bar-category.question {
  background: #fef0f0;
  color: #f56c6c;
}
.top-bar-category.requirement {
  background: #f0f9eb;
  color: #67c23a;
}
.top-bar-stat i {
  margin-right: 4px;
  font-size: 16px;
}
.top-bar-stat i.liked {
  color: #f56c6c;
}

/* ========== 文章区 ========== */
.article-wrapper {
  background: #fff;
  border-radius: 16px;
  padding: 52px 64px;
  margin-bottom: 20px;
  box-shadow: 0 2px 16px rgba(0,0,0,0.04);
}
.article-title {
  font-size: 32px;
  font-weight: 800;
  color: #1d2129;
  line-height: 1.4;
  margin-bottom: 28px;
  letter-spacing: -0.3px;
}
.article-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 32px;
  margin-bottom: 36px;
  border-bottom: 1px solid #f2f3f5;
}
.meta-author {
  display: flex;
  align-items: center;
  gap: 10px;
}
.meta-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #409EFF, #337ECC);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  font-weight: 700;
}
.meta-name {
  font-size: 15px;
  font-weight: 600;
  color: #1d2129;
}
.meta-dot {
  color: #c9cdd4;
}
.meta-org {
  font-size: 14px;
  color: #86909c;
}
.meta-time {
  font-size: 14px;
  color: #c9cdd4;
}
.article-content {
  font-size: 17px;
  color: #1d2129;
  line-height: 2.1;
  word-break: break-word;
}
.article-phone {
  margin-top: 20px;
  padding: 12px 16px;
  background: #f7f8fa;
  border-radius: 8px;
  font-size: 14px;
  color: #4e5969;
}
.article-phone i {
  color: #409EFF;
  margin-right: 4px;
}
.article-files {
  margin-top: 20px;
  padding: 16px;
  background: #f7f8fa;
  border-radius: 8px;
}
.files-title {
  font-size: 14px;
  font-weight: 600;
  color: #1d2129;
  margin-bottom: 10px;
}
.files-title i {
  color: #409EFF;
  margin-right: 4px;
}
.file-item {
  padding: 6px 0;
  font-size: 13px;
}
.file-item i {
  color: #e6a23c;
  margin-right: 6px;
}
.file-item a {
  color: #409EFF;
  text-decoration: none;
}
.file-item a:hover {
  text-decoration: underline;
}

/* ========== 底部操作栏 ========== */
.action-bar {
  background: #fff;
  border-radius: 16px;
  padding: 16px 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 16px rgba(0,0,0,0.04);
  position: sticky;
  top: 80px;
  z-index: 10;
}
.action-bar-inner {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 22px;
  border-radius: 24px;
  background: #f7f8fa;
  cursor: pointer;
  transition: all 0.25s;
  font-size: 15px;
  font-weight: 600;
  color: #4e5969;
  user-select: none;
  border: 1px solid transparent;
}
.action-btn:hover {
  background: #e8f3ff;
  border-color: #bcd7ff;
  color: #409EFF;
}
.action-btn.active {
  background: #fef0f0;
  border-color: #fbc4c4;
  color: #f56c6c;
}
.action-btn i {
  font-size: 20px;
}

/* ========== 评论区 ========== */
.comment-wrapper {
  background: #fff;
  border-radius: 16px;
  padding: 44px 64px;
  box-shadow: 0 2px 16px rgba(0,0,0,0.04);
}
.comment-section-title {
  font-size: 18px;
  font-weight: 700;
  color: #1d2129;
  margin-bottom: 28px;
}
.comment-count-badge {
  display: inline-block;
  padding: 2px 12px;
  border-radius: 12px;
  background: #e8f3ff;
  color: #409EFF;
  font-size: 14px;
  margin-left: 8px;
  vertical-align: middle;
}

/* 评论编辑器 */
.comment-editor {
  display: flex;
  gap: 16px;
  padding: 20px;
  background: #f7f8fa;
  border-radius: 12px;
  margin-bottom: 32px;
}
.editor-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #409EFF, #337ECC);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 700;
  flex-shrink: 0;
}
.editor-body {
  flex: 1;
  min-width: 0;
}
.editor-body ::v-deep textarea {
  border-radius: 8px;
  font-size: 14px;
}
.editor-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 14px;
}
.editor-hint {
  font-size: 13px;
  color: #c9cdd4;
}

/* 评论列表 */
.comment-item {
  display: flex;
  gap: 14px;
  padding: 22px 0;
  border-bottom: 1px solid #f2f3f5;
}
.comment-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}
.cmt-avatar {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  background: linear-gradient(135deg, #409EFF, #337ECC);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 17px;
  font-weight: 700;
  flex-shrink: 0;
}
.cmt-main {
  flex: 1;
  min-width: 0;
}
.cmt-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.cmt-user {
  display: flex;
  align-items: center;
  gap: 8px;
}
.cmt-name {
  font-size: 15px;
  font-weight: 600;
  color: #1d2129;
}
.cmt-org {
  font-size: 13px;
  color: #c9cdd4;
}
.cmt-time {
  font-size: 13px;
  color: #c9cdd4;
}
.cmt-content {
  font-size: 15px;
  color: #4e5969;
  line-height: 1.75;
  margin-bottom: 10px;
  word-break: break-word;
}
.cmt-actions {
  display: flex;
  gap: 20px;
}
.cmt-action {
  font-size: 13px;
  color: #86909c;
  cursor: pointer;
  transition: color 0.2s;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
.cmt-action:hover {
  color: #409EFF;
}
.sub-action {
  margin-top: 6px;
}

/* 回复编辑器 */
.reply-editor {
  margin-top: 14px;
}
.reply-editor ::v-deep textarea {
  border-radius: 8px;
  font-size: 13px;
}
.reply-editor-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}
.reply-editor-hint {
  font-size: 12px;
  color: #c9cdd4;
}

/* 子回复 */
.sub-replies {
  margin-top: 16px;
  padding: 18px 20px;
  background: #f7f8fa;
  border-radius: 10px;
  border-left: 3px solid #e5e6eb;
}
.sub-reply-item {
  display: flex;
  gap: 10px;
  padding: 8px 0;
}
.sub-reply-item + .sub-reply-item {
  border-top: 1px solid #e5e6eb;
}
.sub-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, #67c23a, #5daf34);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}
.sub-reply-body {
  flex: 1;
  min-width: 0;
}
.sub-reply-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 4px;
  font-size: 13px;
}
.sub-name {
  font-weight: 600;
  color: #1d2129;
}
.sub-arrow {
  font-size: 10px;
  color: #c9cdd4;
}
.sub-reply-to {
  color: #86909c;
  font-size: 12px;
}
.sub-time {
  font-size: 12px;
  color: #c9cdd4;
  margin-left: auto;
}
.sub-text {
  font-size: 14px;
  color: #4e5969;
  line-height: 1.6;
  margin-bottom: 4px;
  word-break: break-word;
}
</style>
