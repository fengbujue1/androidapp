package com.bithu.zhongshihua.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Base64;

/**
 * ApiAuthAlgorithm
 *
 * @author ydwang16
 * @version 2023/06/19 14:56
 **/
public class ApiAuthUtil {
    private static final char[] MD5_TABLE = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * 获取签名
     *
     * @param appId    签名的key
     * @param secret 签名秘钥
     * @return 返回签名
     */
    public static String getSignature(String appId, String secret, long ts) {
        try {
            String auth = md5(appId + ts);
            return hmacSHA1Encrypt(auth, secret);
        } catch (SignatureException e) {
            return null;
        }
    }

    /**
     * sha1加密
     *
     * @param encryptText 加密文本
     * @param encryptKey  加密键
     * @return 加密
     */
    private static String hmacSHA1Encrypt(String encryptText, String encryptKey) throws SignatureException {
        byte[] rawHmac;
        try {
            byte[] data = encryptKey.getBytes(StandardCharsets.UTF_8);
            SecretKeySpec secretKey = new SecretKeySpec(data, "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(secretKey);
            byte[] text = encryptText.getBytes(StandardCharsets.UTF_8);
            rawHmac = mac.doFinal(text);
        } catch (InvalidKeyException e) {
            throw new SignatureException("InvalidKeyException:" + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            throw new SignatureException("NoSuchAlgorithmException:" + e.getMessage());
        }
        return new String(Base64.getEncoder().encode(rawHmac));
    }

    private static String md5(String cipherText) {
        try {
            byte[] data = cipherText.getBytes();
            // 信息摘要是安全的单向哈希函数，它接收任意大小的数据，并输出固定长度的哈希值。
            MessageDigest mdInst = MessageDigest.getInstance("MD5");

            // MessageDigest对象通过使用 update方法处理数据， 使用指定的byte数组更新摘要
            mdInst.update(data);

            // 摘要更新之后，通过调用digest（）执行哈希计算，获得密文
            byte[] md = mdInst.digest();

            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char[] str = new char[j * 2];
            int k = 0;
            for (byte byte0 : md) { // i = 0
                str[k++] = MD5_TABLE[byte0 >>> 4 & 0xf]; // 5
                str[k++] = MD5_TABLE[byte0 & 0xf]; // F
            }
            // 返回经过加密后的字符串
            return new String(str);
        } catch (Exception e) {
            return null;
        }
    }
}