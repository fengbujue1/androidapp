<template>
  <view class="container">
    <view class="info-container">
      <text>姓名：{{ currentUser.name }}</text>
      <text>工号：{{ currentUser.jobId }}</text>
    </view>
    <view class="content-container">
      <image
        src="/static/recording-input-background.png"
        class="background"
      ></image>
      <view class="text-container">{{ interviewText }}</view>
    </view>
    <view class="middle-container">
      <image
        src="/static/recording-bottom-mountain.png"
        class="mountain"
      ></image>
      <image
        src="/static/recording.png"
        class="recording-status"
        v-if="recordingStatus === 1"
      ></image>
      <image
        src="/static/recording-stop.png"
        class="recording-status"
        v-else
      ></image>
    </view>
    <view class="time">
      {{ secondsToHMS(time) }}
    </view>
    <view class="action-bar">
      <view
        class="button-container"
        @click="startRecording"
        v-if="recordingStatus !== 1"
      >
        <image src="/static/start.png" class="button"></image>
        <view>开始</view>
      </view>
      <view class="button-container" @click="pauseRecording" v-else>
        <image src="/static/pause.png" class="button"></image>
        <view>暂停</view>
      </view>
      <view class="button-container" @click="stopRecording">
        <image src="/static/finish.png" class="button"></image>
        <view>完成</view>
      </view>
    </view>
    <view> 请注意，点击"开始"按钮后观察到记时开始则为仪器正式开始工作 </view>
    <toast2 ref="toast" />
  </view>
</template>

<script>
import Toast2 from "../../components/toast2";
import { generateDocument } from "../../api/index.js";

