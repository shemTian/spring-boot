package com.qik.demo.common;

import org.apache.commons.codec.binary.Base64;

/**
 * Created by cxq on 2018/7/14.
 */
public class Encoder {
    public static String base64Encoder(String sourceData) {
        return Base64.encodeBase64String(sourceData.getBytes());
    }
    public static String base64Encoder(byte[] sourceData) {
        return Base64.encodeBase64String(sourceData);
    }
    public static byte[] base64Decoder(String sourceDate) {
        return Base64.decodeBase64(sourceDate.getBytes());
    }


}
