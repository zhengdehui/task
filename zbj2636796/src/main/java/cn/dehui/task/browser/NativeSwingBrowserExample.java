package cn.dehui.task.browser;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import chrriis.common.UIUtils;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserAdapter;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserEvent;

/**  
 * @author Christopher Deckers  
 */
public class NativeSwingBrowserExample {

    public static JComponent createContent() {
        final JWebBrowser webBrowser = new JWebBrowser();
        webBrowser.setMenuBarVisible(false);
        webBrowser.setBarsVisible(false);
        webBrowser.setButtonBarVisible(false);
        webBrowser.addWebBrowserListener(new WebBrowserAdapter() {

            @Override
            public void loadingProgressChanged(WebBrowserEvent e) {
                super.loadingProgressChanged(e);
                //                System.out.println("Progress: " + webBrowser.getLoadingProgress());
                if (webBrowser.getLoadingProgress() == 100) {
                    if (webBrowser.getResourceLocation().startsWith("http://www.google.com.hk/sorry/?continue")) {

                        String js = "return document.getElementsByTagName('img')[0].getAttribute('src')";
                        String imgUrl = (String) webBrowser.executeJavascriptWithResult(js);

                        Captcha captcha = new Captcha();
                        try {
                            String cookie = captcha.saveCaptcha(imgUrl, webBrowser.getResourceLocation());
                            System.out.println(cookie);
                            JWebBrowser.setCookie("http://www.google.com.hk/", cookie);
                            JWebBrowser.setCookie("google.com.hk/", cookie);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }

        });
        JWebBrowser.clearSessionCookies();
        webBrowser
                .navigate("http://www.google.com.hk/sorry/?continue=http%3A%2F%2Fwww.google.com.hk%2Fsearch%3Fq%3D%252Furl%253Fsa%253Dt%2526rct%253Dj%2526q%253D%2525E7%252599%2525BE%2525E5%2525BA%2525A6%2526source%253Dweb%2526cd%253D11%2526cad%253Drja%2526ved%253D0CGcQFjAK%2526url%253Dhttps%25253A%25252F%25252Fzh.wikipedia.org%25252Fzh%25252F%252525E7%25252599%252525BE%252525E5%252525BA%252525A6%2526ei%253DudN3Uf-eIcbL0QGD1YHoDA%2526usg%253DAFQjCNHPg9iYw5ykq-6QMAIP_69N6uT_Ig%26ie%3Dutf-8%26oe%3Dutf-8%26aq%3Dt%26rls%3Dorg.mozilla%3Azh-CN%3Aofficial%26client%3Dfirefox-a%26channel%3Dfflb&id=5514747270865174035&captcha=50984346&submit=%E6%8F%90%E4%BA%A4");
        return webBrowser;
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
