// API封装文件

// const BASE_URL = 'https://biexiaozhi.cn/zsh';
const BASE_URL = 'http://192.168.1.8:9099/zsh';

/**
 * 生成文档接口
 * @param {Object} params - 文档生成所需参数
 * @param {string} params.id - 记录ID
 * @param {string} params.phone - 电话
 * @param {string} params.unit - 单位
 * @param {string} params.job - 职位
 * @param {string} params.gender - 性别
 * @param {string} params.age - 年龄
 * @param {string} params.marriage - 婚姻状况
 * @param {string} params.params8 - 参数8
 * @param {string} params.params3 - 参数3
 * @param {string} params.record - 录音文本
 * @param {string} params.duration - 录音时长
 * @param {string} params.zzmm - 政治面貌
 * @returns {Promise} - 返回Promise对象
 */
export function generateDocument(params) {
  console.log(`发送生成文档请求: ${params.id || '未知ID'}`, params);
  
  return new Promise((resolve, reject) => {
    uni.request({
      url: `${BASE_URL}/genDoc`,
      method: 'POST',
      data: params,
      timeout: 30000, // 30秒超时
      success: (res) => {
        if (res.data.success) {
          console.log(`文档生成成功: ${params.id || '未知ID'}`);
          resolve(res.data);
        } else {
          console.warn(`文档生成接口返回失败: ${params.id || '未知ID'}`, res.data);
          reject(new Error('接口返回失败'));
        }
      },
      fail: (err) => {
        console.error(`文档生成请求失败: ${params.id || '未知ID'}`, err);
        reject(err);
      }
    });
  });
}

/**
 * 重试执行异步操作
 * @param {Function} operation - 要执行的异步操作
 * @param {number} maxRetries - 最大重试次数
 * @param {number} delay - 重试间隔(毫秒)
 * @returns {Promise} - 返回Promise对象
 */
export async function retryOperation(operation, maxRetries = 3, delay = 2000) {
  let retries = 0;
  while (retries < maxRetries) {
    try {
      return await operation();
    } catch (err) {
      retries++;
      if (retries >= maxRetries) {
        throw err;
      }
      console.warn(`操作失败，${retries}/${maxRetries}次重试`, err);
      await new Promise(resolve => setTimeout(resolve, delay));
    }
  }
} 