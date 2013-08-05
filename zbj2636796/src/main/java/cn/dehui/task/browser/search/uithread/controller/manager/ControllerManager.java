package cn.dehui.task.browser.search.uithread.controller.manager;

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
import cn.dehui.task.browser.search.uithread.controller.SearchContext;
import cn.dehui.task.browser.search.uithread.ui.SearchFrame;
import cn.dehui.task.browser.search.util.Callback;
import cn.dehui.task.browser.search.util.Status;
import cn.dehui.task.browser.search.util.Utils;

public abstract class ControllerManager {
    protected static final String  KEYWORD_URL_FILE_TPL     = "关键词的前%d位网址汇总.%d.csv";

    protected static final String  SITE_COUNT_FILE_TPL      = "网址统计结果汇总.%d.csv";

    protected static final String  URL_ENCODING             = "utf8";

    protected List<String>         keywordList;

    private File                   outputFolder;

    protected int                  waitTime;

    protected int                  numberOfResultPerUrl;

    protected JWebBrowser          webBrowser;

    protected int                  keywordIndexForStatistic = 0;

    protected int                  keywordIndexForUrl       = 0;

    protected int                  keywordIndexForUrlQuote  = 0;

    protected int                  keywordIndexForSiteCount = 0;

    protected BufferedWriter       bwForUrl;

    protected BufferedWriter       bwForUrlQuote;

    protected BufferedWriter       bwForSiteCount;

    private JPanel                 contentPane;

    private long                   lastBeatHeartTime        = 0;

    private TimerTask              timerTask;

    private Timer                  timer;

    protected IResearchController  runningController;

    private int                    timeout                  = 30000;

    private int                    cookieMod                = 10;

    protected IStatisticController statisticController;

    protected IUrlController       urlController;

    public ControllerManager(JPanel contentPane) {
        webBrowser = createBrowser();

        this.contentPane = contentPane;

        contentPane.add(webBrowser, BorderLayout.CENTER);

        scheduleTimer();

        initControllers();

    }

    protected abstract void initControllers();

    private void scheduleTimer() {
        timer = new Timer(true);
        timerTask = new TimerTask() {
            @Override
            public void run() {
                if (runningController == null || System.currentTimeMillis() - lastBeatHeartTime < timeout) {
                    return;
                }

                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            handleTimeout();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        timer.schedule(timerTask, 1000, 5000);
    }

    protected abstract void handleTimeout();

    protected void outputUrl(SearchContext sc, int maxUrlPerKeyword, boolean withQuote) {
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

    protected void outputSiteCount(SearchContext sc) {
        try {
            String keyword = sc.keyword;
            String keywordToWrite = keyword.charAt(0) == '\"' && keyword.charAt(keyword.length() - 1) == '\"' ? keyword
                    : '\"' + keyword + '\"';
            bwForSiteCount.write(String.format("%s,\"%d\",\"%d\",\"%d\"\r\n", keywordToWrite, sc.siteResultCount,
                    sc.quoteProbableResultCount, sc.quoteResultCount));
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
        urlController.setAction(new Callback<Void>() {
            @Override
            public Void execute() {
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

                    refreshBrowserAndCookie(keywordIndex);

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
                    button.setText(withQuote ? SearchFrame.GET_URL_WITH_QUOTE : SearchFrame.GET_URL);
                }

                return null;
            }
        });
        SwingUtilities.invokeLater(urlController);
        runningController = urlController;
        System.out.printf("Searching keyword: (%d) %s. ", keywordIndex, keyword);
    }

    public void startFetchUrlWithQuote(int maxUrlPerKeyword, JButton button) {
        fetchUrl(maxUrlPerKeyword, button, true);
    }

    protected void refreshBrowserAndCookie(int keywordIndex) {
        if (keywordIndex % 10 == 0) {
            renewWebBrowser();
        }

        if (keywordIndex % cookieMod == 0) {
            Utils.clearSessionCookies();
        }
    }

    public void renewWebBrowser() {
        System.out.println("renewing browser...");

        webBrowser.stopLoading();
        WebBrowserListener[] listeners = webBrowser.getWebBrowserListeners();
        for (WebBrowserListener l : listeners) {
            webBrowser.removeWebBrowserListener(l);
        }

        contentPane.remove(webBrowser);
        webBrowser.disposeNativePeer();

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

    protected void initBufferedWriterIfNeeded(final int maxUrlPerKeyword, boolean withQuote) {
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
        stop();
    }

    private void stop() {
        runningController.stop();
        runningController = null;
    }

    public void stopFetchUrlWithQuote() {
        stop();
    }

    protected void initBufferedWriterForSiteCount() {
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
                bwForSiteCount.write("URL,site数量,引号搜索估算数量,引号搜索实际数量\r\n");
                bwForSiteCount.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public abstract void startCountResult(final JButton button);

    public void stopCountResult() {
        stop();
    }

    public void startStatisticKeyword(int maxUrlPerKeyword, final JButton button) {
        Utils.clearSessionCookies();

        final SearchContext sc = new SearchContext(keywordList.get(keywordIndexForStatistic));

        statisticController.setWebBrowser(webBrowser);
        statisticController.setMaxUrlPerKeyword(maxUrlPerKeyword);
        statisticController.setStatus(Status.UNSTARRED);
        statisticController.setSearchContext(sc);
        statisticController.setAction(new Callback<Void>() {
            @Override
            public Void execute() {
                SearchContext searchContext = statisticController.getSearchContext();
                outputStatistic(searchContext);

                keywordIndexForStatistic++;

                if (keywordIndexForStatistic < keywordList.size()) {
                    statisticController.setStatus(Status.UNSTARRED);
                    searchContext = new SearchContext(keywordList.get(keywordIndexForStatistic));
                    statisticController.setSearchContext(searchContext);

                    refreshBrowserAndCookie(keywordIndexForStatistic);

                    SwingUtilities.invokeLater(statisticController);
                    System.out.printf("Searching keyword: (%d) %s. ", keywordIndexForStatistic,
                            keywordList.get(keywordIndexForStatistic));
                } else {
                    System.out.println("统计完成");
                    stopStatisticKeyword();
                    button.setText(SearchFrame.STATISTIC);
                }

                return null;
            }

        });
        SwingUtilities.invokeLater(statisticController);
        runningController = statisticController;

        statisticController.setMaxResultPerUrl(numberOfResultPerUrl);
        statisticController.setWaitTime(waitTime);
        System.out.printf("Searching keyword: (%d) %s. ", keywordIndexForStatistic,
                keywordList.get(keywordIndexForStatistic));
    }

    protected void outputStatistic(SearchContext searchContext) {
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
        stop();
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

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public void setCookieMod(int cookieMod) {
        this.cookieMod = cookieMod;
    }
}
