<template>
  <view class="dialog" v-if="show">
    <view class="container">
      <view class="question-box">
        <view v-for="(item, index) in questions" :key="index">{{item}}</view>
      </view>
      <view class="cancel-button" @click="close">×</view>
    </view>

  </view>
</template>

<script>
export default {
  name: "toast2",
  props: {
    title: {
      default: '基本信息录入'
    }
  },
  data() {
    return {
      show: false,

      questions: ["你是否能接受自己的不完美，并从中学习？","你对自己的情绪和体验是否充满好奇？","你是否愿意体验各种情绪，包括那些不舒服的？","当你犯错误时，你能否原谅自己并尝试改正？","你认为自己有权利表达自己的情感和需求吗？","你是否愿意面对并接受自己的挑战和困扰？","你是否理解自己的情绪并尊重它们？","你会关注自己内心的感受吗？","你是否允许自己在某些时候感到脆弱？","你认为自己善于理解和接受自己的情绪吗？",
      "你是否尊重他人的观点和想法？","你愿意信任他人并与他人合作解决问题吗？","你是否愿意为了维护关系而做出一些妥协？","你相信他人大多数时候是出于善意吗？","你尊重他人的独立性，并让他们自由地做出决定吗？","你相信别人也可以帮助你吗？","你愿意倾听他人的建议和反馈吗？","你会尽量理解他人的感受吗？","你认为人们应该尊重和理解彼此的差异吗？","你相信大多数人都是值得信任的吗？"
      ]
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
    open() {
      this.show = true
    },
    close() {
      this.show = false
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
    display: flex;
    flex-direction: column;
    .question-box{
      background: #ffffff;
      border-radius: 20rpx;
      padding: 20rpx;
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