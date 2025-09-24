import { login, logout, getInfo, getVersion } from '@sys/view/security_api.js';
import { getAccessToken, setToken, removeToken, getRefreshToken } from '@/utils/auth'
import { dictFormatter } from '@/utils/utils'

const system = {
  state: {
    id: 0, // 用户编号
    name: '', //login name
    nickname: '',//username
    avatar: '',
    roles: [],
    permissions: null,
    version: '',
    title: '',
    userOptions: {},
  },

  mutations: {
    SET_TITLE: (state, title) => {
      state.title = title
    },
    SET_OPTIONS: (state, options) => {
      state.userOptions = options
    },
    SET_DEPTID: (state, deptId) => {
      state.deptId = deptId
    },
    SET_ID: (state, id) => {
      state.id = id
    },
    SET_NAME: (state, name) => {
      state.name = name
    },
    SET_NICKNAME: (state, nickname) => {
      state.nickname = nickname
    },
    SET_AVATAR: (state, avatar) => {
      state.avatar = avatar
    },
    SET_ROLES: (state, roles) => {
      state.roles = roles
    },
    SET_PERMISSIONS: (state, permissions) => {
      state.permissions = permissions
    },
    SET_VERSION: (state, version) => {
      state.version = version;
    }
  },

  actions: {
    // 登录
    Login({ commit }, userInfo) {
      console.log('Login:', JSON.stringify(userInfo));
      return new Promise((resolve, reject) => {
        login(userInfo).then(user => {
          console.log('After Login:', JSON.stringify(user));
          // 设置 token
          setToken({ accessToken: user.token });
          resolve()
        }).catch(error => {
          reject(error)
        })
      })
    },
    SetTitle({ commit, state }, title) {
      commit('SET_TITLE', title)
    },
    // 获取用户信息
    GetInfo({ commit, state }) {
      return new Promise((resolve, reject) => {
        getInfo().then(user => {
          commit('SET_ROLES', user.roles)
          commit('SET_PERMISSIONS', user.permissions)
          commit('SET_DEPTID', user.deptId)
          commit('SET_ID', user.userId)
          commit('SET_NAME', user.loginName)
          commit('SET_NICKNAME', user.username)
          commit('SET_AVATAR', user.avatar)
          const options = user.userOptions ?? {};
          commit('SET_OPTIONS', options)
          const title = `【${dictFormatter('Department', options.departmentId)}】`;
          commit('SET_TITLE', title);
          resolve(user)
        }).catch(error => {
          // reject(error)
        })
      })
    },
    GetVersion({ commit, state }) {
      return new Promise((resolve, reject) => {
        getVersion().then(version => {
          commit('SET_VERSION', version)
          resolve(version)
        }).catch(error => {
          // reject(error)
        })
      })
    },

    /*
    // 社交登录
    SocialLogin({ commit }, userInfo) {
      const code = userInfo.code
      const state = userInfo.state
      const type = userInfo.type
      return new Promise((resolve, reject) => {
        socialLogin(type, code, state).then(res => {
          res = res.data;
          // 设置 token
          setToken(res)
          resolve()
        }).catch(error => {
          reject(error)
        })
      })
    },

    // 短信登录
    SmsLogin({ commit }, userInfo) {
      const mobile = userInfo.mobile.trim()
      const mobileCode = userInfo.mobileCode
      return new Promise((resolve, reject) => {
        smsLogin(mobile, mobileCode).then(res => {
          res = res.data;
          // 设置 token
          setToken(res)
          resolve()
        }).catch(error => {
          reject(error)
        })
      })
    },
    */

    // 退出系统
    LogOut({ commit, state }) {
      return new Promise((resolve, reject) => {
        logout(state.token).then(() => {
          commit('SET_ROLES', [])
          commit('SET_PERMISSIONS', [])
          commit('SET_OPTIONS', {})
          removeToken()
          resolve()
        }).catch(error => {
          commit('SET_ROLES', [])
          commit('SET_PERMISSIONS', [])
          commit('SET_OPTIONS', {})
          removeToken()
          resolve()
          // removeToken()
          //reject(error)
        })
      })
    }
  }
}

export default system
