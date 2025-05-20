# 文档生成服务

这是一个Node.js版本的文档生成服务，用于接收对话记录并生成分析报告文档。

## 功能特点

- 调用OpenAI API处理对话记录
- 生成包含情绪波动图表的Word文档
- 提供REST API接口

## 安装

确保你已安装Node.js 14或更高版本，然后运行:

```bash
npm install
```

## 配置

编辑`controller.js`文件，根据需要修改以下配置:

- OpenAI API密钥和基础URL
- 文档保存路径
- 应用ID和密钥

## 运行

开发模式:

```bash
npm run dev
```

生产模式:

```bash
npm start
```

## API使用

### 生成文档

**接口:** `POST /genDoc`

**请求体:**
```json
{
  "record": "对话记录内容",
  "zwjndr1": "50%",
  "swpzdr1": "40%",
  "zwjndl": 1,
  "swpzdl": 1,
  "duration": 300
}
```

**响应:**
```json
{
  "success": true,
  "data": "https://example.com/zshdoc/xxxx.docx"
}
```

## 依赖库

- express: Web服务器框架
- docx: Word文档生成库
- openai: OpenAI API客户端
- uuid: 唯一ID生成 