let keepTimeInterval = null;
const mode = "speed";
// 报告
function getCurrentDateAsString() {
  const currentDate = new Date();
  const year = currentDate.getFullYear();
  const month = String(currentDate.getMonth() + 1).padStart(2, "0");
  const day = String(currentDate.getDate()).padStart(2, "0");

  return `${year}-${month}-${day}`;
}
export default {
  components: { Toast2 },
  data() {
    return {
      interviewText: "",
      time: 0,
      recordingStatus: 0, // 0 录制停止 1 录制中 2 录制暂停 4 录制丢弃
    };
  },
  computed: {
    currentUser() {
      return this.$store.state.currentUser;
    },
    loginUser() {
      return this.$store.state.loginUser;
    },
  },
  watch: {
    recordingStatus(val) {
      if (val === 1) {
        this.$refs.toast.open();
      } else {
        this.$refs.toast.close();
      }
    },
  },
  onLoad() {
    console.log("录音页面加载");
    uni.getNetworkType({
      success: (res) => {
        console.log(`当前网络类型: ${res.networkType}`);
        if (res.networkType === "none" || res.networkType === "unknown") {
          uni.showModal({
            title: "提示",
            content: "网络不可用，EAP听感分析仪无法正常工作",
          });
          console.warn("网络不可用，EAP听感分析仪无法正常工作");
        }
      },
      fail: (err) => {
        console.error("获取网络状态失败", err);
      },
    });
  },
  onUnload() {
    console.log("录音页面卸载，停止所有录音操作");
    this.recordingStatus = 4;
    this.stopKeepTime();
    const rkModule = uni.requireNativePlugin("rk-module");
    rkModule.stopRecording();
    // #ifdef APP-PLUS
    plus.speech.stopRecognize();
    // #endif
  },
  methods: {
    ontStart() {
      this.title = "...倾听中...";
      this.interviewText = "";
      console.log("开始语音识别");
    },
    onVolumeChange(e) {
      this.valueWidth = 100 * e.volume + "px";
      // console.log(`音量变化: ${e.volume}`);
    },
    onRecognizing(e) {
      this.partialResult = e.partialResult;
      console.log("识别中", e.partialResult);
    },
    onRecognition(e) {
      this.interviewText += e.result;
      this.interviewText
        ? (this.interviewText += "\n")
        : (this.interviewText = "");
      this.result = this.interviewText;
      this.partialResult = e.result;
      console.log("识别结果", e.result);
    },

    onEnd() {
      console.log("语音识别结束");
      if (!this.interviewText || this.interviewText === "") {
        console.warn("识别结果为空");
      }
      this.result = this.interviewText;
      this.title = "未开始";
      this.valueWidth = "0px";
      this.partialResult = "...";
    },
    startRecognize() {
      console.log("开始录音和语音识别");
      // #ifdef APP-PLUS
      const rkModule = uni.requireNativePlugin("rk-module");
      rkModule.startRecorderWithMode(mode, async (data) => {
        console.log("录音处理结果", data);
        if (this.recordingStatus === 4) {
          uni.hideLoading();
          console.log("录音已丢弃，不处理结果");
          return;
        }
        this.$store.commit("setCurrentUser", {
          ...this.currentUser,
          interviewText: this.interviewText,
          path: data.path,
          orderId: data.orderId,
          duration: data.duration,
          isFinished: false,
          date: getCurrentDateAsString(),
          userId: this.loginUser.id,
          time: Date.now(),
          sentenceList: [],
        });
        this.$store.commit("addRecord", this.currentUser);
        if (!data.success) {
          uni.hideLoading();
          uni.showModal({
            title: "提示",
            content: data.msg,
          });
          console.error("录音处理失败", data.msg);
          return;
        }
        try {
          let sentenceList = [];
          if (mode === "speed") {
            // "sentence_list":[{"speech_speed":5.8020477,"speaker_id":0,"text":"你是否能接受自己的不完美，并从中学习？","emotional_energy":0,"end_time":3590,"start_time":660}]}]
            sentenceList = data.sentence_list;
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
            this.interviewText = interviewText;
          }

          console.log("录音处理成功，保存路径", data.path);
          console.log("当前识别文本", this.interviewText);
          this.$store.commit("setCurrentUser", {
            ...this.currentUser,
            interviewText: this.interviewText,
            path: data.path,
            orderId: data.orderId,
            duration: data.duration,
            date: getCurrentDateAsString(),
            userId: this.loginUser.id,
            time: Date.now(),
            isFinished: mode === "speed" ? true : false,
            sentenceList: sentenceList,
          });
          if (mode === "speed") {
            uni.hideLoading();
            uni.showLoading({
              title: "生成报告中...",
              mask: true,
            });
            // 直接调用生成报告
            await this.generateReport(this.currentUser);
          }

          setTimeout(() => {
            uni.hideLoading();
            this.$store.commit("addRecord", this.currentUser);
            this.$store.commit("setCurrentRecord", this.interviewText);
            console.log("跳转到录音详情页");
            this.redirectTo("/pages/recording-details/recording-details");
          }, 2000);
        } catch (e) {
          uni.hideLoading();
          uni.showModal({
            title: "录制语音出错",
            content: `错误原因：${e}`,
          });
          console.error("录制语音出错", e);
        }
      });

      plus.speech.addEventListener("volumeChange", this.onVolumeChange, false);
      plus.speech.addEventListener("recognizing", this.onRecognizing, false);
      plus.speech.addEventListener("recognition", this.onRecognition, false);
      plus.speech.addEventListener("end", this.onEnd, false);
      // todo 这玩意儿要收钱，后台充钱才能用。
      plus.speech.startRecognize({
        engine: "baidu",
        lang: "zh-cn",
        userInterface: false,
        continue: true,
      });
      console.log("启动百度语音识别引擎");
      // #endif
    },
    endRecognize() {
      console.log("结束语音识别");
      const rkModule = uni.requireNativePlugin("rk-module");
      rkModule.stopRecording();
      // #ifdef APP-PLUS
      plus.speech.stopRecognize();
      // #endif
    },
    startRecording() {
      console.log("开始/恢复录音", { status: this.recordingStatus });
      this.startKeepTime();
      if (this.recordingStatus === 2) {
        const rkModule = uni.requireNativePlugin("rk-module");
        rkModule.resumeRecording();
        this.startKeepTime();
        this.recordingStatus = 1;
        console.log("恢复暂停的录音");
        plus.speech.startRecognize({
          engine: "baidu",
          lang: "zh-cn",
          userInterface: false,
          continue: true,
        });
        return;
      }
      this.recordingStatus = 1;
      this.startRecognize();
    },
    pauseRecording() {
      console.log("暂停录音");
      this.recordingStatus = 2;
      const rkModule = uni.requireNativePlugin("rk-module");
      rkModule.pauseRecording();
      // #ifdef APP-PLUS
      plus.speech.stopRecognize();
      // #endif
      if (keepTimeInterval) {
        clearInterval(keepTimeInterval);
        keepTimeInterval = null;
      }
    },
    stopRecording() {
      if (this.recordingStatus !== 1) {
        console.warn("尝试停止未开始的录音", {
          status: this.recordingStatus,
        });
        uni.showToast({
          title: "未开始录制",
          icon: "error",
        });
        return;
      }
      console.log("停止录音并开始处理");
      const titleText =
        mode === "speed" ? "识别语音中...请勿退出程序" : "处理中，大约需要30s";
      uni.showLoading({
        title: titleText,
        mask: true,
      });
      this.recordingStatus = 3;
      this.stopKeepTime();
      const rkModule = uni.requireNativePlugin("rk-module");
      rkModule.stopRecording();
      // #ifdef APP-PLUS
      setTimeout(() => {
        plus.speech.stopRecognize();
        console.log("停止语音识别");
      }, 3000);
      // #endif
    },
    startKeepTime() {
      console.log("开始计时");
      this.stopKeepTime();
      keepTimeInterval = setInterval(() => {
        this.time++;
      }, 1000);
    },
    stopKeepTime() {
      if (keepTimeInterval) {
        console.log("停止计时", { totalTime: this.time });
        clearInterval(keepTimeInterval);
        keepTimeInterval = null;
      }
    },
    secondsToHMS(seconds) {
      const hours = Math.floor(seconds / 3600);
      const minutes = Math.floor((seconds % 3600) / 60);
      const remainingSeconds = seconds % 60;

      const hoursStr = hours < 10 ? "0" + hours : hours;
      const minutesStr = minutes < 10 ? "0" + minutes : minutes;
      const secondsStr =
        remainingSeconds < 10 ? "0" + remainingSeconds : remainingSeconds;

      return hoursStr + ":" + minutesStr + ":" + secondsStr;
    },
    generateReport(item) {
      console.log("急速语音识别生成文档");
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
        record: item.interviewText,
        duration: item.duration,
        zzmm: item.zzmm,
      };

      console.log("调用生成接口", params);

      return new Promise((resolve, reject) => {
        generateDocument(params)
          .then((result) => {
            item.docPath = result.data;
            this.$store.commit("updateRecord", item);
            resolve();
          })
          .catch((err) => {
            console.error("生成文档失败", err);
            resolve(); // 即使失败也继续流程
          });
      });
    },
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
    height: 300rpx;
    position: relative;
    color: #666666;
    padding: 20rpx;

    .background {
      position: absolute;
      top: 0;
      left: 0;
      width: 684rpx;
      height: 340rpx;
      z-index: -1;
    }

    .text-container {
      width: 644rpx;
      height: 300rpx;
      overflow-y: scroll;
    }
  }

  .middle-container {
    position: relative;
    height: 400rpx;

    .mountain {
      margin-left: 25rpx;
      width: 700rpx;
      height: 261rpx;
      opacity: 0.5;
    }

    .recording-status {
      position: absolute;
      width: 700rpx;
      height: 170rpx;
      bottom: 0;
      left: 50%;
      transform: translateX(-50%);
    }
  }

  .time {
    margin-top: 40rpx;
    font-size: 26px;
  }

  .action-bar {
    margin-top: 30rpx;
    width: 400rpx;
    display: flex;
    justify-content: space-between;

    .button-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;

      .button {
        width: 100rpx;
        height: 100rpx;
      }

      view {
        margin-top: 20rpx;
      }
    }
  }
}
</style>
