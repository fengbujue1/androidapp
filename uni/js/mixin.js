import RemoteLogger from './remoteLogger';

export default {
    data() {
        return {}
    },
    methods: {
        px2rpx(px) {
            return Math.floor(px / uni.getSystemInfoSync().windowWidth * 750)
        },
        navigateTo(url) {
            uni.navigateTo({
                url, fail: err => {
                    RemoteLogger.error(JSON.stringify(err))
                }
            })
        },
        redirectTo(url) {
            uni.redirectTo({
                url, fail: err => {
                    RemoteLogger.error(JSON.stringify(err))
                }
            })
        },
        switchTab(url) {
            uni.switchTab({url})
        },
        showToast(title) {
            uni.showToast({
                title,
                icon: 'none'
            })
        },
        generateUUID() {
            return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
                const r = Math.random() * 16 | 0,
                    v = c === 'x' ? r : (r & 0x3 | 0x8);
                return v.toString(16);
            });
        }
    }
}
