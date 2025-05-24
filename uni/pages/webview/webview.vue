<template>
	<view>
		<web-view v-if="!!src" :src="`https://view.officeapps.live.com/op/embed.aspx?src=${src}`"></web-view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				src: ''
			}
		},
    onLoad(options) {
		 console.log("文档地址：",options.src);
      this.src = options.src
    },
		methods: {
			
		}
	}
</script>

<style>

</style>
