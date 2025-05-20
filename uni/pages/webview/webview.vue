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
      this.src = options.src
    },
		methods: {
			
		}
	}
</script>

<style>

</style>
