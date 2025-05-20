<script>
import { generateDocument, retryOperation } from './api/index.js';

let interval = null;
export default {
  onLaunch: function () {
    const rkModule = uni.requireNativePlugin("rk-module");
    rkModule.checkPer();
    if (interval) {
      clearInterval(interval);
      interval = null;
    }

    function getInterviewText(data) {
      let result = "";
      const roleList = ["A", "B"];
      let len = 0;
      const map = new Map();
      for (const item1 of data) {
        if (map.has(item1.rl)) {
          // 代表已经添加过了
          result += `${map.get(item1.rl)}：${item1.text}\n`;
        } else {
          if (len < 2) {
            map.set(item1.rl, roleList[len]);
            result += `${roleList[len]}：${item1.text}\n`;
            len++;
          } else {
            result += `${roleList[0]}：${item1.text}\n`;
          }
        }
      }
      return result;
    }
    const list = this.$store.state.recordList;
    interval = setInterval(() => {
      console.log("开始查询订单状态");
      try {
        if (!list || list.length === 0) {
          console.log("记录列表为空，无需处理");
          return;
        }

        list.forEach((item) => {
          if (!item) {
            console.warn("发现无效记录项");
            return;
          }

          if (!item.isFinished) {
            console.log(`处理未完成订单: ${item.uuid}`);
            if (!item.orderId) {
              console.warn(`订单缺少orderId: ${item.uuid}`);
              return;
            }

            rkModule.queryOrder(item.orderId, (data) => {
              if (!data) {
                console.warn(`订单查询未返回数据: ${item.orderId}`);
                return;
              }
              item.isFinished = true;
              item.data = data;
              console.log(`订单查询成功: ${item.orderId}`, data);
              item.content = getInterviewText(data);
              this.$store.commit("updateRecord", item);
            });
          }
          else if (item.isFinished) {
            console.log(`检查已完成订单: ${item.uuid}`);
            if (!item.docPath) {
              console.log(`准备生成文档: ${item.uuid}`);
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

              // 使用封装的API和重试机制
              retryOperation(async () => {
                const result = await generateDocument(params);
                if (result.data) {
                  item.docPath = result.data;
                  this.$store.commit("updateRecord", item);
                }
              }, 2).catch(err => {
                console.error(`文档生成最终失败: ${item.uuid}`, err);
              });
            }
          }
        });
      } catch (e) {
        console.error(`定时任务执行出错`, e);
        // 记录错误，但不中断定时任务
      }
    }, 40 * 1000);

    // 添加全局未捕获异常处理
    // uni.onError(function(err) {
    //   console.error('[全局错误]', err);
    // });
  },

  onShow: function () {
  },
  onHide: function () {
  },
  methods: {},
};
</script>

<style>
/*每个页面公共css */
</style>
