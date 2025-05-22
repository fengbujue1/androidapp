import App from './App'
import store from "@/js/store";
import RemoteLogger from "@/js/remoteLogger";

// 初始化远程日志记录器，替代原有的console
//RemoteLogger.replaceConsole();

// #ifndef VUE3
import Vue from 'vue'
import './uni.promisify.adaptor'
Vue.config.productionTip = false
App.mpType = 'app'
const app = new Vue({
  store,
  ...App
})
app.$mount()
// #endif
import mixin from "./js/mixin";
Vue.mixin(mixin)
// #ifdef VUE3
import { createSSRApp } from 'vue'
export function createApp() {
  const app = createSSRApp(App)
  return {
    app
  }
}
// #endif