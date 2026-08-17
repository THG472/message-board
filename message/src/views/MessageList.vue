<template>
  <div class="message-list-page">
    <div class="main-layout">
      <!-- 左侧：留言列表 -->
      <div class="left-content">
        <!-- 顶部操作栏 -->
        <div class="toolbar">
          <div class="toolbar-left">
            <el-radio-group v-model="activeTab" size="medium" @change="handleTabChange">
              <el-radio-button label="">全部留言</el-radio-button>
              <el-radio-button label="question">问题反馈</el-radio-button>
              <el-radio-button label="requirement">需求建议</el-radio-button>
            </el-radio-group>
          </div>
          <el-button type="primary" icon="el-icon-plus" size="medium" @click="showPublishDialog = true" :disabled="!currentUid">
            发布留言
          </el-button>
        </div>

        <!-- 统计卡片 -->
        <div class="stats-row">
          <div class="stat-card">
            <div class="stat-card-icon icon-total">
              <i class="el-icon-document"></i>
            </div>
            <div class="stat-card-body">
              <div class="stat-card-num">{{ total }}</div>
              <div class="stat-card-label">留言总数</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-card-icon icon-question">
              <i class="el-icon-warning-outline"></i>
            </div>
            <div class="stat-card-body">
              <div class="stat-card-num">{{ questionCount }}</div>
              <div class="stat-card-label">问题反馈</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-card-icon icon-requirement">
              <i class="el-icon-edit-outline"></i>
            </div>
            <div class="stat-card-body">
              <div class="stat-card-num">{{ requirementCount }}</div>
              <div class="stat-card-label">需求建议</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-card-icon icon-like">
              <i class="el-icon-star-on"></i>
            </div>
            <div class="stat-card-body">
              <div class="stat-card-num">{{ totalLikes }}</div>
              <div class="stat-card-label">累计点赞</div>
            </div>
          </div>
        </div>

        <!-- 留言列表 -->
        <div v-loading="loading" class="message-list" element-loading-text="加载中...">
          <el-empty v-if="list.length === 0 && !loading" description="暂无留言，快来发布第一条吧" :image-size="120" />
          <div
            v-for="item in list"
            :key="item.id"
            class="message-card"
            @click="goDetail(item.id)"
          >
            <div class="card-body">
              <div class="card-main">
                <div class="card-header">
                  <span class="card-category" :class="item.category">
                    {{ item.category === 'question' ? '问题' : '需求' }}
                  </span>
                  <span class="card-title">{{ item.title }}</span>
                </div>
                <div class="card-content">{{ item.content }}</div>
              </div>
              <div class="card-stats">
                <div
                  class="card-stat-item"
                  :class="{ active: item.liked }"
                  @click.stop="handleLike(item)"
                >
                  <span class="card-stat-num">{{ item.likeCount }}</span>
                  <span class="card-stat-text">点赞</span>
                </div>
                <div class="card-stat-item">
                  <span class="card-stat-num">{{ item.commentCount }}</span>
                  <span class="card-stat-text">评论</span>
                </div>
              </div>
            </div>
            <div class="card-footer">
              <div class="footer-user">
                <span class="user-avatar">{{ item.userName ? item.userName.charAt(0) : '?' }}</span>
                <span class="user-name">{{ item.userName }}</span>
                <span v-if="item.orgName" class="user-org">{{ item.orgName }}</span>
              </div>
              <span class="footer-time">{{ item.createTime }}</span>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <el-pagination
          v-if="total > 0"
          class="pagination"
          background
          layout="total, prev, pager, next, jumper"
          :total="total"
          :page-size="pageSize"
          :current-page.sync="pageNum"
          @current-change="fetchList"
        />
      </div>

      <!-- 右侧：热榜 -->
      <div class="right-sidebar">
        <!-- 点赞热榜 -->
        <div class="rank-panel">
          <div class="rank-panel-header like-header">
            <i class="el-icon-star-on"></i>
            <span>点赞排行榜</span>
          </div>
          <div v-loading="loadingLike" class="rank-panel-body">
            <el-empty v-if="likeList.length === 0 && !loadingLike" description="暂无数据" :image-size="50" />
            <div
              v-for="item in likeList"
              :key="'like-' + item.id"
              class="rank-item"
              @click="goDetail(item.id)"
            >
              <div class="rank-num" :class="'rank-' + item.rank">
                <template v-if="item.rank === 1">🥇</template>
                <template v-else-if="item.rank === 2">🥈</template>
                <template v-else-if="item.rank === 3">🥉</template>
                <template v-else>{{ item.rank }}</template>
              </div>
              <div class="rank-info">
                <div class="rank-title" :title="item.title">{{ item.title }}</div>
                <div class="rank-meta">{{ item.userName }}</div>
              </div>
              <div class="rank-count like-count">{{ item.likeCount }}</div>
            </div>
          </div>
        </div>

        <!-- 评论热榜 -->
        <div class="rank-panel">
          <div class="rank-panel-header comment-header">
            <i class="el-icon-chat-dot-round"></i>
            <span>评论排行榜</span>
          </div>
          <div v-loading="loadingComment" class="rank-panel-body">
            <el-empty v-if="commentList.length === 0 && !loadingComment" description="暂无数据" :image-size="50" />
            <div
              v-for="item in commentList"
              :key="'comment-' + item.id"
              class="rank-item"
              @click="goDetail(item.id)"
            >
              <div class="rank-num" :class="'rank-' + item.rank">
                <template v-if="item.rank === 1">🥇</template>
                <template v-else-if="item.rank === 2">🥈</template>
                <template v-else-if="item.rank === 3">🥉</template>
                <template v-else>{{ item.rank }}</template>
              </div>
              <div class="rank-info">
                <div class="rank-title" :title="item.title">{{ item.title }}</div>
                <div class="rank-meta">{{ item.userName }}</div>
              </div>
              <div class="rank-count comment-count">{{ item.commentCount }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 发布留言对话框 -->
    <el-dialog title="发布留言" :visible.sync="showPublishDialog" width="680px" :close-on-click-modal="false" top="8vh">
      <el-form ref="publishForm" :model="publishForm" :rules="rules" label-width="80px" label-position="top">
        <el-form-item label="分类" prop="category">
          <el-radio-group v-model="publishForm.category">
            <el-radio label="question">问题反馈</el-radio>
            <el-radio label="requirement">需求建议</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="publishForm.title" placeholder="请输入留言标题，简明扼要地描述您的问题或需求" maxlength="100" show-word-limit size="medium" />
        </el-form-item>
        <el-form-item label="详细描述" prop="content">
          <el-input
            v-model="publishForm.content"
            type="textarea"
            :rows="6"
            placeholder="请详细描述您遇到的问题或希望增加的功能，包括场景、步骤、期望结果等..."
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="publishForm.phone" placeholder="选填，方便我们联系您" maxlength="20" size="medium" style="width: 260px" />
        </el-form-item>
        <el-form-item label="附件">
          <div class="upload-area">
            <el-upload
              ref="uploadRef"
              action=""
              :auto-upload="false"
              :file-list="fileList"
              :on-remove="handleFileRemove"
              :on-change="handleFileChange"
              :before-upload="() => false"
              multiple
            >
              <el-button size="small" type="primary" icon="el-icon-upload2">选择文件</el-button>
              <span class="upload-hint">选填，支持 jpg/png/pdf/doc/xlsx/zip 等格式，单文件最大10MB</span>
            </el-upload>
          </div>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="showPublishDialog = false" size="medium">取消</el-button>
        <el-button type="primary" @click="handlePublish" :loading="publishing" size="medium">确认发布</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getMessageList, publishMessage, toggleLike, getHotByLike, getHotByComment, uploadFiles } from '../api'
