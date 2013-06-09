package cn.dehui.task.browser.search.uithread.controller;

import java.awt.BorderLayout;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserListener;
import cn.dehui.task.browser.search.uithread.controller.google2.GoogleController2;
import cn.dehui.task.browser.search.uithread.controller.google2.ResultCountGoogleController2;
import cn.dehui.task.browser.search.uithread.controller.google2.StatisticGoogleController2;
import cn.dehui.task.browser.search.uithread.controller.google2.UrlGoogleController2;
import cn.dehui.task.browser.search.uithread.controller.util.Callback;
import cn.dehui.task.browser.search.uithread.controller.util.Status;
import cn.dehui.task.browser.search.uithread.controller.util.Utils;
import cn.dehui.task.browser.search.uithread.ui.GoogleSearchFrame2;

public class ControllerManager {
    protected static final String        KEYWORD_URL_FILE_TPL     = "关键词的前%d位网址汇总.%d.csv";

    protected static final String        SITE_COUNT_FILE_TPL      = "网址统计结果汇总.%d.csv";

    protected static final String        URL_ENCODING             = "utf8";

    private List<String>                 keywordList;

    private File                         outputFolder;

    private int                          waitTime;

    private int                          numberOfResultPerUrl;

    private JWebBrowser                  webBrowser;

    private StatisticGoogleController2   statisticGoogleController;

    private UrlGoogleController2         urlController;

    private ResultCountGoogleController2 resultCountGoogleController;

    private int                          keywordIndexForStatistic = 0;

    private int                          keywordIndexForUrl       = 0;

    private int                          keywordIndexForUrlQuote  = 0;

    private int                          keywordIndexForSiteCount = 0;

    private BufferedWriter               bwForUrl;

    private BufferedWriter               bwForUrlQuote;

    private BufferedWriter               bwForSiteCount;

    private JPanel                       contentPane;

    private long                         lastBeatHeartTime        = 0;

    private TimerTask                    timerTask;

    private Timer                        timer;

    private GoogleController2            runningController;

    public ControllerManager(JPanel contentPane) {
        webBrowser = createBrowser();

        this.contentPane = contentPane;

        contentPane.add(webBrowser, BorderLayout.CENTER);

        statisticGoogleController = new StatisticGoogleController2(this);

        urlController = new UrlGoogleController2(this);

        resultCountGoogleController = new ResultCountGoogleController2(this);

        scheduleTimer();
    }

