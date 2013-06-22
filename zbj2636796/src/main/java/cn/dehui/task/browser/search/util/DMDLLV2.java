package cn.dehui.task.browser.search.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.imageio.ImageIO;

import com.sun.jna.Library;
import com.sun.jna.Native;

/**
 * 
 * @author cqz
 *         QQ:6199401
 *         email:cheqinzho@qq.com
 * 
 */
public class DMDLLV2 {
    public static final String USERNAME  = "baby2321";

    public static final String PASSWORD  = "1234567abc";

    public static final String AGENTUSER = "dengchangwen";

    public static final String DLLPATH   = "FastVerCode";

    public static final String IMGPATH   = "D:\\1.jpg";

    public interface FastVerCode extends Library {
        FastVerCode INSTANCE = (FastVerCode) Native.loadLibrary(DLLPATH, FastVerCode.class);

        /**
         * 通过发送验证码本地图片到服务器识别
         * 
         * @param path
         *            验证码本地路径，例如（c:\1.jpg)
         * @param UserName
         *            联众账号
         * @param passWord
         *            联众密码
         * @return
         */
        public String RecYZM(String path, String UserName, String passWord);

        /**
         * 无返回值
         * 对打错的验证码进行报告
         * 
         * @param codeUser
         *            联众账号
         * @param daMaWorker
         *            打码工人
         */
        public void ReportError(String codeUser, String daMaWorker);

        /**
         * 通过上传验证码图片字节到服务器进行验证码识别，方便多线程发送
         * 
         * @param imgByte
         *            验证码图片字节集
         * @param len
         *            字节集长度
         * @param username
         *            联众账号
         * @param password
         *            联众密码
         * @return
         */
        public String RecByte(byte[] imgByte, int len, String username, String password);

        public String GetUserInfo(String UserName, String passWord);

        // LPSTR RecByte_A(BYTE* byte,int len,LPSTR strVcodeUser,LPSTR strVcodePass,LPSTR strAgentUser)

        /**
         * 通过上传验证码图片字节到服务器进行验证码识别，方便多线程发送,这个函数可以保护作者的收入，一定需要是作者的下线才能使用作者的软件
         * 
         * @param imgByte
         *            验证码图片字节集
         * @param len
         *            字节集长度
         * @param username
         *            联众账号
         * @param password
         *            联众密码
         * @param agentUser
         *            作者账号
         * @return 成功返回->验证码结果|!|打码工人
         * 
         *         后台没点数了返回:No Money!
         *         未注册返回:No Reg!
         *         上传验证码失败:Error:Put Fail!
         *         识别超时了:Error:TimeOut!
         *         上传无效验证码:Error:empty picture!
         */
        public String RecByte_A(byte[] imgByte, int len, String username, String password, String agentUser);

        /**
         * 命令名称:RecYZM_A
         * 命令功能:通过发送验证码本地图片到服务器识别,这个函数可以保护作者的收入，一定需要是作者的下线才能使用作者的软件
         * 
         * @param path
         *            验证码本地路径，例如（c:\1.jpg)
         * @param UserName
         *            联众账号
         * @param passWord
         *            联众密码
         * @param agentUser
         *            作者账号
         * @return
         *         返回值类型:文本型 成功返回->验证码结果|!|打码工人
         * 
         *         后台没点数了返回:No Money!
         *         未注册返回:No Reg!
         *         上传验证码失败:Error:Put Fail!
         *         识别超时了:Error:TimeOut!
         *         上传无效验证码:Error:empty picture!
         */
        public String RecYZM_A(String path, String UserName, String passWord, String agentUser);

        /**
         * 命令名称:Reglz
         * 命令功能:通过作者的下线注册联众账号
         * 
         * @param userName
         *            联众账号
         * @param passWord
         *            联众密码
         * @param email
         *            QQ邮箱
         * @param qq
         *            QQ号
         * @param agentid
         *            作者的推广id
         * @param agentName
         *            作者账号
         * @return
         *         返回值类型:整数型 成功返回1
         * 
         *         注册失败返回-1 网络传输异常
         *         返回0 未知异常
         */
        public int Reglz(String userName, String passWord, String email, String qq, String agentid, String agentName);

        /**
         * 命令名称:GetUserInfo_A
         * 命令功能:查询剩余验证码点数,这个函数可以保护作者的收入，一定需要是作者的下线才能使用作者的软件
         * 
         * @param UserName
         *            联众账号
         * @param passWord
         *            联众密码
         * @param agentUser
         *            作者账号
         * @return 成功返回->剩余验证码点数
         */
        public String GetUserInfo_A(String UserName, String passWord, String agentUser);

        /**
         * 命令名称:RecByte_2
         * 命令功能:通过上传验证码图片字节到服务器进行验证码识别，方便多线程发送
         * 
         * @param imgByte
         *            验证码图片字节集
         * @param len
         *            字节集长度
         * @param username
         *            联众账号
         * @param password
         *            联众密码
         * @param yzmtype
         *            验证码类型
         * @param yzmlengthMin
         *            验证码最小位数
         * @param yzmlengthMax
         *            验证码最大位数
         * @return 成功返回->验证码结果|!|打码工人
         * 
         *         后台没点数了返回:No Money!
         *         未注册返回:No Reg!
         *         上传验证码失败:Error:Put Fail!
         *         识别超时了:Error:TimeOut!
         *         上传无效验证码:Error:empty picture!
         */
        public String RecByte_2(byte[] imgByte, int len, String username, String password, int yzmtype,
                int yzmlengthMin, int yzmlengthMax);