import { mapState } from 'vuex'

export default {
  name: 'MessageList',
  data() {
    return {
      list: [],
      loading: false,
      pageNum: 1,
      pageSize: 10,
      total: 0,
      questionCount: 0,
      requirementCount: 0,
      totalLikes: 0,
      activeTab: '',
      showPublishDialog: false,
      publishing: false,
      publishForm: {
        category: 'question',
        title: '',
        content: '',
        phone: '',
        filePaths: '',
        fileNames: ''
      },
      fileList: [],
      uploading: false,
      rules: {
        category: [{ required: true, message: '请选择分类', trigger: 'change' }],
        title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
        content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
      },
      likeList: [],
      commentList: [],
      loadingLike: false,
      loadingComment: false
    }
  },
  computed: {
    ...mapState(['currentUid'])
  },
  created() {
    const urlUid = this.$route.query.uid
    if (urlUid) {
      this.$store.commit('SET_UID', urlUid)
    }
    this.fetchList()
    this.fetchHotByLike()
    this.fetchHotByComment()
  },
  methods: {
    async fetchList() {
      this.loading = true
      try {
        // 并行请求各分类数据
        const [allRes, questionRes, requirementRes] = await Promise.all([
          getMessageList({ pageNum: this.pageNum, pageSize: this.pageSize, category: this.activeTab || undefined }),
          getMessageList({ pageNum: 1, pageSize: 1, category: 'question' }),
          getMessageList({ pageNum: 1, pageSize: 1, category: 'requirement' })
        ])
        this.list = allRes.records || []
        this.total = allRes.total || 0
        this.questionCount = questionRes.total || 0
        this.requirementCount = requirementRes.total || 0
        // 累计点赞数从列表数据估算（服务端可后续优化为单独接口）
        this.totalLikes = (allRes.records || []).reduce((sum, item) => sum + (item.likeCount || 0), 0)
      } catch (e) {
        this.$message.error('加载留言列表失败')
      } finally {
        this.loading = false
      }
    },
    handleTabChange() {
      this.pageNum = 1
      this.fetchList()
    },
    goDetail(id) {
      const routeUrl = this.$router.resolve({ path: `/detail/${id}`, query: { uid: this.currentUid } })
      window.open(routeUrl.href, '_blank')
    },
    async handleLike(item) {
      if (!this.currentUid) {
        this.$message.warning('请通过正确的链接访问系统')
        return
      }
      try {
        const liked = await toggleLike(item.id)
        item.liked = liked
        item.likeCount += liked ? 1 : -1
        this.totalLikes += liked ? 1 : -1
        this.fetchHotByLike()
      } catch (e) {
        this.$message.error('操作失败')
      }
    },
    handleFileRemove(file, fileList) {
      this.fileList = fileList
    },
    handleFileChange(file, fileList) {
      this.fileList = fileList
    },
    async handlePublish() {
      try {
        await this.$refs.publishForm.validate()
      } catch {
        return
      }
      this.publishing = true
      try {
        // 先上传附件
        if (this.fileList.length > 0) {
          this.uploading = true
          const rawFiles = this.fileList.map(f => f.raw)
          const uploadResult = await uploadFiles(rawFiles)
          const paths = uploadResult.map(item => item.path).join(',')
          const names = uploadResult.map(item => item.name).join(',')
          this.publishForm.filePaths = paths
          this.publishForm.fileNames = names
          this.uploading = false
        }
        await publishMessage(this.publishForm)
        this.$message.success('发布成功')
        this.showPublishDialog = false
        this.publishForm = { category: 'question', title: '', content: '', phone: '', filePaths: '', fileNames: '' }
        this.fileList = []
        this.pageNum = 1
        this.fetchList()
      } catch (e) {
        this.$message.error(e.message || '发布失败')
      } finally {
        this.publishing = false
        this.uploading = false
      }
    },
    async fetchHotByLike() {
      this.loadingLike = true
      try {
        this.likeList = await getHotByLike(10)
      } catch (e) {
        // 静默失败
      } finally {
        this.loadingLike = false
      }
    },
    async fetchHotByComment() {
      this.loadingComment = true
      try {
        this.commentList = await getHotByComment(10)
      } catch (e) {
        // 静默失败
      } finally {
        this.loadingComment = false
      }
    }
  }
}
</script>

