<template>
  <view class="container">
    <view class="info-container">
      <text>姓名：{{ currentUser.name }}</text>
      <text>工号：{{ currentUser.jobId }}</text>
    </view>
    <view class="content-container">
      <image
        src="/static/recording-details-input-background.png"
        class="background"
      ></image>
      <textarea
        v-if="currentUser.content"
        v-model="currentUser.content"
        class="text-container"
        :maxlength="-1"
      ></textarea>
      <textarea
        v-else
        v-model="currentUser.interviewText"
        :disabled="!edit"
        class="text-container"
        :maxlength="-1"
      ></textarea>
    </view>
    <view>
      <slider
        style="width: 600rpx"
        :value="currentTime"
        :min="0"
        :max="duration"
        @change="sliderChange"
        @changing="sliderChanging"
        activeColor="#7E100F"
        backgroundColor="#eaeaea"
        block-color="#7e100f"
        block-size="18"
      />
    </view>
    <view class="time">
      {{ secondsToMinutesSeconds(currentTime) }}/{{
        secondsToMinutesSeconds(duration)
      }}
    </view>
    <view class="action-bar">
      <image
        src="/static/recording-bottom-mountain.png"
        class="mountain"
      ></image>
      <view class="button" @click="changeState">{{
        state ? "暂停" : "播放"
      }}</view>
      <view class="button" @click="doEdit">编辑</view>
      <view class="button" @click="opendoc">心理报告</view>
      <view class="button" @click="checkVoiceRecognition">查询识别</view>
      <view class="button" @click="generateDocument">生成文档</view>
    </view>
    <toast ref="toast" title="基本信息修改" />
  </view>
</template>

<script>
import toast from "../../components/toast";
const innerAudioContext = uni.createInnerAudioContext();
innerAudioContext.autoplay = false;

