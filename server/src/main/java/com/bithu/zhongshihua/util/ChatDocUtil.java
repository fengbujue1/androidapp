package com.bithu.zhongshihua.util;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bithu.zhongshihua.dto.UploadResp;
import com.bithu.zhongshihua.dto.chat.ChatExtends;
import com.bithu.zhongshihua.dto.chat.ChatMessage;
import com.bithu.zhongshihua.dto.chat.ChatRequest;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Test
 *
 * @author ydwang16
 * @version 2023/09/06 11:45
 **/
public class ChatDocUtil {
    public UploadResp upload(String filePath, String url, String appId, String secret) {
        File file = new File(filePath);
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .connectTimeout(20, TimeUnit.SECONDS)
                .build();

        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);
        builder.addFormDataPart("file", file.getName(),
                RequestBody.create(MediaType.parse("multipart/form-data"), file));
        builder.addFormDataPart("fileType", "wiki");
        RequestBody body = builder.build();
        long ts = System.currentTimeMillis() / 1000;
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("appId", appId)
                .addHeader("timestamp", String.valueOf(ts))
                .addHeader("signature", ApiAuthUtil.getSignature(appId, secret, ts))
                .build();
        try {
            Response response = okHttpClient.newCall(request).execute();
            if (Objects.equals(response.code(), 200)) {
                String respBody = response.body().string();
                return JSON.parseObject(respBody, UploadResp.class);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public void chat(String chatUrl, String fileId, String question, String appId, String secret, AA aa) {
        ChatMessage message = new ChatMessage();
        message.setRole("user");
        message.setContent(question);
        // 请求内容
        ChatExtends chatExtends = new ChatExtends();
        chatExtends.setWikiPromptTpl("请将以下内容作为中石化心理咨询的资料输入：\\n<wikicontent>\\n下面给出的内容是实际进行心理咨询的录音音频转文字的内容，录音转文字结果可能会有部分错别字，请注意。\\n录音内容:<wikiquestion>\\n" +
                "现在你是心理分析仪的大脑，请你根据谈话的内容的语境、含义，再结合中石化心理咨询的20个问题，获取出以下信息（使用标准的json返回，每条都会给出相应的key，在小括号中就是key。不要返回其他额外的信息）：\\n" +
                "1. 自我接纳度的总分(zwjndl)，思想偏执度的总分(swpzdl)，只回答数字，没有的话就是0\\n" +
                "2. 自我接纳度的有效比例(zwjndr1)，思想偏执度的有效比例(swpzdr1)，回答百分之多少，不要回答其他信息。\\n" +
//                "3. 你匹配到的问题以及回答(questions)，不用完全匹配，意思相近即可\\n" +
                "下面是一个返回示例：" +
                "{\n" +
                "    \"zwjndl\": -2,\n" +
                "    \"swpzdl\": 5,\n" +
                "    \"zwjndr1\": \"20%\",\n" +
                "    \"swpzdr1\": \"50%\"\n" +
//                "    \"questions\": \"Q：你自己关注内心的感受吗？A：是的\"\n" +
                "}");
        ChatRequest request = ChatRequest.builder()
                .fileIds(Collections.singletonList(fileId))
                .topN(3)
                .chatExtends(chatExtends)
                .messages(Collections.singletonList(message))
                .build();

        // 构造url鉴权
        long ts = System.currentTimeMillis() / 1000;
        String signature = ApiAuthUtil.getSignature(appId, secret, ts);
        String requestUrl = chatUrl + "?" + "appId=" + appId + "&timestamp=" + ts + "&signature=" + signature;
        // ws
        Request wsRequest = (new Request.Builder()).url(requestUrl).build();
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder().build();
        StringBuffer buffer = new StringBuffer();
        WebSocket webSocket = okHttpClient.newWebSocket(wsRequest, new WebSocketListener() {
            @Override
            public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                super.onClosed(webSocket, code, reason);
                webSocket.close(1002, "websocket finish");
                okHttpClient.connectionPool().evictAll();
            }

            @Override
            public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
                super.onFailure(webSocket, t, response);
                webSocket.close(1001, "websocket finish");
                okHttpClient.connectionPool().evictAll();
            }

            @Override
            public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
                System.out.println(text);
                JSONObject jsonObject = JSON.parseObject(text);
                String content = jsonObject.getString("content");
                if (content != null && content != "") {
                    buffer.append(content);
                }
                if (Objects.equals(jsonObject.getInteger("status"), 2)) {
                    System.out.println("回答内容：" + buffer);
                    try {
                        aa.aa(buffer.toString());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    webSocket.close(1000, "websocket finish");
                    okHttpClient.connectionPool().evictAll();
                }
            }
        });
        webSocket.send(JSON.toJSONString(request));
    }

    public interface AA{
        void aa(String aa) throws IOException;
    }
}