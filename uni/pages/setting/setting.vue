<template>
	<view class="container">
    <view class="top-background">
      <image src="/static/setting-background3.png" class="image1"></image>
      <image src="/static/setting-background2.png" class="image2"></image>
      <image src="/static/setting-background3.png" class="image3"></image>
      <image src="/static/setting-background1.png" class="image4"></image>
    </view>
    <view class="head-container">
      <image src="/static/logo2.png" class="head"></image>
      <view style="margin-top: 20rpx; font-size: 26px">管理员</view>
    </view>
    <view class="setting-menu-container">
      <view class="setting-item" @click="goto('questions')">
        <image src="/static/icon2.png" class="icon"></image>
        <view class="view">常见问题解答</view>
      </view>
      <view class="setting-item" @click="goto('customer')">
        <image src="/static/icon3.png" class="icon"></image>
        <view class="view">联系客服</view>
      </view>
      <view class="setting-item" @click="goto('about')">
        <image src="/static/icon4.png" class="icon"></image>
        <view class="view">关于</view>
      </view>
      <view class="setting-item" @click="gotoBQSY">
        <image src="/static/icon5.png" class="icon"></image>
        <view class="view">版权所有</view>
      </view>
      <view class="setting-item" @click="toLogin()">
        <image src="/static/icon1.png" class="icon"></image>
        <view class="view">退出登录</view>
      </view>
    </view>
    <image src="/static/index-bottom-mountain.png" class="bottom-mountain"></image>
  </view>
</template>

<script>
	export default {
		data() {
			return {
				
			}
		},
		methods: {
      toLogin() {
        uni.showModal({
          title: '提示',
          content: '是否退出登录？',
          success: res => {
            if (res.confirm) {
              this.$store.commit('clearLoginUser')
              this.redirectTo('/pages/login/login')
            }
          }
        })

      },
			goto(type) {
        uni.navigateTo({
          url: `/pages/tips/tips?id=1&type=${type}`,
          fail: err => {
            console.log(err)
          }
        })
      },
      gotoBQSY() {
        uni.navigateTo({
          url: `/pages/bqsy/bqsy`,
        })
      }
		}
	}
</script>

<style scoped lang="scss">
.container {
  background: #f9f9f9;
  position: relative;
  height: 100vh;
  .top-background {
    position: absolute;
    width: 100%;
    height: 40vh;
    background: #fdf1e5;
    .image1 {
      position: absolute;
      width: 274rpx;
      height: 94rpx;
      top: 40rpx;
      left: 0;
    }
    .image2 {
      position: absolute;
      width: 214rpx;
      height: 142rpx;
      right: 0;
      top: 20rpx;
    }
    .image3 {
      position: absolute;
      width: 274rpx;
      height: 94rpx;
      right: 40rpx;
      bottom: 20rpx;
    }
    .image4 {
      position: absolute;
      width: 750rpx;
      height: 258rpx;
      left: 0;
      bottom: 0;
    }

  }

  .head-container {
    width: 100%;
    height: 34vh;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    .head {
      width: 450px;
      height: 177px;
    }
  }

  .setting-menu-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    .setting-item {
      z-index: 9;
      margin-top: 20rpx;
      border-radius: 10rpx;
      background: #ffffff;
      width: 600rpx;
      height: 100rpx;
      display: flex;
      align-items: center;
      padding: 0 20rpx;
      .icon {
        width: 40rpx;
        height: 40rpx;
      }
      .view {
        margin-left: 30rpx;
        font-size: 20px;
      }
    }
    .setting-item:first-child {
      margin-top: 0;
    }
  }

  .bottom-mountain {
    position: fixed;
    bottom: 0;
    width: 700rpx;
    height: 272rpx;
    opacity: 0.2;
  }
}
</style>
