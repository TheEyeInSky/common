package com.dongzy.common.common.text;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.Security;

/**
 * 用于对字符串进行加解密的类，支持对称加密和非对称加密两种方式
 * 对称加密：加密后可以重新解密出来的数据处理技术，需要显示给用户的数据，如用户名等
 * 非对称加密：加密后无法进行解密的数据处理技术，无需显示给用户的数据，如登录密码等
 * 其中encrypt和decrypt是对称加密和解密的方法
 * 而Md5Digest，SHA1Digest，SHA256Digest，SHA384Digest，SHA512Digest等是非对称加密技术
 * SHA加密算法数字越大，加密后的破解难度也越大，但是相应的性能消耗和存储空间占用也越大。
 *
 * @author zouyong
 * @since SDK1.8
 */
public class CipherUtils {

    private final static String STR_DEFAULT_KEY = "phhc";
    private final static CipherUtils CIPHER_UTILS = new CipherUtils(null);

    private Cipher encryptCipher = null;
    private Cipher decryptCipher = null;

    /**
     * 获取默认的加解密类
     *
     * @return 默认加解密类
     */
    public static CipherUtils getInstance() {
        return CIPHER_UTILS;
    }

    /**
     * 根据传入的参数构造函数
     *
     * @param strKey 加密的密钥
     */
    public CipherUtils(String strKey) {
        strKey = (StringUtils.isBlank(strKey)) ? STR_DEFAULT_KEY : strKey;
        Key key = this.getKey(strKey.getBytes());
        initEncryptDecrypt(key);
    }

    /**
     * 根据传入的参数构造函数
     *
     * @param key 加密的密钥
     */
    private void initEncryptDecrypt(Key key) {
        Security.addProvider(new com.sun.crypto.provider.SunJCE());
        try {
            encryptCipher = Cipher.getInstance("DES");
            encryptCipher.init(Cipher.ENCRYPT_MODE, key);

            decryptCipher = Cipher.getInstance("DES");
            decryptCipher.init(Cipher.DECRYPT_MODE, key);
        } catch (Exception ex) {
            throw new IllegalArgumentException("初始化加解密类时发生未知异常！", ex);
        }
    }

    /**
     * 对字符串进行加密处理
     *
     * @param text 需要加密的字符串
     * @return 加密后的字符串
     */
    public String encrypt(String text) {
        if (text == null) {
            return null;
        }
        if (StringUtils.EMPTY.equals(text)) {
            return StringUtils.EMPTY;
        }
        try {
            return byteArr2HexStr(encrypt(text.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalArgumentException("加密字符串" + text + "时发生异常", ex);
        }
    }

    /**
     * 解密字符串
     *
     * @param text 需要解密的字符串
     * @return 解密后的字符串
     */
    public String decrypt(String text) {
        if (text == null) {
            return null;
        }
        if (StringUtils.EMPTY.equals(text)) {
            return StringUtils.EMPTY;
        }
        try {
            return new String(decrypt(hexStr2ByteArr(text)), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new IllegalArgumentException("解密字符串" + text + "时发生异常", ex);
        }
    }

    private byte[] decrypt(byte[] arrB) throws BadPaddingException, IllegalBlockSizeException {
        return decryptCipher.doFinal(arrB);
    }

    private byte[] encrypt(byte[] arrB) throws BadPaddingException, IllegalBlockSizeException {
        return encryptCipher.doFinal(arrB);
    }

    private Key getKey(byte[] arrBTmp) {
        // 创建一个空的8位字节数组（默认值为0）
        byte[] arrB = new byte[8];

        // 将原始字节数组转换为8位
        for (int i = 0; i < arrBTmp.length && i < arrB.length; i++) {
            arrB[i] = arrBTmp[i];
        }

        // 生成密钥
        return new javax.crypto.spec.SecretKeySpec(arrB, "DES");
    }

    private String byteArr2HexStr(byte[] arrB) {
        int iLen = arrB.length;
        // 每个byte用两个字符才能表示，所以字符串的长度是数组长度的两倍
        StringBuilder sb = new StringBuilder(iLen * 2);
        for (byte anArrB : arrB) {
            int intTmp = anArrB;
            // 把负数转换为正数
            while (intTmp < 0) {
                intTmp = intTmp + 256;
            }
            // 小于0F的数需要在前面补0
            if (intTmp < 16) {
                sb.append("0");
            }
            sb.append(Integer.toString(intTmp, 16));
        }
        return sb.toString();
    }

    private static byte[] hexStr2ByteArr(String strIn) {
        byte[] arrB = strIn.getBytes();
        int iLen = arrB.length;

        // 两个字符表示一个字节，所以字节数组长度是字符串长度除以2
        byte[] arrOut = new byte[iLen / 2];
        for (int i = 0; i < iLen; i = i + 2) {
            String strTmp = new String(arrB, i, 2);
            arrOut[i / 2] = (byte) Integer.parseInt(strTmp, 16);
        }
        return arrOut;
    }

    //endregion

    //region SHA和MD5加密

    /**
     * @param text 需要加密的字符串
     * @return 加密后的字符串
     */
    public static String Md5Digest(String text) {
        return digest(text, "MD5");
    }

    /**
     * @param text 需要加密的字符串
     * @return 加密后的字符串
     */
    public static String SHA1Digest(String text) {
        return digest(text, "SHA-1");
    }

    /**
     * @param text 需要加密的字符串
     * @return 加密后的字符串
     */
    public static String SHA256Digest(String text) {
        return digest(text, "SHA-256");
    }

    /**
     * @param text 需要加密的字符串
     * @return 加密后的字符串
     */
    public static String SHA384Digest(String text) {
        return digest(text, "SHA-384");
    }

    /**
     * @param text 需要加密的字符串
     * @return 加密后的字符串
     */
    public static String SHA512Digest(String text) {
        return digest(text, "SHA-512");
    }

    /**
     * 单向加密字符串公共方法
     *
     * @param text      需要加密的字符串
     * @param algorithm 加密的算法
     * @return 加密后的字符串
     */
    private static String digest(String text, String algorithm) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            md.update(text.getBytes(StandardCharsets.UTF_8));
            return toHexString(md.digest());
        } catch (Exception ex) {
            throw new IllegalArgumentException("加密字符串" + text + "时发生异常", ex);
        }
    }

    /**
     * 字节数组转成16进制表示格式的字符串
     *
     * @param byteArray 需要转换的字节数组
     * @return 16进制表示格式的字符串
     **/
    private static String toHexString(byte[] byteArray) {
        final StringBuilder hexString = new StringBuilder();

        for (byte aByteArray : byteArray) {
            if ((aByteArray & 0xff) < 0x10)//0~F前面不零
                hexString.append("0");
            hexString.append(Integer.toHexString(0xFF & aByteArray));
        }

        return hexString.toString().toLowerCase();
    }

    //endregion

}