    private void scheduleTimer() {
        timer = new Timer(true);
        timerTask = new TimerTask() {
            @Override
            public void run() {
                if (runningController == null || System.currentTimeMillis() - lastBeatHeartTime < 30000) {
                    return;
                }

                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            if (!webBrowser.getResourceLocation().startsWith("http://www.google.com/sorry/")) {

                                if (runningController.getLastSearchUrl() != null
                                        && runningController.getLastSearchUrl().startsWith(
                                                "http://www.google.com/sorry/")) {
                                    //                                    webBrowser.reloadPage();
                                    webBrowser.navigate(webBrowser.getResourceLocation());
                                } else {
                                    runningController.research();
                                }
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        timer.schedule(timerTask, 1000, 5000);
    }

    private void outputUrl(SearchContext sc, int maxUrlPerKeyword, boolean withQuote) {
        BufferedWriter bw = withQuote ? bwForUrlQuote : bwForUrl;
        try {
            String keyword = sc.keyword;
            String keywordToWrite = keyword.charAt(0) == '\"' && keyword.charAt(keyword.length() - 1) == '\"' ? keyword
                    : '\"' + keyword + '\"';
            bw.write(keywordToWrite);
            int len = Math.min(maxUrlPerKeyword, sc.urlList.size());
            for (int i = 0; i < len; i++) {
                String url = sc.urlList.get(i).toString();
                url = Utils.decodeUrl(url);

                bw.write(",\"" + url + "\"");
            }
            bw.write("\r\n");
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void outputSiteCount(SearchContext sc) {
        try {
            String keyword = sc.keyword;
            String keywordToWrite = keyword.charAt(0) == '\"' && keyword.charAt(keyword.length() - 1) == '\"' ? keyword
                    : '\"' + keyword + '\"';
            bwForSiteCount.write(String.format("%s,\"%d\",\"%d\"\r\n", keywordToWrite, sc.siteResultCount,
                    sc.quoteResultCount));
            bwForSiteCount.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startFetchUrl(final int maxUrlPerKeyword, final JButton button) {
        fetchUrl(maxUrlPerKeyword, button, false);
    }

    private void fetchUrl(final int maxUrlPerKeyword, final JButton button, final boolean withQuote) {
        Utils.clearSessionCookies();
        initBufferedWriterIfNeeded(maxUrlPerKeyword, withQuote);

        int keywordIndex = withQuote ? keywordIndexForUrlQuote : keywordIndexForUrl;

        String keyword = keywordList.get(keywordIndex);
        final SearchContext sc = new SearchContext(withQuote ? "\"" + keyword + "\"" : keyword);

        urlController.setWebBrowser(webBrowser);
        urlController.setMaxUrlPerKeyword(maxUrlPerKeyword);
        urlController.setStatus(Status.UNSTARRED);
        urlController.setSearchContext(sc);
        urlController.setAction(new Callback() {
            @Override
            public void execute() {
                SearchContext searchContext = urlController.getSearchContext();
                outputUrl(searchContext, maxUrlPerKeyword, withQuote);

                if (withQuote) {
                    keywordIndexForUrlQuote++;
                } else {
                    keywordIndexForUrl++;
                }
                int keywordIndex = withQuote ? keywordIndexForUrlQuote : keywordIndexForUrl;

                if (keywordIndex < keywordList.size()) {
                    urlController.setStatus(Status.UNSTARRED);
                    String keyword = keywordList.get(keywordIndex);
                    searchContext = new SearchContext(withQuote ? "\"" + keyword + "\"" : keyword);
                    urlController.setSearchContext(searchContext);

                    if (keywordIndex % 10 == 0) {
                        renewWebBrowser();
                        Utils.clearSessionCookies();
                        //                        urlController.setWebBrowser(webBrowser);
                    }

                    SwingUtilities.invokeLater(urlController);
                    System.out.printf("Searching keyword: (%d) %s. ", keywordIndex, keyword);
                } else {
                    try {
                        (withQuote ? bwForUrlQuote : bwForUrl).close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("URL获取完成");
                    stopFetchUrl();
                    button.setText(withQuote ? GoogleSearchFrame2.GET_URL_WITH_QUOTE : GoogleSearchFrame2.GET_URL);
                }
            }
        });
        SwingUtilities.invokeLater(urlController);
        runningController = urlController;
        System.out.printf("Searching keyword: (%d) %s. ", keywordIndex, keyword);
    }

    public void renewWebBrowser() {
        //        Utils.clearSessionCookies();
        System.out.println("renewing browser...");

        webBrowser.stopLoading();
        WebBrowserListener[] listeners = webBrowser.getWebBrowserListeners();
        for (WebBrowserListener l : listeners) {
            webBrowser.removeWebBrowserListener(l);
        }

        webBrowser.disposeNativePeer();
        contentPane.remove(webBrowser);

        webBrowser = null;

        if (NativeInterface.isOpen()) {
            NativeInterface.close();
        }
        NativeInterface.open();

        JWebBrowser newBrowser = createBrowser();
        webBrowser = newBrowser;
        contentPane.add(webBrowser, BorderLayout.CENTER);

        runningController.setWebBrowser(webBrowser);
    }

    private void initBufferedWriterIfNeeded(final int maxUrlPerKeyword, boolean withQuote) {
        int keywordIndex = withQuote ? keywordIndexForUrlQuote : keywordIndexForUrl;
        BufferedWriter bw = withQuote ? bwForUrlQuote : bwForUrl;
        if (keywordIndex == 0) {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                if (withQuote) {
                    bwForUrlQuote = new BufferedWriter(new FileWriter(new File(outputFolder, String.format(
                            KEYWORD_URL_FILE_TPL, maxUrlPerKeyword, System.currentTimeMillis()))));
                    bw = bwForUrlQuote;
                } else {
                    bwForUrl = new BufferedWriter(new FileWriter(new File(outputFolder, String.format(
                            KEYWORD_URL_FILE_TPL, maxUrlPerKeyword, System.currentTimeMillis()))));
                    bw = bwForUrl;
                }
                bw.write("Keywords");
                for (int i = 0; i < maxUrlPerKeyword; i++) {
                    bw.write(",第" + (i + 1));
                }
                bw.write("\r\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopFetchUrl() {
        urlController.stop();
        runningController = null;
    }

    public void startFetchUrlWithQuote(int maxUrlPerKeyword, JButton button) {
        fetchUrl(maxUrlPerKeyword, button, true);
    }

    public void stopFetchUrlWithQuote() {
        stopFetchUrl();
    }

    private void initBufferedWriterForSiteCount() {
        if (keywordIndexForSiteCount == 0) {
            if (bwForSiteCount != null) {
                try {
                    bwForSiteCount.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                bwForSiteCount = new BufferedWriter(new FileWriter(new File(outputFolder, String.format(
                        SITE_COUNT_FILE_TPL, System.currentTimeMillis()))));
                bwForSiteCount.write("URL,site数量,引号搜索数量\r\n");
                bwForSiteCount.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void startCountResult(final JButton button) {
        Utils.clearSessionCookies();
        initBufferedWriterForSiteCount();

        String url = keywordList.get(keywordIndexForSiteCount);
        SearchContext sc = new SearchContext("\"" + Utils.removeHeadFootForUrl(url) + "\"");

        resultCountGoogleController.setWebBrowser(webBrowser);
        resultCountGoogleController.setStatus(Status.UNSTARRED);
        resultCountGoogleController.setSearchContext(sc);
        resultCountGoogleController.setAction(new Callback() {
            @Override
            public void execute() {
                SearchContext searchContext = resultCountGoogleController.getSearchContext();
                outputSiteCount(searchContext);

                keywordIndexForSiteCount++;

                if (keywordIndexForSiteCount < keywordList.size()) {
                    resultCountGoogleController.setStatus(Status.UNSTARRED);
                    String url = keywordList.get(keywordIndexForSiteCount);
                    searchContext = new SearchContext("\"" + Utils.removeHeadFootForUrl(url) + "\"");
                    resultCountGoogleController.setSearchContext(searchContext);

                    if (keywordIndexForSiteCount % 10 == 0) {
                        renewWebBrowser();
                        //                        resultCountGoogleController.setWebBrowser(webBrowser);
                    }

                    SwingUtilities.invokeLater(resultCountGoogleController);
                    System.out.printf("Searching URL: (%d) %s. ", keywordIndexForSiteCount, searchContext.keyword);
                } else {
                    try {
                        bwForSiteCount.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Site获取完成");
                    stopCountResult();
                    button.setText(GoogleSearchFrame2.SITE_COUNT);
                }
            }
        });
        SwingUtilities.invokeLater(resultCountGoogleController);
        runningController = resultCountGoogleController;
        System.out.printf("Searching URL: (%d) %s. ", keywordIndexForSiteCount, sc.keyword);
    }

    public void stopCountResult() {
        resultCountGoogleController.stop();
        runningController = null;
    }

    public void startStatisticKeyword(int maxUrlPerKeyword, final JButton button) {
        Utils.clearSessionCookies();

        final SearchContext sc = new SearchContext(keywordList.get(keywordIndexForStatistic));

        statisticGoogleController.setWebBrowser(webBrowser);
        statisticGoogleController.setMaxUrlPerKeyword(maxUrlPerKeyword);
        statisticGoogleController.setStatus(Status.UNSTARRED);
        statisticGoogleController.setSearchContext(sc);
        statisticGoogleController.setAction(new Callback() {
            @Override
            public void execute() {
                SearchContext searchContext = statisticGoogleController.getSearchContext();
                outputStatistic(searchContext);

                keywordIndexForStatistic++;

                if (keywordIndexForStatistic < keywordList.size()) {
                    statisticGoogleController.setStatus(Status.UNSTARRED);
                    searchContext = new SearchContext(keywordList.get(keywordIndexForStatistic));
                    statisticGoogleController.setSearchContext(searchContext);

                    renewWebBrowser();
                    Utils.clearSessionCookies();

                    //                    statisticGoogleController.setWebBrowser(webBrowser);

                    SwingUtilities.invokeLater(statisticGoogleController);
                    System.out.printf("Searching keyword: (%d) %s. ", keywordIndexForStatistic,
                            keywordList.get(keywordIndexForStatistic));
                } else {
                    System.out.println("统计完成");
                    stopStatisticKeyword();
                    button.setText(GoogleSearchFrame2.STATISTIC);
                }
            }

        });
        SwingUtilities.invokeLater(statisticGoogleController);
        runningController = statisticGoogleController;

        statisticGoogleController.setMaxResultPerUrl(numberOfResultPerUrl);
        statisticGoogleController.setWaitTime(waitTime);
        System.out.printf("Searching keyword: (%d) %s. ", keywordIndexForStatistic,
                keywordList.get(keywordIndexForStatistic));
    }

    private void outputStatistic(SearchContext searchContext) {
        System.out.printf("Outputing statistic: (%d) %s. \r\n", keywordIndexForStatistic,
                keywordList.get(keywordIndexForStatistic));
        String format = "\"%s\",\"%d\"\r\n";
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(new File(outputFolder, searchContext.keyword + ".csv")));
            for (Map.Entry<String, Integer> entry : searchContext.statisticMap.entrySet()) {
                bw.write(String.format(format, entry.getKey(), entry.getValue()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void stopStatisticKeyword() {
        statisticGoogleController.stop();
        runningController = null;
    }

    private JWebBrowser createBrowser() {
        JWebBrowser webBrowser = new JWebBrowser();
        webBrowser.setMenuBarVisible(false);
        return webBrowser;
    }

    public JWebBrowser getWebBrowser() {
        return webBrowser;
    }

    public void setKeywordList(List<String> keywordList) {
        this.keywordList = keywordList;

        keywordIndexForStatistic = 0;
        keywordIndexForUrl = 0;
        keywordIndexForUrlQuote = 0;
        keywordIndexForSiteCount = 0;
    }

    public void setOutputFolder(File outputFolder) {
        this.outputFolder = outputFolder;
    }

    public void setWaitTime(int waitTime) {
        this.waitTime = waitTime;
    }

    public void setNumberOfResultPerUrl(int numberOfResultPerUrl) {
        this.numberOfResultPerUrl = numberOfResultPerUrl;
    }

    public void exit() {
        webBrowser.stopLoading();
        webBrowser.disposeNativePeer();
    }

    public void setLastBeatHeartTime(long lastBeatHeartTime) {
        this.lastBeatHeartTime = lastBeatHeartTime;
    }
}