innerAudioContext.onError((res) => {
  console.log(res.errMsg);
  console.log(res.errCode);
});
export default {
  components: { toast },

  data() {
    return {
      time: 0,
      edit: false,
      duration: 0,
      currentTime: 0,
      state: false,
    };
  },
  computed: {
    currentUser() {
      return this.$store.state.currentUser;
    },
  },
  onLoad() {
    innerAudioContext.onPlay(() => {
      console.log("开始播放");
      this.state = true;
    });
    innerAudioContext.onStop((data) => {
      console.log(data);
      this.state = false;
    });
    innerAudioContext.src = this.currentUser.path;
    innerAudioContext.onCanplay(() => {
      this.duration = this.currentUser.duration || 0;
      // innerAudioContext.play()
    });
    innerAudioContext.onTimeUpdate(() => {
      this.setPlayData();
    });
  },
  methods: {
    changeState() {
      if (this.state) {
        innerAudioContext.stop();
      } else {
        innerAudioContext.play();
      }
    },
    opendoc() {
      const docPath = this.currentUser.docPath;
      if (!docPath) {
        uni.showToast({
          title: "当前文档正在生成中...大约需要5分钟",
          icon: "none",
        });
        return;
      }
      this.navigateTo("/pages/webview/webview?src=" + docPath);
      innerAudioContext.stop();
    },
    setPlayData(event) {
      if (!innerAudioContext.duration && !innerAudioContext.currentTime) return;
      this.duration = innerAudioContext.duration || 0;
      this.currentTime = innerAudioContext.currentTime || 0;
    },
    secondsToMinutesSeconds(seconds) {
      let minutes = Math.floor(seconds / 60);
      if (minutes < 10) {
        minutes = "0" + minutes;
      }
      const remainingSeconds = Math.floor(seconds % 60);
      return (
        minutes + ":" + (remainingSeconds < 10 ? "0" : "") + remainingSeconds
      );
    },
    sliderChanging(e) {
      this.isSlidering = true;
      this.currentTime = e.detail.value;
    },
    sliderChange(e) {
      this.isSlidering = false;
      this.currentTime = e.detail.value;
      innerAudioContext.seek(e.detail.value);
      innerAudioContext.onCanplay(() => {
        // innerAudioContext.play()
      });
    },
    doEdit() {
      this.$refs.toast.open(true);
    },
    checkVoiceRecognition() {
      const item = this.currentUser;
      // if (item.isFinished) {
      //   uni.showToast({
      //     title: '语音识别已完成',
      //     icon: 'success'
      //   });
      //   return;
      // }
      uni.showLoading({
        title: "语音识别中...请勿退出程序",
        mask: true,
      });
      const rkModule = uni.requireNativePlugin("rk-module");
      rkModule.queryOrder(
        item.orderId,
        async (data) => {
          uni.hideLoading();
          if (!data) {
            uni.showToast({
              title: "语音识别尚未完成",
              icon: "none",
            });
            return;
          }
          const sentenceList = data.sentence_list;
          if (!sentenceList || sentenceList.length === 0) {
            uni.showToast({
              title: "没有识别到语音",
              icon: "none",
            });
            return;
          }
          const speakerMap = new Map();
          let interviewText = "";
          const roleList = ["A", "B"];
          let len = 0;
          sentenceList.forEach((item) => {
            if (speakerMap.has(item.speaker_id)) {
              interviewText +=
                speakerMap.get(item.speaker_id) + "：" + item.text + "\n";
            } else {
              if (len < 2) {
                speakerMap.set(item.speaker_id, roleList[len]);
                interviewText += roleList[len] + "：" + item.text + "\n";
                len++;
              } else {
                interviewText += roleList[0] + "：" + item.text + "\n";
              }
            }
          });
          item.interviewText = interviewText;
          item.isFinished = true;
          this.$store.commit("updateRecord", item);
          uni.showToast({
            title: "语音识别已完成",
            icon: "success",
          });
          uni.showLoading({
            title: "生成报告中...",
            mask: true,
          });
          // 直接调用生成报告
          try {
            await this.generateDocument(this.currentUser);
          } catch (e) {
            console.error("生成文档失败");
            console.error(e);
          } finally {
            uni.hideLoading()
          }
          // function getInterviewText(data) {
          //   let result = "";
          //   const roleList = ["A", "B"];
          //   let len = 0;
          //   const map = new Map();
          //   for (const item1 of data) {
          //     if (map.has(item1.rl)) {
          //       // 代表已经添加过了
          //       result += `${map.get(item1.rl)}：${item1.text}\n`;
          //     } else {
          //       if (len < 2) {
          //         map.set(item1.rl, roleList[len]);
          //         result += `${roleList[len]}：${item1.text}\n`;
          //         len++;
          //       } else {
          //         result += `${roleList[0]}：${item1.text}\n`;
          //       }
          //     }
          //   }
          //   return result;
          // }

          // item.isFinished = true;
          // item.data = data;
          // item.content = getInterviewText(data);
          // this.$store.commit("updateRecord", item);

          // uni.showToast({
          //   title: '语音识别已完成',
          //   icon: 'success'
          // });
        },
        item.path
      );
    },
    generateDocument() {
      const item = this.currentUser;
      if (!item.isFinished) {
        uni.showToast({
          title: "请先等待语音识别完成",
          icon: "none",
        });
        return;
      }

      if (item.docPath) {
        uni.showToast({
          title: "文档已生成",
          icon: "success",
        });
        return;
      }

      const params = {
        id: item.id,
        phone: item.phone,
        unit: item.unit,
        job: item.job,
        gender: item.gender,
        age: item.age,
        marriage: item.marriage,
        params8: item.params8,
        params3: item.params3,
        record: item.content,
        duration: item.duration,
        zzmm: item.zzmm,
      };

      uni.showLoading({
        title: "正在生成文档...",
      });

      uni.request({
        url: "https://biexiaozhi.cn/zsh/genDoc",
        method: "POST",
        data: params,
        success: (res) => {
          uni.hideLoading();
          if (res.data.success) {
            item.docPath = res.data.data;
            this.$store.commit("updateRecord", item);
            uni.showToast({
              title: "文档生成成功",
              icon: "success",
            });
          } else {
            uni.showToast({
              title: "文档生成失败",
              icon: "none",
            });
          }
        },
        fail: () => {
          uni.hideLoading();
          uni.showToast({
            title: "网络请求失败",
            icon: "none",
          });
        },
      });
    },
  },
  onUnload() {
    innerAudioContext.stop();
  },
};
</script>

<style lang="scss" scoped>
.container {
  display: flex;
  flex-direction: column;
  align-items: center;

  .info-container {
    width: 90%;
    display: flex;
    justify-content: space-between;
    color: #000;
    font-weight: bold;
    padding: 20rpx;
  }

  .content-container {
    width: 644rpx;
    height: 640rpx;
    position: relative;
    color: #666666;
    padding: 20rpx;

    .background {
      position: absolute;
      top: 0;
      left: 0;
      width: 684rpx;
      height: 640rpx;
      z-index: -1;
    }

    .text-container {
      height: 640rpx;
      width: 644rpx;
      overflow-y: scroll;
    }
  }

  .time {
    font-size: 26px;
  }

  .action-bar {
    margin-top: 0rpx;
    width: 700rpx;
    height: 300rpx;
    display: flex;
    align-items: center;
    justify-content: space-around;
    position: relative;
    flex-wrap: wrap;

    .button {
      margin-top: 40rpx;
      background: #7e100f;
      border-radius: 30rpx;
      width: 180rpx;
      height: 60rpx;
      color: #ffffff;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 10rpx;
    }

    .mountain {
      position: absolute;
      top: 0;
      left: 0;
      width: 700rpx;
      height: 261rpx;
      opacity: 0.6;
      z-index: -1;
    }
  }
}
</style>
