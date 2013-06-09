package cn.dehui.task.browser.search.commonthread.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import chrriis.common.UIUtils;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserListener;
import cn.dehui.task.browser.search.commonthread.Callback;
import cn.dehui.task.browser.search.commonthread.controller.StatisticGoogleController;
import cn.dehui.task.browser.search.commonthread.controller.UrlGoogleController;
import cn.dehui.task.browser.search.uithread.controller.util.Status;

public class GoogleSearchFrame extends SearchFrame {

    private static final long         serialVersionUID = 1774193438080790342L;

    private static final String       GOOGLE           = "Google";

    private StatisticGoogleController statisticGoogleController;

    private UrlGoogleController       urlController;

    private int                       keywordIndex     = 0;

    private long                      timestamp;

    @Override
    protected ActionListener getInitUrlListener() {
        return new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (initUrlBtn.getText().equals("获取URL")) {

                    if (keywordList.isEmpty() || keywordIndexForUrl >= keywordList.size()) {
                        return;
                    }

                    initUrlBtn.setText("暂停获取URL");
                    if (urlController == null) {
                        urlController = new UrlGoogleController(webBrowser);
                    }

                    urlController.initControl();

                    try {
                        if (bw == null) {
                            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(outputFolder,
                                    String.format(KEYWORD_URL_FILE_TPL, 10, System.currentTimeMillis()))), "GBK"));
                            bw.write("Keywords,第1,第2,第3,第4,第5,第6,第7,第8,第9,第10\r\n");
                        }
                        urlController.setBw(bw);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }

                    urlController.setAction(new Callback<Void>() {

                        @Override
                        public Void execute() {
                            keywordIndexForUrl++;

                            if (keywordIndexForUrl < keywordList.size()) {
                                urlController.setStatus(Status.UNSTARRED);
                                System.out.printf("Used time: %d ms\r\n", System.currentTimeMillis() - timestamp);
                                timestamp = System.currentTimeMillis();
                                System.out.printf("Searching keyword: (%d) %s. ", keywordIndexForUrl,
                                        keywordList.get(keywordIndexForUrl));
                                urlController.setKeyword(keywordList.get(keywordIndexForUrl));
                                //                                urlController.run();
                                //                                SwingUtilities.invokeLater(urlController);
                                new Thread(urlController).start();
                            } else {
                                try {
                                    bw.close();
                                    System.out.println("URL获取完成");
                                    urlController.stop();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                initUrlBtn.setText("获取URL");
                            }

                            if (keywordIndexForUrl % 10 == 0) {
                                renewWebBrowser();
                            }

                            return null;
                        }
                    });

                    urlController.setStatus(Status.UNSTARRED);
                    timestamp = System.currentTimeMillis();
                    System.out.printf("Searching keyword: (%d) %s. ", keywordIndexForUrl,
                            keywordList.get(keywordIndexForUrl));
                    urlController.setKeyword(keywordList.get(keywordIndexForUrl));
                    //                    urlController.run();
                    //                    SwingUtilities.invokeLater(urlController);
                    new Thread(urlController).start();
                } else {
                    initUrlBtn.setText("获取URL");
                    urlController.stop();
                }
            }
        };
    }

    private void renewWebBrowser() {
        System.out.println("renewing browser...");
        try {
            SwingUtilities.invokeAndWait(new Runnable() {

                @Override
                public void run() {
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
                    tabbedPane.add(getBrowserTitle(), newBrowser);

                    if (urlController != null) {
                        urlController.setWebBrowser(newBrowser);
                        urlController.initControl();
                    }

                    if (statisticGoogleController != null) {
                        statisticGoogleController.setWebBrowser(newBrowser);
                        statisticGoogleController.initControl();
                    }
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected ActionListener getFetchUrlListener() {
        return new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String maxUrlPerKeyword = maxUrlCountTextField.getText().trim();

                if (maxUrlPerKeyword.isEmpty()) {
                    JOptionPane.showMessageDialog(GoogleSearchFrame.this, "URL个数没有正确填写", "URL个数没有正确填写",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                int maxUrlPerKeywordInt;
                try {
                    maxUrlPerKeywordInt = Integer.parseInt(maxUrlPerKeyword);
                } catch (Exception e1) {
                    JOptionPane.showMessageDialog(GoogleSearchFrame.this, "URL个数没有正确填写", "URL个数没有正确填写",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (fetchUrlBtn.getText().equals("对关键词加引号搜索")) {

                    if (keywordList.isEmpty() || keywordIndexForFetch >= keywordList.size()) {
                        return;
                    }

                    fetchUrlBtn.setText("暂停对关键词加引号搜索");
                    if (urlController == null) {
                        urlController = new UrlGoogleController(webBrowser);
                    }
                    urlController.setMaxUrlForOneKeyword(maxUrlPerKeywordInt);

                    urlController.initControl();

                    try {
                        if (bw == null) {
                            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                                    new File(outputFolder, String.format(KEYWORD_URL_FILE_TPL, maxUrlPerKeywordInt,
                                            System.currentTimeMillis()))), "GBK"));
                            bw.write("Keywords");
                            for (int i = 0; i < maxUrlPerKeywordInt; i++) {
                                bw.write(",第" + (i + 1));
                            }
                            bw.write("\r\n");
                        }
                        urlController.setBw(bw);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }

                    urlController.setAction(new Callback<Void>() {

                        @Override
                        public Void execute() {
                            keywordIndexForFetch++;
                            if (keywordIndexForFetch < keywordList.size()) {
                                urlController.setStatus(Status.UNSTARRED);
                                System.out.printf("Used time: %d ms\r\n", System.currentTimeMillis() - timestamp);
                                timestamp = System.currentTimeMillis();
                                System.out.printf("Searching keyword: (%d) %s. ", keywordIndexForFetch,
                                        keywordList.get(keywordIndexForFetch));
                                urlController.setKeyword("\"" + keywordList.get(keywordIndexForFetch) + "\"");
                                //                                urlController.run();
                                SwingUtilities.invokeLater(urlController);
                            } else {
                                try {
                                    bw.close();
                                    System.out.println("URL获取完成");
                                    urlController.stop();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                fetchUrlBtn.setText("对关键词加引号搜索");
                            }

                            if (keywordIndexForFetch % 2 == 0) {
                                renewWebBrowser();
                            }

                            return null;
                        }
                    });

                    urlController.setStatus(Status.UNSTARRED);
                    timestamp = System.currentTimeMillis();
                    System.out.printf("Searching keyword: (%d) %s. ", keywordIndexForFetch,
                            keywordList.get(keywordIndexForFetch));
                    urlController.setKeyword("\"" + keywordList.get(keywordIndexForFetch) + "\"");
                    //                    urlController.run();
                    SwingUtilities.invokeLater(urlController);
                } else {
                    fetchUrlBtn.setText("对关键词加引号搜索");
                    urlController.stop();
                }
            }
        };
    }

    @Override
    protected ActionListener getStartListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (startBtn.getText().equals("开始")) {

                    if (keywordList.isEmpty() || keywordIndex >= keywordList.size()) {
                        return;
                    }

                    startBtn.setText("暂停");
                    if (statisticGoogleController == null) {
                        statisticGoogleController = new StatisticGoogleController(webBrowser);
                    }

                    int maxUrlResultCount = Integer.parseInt(maxUrlResultCountTextField.getText().trim());
                    int waitTime = Integer.parseInt(waitTimeTextField.getText().trim());

                    statisticGoogleController.setMaxUrlResultCount(maxUrlResultCount);
                    statisticGoogleController.setWaitTime(waitTime);
                    statisticGoogleController.setOutputPath(outputFolder);
                    statisticGoogleController.initControl();
                    //                    controller.setObject(object);

                    statisticGoogleController.setAction(new Callback<Void>() {

                        @Override
                        public Void execute() {
                            keywordIndex++;
                            if (keywordIndex < keywordList.size()) {
                                statisticGoogleController.setStatus(Status.UNSTARRED);
                                System.out.printf("Searching keyword: (%d) %s\r\n", keywordIndex,
                                        keywordList.get(keywordIndex));
                                statisticGoogleController.setKeyword(keywordList.get(keywordIndex));
                                //                                controller.run();
                                SwingUtilities.invokeLater(statisticGoogleController);
                            } else {
                                statisticGoogleController.stop();
                                startBtn.setText("开始");
                            }

                            if (keywordIndex % 2 == 0) {
                                renewWebBrowser();
                            }

                            return null;
                        }
                    });

                    statisticGoogleController.setStatus(Status.UNSTARRED);
                    System.out.printf("Searching keyword: (%d) %s\r\n", keywordIndex, keywordList.get(keywordIndex));
                    statisticGoogleController.setKeyword(keywordList.get(keywordIndex));
                    //                    controller.run();
                    SwingUtilities.invokeLater(statisticGoogleController);

                    //                    for (; keywordIndex < keywordList.size(); keywordIndex++) {
                    //                        webBrowser.stopLoading();
                    //                        controller.setKeyword(keywordList.get(keywordIndex));
                    //                        //                        controller.setKeyword("百度");
                    //                        //                    controller.setKeyword("how to make money");
                    //                        controller.run();
                    //                        //                                new Thread(controller).start();
                    //                        try {
                    //                            object.wait();
                    //
                    //                            if (startBtn.getText().equals("开始")) {
                    //                                return;
                    //                            }
                    //                        } catch (InterruptedException e1) {
                    //                            e1.printStackTrace();
                    //                        }
                    //                    }

                } else {
                    startBtn.setText("开始");
                    statisticGoogleController.stop();
                }
            }
        };
    }

    @Override
    protected String getBrowserTitle() {
        return GOOGLE;
    }

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        NativeInterface.open();
        UIUtils.setPreferredLookAndFeel();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                SearchFrame frame = new GoogleSearchFrame();
                frame.setSize(1024, 600);
                frame.setLocationByPlatform(true);
                frame.setVisible(true);
            }
        });
        NativeInterface.runEventPump();
    }
}
