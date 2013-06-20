package cn.dehui.zbj1752248.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class AdslUtil {
    private static final String CONNECT_CMD    = "rasdial %s %s %s";

    private static final String DISCONNECT_CMD = "rasdial %s /disconnect";

    private static String       title          = "我的宽带";

    private static String       username       = "02008334158@163.gd";

    private static String       password       = "VDYYKKND";

    private static int          sleepTime      = 2000;

    static {
        File adslConfig = new File("adsl.ini");
        if (adslConfig.exists() && adslConfig.isFile()) {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(adslConfig), "GBK"));
                title = br.readLine();
                username = br.readLine();
                password = br.readLine();
                sleepTime = Integer.parseInt(br.readLine());
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }

    /** 
     * 执行CMD命令,并返回String字符串 
     */
    public static String executeCmd(String strCmd) throws Exception {
        String command = "cmd /c " + strCmd;
        //        System.out.println(command);
        Process p = Runtime.getRuntime().exec(command);

        List<Byte> byteList = new ArrayList<Byte>();

        InputStream inputStream = p.getInputStream();
        int tmp;
        while ((tmp = inputStream.read()) != -1) {
            byteList.add((byte) tmp);
        }
        inputStream.close();

        byte[] bytes = new byte[byteList.size()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = byteList.get(i);
        }

        String result = new String(bytes, "GBK");

        return result;
    }

    /** 
     * 连接ADSL 
     */
    public static boolean connect(String title, String username, String password) throws Exception {
        while (true) {
            System.out.println("正在建立连接.");
            String result = executeCmd(String.format(CONNECT_CMD, title, username, password));
            // 判断是否连接成功  
            if (result.indexOf("已连接") > 0) {
                System.out.println("已成功建立连接.");
                return true;
            }

            System.err.println(result);
            System.err.println("建立连接失败");
        }
    }

    /** 
     * 断开ADSL 
     */
    public static boolean disconnect(String title) throws Exception {
        String result = executeCmd(String.format(DISCONNECT_CMD, title));

        if (result.indexOf("没有连接") != -1) {
            System.err.println(title + "连接不存在!");
            return false;
        } else {
            System.out.println("连接已断开");
            return true;
        }
    }

    public static void renewIP() throws Exception {
        disconnect(title);
        Thread.sleep(sleepTime);
        connect(title, username, password);
    }

    public static void main(String[] args) throws Exception {
        //        connect(title, username, password);
        //        Thread.sleep(1000);
        renewIP();

        //        int a = 128;
        //
        //        System.out.println((byte) a);
    }
}
