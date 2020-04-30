package com.activiti.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class Md5 {


    public static String md5password(String password) {

        try {
            //得到一个信息摘要器
            MessageDigest digest = MessageDigest.getInstance("md5");
            byte[] result = digest.digest(password.getBytes());

            StringBuffer buffer = new StringBuffer();
            //把每一个byte做一个与运算0xff;
            for(byte b:result) {
                //与运算
                int number = b & 0xff;//加盐 十六进制
                String str = Integer.toHexString(number);
                if(str.length() == 1) {
                    buffer.append("0");
                }
                buffer.append(str);
            }
            //标准的md5加密后的结果
            return buffer.toString();
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "";
        }

    }



    /**
     * 加密后解密 运行一次为加密 再运行一次为解密
     * @param inStr
     * @return
     */
    public static String JM(String inStr) {
        byte[] a = inStr.getBytes();
        for(int i = 0 ; i < a.length ; i++) {
            a[i] = (byte) (a[i] ^ 't');

        }
        return new String(a);

    }


}
