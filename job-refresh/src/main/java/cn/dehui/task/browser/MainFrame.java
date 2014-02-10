package cn.dehui.task.browser;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import chrriis.common.UIUtils;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserAdapter;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserListener;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserNavigationEvent;

public class MainFrame extends JFrame {

    private static final String GET_URLS_JS        = "var urls=new Array();"
                                                           + "var as=document.getElementById('ContainerFrame').contentWindow.document.getElementsByTagName('a');"
                                                           + "for(var i = 0; i < as.length; i++){"
                                                           + "if(as[i].innerHTML=='修改'){"
                                                           + "var onclickText=as[i].getAttribute('onclick').toString();"
                                                           + "var begin=onclickText.indexOf(\"'\")+1;"
                                                           + "var end=onclickText.indexOf(\"'\",begin);"
                                                           + "var url=onclickText.substring(begin,end);"
                                                           + "urls.push(url);}}" + "return urls;";

    private static final long   serialVersionUID   = 6144722027050898517L;

    private static final String LOGIN_JS           = "document.getElementById('txtusername').value='"
                                                           + System.getProperty("cn.dehui.58job.username") + "';"
                                                           + "document.getElementById('txtpwd').value='"
                                                           + System.getProperty("cn.dehui.58job.password") + "';"
                                                           + "document.getElementById('isremember').click();"
                                                           + "document.getElementById('loginButton').click();";

    private static final String GET_BUTTON_TEXT_JS = "return document.getElementById('ContainerFrame').contentWindow.document.getElementById('ContainerFrame').contentWindow.document.getElementById('fabu').value;";

    private static final String SUBMIT_JS          = "document.getElementById('ContainerFrame').contentWindow.document.getElementById('ContainerFrame').contentWindow.document.getElementById('fabu').click();";

    private JPanel              contentPane;

    private JWebBrowser         webBrowser;

    private JTextField          intervalTextField;

    private JTabbedPane         tabbedPane;

    private Object[]            urls               = null;

    private int                 urlIndex           = 0;

    int                         interval;

    boolean                     on                 = false;

    //    boolean                     toSubmit           = true;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        NativeInterface.open();
        UIUtils.setPreferredLookAndFeel();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                MainFrame frame = new MainFrame();
                frame.setSize(1024, 600);
                frame.setLocationByPlatform(true);
                frame.setVisible(true);
            }
        });
        NativeInterface.runEventPump();

    }

    JWebBrowser createBrowser() {
        final JWebBrowser webBrowser = new JWebBrowser();
        webBrowser.setMenuBarVisible(false);

        webBrowser.addWebBrowserListener(new WebBrowserAdapter() {

            @Override
            public void locationChanged(WebBrowserNavigationEvent e) {
                super.locationChanged(e);
                int count = 0;
                do {
                    sleep(100);
                    if (webBrowser.getLoadingProgress() == 100) {
                        break;
                    }
                } while (count++ < 50);

                handle(e);
            }
        });
        return webBrowser;
    }

    private void handle(WebBrowserNavigationEvent e) {
        String location = e.getNewResourceLocation();
        //        System.out.println("Location: " + location);

        if (location.startsWith("https://passport.58.com/pso/viplogin/")) {
            doLogin();
        } else if (location.startsWith("http://vip.58.com/app/position/")) {
            if (!on) {
                return;
            }
            if (urls != null) {
                return;
            }

            urls = (Object[]) webBrowser.executeJavascriptWithResult(GET_URLS_JS);

            if (urls.length > 0) {
                String url = (String) urls[urlIndex++];
                webBrowser.navigate(url);
                System.out.println("修改: " + url);
                //                toSubmit = true;
            }
        } else if (location.startsWith("http://vip.58.com/fun/postposition/")) {
            if (!on) {
                webBrowser.stopLoading();
                webBrowser.navigate("http://vip.58.com/app/position/");
                return;
            }

            sleep(interval);
            webBrowser.executeJavascript(SUBMIT_JS);
            System.out.println("发布: " + location);
        } else if (location.startsWith("http://v.58.com/v2/zppostreceive")) {
            if (!on) {
                webBrowser.stopLoading();
                webBrowser.navigate("http://vip.58.com/app/position/");
                return;
            }
            sleep(1000);
            if (urlIndex == urls.length) {
                urlIndex = 0;
                renewWebBrowser();
                urls = null;
                webBrowser.navigate("http://vip.58.com/app/position/");
                return;
            }
            String url = (String) urls[urlIndex++];
            webBrowser.navigate(url);
            System.out.println("修改: " + url);
        }
    }

    private void renewWebBrowser() {
        System.out.println("renewing browser...");
        webBrowser.stopLoading();

        WebBrowserListener[] listeners = webBrowser.getWebBrowserListeners();
        for (WebBrowserListener l : listeners) {
            webBrowser.removeWebBrowserListener(l);
        }

        webBrowser.disposeNativePeer();
        tabbedPane.remove(webBrowser);

        webBrowser = null;

        if (NativeInterface.isOpen()) {
            NativeInterface.close();
        }
        NativeInterface.open();

        JWebBrowser newBrowser = createBrowser();
        webBrowser = newBrowser;
        tabbedPane.add("job.58", newBrowser);
    }

    protected static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void doLogin() {
        System.out.println("start login...");
        webBrowser.executeJavascript(LOGIN_JS);
    }

    /**
     * Create the frame.
     */
    public MainFrame() {
        setTitle("职位刷新工具");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                webBrowser.stopLoading();
                webBrowser.disposeNativePeer();
            }
        });

        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        JPanel panel = new JPanel();
        FlowLayout flowLayout = (FlowLayout) panel.getLayout();
        flowLayout.setAlignment(FlowLayout.LEFT);
        contentPane.add(panel, BorderLayout.NORTH);

        intervalTextField = new JTextField();
        intervalTextField.setText("5000");
        intervalTextField.setHorizontalAlignment(SwingConstants.LEFT);
        panel.add(intervalTextField);
        intervalTextField.setColumns(10);

        final JButton startBtn = new JButton("开始刷新");
        startBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (startBtn.getText().equals("开始刷新")) {

                    if ("".equals(intervalTextField.getText().trim())) {
                        return;
                    }

                    urls = null;
                    urlIndex = 0;
                    //                    toSubmit = true;
                    on = true;
                    startBtn.setText("停止刷新");
                    interval = Integer.parseInt(intervalTextField.getText().trim());
                    webBrowser.navigate("http://vip.58.com/app/position/");
                } else {
                    on = false;
                    startBtn.setText("开始刷新");

                }
            }
        });
        panel.add(startBtn);

        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        contentPane.add(tabbedPane, BorderLayout.CENTER);

        webBrowser = createBrowser();
        tabbedPane.add("job.58", webBrowser);
    }
}
