<template>
  <view class="container">
    <view class="top-background">
      <image src="/static/smoke.png" class="smoke"></image>
      <image src="/static/smoke.png" class="smoke absolute"></image>
<!--      <image src="/static/index-title.png" class="index-title"></image>-->
      <view class="index-title">EAP听感分析仪</view>
      <image src="/static/index-top-mountain.png" class="index-top-mountain"></image>
    </view>
    <view class="start-button-container">
      <image src="/static/index-bottom-mountain.png" class="mountain-background-image"></image>
      <view class="start-button" @click="startRecord">
        <image src="/static/start-button.png" class="start-button-image"></image>
        <view class="microphone-container">
          <image src="/static/microphone.svg" class="microphone"></image>
          <text>开始录制</text>
        </view>
      </view>
    </view>
    <view class="tip" @click="toWebview">
      当您需要与员工进行访谈时请点击“开始录制”按钮
    </view>
    <view class="index-bottom-background">
      <image src="/static/index-bottom-mountain.png" class="index-bottom-background-image"></image>
    </view>
    <toast ref="toast" @submit="submit"/>
  </view>
</template>

<script>
// 获取 module
import toast from "../../components/toast";
export default {
  components: {toast},

  data() {
    return {}
  },
  mounted() {

  },
  methods: {
    toWebview() {
    },
    startRecord() {
		console.log("开始录制")
      this.$refs.toast.open()
    },
    submit() {
      this.navigateTo('/pages/recording/recording')
    }

  }
}
</script>

<style lang="scss" scoped>
.container {
  .top-background {
    width: 100%;
    background: #fdf1e5;
    display: flex;
    flex-direction: column;
    position: relative;
    .smoke {
      margin-top: 60rpx;
      margin-left: auto;
      width: 180rpx;
      height: 120rpx;
      opacity: 0.3;
    }

    .absolute {
      position: absolute;
      left: 350rpx;
      top: 60rpx;
      transform: translateX(-50%);
    }


    .index-title {
      height: 50rpx;
      position: absolute;
      left: 50%;
      top: 160rpx;
      transform: translateX(-50%);
      z-index: 10;
      color: #c07c0f;
      font-size: 40px;
    }

    .index-top-mountain {
      //margin-top: 10vh;
      width: 100%;
    }
  }

  .start-button-container {
    margin-top: 5%;
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
    .mountain-background-image {
      position: absolute;
      top: 0rpx;
      left: 0;
      width: 700rpx;
      height: 272rpx;
      z-index: -1;
      opacity: 0.1;
    }
    .start-button {
      width: 300rpx;
      height: 300rpx;
      border-radius: 50%;
      background: #7e100f;
      display: flex;
      align-items: center;
      justify-content: center;
      position: relative;
      overflow: hidden;
      .start-button-image {
        position: absolute;
        width: 300rpx;
        height: 278rpx;
      }
    }
    .microphone-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      .microphone {
        width: 120rpx;
        height: 120rpx;
      }
      text {
        color: #ffcc9c;
        margin-top: 40rpx;
      }
    }

  }
  .tip {
    margin-top: 2vh;
    width: 100%;
    text-align: center;
  }

  .index-bottom-background {
    width: 100%;
    text-align: center;
    .index-bottom-background-image {
      width: 700rpx;
      height: 272rpx;
      opacity: 0.4;
    }

  }

}
</style>