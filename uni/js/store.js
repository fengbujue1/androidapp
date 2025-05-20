import Vue from 'vue'
import Vuex from 'vuex'
import createPersistentStore from 'vuex-persistedstate'
Vue.use(Vuex)


export default new Vuex.Store({
  state: {
    // 当前访谈员工
    currentUser: {},
    recordList: [],
    currentRecord: {},
    loginUser: {}
  },
  getters: {
  },
  mutations: {
    clearLoginUser(state) {
      state.loginUser = {}
    },
    setCurrentUser(state, currentUser) {
      state.currentUser = currentUser
    },
    setRecordList(state, recordList) {
      state.recordList = recordList
    },
    addRecord(state, record) {
      const index = state.recordList.findIndex(item => item.uuid === record.uuid)
      if (index !== -1) {
        state.recordList.splice(index, 1)
      }
      state.recordList.push(record)
    },
    addRecordingFile(state, {part, filePath}) {
      const currentUser = state.currentUser
      console.log('currentUser', currentUser)
      const recordingFiles = JSON.parse(JSON.stringify(state.currentUser.recordingFiles || {}))
      recordingFiles[`_${part}`] = filePath
      console.log(recordingFiles, filePath, `_${part}`)
      state.currentUser.recordingFiles = recordingFiles
      console.log(state.currentUser)
    },
    setCurrentRecord(state, record) {
      state.currentRecord = record
    },
    updateRecord(state, record) {
      const index = state.recordList.findIndex(item => item.uuid === record.uuid)
      if (index !== -1) {
        state.recordList.splice(index, 1, record)
      }
    },
    deleteRecord(state, record) {
      state.recordList = state.recordList.filter(item => item.uuid !== record.uuid)
    },
    setLoginUser(state, user) {
      state.loginUser = user
    }
  },
  actions: {
  },
  modules: {
  },
  plugins: [
    createPersistentStore({
      storage: {
        getItem: key => {
          return uni.getStorageSync(key)
        },
        setItem: (key, value) => {
          uni.setStorageSync(key, value)
        },
        removeItem: key => {
          uni.removeStorageSync(key)
        }
      },
      reducer(val) {
        return {
          recordList: val.recordList,
          loginUser: val.loginUser
        }
      },
      key: 'zsh'
    })
  ]
})
