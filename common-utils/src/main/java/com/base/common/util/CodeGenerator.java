package com.base.common.util;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.Random;

/**
 * 编号生成器
 *
 * @author GISirFive
 * @since 2017年6月26日下午4:20:16
 */
public class CodeGenerator {

    private static final char[] CHARS = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
            'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};

    /**
     * 生成资源名称
     *
     * @param extendLength 扩展名长度
     * @return
     */
    public static String getResourceName(int extendLength) {
        return generateNumberCode(16 - extendLength);
    }

    /**
     * 生成通用ID
     *
     * @return
     */
    public static String generateCommonId() {
        return generateNumberCode(24);
    }

    /**
     * 生成token
     *
     * @return
     */
    public static String generateToken() {
        return generateNumberCode(32);
    }

    /**
     * 生成盐值
     *
     * @return
     */
    public static String generateSalt() {
        return generateCharCode(8);
    }

    /**
     * 生成编号
     *
     * @param length 长度
     * @return
     */
    public static String generateNumberCode(int length) {
        return RandomStringUtils.randomNumeric(length);
    }

    /**
     * 生成字母
     *
     * @param length
     * @return
     */
    private static String generateCharCode(int length) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < length; i++) {
            buffer.append(CHARS[new Random().nextInt(CHARS.length)]);
        }
        return buffer.toString();
    }
}