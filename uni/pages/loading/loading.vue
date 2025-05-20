<template>
  <view class="container">
    <image src="/static/loading-page.png" class="loading-background"></image>
    <view class="top-container">
      <text class="count-down">{{ countDown }}S</text>
      <view class="button" @click="toIndex">点击进入</view>
    </view>
    <view class="logo">
      <image src="/static/logo.png" class="logo-image"></image>
    </view>
    <view class="brand">
      EAP听感分析仪
    </view>
  </view>
</template>

<script>
let interval
export default {
  data() {
    return {
      countDown: 3
    }
  },
  computed: {
    loginUser() {
      return this.$store.state.loginUser
    }
  },
  mounted() {
    if (interval) {
      return
    }
    interval = setInterval(() => {
      this.countDown--
      if (this.countDown === 0) {
        clearInterval(interval)
        interval = null
        this.toIndex()
      }
    }, 1000)
  },
  methods: {
    toIndex() {
		  console.error('mounted: 页面已挂载')
      if (interval) {
        clearInterval(interval)
        interval = null
      }
      const loginUser = this.loginUser
      if (loginUser && loginUser.id) {
        this.switchTab('/pages/index/index')
      } else {
        this.navigateTo('/pages/login/login')
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

  .top-container {
    margin-top: 60rpx;
    width: 80%;
    display: flex;
    justify-content: space-between;

    .count-down {
    }

    .button {
      padding: 10rpx 20rpx;
      background: #c5c5c4;
      border-radius: 60rpx;
      color: #fff;
    }
  }

  .logo {
    margin-top: 2vh;
    width: 360rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    //border-radius: 50%;
    //overflow: hidden;

    .logo-image {
      width: 296rpx;
      height: 726rpx;
    }
  }

  .brand {
    margin-top: 10vh;
    font-weight: bold;
    font-size: 52rpx;
  }
}


</style>
