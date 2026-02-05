package com.base.common.util.encryption;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * @author GISirFive
 * @date Create on 2019/11/11 11:15
 */
public class AESUtils {

    private static final String VI = EncryptKeyConst.IV;
    private static final String KEY = EncryptKeyConst.KEY;


    /**
     * 使用默认的key和iv加密
     *
     * @param data
     * @return
     * @throws Exception
     */
    public static String encrypt(String data) throws Exception {
        return encrypt(data, KEY, VI);
    }

    /**
     * 使用默认的key和iv解密
     *
     * @param data
     * @return
     * @throws Exception
     */
    public static String decrypt(String data) throws Exception {
        return decrypt(data, KEY, VI);
    }

    /**
     * 加密方法
     *
     * @param data 要加密的数据
     * @param key  加密key
     * @param iv   加密iv
     * @return 加密的结果
     * @throws Exception
     */
    private static String encrypt(String data, String key, String iv) throws Exception {
        byte[] raw = key.getBytes();
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher;
        byte[] encrypted;
        cipher = Cipher.getInstance("AES/CBC/NoPadding");
        // "算法/模式/补码方式"
        // 使用CBC模式，需要一个向量iv，可增加加密算法的强度
        IvParameterSpec ivParam = new IvParameterSpec(iv.getBytes());
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivParam);
        encrypted = cipher.doFinal(data.getBytes());
        // 此处使用BASE64做转码功能，同时能起到2次加密的作用。
        Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(encrypted);
    }

    /**
     * 解密方法
     *
     * @param data 要解密的数据
     * @param key  解密key
     * @param iv   解密iv
     * @return 解密的结果
     * @throws Exception
     */
    private static String decrypt(String data, String key, String iv) throws Exception {
        byte[] raw = key.getBytes("UTF-8");
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        IvParameterSpec ivParam = new IvParameterSpec(iv.getBytes());
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivParam);
        // 先用base64解密
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] encrypted1 = decoder.decode(data);
        byte[] original = cipher.doFinal(encrypted1);
        return new String(original);
    }

}
