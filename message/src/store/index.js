import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    currentUid: '',      // 当前登录用户UID
    currentUserName: '', // 当前登录用户姓名(xm)
    currentSfzh: '',     // 当前登录用户身份证号(sfzh，用于页面水印)
    currentOrgName: ''   // 当前登录用户机构名称
  },
  mutations: {
    SET_UID(state, uid) {
      state.currentUid = uid
    },
    SET_USER_INFO(state, userInfo) {
      state.currentUserName = userInfo.userName || ''
      state.currentSfzh = userInfo.sfzh || ''
      state.currentOrgName = userInfo.orgName || ''
    }
  },
  actions: {},
  modules: {}
})
