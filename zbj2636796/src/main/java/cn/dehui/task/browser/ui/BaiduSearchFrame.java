package cn.dehui.task.browser.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import chrriis.common.UIUtils;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import cn.dehui.task.browser.controller.Callback;
import cn.dehui.task.browser.controller.Status;
import cn.dehui.task.browser.controller.baidu.StatisticBaiduController;
import cn.dehui.task.browser.controller.baidu.UrlBaiduController;

public class BaiduSearchFrame extends SearchFrame {

    private static final long        serialVersionUID = 1774193438080790342L;

    private static final String      BAIDU            = "Baidu";

    private StatisticBaiduController statisticController;

    private UrlBaiduController       urlController;

    private int                      keywordIndex     = 0;

    private long                     timestamp;

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
                        urlController = new UrlBaiduController(webBrowser);
                    }

                    urlController.initControl();

                    try {
                        if (bw == null) {
                            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(outputFolder,
                                    "关键词的前十位网址汇总." + System.currentTimeMillis() + ".csv")), "GBK"));
                            bw.write("Keywords,第1,第2,第3,第4,第5,第6,第7,第8,第9,第10\r\n");
                        }
                        urlController.setBw(bw);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }

                    urlController.setAction(new Callback() {

                        @Override
                        public void execute() {
                            keywordIndexForUrl++;
                            if (keywordIndexForUrl < keywordList.size()) {
                                urlController.setStatus(Status.UNSTARRED);
                                System.out.printf("Used time: %d ms\r\n", System.currentTimeMillis() - timestamp);
                                timestamp = System.currentTimeMillis();
                                System.out.printf("Searching keyword: (%d) %s. ", keywordIndexForUrl,
                                        keywordList.get(keywordIndexForUrl));
                                urlController.setKeyword(keywordList.get(keywordIndexForUrl));
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
                                initUrlBtn.setText("获取URL");
                            }
                        }
                    });

                    urlController.setStatus(Status.UNSTARRED);
                    timestamp = System.currentTimeMillis();
                    System.out.printf("Searching keyword: (%d) %s. ", keywordIndexForUrl,
                            keywordList.get(keywordIndexForUrl));
                    urlController.setKeyword(keywordList.get(keywordIndexForUrl));
                    //                    urlController.run();
                    SwingUtilities.invokeLater(urlController);
                } else {
                    initUrlBtn.setText("获取URL");
                    urlController.stop();
                }
            }
        };
    }

    @Override
    protected ActionListener getFetchUrlListener() {
        return new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String maxUrlPerKeyword = maxUrlCountTextField.getText().trim();

                if (maxUrlPerKeyword.isEmpty()) {
                    JOptionPane.showMessageDialog(BaiduSearchFrame.this, "URL个数没有正确填写", "URL个数没有正确填写",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                int maxUrlPerKeywordInt;
                try {
                    maxUrlPerKeywordInt = Integer.parseInt(maxUrlPerKeyword);
                } catch (Exception e1) {
                    JOptionPane.showMessageDialog(BaiduSearchFrame.this, "URL个数没有正确填写", "URL个数没有正确填写",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (fetchUrlBtn.getText().equals("对关键词加引号搜索")) {

                    if (keywordList.isEmpty() || keywordIndexForFetch >= keywordList.size()) {
                        return;
                    }

                    fetchUrlBtn.setText("暂停对关键词加引号搜索");
                    if (urlController == null) {
                        urlController = new UrlBaiduController(webBrowser);
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

                    urlController.setAction(new Callback() {

                        @Override
                        public void execute() {
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
                    if (statisticController == null) {
                        statisticController = new StatisticBaiduController(webBrowser);
                    }

                    int maxUrlResultCount = Integer.parseInt(maxUrlResultCountTextField.getText().trim());
                    int waitTime = Integer.parseInt(waitTimeTextField.getText().trim());

                    statisticController.setMaxUrlResultCount(maxUrlResultCount);
                    statisticController.setWaitTime(waitTime);
                    statisticController.setOutputPath(outputFolder);
                    statisticController.initControl();
                    //                    controller.setObject(object);

                    statisticController.setAction(new Callback() {

                        @Override
                        public void execute() {
                            keywordIndex++;
                            if (keywordIndex < keywordList.size()) {
                                statisticController.setStatus(Status.UNSTARRED);
                                System.out.printf("Searching keyword: (%d) %s\r\n", keywordIndex,
                                        keywordList.get(keywordIndex));
                                statisticController.setKeyword(keywordList.get(keywordIndex));
                                //                                controller.run();
                                SwingUtilities.invokeLater(statisticController);
                            } else {
                                statisticController.stop();
                                startBtn.setText("开始");
                            }
                        }
                    });

                    statisticController.setStatus(Status.UNSTARRED);
                    System.out.printf("Searching keyword: (%d) %s\r\n", keywordIndex, keywordList.get(keywordIndex));
                    statisticController.setKeyword(keywordList.get(keywordIndex));
                    //                    controller.run();
                    SwingUtilities.invokeLater(statisticController);

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
                    statisticController.stop();
                }
            }
        };
    }

    @Override
    protected String getBrowserTitle() {
        return BAIDU;
    }

    @Override
    protected String getWaitTime() {
        return "0";
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
                SearchFrame frame = new BaiduSearchFrame();
                frame.setSize(1024, 600);
                frame.setLocationByPlatform(true);
                frame.setVisible(true);
            }
        });
        NativeInterface.runEventPump();
    }
}
