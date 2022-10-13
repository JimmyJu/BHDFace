package com.baidu.idl.face.main.utils;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;


public class ScanUtils {

    /**
     * des解密
     */
    public static byte[] decrypt(byte[] data, byte[] key) throws Exception {
        try {
            DESKeySpec dks = new DESKeySpec(key);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            // key的长度不能够小于8位字节
            Key secretKey = keyFactory.generateSecret(dks);
            Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding");

            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            byte[] bytes = cipher.doFinal(data);
            return bytes;
        } catch (Exception e) {
            throw new Exception(e);
        }
    }


    /*
     * String转byte
     * */
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    public static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    /*
     * 16位转8
     * */
    public static byte[] merge2BytesTo1Byte(String str) {
        byte[] bytes = new byte[str.length() / 2];
        char s, e;
        for (int i = 0; i < str.length(); i += 2) {
            s = str.charAt(i);//第I个索引字符
            e = str.charAt(i + 1);//第I+1个索引字符
            if (s <= '9') {
                if (e <= '9') {
                    bytes[i / 2] = (byte) (((s - 0x30) << 4) + (e - 0x30));
                } else if ((e <= 'z' && e >= 'a')) {
                    bytes[i / 2] = (byte) (((s - 0x30) << 4) + (e - 0x57));//左移4位

                }
            } else if (s > '9') {
                if (e <= '9') {
                    bytes[i / 2] = (byte) (((s - 0x57) << 4) + (e - 0x30));
                } else if ((s <= 'z' && e >= 'a')) {
                    bytes[i / 2] = (byte) (((s - 0x57) << 4) + (e - 0x57));
                }
            }
        }
        return bytes;
    }

    /*
     * byte转String
     * */

    public static final String byte2hex(byte b[]) {
        if (b == null) {
            throw new IllegalArgumentException(
                    "Argument b ( byte array ) is null! ");
        }
        String hs = "";
        String stmp = "";
        for (int n = 0; n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0xff);
            if (stmp.length() == 1) {
                hs = hs + "0" + stmp;
            } else {
                hs = hs + stmp;
            }
        }
        return hs.toUpperCase();
    }

    public static String addZero(String id) {
        String ids = null;
        if (id.length() == 1) {
            ids = "0000000" + id;
        } else if (id.length() == 2) {
            ids = "000000" + id;
        } else if (id.length() == 3) {
            ids = "00000" + id;
        } else if (id.length() == 4) {
            ids = "0000" + id;
        } else if (id.length() == 5) {
            ids = "000" + id;
        } else if (id.length() == 6) {
            ids = "00" + id;
        } else if (id.length() == 7) {
            ids = "0" + id;
        } else if (id.length() == 8) {
            ids = id;
        }

        return ids;
    }
}
