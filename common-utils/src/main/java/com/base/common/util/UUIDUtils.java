package com.base.common.util;

import java.util.UUID;

/**
 * <p>
 * UUID生成器
 * <p>
 * </p>
 *
 * @author GISirFive
 * @date Create on 2017/11/13 0:10
 */
public class UUIDUtils {

    /**
     * 随机数-纯数字
     */
    private static final String RANDOM_SEED_NUMBER = "1234567890";

    /**
     * 随机数-数字+字母
     */
    private static final String RANDOM_SEED_CHAR = "1234567890abcdefghijkmnpqrstuvwxyz";

    /**
     * 获得36位原生UUID
     *
     * @return
     */
    public static String getUuid() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    /**
     * 获得32位UUID
     *
     * @return 获得32位UUID
     */
    public static String getUuid32() {
        String uuidStr = getUuid().replaceAll("-", "");
        return uuidStr;
    }

    /**
     * 创建指定数量的随机字符串
     *
     * @param numberFlag 是否是数字
     * @param length     随机字符串长度
     * @return 随机字符串
     */
    public static String getRandom(boolean numberFlag, int length) {
        String retStr = "";
        String strTable = numberFlag ? RANDOM_SEED_NUMBER : RANDOM_SEED_CHAR;
        int len = strTable.length();
        boolean bDone = true;
        do {
            retStr = "";
            int count = 0;
            for (int i = 0; i < length; i++) {
                double dblR = Math.random() * len;
                int intR = (int) Math.floor(dblR);
                char c = strTable.charAt(intR);
                if (('0' <= c) && (c <= '9')) {
                    count++;
                }
                retStr += strTable.charAt(intR);
            }
            if (count >= 2) {
                bDone = false;
            }
        } while (bDone);

        return retStr;
    }
}
