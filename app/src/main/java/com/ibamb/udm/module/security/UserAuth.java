
package com.ibamb.udm.module.security;

import android.util.Base64;

import com.ibamb.udm.module.constants.Constants;
import com.ibamb.udm.module.constants.Control;
import com.ibamb.udm.module.net.UDPMessageSender;
import com.ibamb.udm.module.util.Convert;

import java.io.IOException;
import java.util.Arrays;


public class UserAuth {

    /**
     * 登录成功返回 TRUE,否则返回 FALSE
     *
     * @param userName
     * @param password
     * @param devMac
     * @return
     */
    public static boolean login(String userName, String password, String devMac,String ip) {
        boolean isSuccessful = false;
        try {
            /**
             * 将用户名和密码采用BASE64加密并转成字节数组。注意用户名和密码之间要留一个空格，加密时需将空格和用户名一起加密。
             */
            String enUserName = Base64.encodeToString((userName + " ").getBytes(DefualtECryptValue.CHARSET), Base64.NO_WRAP);
            String enPassword = Base64.encodeToString(password.getBytes(DefualtECryptValue.CHARSET), Base64.NO_WRAP);
            byte[] byteUserName = Convert.hexStringtoBytes(str2HexString(enUserName));
            byte[] bytePassword = Convert.hexStringtoBytes(str2HexString(enPassword));
            //将目标设备mac地址转成字节数组
            byte[] macData = Convert.hexStringtoBytes(devMac.replaceAll(":", ""));
            //发送登录报文总字节数。
            int frameLength = 1 + 1 + 2 + 4 + 8 + byteUserName.length + bytePassword.length;
            //构造发送报文
            byte[] loginFrame = new byte[frameLength];
            loginFrame[0] = Convert.intToUnsignedByte(Control.AUTH_USER);//control
            loginFrame[1] = 0;//id
            //报文总长度转成字节数组
            byte[] byteFrameLength = Convert.shortToBytes((short)(frameLength));//length
            loginFrame[2] = byteFrameLength[0];
            loginFrame[3] = byteFrameLength[1];
            //目标设备IP地址，默认填0，一般使用mac地址登录。
            loginFrame[4] = 0;
            loginFrame[5] = 0;
            loginFrame[6] = 0;
            loginFrame[7] = 0;
            //mac
            loginFrame[8] = macData[0];
            loginFrame[9] = macData[1];
            loginFrame[10] = macData[2];
            loginFrame[11] = macData[3];
            loginFrame[12] = macData[4];
            loginFrame[13] = macData[5];
            loginFrame[14] = 0;
            loginFrame[15] = 0;
            /**
             * 用户名和密码连续字节
             */
            int pos = 16;
            System.arraycopy(byteUserName, 0, loginFrame, pos, byteUserName.length);
            pos = pos + byteUserName.length;
            System.arraycopy(bytePassword, 0, loginFrame, pos, bytePassword.length);
            /**
             * 发送认证
             */
            UDPMessageSender sender = new UDPMessageSender();
            byte[] replyData = sender.sendByUnicast(loginFrame, 4,ip);
            if(replyData==null){
                isSuccessful = false;
            }else{
                int replyType = Convert.bytes2int(Arrays.copyOfRange(replyData, 0, 4));
                //返回0表示成功
                if (replyType != Constants.UDM_LOGIN_SUCCESS) {
                    isSuccessful = false;
//                    socket = null;
                } else {
                    isSuccessful = true;
                }
            }

        } catch (IOException ex) {

        } finally {

        }
        return isSuccessful;
    }

    /**
     * 将字符串转成16进制编码
     *
     * @param str
     * @return
     */
    private static String str2HexString(String str) {
        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder("");
        byte[] bs = str.getBytes();
        int bit;
        for (int i = 0; i < bs.length; i++) {
            bit = (bs[i] & 0x0f0) >> 4;
            sb.append(chars[bit]);
            bit = bs[i] & 0x0f;
            sb.append(chars[bit]);
            // sb.append(' ');
        }
        return sb.toString().trim();
    }
}
