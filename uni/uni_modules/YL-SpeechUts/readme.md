
## 插件说明：
#### 该插件为Uts版插件，仅支持安卓端！

## 插件配置方法：

购买插件后，导入到项目，会自动生成插件目录：uni_modules>YL-SpeechUts，
将你下载的讯飞sdk（在线语音听写和在线语音合成流式版）替换掉YL-SpeechUts>app-android>libs下的讯飞sdk，
init初始化时，appId修改为自己的即可！

## 1.使用方法：
- 1.引入插件：
   
```javascript
import * as sr from "../../uni_modules/YL-SpeechUts"
```
- 2.初始化(注意：科大讯飞的appid，需要自己去科大讯飞官网申请)

```javascript
//初始化
sr.init("6005f95c");//体验测试阶段可以使用demo的，正式发布需要替换为自己的
//创建文字转语音对象
sr.createTts(); 
//创建语音转文字对象
sr.createIat();
```

- 3.语音合成：

```javascript
sr.textToVoice(text,res=>{})
```

- 4.语音听写：

```javascript
sr.voiceToText(res=>{})
```

- 5.停止方法

```javascript
sr.stopSpeaking();//停止语音合成
sr.stopListening();//停止语音听写
```

- 6.销毁：(退出页面前销毁)

```javascript
sr.destroy();
```

- 7.其它可供调用的方法：

```javascript
//语音合成
sr.setSpeaker("aisjiuxu");//设置发音人（可能收费，自己在讯飞后台配置）
sr.pauseSpeaking();//暂停
sr.resumeSpeaking();//恢复
sr.getTtsPath(path=>{});//获取语音文件路径

//语音听写
sr.stopListening();//停止
sr.setVadBos(10 * 1000);//设置语音听写前端点超时时间ms（最大10s,一般按默认即可）
sr.setVadEos(10 * 1000);//设置语音听写后端点超时时间ms（最大10s,一般按默认即可）
sr.setLang("zh_cn");//设置听写语言，默认中文（zh_cn，en_us）
sr.getIatPath(path=>{});//获取语音文件路径
```

代码示例：

```html
<template>
	<div style="padding: 20rpx;">
		<text style="display: block;margin-bottom: 20rpx;font-size: 20rpx;color: #FF0000;">文字转语音：</text>
		<text>{{text}}</text>
		<div style="display: flex;flex-direction: row;margin-top: 20rpx;">
			<text style="font-size: 20rpx;">状态：</text>
			<text style="margin-bottom: 20rpx;color: #FF0000;font-size: 20rpx;">{{toVoiceStatus}}</text>
		</div>
		<button type="primary" style="margin: 20rpx 0;" plain="true" @click="textToVoice()">语音朗读</button>
		<div style="display: flex;align-items: center;justify-content: space-between;flex-direction: row;">
			<button type="warn" plain="true" @click="stopSpeaking()" style="width: 160rpx;">停止</button>
			<button type="primary" plain="true" @click="pauseSpeaking()" style="width: 160rpx;">暂停</button>
			<button type="primary" plain="true" @click="resumeSpeaking()" style="width: 160rpx;">继续</button>
		</div>

		<text style="display: block;margin-bottom: 20rpx;margin-top: 20rpx;">语音合成文件路径：{{ttsPath}}</text>

		<text
			style="display: block;margin-top: 50rpx;;margin-bottom: 20rpx;font-size: 20rpx;color: #FF0000;">语音转文字：</text>
		<text style="display: block;margin-bottom: 20rpx;">{{transText}}</text>
		<div style="display: flex;flex-direction: row;">
			<text style="font-size: 20rpx;">状态：</text>
			<text style="margin-bottom: 20rpx;color: #FF0000;font-size: 20rpx;">{{toTextStatus}}</text>
			<text style="font-size: 20rpx;margin-left: 50rpx;">音量：</text>
			<text style="margin-bottom: 20rpx;color: #FF0000;font-size: 20rpx;">{{volume}}</text>
		</div>
		<button type="primary" style="margin: 20rpx 0;" plain="true" @click="voiceToText()">开始录音</button>
		<button type="primary" style="margin: 20rpx 0;" plain="true" @click="stopVoiceToText()">停止录音</button>
		<text style="display: block;margin-bottom: 20rpx;">语音听写文件路径：{{iatPath}}</text>

	</div>
</template>

<script>
	// 获取 module 
	import * as sr from "../../uni_modules/YL-SpeechUts"

	export default {
		data() {
			return {
				text: "uni-app是一个使用 Vue.js开发所有前端应用的框架",
				toVoiceStatus: "未开始",
				transText: "",
				toTextStatus: "未开始",
				volume: 0,
				ttsPath: "",
				iatPath: ""
			}
		},
		mounted() {
			//初始化
			sr.init("6005f95c");
			//创建文字转语音对象
			sr.createTts();
			//创建语音转文字对象
			sr.createIat();
		},
		methods: {
			textToVoice() {
				if (this.toVoiceStatus == '未开始' || this.toVoiceStatus == "朗读完成" || this.toVoiceStatus == "朗读停止") {
					sr.textToVoice(this.text, data => {
						switch (data.code) {
							case 1001:
								this.toVoiceStatus = "开始朗读"
								break;
							case 1002:
								this.toVoiceStatus = "暂停朗读"
								break;
							case 1003:
								this.toVoiceStatus = "继续朗读"
								break;
							case 1004:
								this.toVoiceStatus = "正在缓冲..."
								break;
							case 1005:
								this.toVoiceStatus = "正在朗读..."
								break;
							case 1006:
								this.toVoiceStatus = "朗读完成"
								sr.getTtsPath(path => {
									this.ttsPath = path;
								})
								break;
							case 1007:
								this.toVoiceStatus = "朗读停止"
								break;
						}
					});
				}
			},
			stopSpeaking() {
				sr.stopSpeaking();
			},
			pauseSpeaking() {
				sr.pauseSpeaking();
			},
			resumeSpeaking() {
				sr.resumeSpeaking();
			},
			voiceToText() {
				let that = this;
				sr.voiceToText(data => {
					if (data.code == 1001) {
						that.toTextStatus = "倾听中，请说话..."
					} else if (data.code == 1006) {
						this.toTextStatus = "倾听完毕"
					} else if (data.code == 1007) {
						this.toTextStatus = "停止倾听"
					} else if (data.code == 1008) {
						this.volume = data.msg;
					} else if (data.code == 1009) { //结果
						this.transText = data.msg;
						sr.getIatPath(path => {
							this.iatPath = path;
						})
					} else if (data.code == 1010) { //error信息
						this.transText = data.msg;
					}
				})
			},
			stopVoiceToText() {
				sr.stopListening();
			}
		}
	}
</script>
```

code:1006表示停止；
code:1009表示语音听写的结果回调；
code:1010表示错误回调；

其它各种回调状态，可参考以上案例代码！

