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
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserEvent;

public class MainFrame extends JFrame {

    private static final long   serialVersionUID   = 6144722027050898517L;

    private static final String LOGIN_JS           = "document.getElementById('txtusername').value='zhiyuandg';"
                                                           + "document.getElementById('txtpwd').value='197888';"
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

    boolean                     toSubmit           = true;

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
        //        webBrowser.setBarsVisible(true);
        //        webBrowser.setButtonBarVisible(true);

        //        webBrowser.navigate("about:blank");

        webBrowser.addWebBrowserListener(new WebBrowserAdapter() {
            @Override
            public void loadingProgressChanged(WebBrowserEvent e) {
                super.loadingProgressChanged(e);

                if (webBrowser.getLoadingProgress() == 100) {
                    String location = webBrowser.getResourceLocation();
                    System.out.println("Location: " + location);

                    if (location.startsWith("https://passport.58.com/pso/viplogin/")) {
                        doLogin();
                    } else if (location.startsWith("http://vip.58.com/app/position/")) {
                        if (!on) {
                            return;
                        }
                        if (urls != null) {
                            return;
                        }

                        //                        String js = "var urls = new Array();"
                        //                                + "var as = document.getElementById('ContainerFrame').contentWindow.document.getElementsByTagName('a');"
                        //                                + "for(var i = 0; i < as.length; i++){"
                        //                                + "if(as[i].innerHTML == '修改'){"
                        //                                + "var url = as[i].href.substring(as[i].href.indexOf(\"'\") + 1, as[i].href.length - 1);"
                        //                                + "urls.push(url);}}" + "return urls;";

                        String js = "var urls=new Array();"
                                + "var as=document.getElementById('ContainerFrame').contentWindow.document.getElementsByTagName('a');"
                                + "for(var i = 0; i < as.length; i++){" + "if(as[i].innerHTML=='修改'){"
                                + "var onclickText=as[i].getAttribute('onclick');"
                                + "var begin=onclickText.indexOf(\"'\")+1;"
                                + "var end=onclickText.indexOf(\"'\",begin);"
                                + "var url=onclickText.substring(begin,end);" + "urls.push(url);}}" + "return urls;";

                        urls = (Object[]) webBrowser.executeJavascriptWithResult(js);

                        if (urls.length > 0) {
                            webBrowser.navigate((String) urls[urlIndex++]);
                            toSubmit = true;
                        }
                    } else if (location.startsWith("http://vip.58.com/fun/postposition/")) {
                        if (!on) {
                            webBrowser.stopLoading();
                            webBrowser.navigate("http://vip.58.com/app/position/");
                            return;
                        }

                        if (toSubmit) {
                            System.out.println("into submit...");

                            Object buttonTitle = webBrowser.executeJavascriptWithResult(GET_BUTTON_TEXT_JS);
                            System.out.println("buttonTitle: " + buttonTitle);
                            if (buttonTitle != null) {
                                webBrowser.stopLoading();
                                toSubmit = false;
                                webBrowser.executeJavascript(SUBMIT_JS);
                            }
                        } else {
                            System.out.println("into confirm...");
                            webBrowser.stopLoading();
                            try {
                                Thread.sleep(interval);
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                            }
                            if (urlIndex == urls.length) {
                                urlIndex = 0;
                            }
                            toSubmit = true;
                            webBrowser.navigate((String) urls[urlIndex++]);
                        }
                    }
                }
            }
        });
        return webBrowser;
    }

    private void doLogin() {
        System.out.println("start login...");
        webBrowser.executeJavascript(LOGIN_JS);
    }

    private JWebBrowser createSubBrowser() {
        final JWebBrowser webBrowser = new JWebBrowser();
        webBrowser.setMenuBarVisible(false);

        webBrowser.addWebBrowserListener(new WebBrowserAdapter() {
            @Override
            public void loadingProgressChanged(WebBrowserEvent e) {
                super.loadingProgressChanged(e);

                if (webBrowser.getLoadingProgress() == 100) {
                    String location = webBrowser.getResourceLocation();
                    System.out.println("Location: " + location);

                }
            }

        });
        return webBrowser;
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
                    toSubmit = true;
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
        //        contentPane.add(webBrowser, BorderLayout.CENTER);
        tabbedPane.add("job.58", webBrowser);

        //        subBrowser = createSubBrowser();
        //        tabbedPane.add("子页面", subBrowser);
    }
}
