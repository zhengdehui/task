package cn.dehui.task.browser;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;

import chrriis.common.UIUtils;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserAdapter;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserEvent;
import cn.dehui.task.browser.search.util.DMDLLV2.FastVerCode;

/**  
 * @author Christopher Deckers  
 */
public class NativeSwingBrowserExample {

    static final String  URL     = "http://www.yzmbuy.com/index.php/demo";

    static final String  FINDURL = "http://www.yzmbuy.com/index.php?mod=demo&act=result&id=";

    private static Robot robot;

    static {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    public static JComponent createContent() {
        final JWebBrowser webBrowser = new JWebBrowser();
        webBrowser.setMenuBarVisible(false);
        webBrowser.addWebBrowserListener(new WebBrowserAdapter() {

            @Override
            public void loadingProgressChanged(WebBrowserEvent e) {
                super.loadingProgressChanged(e);
                //                System.out.println("Progress: " + webBrowser.getLoadingProgress());
                if (webBrowser.getLoadingProgress() == 100) {
                    if (webBrowser.getResourceLocation().startsWith("http://www.google.com/sorry/?continue")) {

                        Point browserLocation = webBrowser.getNativeComponent().getLocationOnScreen();
                        Rectangle rect = new Rectangle((int) (browserLocation.getX()) + 30, (int) (browserLocation
                                .getY()) + 113, 202, 72);
                        BufferedImage img = robot.createScreenCapture(rect);
                        File captchaFile = new File("google_search_captcha.png");
                        try {
                            ImageIO.write(img, "png", captchaFile);

                            ByteArrayOutputStream buf = new ByteArrayOutputStream();
                            ImageIO.write(img, "jpg", buf);
                            byte[] data = buf.toByteArray();
                            String code = FastVerCode.INSTANCE.RecByte(data, data.length, "baby2321", "1234567abc");

                            //                            String code = getCode("baby2321", "1234567abc", captchaFile);

                            System.out.println(code);

                        } catch (IOException ex) {
                            ex.printStackTrace();
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                        System.out.println("img done");
                    }
                }
            }

        });
        JWebBrowser.clearSessionCookies();
        webBrowser
                .navigate("http://www.google.com/sorry/?continue=http://www.google.com/search%3Fq%3D%2522http://www.wikihow.com/Make-Money%2522%26ei%3DM355Ub7-GIr1iQK2o4DIAQ%26start%3D100%26sa%3DN%26biw%3D989%26bih%3D439");
        return webBrowser;
    }

    /**
     *   获取验证码
     * @param userName 用户名
     * @param password  密码
     * @param imgPath    图片路径
     * @return
     * @throws Exception
     */
    public static String getCode(String userName, String password, File imgFile) throws Exception {
        HttpClient httpClient = new DefaultHttpClient();
        httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);
        httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 30000);

        String id = null;
        do {
            HttpPost postRequest = new HttpPost(URL);
            BasicHttpParams httpParams = new BasicHttpParams();
            httpParams.setParameter("info[lz_user]", userName);
            httpParams.setParameter("info[lz_pass]", password);
            httpParams.setParameter("pesubmit", "");
            postRequest.setParams(httpParams);

            MultipartEntity reqEntity = new MultipartEntity() {
                @Override
                protected String generateContentType(final String boundary, final Charset charset) {
                    return "image/*";
                }
            };
            FileBody fileBody = new FileBody(imgFile);
            reqEntity.addPart("imagepath", fileBody);

            postRequest.setEntity(reqEntity);

            HttpResponse response = httpClient.execute(postRequest);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                String result = EntityUtils.toString(response.getEntity());
                System.out.println("post result: " + result);
                int demoIndex = result.indexOf("demo/");
                if (demoIndex != -1 && demoIndex + 12 <= result.length()) {
                    int beginIndex = demoIndex + 5;
                    int endIndex = demoIndex + 12;
                    id = result.substring(beginIndex, endIndex);
                    break;
                }
            }
        } while (true);

        Thread.sleep(2000);
        for (;;) {
            System.out.println("获取验证码URL" + FINDURL + id);
            HttpGet httpGet = new HttpGet(FINDURL + id);

            HttpResponse httpResponse = httpClient.execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                System.out.println("status: " + httpResponse.getStatusLine().getStatusCode());
                Thread.sleep(1000);
                continue;
            }

