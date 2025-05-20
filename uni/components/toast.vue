<template>
  <view class="dialog" v-if="show">
    <view class="container">
      <view class="info-box">
        <view class="box-title">{{ title }}</view>
        <view class="form-line">
          <text>*姓名：</text>
          <input type="text" placeholder="请输入姓名" v-model="form.name">
        </view>
        <view class="form-line">
          <text>工号：</text>
          <input type="text" placeholder="请输入工号" v-model="form.jobId">
        </view>
        <view class="form-line">
          <text>电话号码：</text>
          <input type="text" placeholder="请输入电话号码" v-model="form.phone">
        </view>
        <view class="form-line">
          <text>单位：</text>
          <input type="text" placeholder="请输入单位" v-model="form.unit">
        </view>
        <view class="form-line">
          <text>岗位：</text>
          <input type="text" placeholder="请输入岗位" v-model="form.job">
        </view>
        <view class="form-line">
          <text>性别：</text>
          <input type="text" placeholder="请输入性别" v-model="form.gender">

          <!--          <picker mode="selector" :range="range1" @change="bindPickerChange1">{{ range1[form.gender] }}</picker>-->
        </view>
        <view class="form-line">
          <text>年龄段：</text>
          <input type="text" placeholder="请输入年龄段" v-model="form.age">
        </view>
        <view class="form-line">
          <text>婚姻状况：</text>
          <input type="text" placeholder="请输入婚姻状况" v-model="form.marriage">

          <!--          <picker mode="selector" :range="range" @change="bindPickerChange">{{ range[form.marriage] }}</picker>-->
        </view>
<!--        <view class="form-line">-->
<!--          <text>政治面貌：</text>-->
<!--          <input type="text" placeholder="请输入政治面貌" v-model="form.zzmm">-->

          <!--          <picker mode="selector" :range="range" @change="bindPickerChange">{{ range[form.marriage] }}</picker>-->
<!--        </view>-->
<!--        <view class="form-line">-->
<!--          <text>婚姻状况：</text>-->
<!--          <input type="text" placeholder="请输入婚姻状况" v-model="form.marriage">-->

<!--          &lt;!&ndash;          <picker mode="selector" :range="range" @change="bindPickerChange">{{ range[form.marriage] }}</picker>&ndash;&gt;-->
<!--        </view>-->

        <view class="confirm-button" @click="confirm">
          开始
        </view>
      </view>
      <view class="cancel-button" @click="close">×</view>
    </view>
  </view>
</template>

<script>
export default {
  name: "toast",
  props: {
    title: {
      default: '基本信息录入'
    }
  },
  data() {
    return {
      show: false,
      form: {
        name: '',
        jobId: '',
        phone: '',
        job: '',
        age: '',
        gender: '',
        marriage: '',
        zzmm: ''
      },
      range: ['未婚', '已婚'],
      range1: ['男', '女'],
    }
  },
  computed: {
    currentUser: {
      get() {
        return this.$store.state.currentUser
      },
      set(val) {
        console.log(val)
        this.$store.commit('setCurrentUser', val)
      }
    }
  },
  methods: {
    bindPickerChange1(e) {
      this.form.gender = e.detail.value
    },
    bindPickerChange(e) {
      console.log(e)
      this.form.marriage = e.detail.value
    },
    open(edit) {
      if (edit) {
        this.form = JSON.parse(JSON.stringify(this.currentUser))
      }
      this.show = true
    },
    close() {
      this.show = false
    },
    generateDateString() {
      const currentDate = new Date();
      const year = currentDate.getFullYear();
      const month = String(currentDate.getMonth() + 1).padStart(2, '0');
      const day = String(currentDate.getDate()).padStart(2, '0');

      // 假设这个数字是一个递增的序号，每次调用方法递增1
      let sequenceNumber = 1;

      // 将序号转换为四位数字，不足四位的在前面补0
      const paddedSequence = String(sequenceNumber).padStart(4, '0');

      return `${year}${month}${day}-${paddedSequence}`;
    },
    confirm() {
      console.log('submit')
      if (!this.form.name) {
        this.showToast('请输入姓名')
        return
      }
      this.form.uuid = this.generateUUID()
      this.form.id = this.generateDateString()
      this.form.recordingFiles = {}
      console.log('submit')
      this.$store.commit('setCurrentUser', this.form)
      this.$emit('submit')
      this.form = {
        name: '',
        jobId: ''
      }
      this.close()
    }
  }
}
</script>

<style lang="scss" scoped>
.dialog {
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, .5);
  position: fixed;
  top: 0;
  left: 0;
  z-index: 999;
  display: flex;
  justify-content: center;
  align-items: center;


  .container {
    .info-box {
      width: 480rpx;
      height: 900rpx;
      border-radius: 10rpx;
      background: #ffffff;
      display: flex;
      flex-direction: column;
      align-items: center;

      .box-title {
        margin-top: 20rpx;
        font-weight: bold;
      }

      .form-line {
        margin-top: 30rpx;
        display: flex;
        align-items: center;
        width: 90%;

        text {
          width: 30%;
          text-align: right;
        }

        input {
          width: 60%;
          padding-left: 20rpx;
          background: #f1f1f1;
          border-radius: 6rpx;
          height: 50rpx;
          line-height: 50rpx;
        }
      }

      .confirm-button {
        margin-top: 50rpx;
        width: 180rpx;
        height: 60rpx;
        line-height: 60rpx;
        text-align: center;
        color: #ffffff;
        border-radius: 30rpx;
        background: #7e100f;
      }
    }


    .cancel-button {
      margin-top: 40rpx;
      margin-left: 220rpx;
      width: 40rpx;
      height: 40rpx;
      text-align: center;
      line-height: 40rpx;
      border-radius: 50%;
      color: #fff;
      border: 3rpx solid #fff;
      font-weight: bold;
    }

  }
}
</style>