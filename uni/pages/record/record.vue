<template>
	<view class="container">
    <view class="input-container">
      <input type="text" placeholder="输入关键词" v-model="keywords"/>
      <image src="/static/glass.png" class="glass"></image>
    </view>
    <view class="card-container">
      <view class="card" v-for="(item, index) in showRecordList" :key="index" @click="toDetails(item)">
        <view style="width: 100%; display: flex; justify-content: flex-end; color: red">
          <text @click.stop="deleteCurrent(item)">删除</text>
        </view>
        <view class="title-container">
          <view>{{item.name}}</view>
          <view>{{item.date}}</view>
        </view>
        <view class="body">
          <view class="body-title">
            <text>访谈文字</text>
            <text style="color: rgba(126, 16, 15, 1)">录音时长：{{secondsToMinutesSeconds(item.duration)}}</text>
          </view>
          <text class="body-content">{{item.content}}</text>
        </view>
      </view>
      <view style="width: 100%; height: 30rpx"></view>
      <image src="/static/index-bottom-mountain.png" class="bottom-mountain"></image>
    </view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
        keywords: ''
			}
		},
    computed: {
      recordList () {
        return this.$store.state.recordList
      },
      showRecordList() {
        const list = this.recordList
        return list.filter(item => (item.name.includes(this.keywords) || item.content.includes(this.keywords)) && this.loginUser.id === item.userId).sort((item1, item2) => new Date(item2.time || item2.date).getTime() - new Date(item1.time || item1.date).getTime())
      },
      loginUser() {
        return this.$store.state.loginUser
      }
    },
    mounted() {
      console.log(this.loginUser)
    },
    methods: {
      toDetails(item) {
        this.$store.commit('setCurrentUser', item)
        this.navigateTo('/pages/recording-details/recording-details')
      },

      deleteCurrent(record) {
        uni.showModal({
          title: '提示',
          content: '确定删除此记录吗？',
          success: (res) => {
            if (res.confirm) {
              uni.showToast({
                title: '删除成功'
              })
              this.$store.commit('deleteRecord', record)
            }
          }
        })
      },
      secondsToMinutesSeconds(seconds) {
        let minutes = Math.floor(seconds / 60);
        if (minutes < 10) {
          minutes = "0" + minutes
        }
        const remainingSeconds = Math.floor(seconds % 60);
        return minutes + ":" + (remainingSeconds < 10 ? "0" : "") + remainingSeconds;
      },
			getInterviewText(item) {
        let data = item.data
        let result = ''
        for (const item1 of data) {
          result += item1.text + "\n"
        }
  			return result
			}
		}
	}
</script>

<style scoped lang="scss">

.container {
  background: #f9f9f9;
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100vw;
  height: 100vh;
  .input-container {
    margin-top: 20rpx;
    background: #ffffff;
    width: 80%;
    padding: 20rpx 30rpx;
    display: flex;
    align-items: center;
    justify-content: space-between;
    border-radius: 40rpx;
    input {
      width: 70%;
    }
    .glass {
      width: 30rpx;
      height: 30rpx;
    }
  }
  .card-container {
    width: calc(80% + 60rpx);
    height: calc(100% - 90rpx);
    overflow-y: scroll;
    .card {
      border-radius: 5rpx;
      margin-top: 20rpx;
      background: #fff;
      padding: 40rpx;
      .title-container {
        display: flex;
        justify-content: space-between;
        margin-bottom: 20rpx;
      }
      .body {
        border-top: 2px solid rgba(234, 234, 234, 1);
        padding: 10rpx 0;
        .body-title {
          margin-top: 10rpx;
          width: 100%;
          display: flex;
          justify-content: space-between;
        }
        .body-content {
          margin-top: 20rpx;
          overflow: hidden;
          text-overflow: ellipsis;
          display: -webkit-box;
          -webkit-line-clamp: 3;
          -webkit-box-orient: vertical;
        }
      }
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