        /**
         * 
         命令名称:RecYZM_2
         * 命令功能:通过发送验证码本地图片到服务器识别
         * 
         * @param path
         *            验证码本地路径，例如（c:\1.jpg)
         * @param username
         *            联众账号
         * @param password
         *            联众密码
         * @param yzmtype
         *            验证码类型
         * @param yzmlengthMin
         *            验证码最小位数
         * @param yzmlengthMax
         *            验证码最大位数
         * @return
         *         成功返回->验证码结果|!|打码工人
         * 
         *         后台没点数了返回:No Money!
         *         未注册返回:No Reg!
         *         上传验证码失败:Error:Put Fail!
         *         识别超时了:Error:TimeOut!
         *         上传无效验证码:Error:empty picture!
         */
        public String RecYZM_2(String path, String username, String password, int yzmtype, int yzmlengthMin,
                int yzmlengthMax);

        /**
         * 命令名称:RecByte_A_2
         * 命令功能:通过上传验证码图片字节到服务器进行验证码识别，方便多线程发送
         * 
         * @param imgByte
         *            验证码图片字节集
         * @param len
         *            字节集长度
         * @param username
         *            联众账号
         * @param password
         *            联众密码
         * @param yzmtype
         *            验证码类型
         * @param yzmlengthMin
         *            验证码最小位数
         * @param yzmlengthMax
         *            验证码最大位数
         * @param agentUser
         *            作者账号
         * @return
         *         成功返回->验证码结果|!|打码工人
         * 
         *         后台没点数了返回:No Money!
         *         未注册返回:No Reg!
         *         上传验证码失败:Error:Put Fail!
         *         识别超时了:Error:TimeOut!
         *         上传无效验证码:Error:empty picture!
         */
        public String RecByte_A_2(byte[] imgByte, int len, String username, String password, int yzmtype,
                int yzmlengthMin, int yzmlengthMax, String agentUser);

        /**
         * 命令名称:RecYZM_A_2
         * 命令功能:通过发送验证码本地图片到服务器识别
         * 
         * @param path
         *            验证码本地路径，例如（c:\1.jpg)
         * @param username
         *            联众账号
         * @param password
         *            联众密码
         * @param yzmtype
         *            验证码类型
         * @param yzmlengthMin
         *            验证码最小位数
         * @param yzmlengthMax
         *            验证码最大位数
         * @param agentUser
         *            作者账号
         * @return
         *         成功返回->验证码结果|!|打码工人
         * 
         *         后台没点数了返回:No Money!
         *         未注册返回:No Reg!
         *         上传验证码失败:Error:Put Fail!
         *         识别超时了:Error:TimeOut!
         *         上传无效验证码:Error:empty picture!
         */
        public String RecYZM_A_2(String path, String username, String password, int yzmtype, int yzmlengthMin,
                int yzmlengthMax, String agentUser);

    }

    public static void main(String[] args) throws Exception {

        System.out.println("GetUserInfo:" + FastVerCode.INSTANCE.GetUserInfo(USERNAME, PASSWORD));
        System.out.println("RecYZM:"
                + FastVerCode.INSTANCE.RecYZM("D:\\workspace\\GitHub\\task\\zbj2636796\\google_search_captcha.png",
                        USERNAME, PASSWORD));
        // FastVerCode.INSTANCE.ReportError(USERNAME, "ccc5");
        // getCodeByRecByte();
        // System.out.println("Reglz:" + FastVerCode.INSTANCE.Reglz("6199401", "6199401", "6199401@qq.com", "6199401", "ww", "ww"));

        //        System.out.println("RecYZM_2:" + FastVerCode.INSTANCE.RecYZM_2(IMGPATH, USERNAME, PASSWORD, 0, 3, 5));
        //        System.out.println("RecYZM_A_2:"
        //                + FastVerCode.INSTANCE.RecYZM_A_2(IMGPATH, USERNAME, PASSWORD, 1, 3, 5, AGENTUSER));

        //
    }

    public static void getCodeByRecByte_A() throws Exception {
        System.out.println("正在获取验证码........");
        byte[] b = toByteArrayFromFile(IMGPATH);
        System.out.println("RecByte:" + FastVerCode.INSTANCE.RecByte(b, b.length, USERNAME, PASSWORD));

    }

    public static void getCodeByRecByte() throws Exception {
        System.out.println("正在获取验证码........");
        byte[] b = toByteArrayFromFile(IMGPATH);
        System.out.println("RecByte:" + FastVerCode.INSTANCE.RecByte(b, b.length, USERNAME, PASSWORD));

    }

    public static byte[] toByteArray(File imageFile) throws Exception {
        BufferedImage img = ImageIO.read(imageFile);
        ByteArrayOutputStream buf = new ByteArrayOutputStream((int) imageFile.length());
        try {
            ImageIO.write(img, "jpg", buf);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return buf.toByteArray();
    }

    public static byte[] toByteArrayFromFile(String imageFile) throws Exception {
        InputStream is = null;

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            is = new FileInputStream(imageFile);

            byte[] b = new byte[1024];

            int n;

            while ((n = is.read(b)) != -1) {

                out.write(b, 0, n);

            }// end while

        } catch (Exception e) {
            throw new Exception("System error,SendTimingMms.getBytesFromFile", e);
        } finally {

            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                }// end try
            }// end if

        }// end try
        return out.toByteArray();
    }
}