<style scoped>
.message-list-page {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0;
}

/* ========== 主布局 ========== */
.main-layout {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}
.left-content {
  flex: 1;
  min-width: 0;
}
.right-sidebar {
  width: 300px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 20px;
  position: sticky;
  top: 84px;
}

/* ========== 工具栏 ========== */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  padding: 16px 24px;
  border-radius: 12px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
}
.toolbar ::v-deep .el-radio-button__inner {
  padding: 10px 24px;
  font-size: 14px;
}

/* ========== 统计卡片 ========== */
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}
.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
  transition: transform 0.2s, box-shadow 0.2s;
}
.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(0,0,0,0.08);
}
.stat-card-icon {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  flex-shrink: 0;
}
.stat-card-icon.icon-total {
  background: #ecf5ff;
  color: #409EFF;
}
.stat-card-icon.icon-question {
  background: #fef0f0;
  color: #f56c6c;
}
.stat-card-icon.icon-requirement {
  background: #f0f9eb;
  color: #67c23a;
}
.stat-card-icon.icon-like {
  background: #fdf6ec;
  color: #e6a23c;
}
.stat-card-num {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  line-height: 1;
}
.stat-card-label {
  font-size: 13px;
  color: #909399;
  margin-top: 6px;
}

/* ========== 留言列表 ========== */
.message-card {
  background: #fff;
  border-radius: 10px;
  margin-bottom: 12px;
  padding: 16px 20px;
  cursor: pointer;
  box-shadow: 0 1px 8px rgba(0,0,0,0.04);
  transition: all 0.25s ease;
  border: 1px solid transparent;
}
.message-card:hover {
  border-color: #409EFF;
  box-shadow: 0 2px 12px rgba(64,158,255,0.1);
  transform: translateY(-1px);
}
.card-body {
  display: flex;
  gap: 18px;
  align-items: center;
}
.card-main {
  flex: 1;
  min-width: 0;
}
.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}
.card-category {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 3px;
  font-size: 11px;
  font-weight: 600;
  flex-shrink: 0;
}
.card-category.question {
  background: #fef0f0;
  color: #f56c6c;
}
.card-category.requirement {
  background: #f0f9eb;
  color: #67c23a;
}
.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #1d2129;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.card-content {
  color: #86909c;
  font-size: 13px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.card-stats {
  display: flex;
  gap: 16px;
  flex-shrink: 0;
  padding-left: 16px;
  border-left: 1px solid #f0f0f0;
}
.card-stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1px;
  cursor: pointer;
  padding: 4px 10px;
  border-radius: 6px;
  transition: all 0.2s;
}
.card-stat-item:hover {
  background: #f5f7fa;
}
.card-stat-item.active {
  background: #fef0f0;
}
.card-stat-num {
  font-size: 16px;
  font-weight: 700;
  color: #1d2129;
}
.card-stat-item.active .card-stat-num {
  color: #f56c6c;
}
.card-stat-text {
  font-size: 11px;
  color: #86909c;
}
.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #f2f3f5;
}
.footer-user {
  display: flex;
  align-items: center;
  gap: 8px;
}
.user-avatar {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: linear-gradient(135deg, #409EFF, #337ECC);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
}
.user-name {
  font-size: 13px;
  color: #1d2129;
  font-weight: 500;
}
.user-org {
  font-size: 12px;
  color: #86909c;
}
.footer-time {
  font-size: 12px;
  color: #c9cdd4;
}
.pagination {
  margin-top: 28px;
  text-align: center;
}

/* ========== 右侧热榜 ========== */
.rank-panel {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
}
.rank-panel-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px 20px;
  font-size: 15px;
  font-weight: 600;
  color: #fff;
}
.rank-panel-header.like-header {
  background: linear-gradient(135deg, #f56c6c, #e85d5d);
}
.rank-panel-header.comment-header {
  background: linear-gradient(135deg, #409EFF, #337ECC);
}
.rank-panel-body {
  padding: 4px 0;
  max-height: 420px;
  overflow-y: auto;
}
.rank-item {
  display: flex;
  align-items: center;
  padding: 10px 20px;
  cursor: pointer;
  transition: background 0.15s;
}
.rank-item:hover {
  background: #f7f8fa;
}
.rank-num {
  width: 32px;
  text-align: center;
  font-size: 16px;
  font-weight: 700;
  flex-shrink: 0;
  color: #86909c;
}
.rank-num.rank-1,
.rank-num.rank-2,
.rank-num.rank-3 {
  font-size: 20px;
}
.rank-info {
  flex: 1;
  padding: 0 10px;
  min-width: 0;
}
.rank-title {
  font-size: 13px;
  color: #1d2129;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.rank-meta {
  font-size: 12px;
  color: #c9cdd4;
  margin-top: 2px;
}
.rank-count {
  font-size: 15px;
  font-weight: 700;
  flex-shrink: 0;
  min-width: 30px;
  text-align: right;
}
.like-count {
  color: #f56c6c;
}
.comment-count {
  color: #409EFF;
}

/* ========== 上传区域 ========== */
.upload-area {
  width: 100%;
}
.upload-hint {
  font-size: 12px;
  color: #c9cdd4;
  margin-left: 12px;
}
</style>