            String result = decodeUnicode(EntityUtils.toString(httpResponse.getEntity()));
            System.out.println("get result: " + result);

            if (result.indexOf("打码成功") != -1) {
                return result.substring(result.indexOf("\"result\":") + 10, result.indexOf(",\"damaworker\"") - 1);
            }
            Thread.sleep(1000);
        }

        //        MultipartPostMethod filePost = new MultipartPostMethod(URL);
        //        FilePart cbFile = new FilePart("imagepath", new File(imgPath));
        //        cbFile.setContentType("image/*");
        //        filePost.addParameter("info[lz_user]", userName);
        //        filePost.addParameter("info[lz_pass]", password);
        //        filePost.addParameter("pesubmit", "");
        //        filePost.addPart(cbFile);
        //        client.getHttpConnectionManager().getParams().setConnectionTimeout(10000);
        //        client.executeMethod(filePost);
        //        result = filePost.getResponseBodyAsString();
        //        System.out.println("result:" + result);
        //        String id = result.substring(result.indexOf("demo/") + 5, result.indexOf("demo/") + 12);
        //        if (id == null || id.length() < 6) {
        //            throw new Exception("无效ID");
        //        }
        //        Thread.currentThread().sleep(2000L);
        //        System.out.println(FINDURL + id);
        //        for (;;) {
        //            System.out.println("获取验证码URL" + FINDURL + id);
        //            getMethod = new GetMethod(FINDURL + id);
        //            client.executeMethod(getMethod);
        //            result = decodeUnicode(getMethod.getResponseBodyAsString());
        //            System.out.println("result2:" + result.length());
        //            System.out.println("result2:" + result);
        //
        //            if (result.indexOf("打码成功") != -1) {
        //                code = result.substring(result.indexOf("\"result\":") + 10, result.indexOf(",\"damaworker\"") - 1);
        //                break;
        //            }
        //            Thread.currentThread().sleep(1000);
        //        }
        //        return code;
    }

    /**
     * unicode 转换成 中文
     * 
     * @param theString
     * @return
     */
    public static String decodeUnicode(String theString) {
        char aChar;
        int len = theString.length();
        StringBuffer outBuffer = new StringBuffer(len);
        for (int x = 0; x < len;) {
            aChar = theString.charAt(x++);
            if (aChar == '\\') {
                aChar = theString.charAt(x++);
                if (aChar == 'u') {
                    int value = 0;
                    for (int i = 0; i < 4; i++) {
                        aChar = theString.charAt(x++);
                        switch (aChar) {
                            case '0':
                            case '1':
                            case '2':
                            case '3':
                            case '4':
                            case '5':
                            case '6':
                            case '7':
                            case '8':
                            case '9':
                                value = (value << 4) + aChar - '0';
                                break;
                            case 'a':
                            case 'b':
                            case 'c':
                            case 'd':
                            case 'e':
                            case 'f':
                                value = (value << 4) + 10 + aChar - 'a';
                                break;
                            case 'A':
                            case 'B':
                            case 'C':
                            case 'D':
                            case 'E':
                            case 'F':
                                value = (value << 4) + 10 + aChar - 'A';
                                break;
                            default:
                                throw new IllegalArgumentException("Malformed      encoding.");
                        }

                    }
                    outBuffer.append((char) value);
                } else {
                    if (aChar == 't') {
                        aChar = '\t';
                    } else if (aChar == 'r') {
                        aChar = '\r';
                    } else if (aChar == 'n') {
                        aChar = '\n';
                    } else if (aChar == 'f') {
                        aChar = '\f';
                    }
                    outBuffer.append(aChar);
                }
            } else {
                outBuffer.append(aChar);
            }

        }
        return outBuffer.toString();

    }

    /* Standard main method to try that test as a standalone application. */
    public static void main(String[] args) {
        NativeInterface.open();
        UIUtils.setPreferredLookAndFeel();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame frame = new JFrame("DJ Native Swing Test");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.getContentPane().add(createContent(), BorderLayout.CENTER);
                frame.setSize(1024, 600);
                frame.setLocationByPlatform(true);
                frame.setVisible(true);
            }
        });
        NativeInterface.runEventPump();
    }

}
