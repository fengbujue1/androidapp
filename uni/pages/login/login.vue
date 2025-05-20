<template>
  <view class="container">
    <image src="/static/loading-page.png" class="loading-background"></image>
    <view class="top-background"></view>
    <view class="login">
      <image src="/static/logo2.png" style="width: 450px;height: 177px; margin-top: 130px"></image>
      <form class="form" @submit="login">
        <input type="text" v-model="username" placeholder="用户名" />
        <input type="password" v-model="password" placeholder="密码" />
        <button type="submit" @click="login">登录</button>
      </form>
    </view>
  </view>
</template>

<script>
// 获取 module
const userList = [{id: 1, username: '123456', password: '123456'}, {id: 2, username: 'zsh1', password: 'zsh1'},  {id: 3, username: 'zsh2', password: 'zsh2'},  {id: 4, username: 'zsh3', password: 'zsh3'},  {id: 5, username: 'zsh4', password: 'zsh4'}]
export default {
  data() {
    return {
      username: '',
      password: ''
    }
  },
  mounted() {

  },
  methods: {
    login() {
      const user = userList.find(item => item.username === this.username && item.password === this.password)
      if (user) {
        console.log(user)
        this.$store.commit('setLoginUser', user)
        this.switchTab('/pages/index/index')
      } else {
        uni.showModal({
          title: '提示',
          content: '用户名或密码错误'
        })
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.container {
  display: flex;
  align-items: center;
  flex-direction: column;
  width: 100%;
  height: 100%;

  .loading-background {
    position: fixed;
    height: 100vh;
    width: 100vw;
    z-index: -1;
  }
}
.login {
  display: flex;
  align-items: center;
  height: 100vh;
  flex-direction: column;
}

.form {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  //background-color: #fff;
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0px 0px 10px rgba(0, 0, 0, 0.1);
  margin-top: 120px;
}

input {
  width: 50vw;
  height: 30px;
  margin-bottom: 50px;
  padding: 10px;
  border: none;
  border-radius: 5px;
  box-shadow: 0px 0px 5px rgba(0, 0, 0, 0.1);
}

button {
  padding: 10px;
  border: none;
  border-radius: 5px;
  background-color: #7e100f;
  color: #fff;
  box-shadow: 0px 0px 5px rgba(0, 0, 0, 0.1);
  cursor: pointer;
}

.logo {

}
</style>