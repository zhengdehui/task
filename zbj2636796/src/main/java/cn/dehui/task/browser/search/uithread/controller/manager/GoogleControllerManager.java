package cn.dehui.task.browser.search.uithread.controller.manager;

import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import cn.dehui.task.browser.search.uithread.controller.SearchContext;
import cn.dehui.task.browser.search.uithread.controller.google.ResultCountGoogleController;
import cn.dehui.task.browser.search.uithread.controller.google.StatisticGoogleController;
import cn.dehui.task.browser.search.uithread.controller.google.UrlGoogleController;
import cn.dehui.task.browser.search.uithread.ui.SearchFrame;
import cn.dehui.task.browser.search.util.Callback;
import cn.dehui.task.browser.search.util.Status;
import cn.dehui.task.browser.search.util.Utils;

public class GoogleControllerManager extends ControllerManager {

    private ResultCountGoogleController resultCountGoogleController;

    public GoogleControllerManager(JPanel contentPane) {
        super(contentPane);
    }

    @Override
    public void startCountResult(final JButton button) {
        Utils.clearSessionCookies();
        initBufferedWriterForSiteCount();

        String url = keywordList.get(keywordIndexForSiteCount);
        SearchContext sc = new SearchContext("\"" + Utils.removeHeadFootForUrl(url) + "\"");

        resultCountGoogleController.setWebBrowser(webBrowser);
        resultCountGoogleController.setStatus(Status.UNSTARRED);
        resultCountGoogleController.setSearchContext(sc);
        resultCountGoogleController.setAction(new Callback<Void>() {
            @Override
            public Void execute() {
                SearchContext searchContext = resultCountGoogleController.getSearchContext();
                outputSiteCount(searchContext);

                keywordIndexForSiteCount++;

                if (keywordIndexForSiteCount < keywordList.size()) {
                    resultCountGoogleController.setStatus(Status.UNSTARRED);
                    String url = keywordList.get(keywordIndexForSiteCount);
                    searchContext = new SearchContext("\"" + Utils.removeHeadFootForUrl(url) + "\"");
                    resultCountGoogleController.setSearchContext(searchContext);

                    refreshBrowserAndCookie(keywordIndexForSiteCount);

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
                    button.setText(SearchFrame.SITE_COUNT);
                }

                return null;
            }
        });
        SwingUtilities.invokeLater(resultCountGoogleController);
        runningController = resultCountGoogleController;
        System.out.printf("Searching URL: (%d) %s. ", keywordIndexForSiteCount, sc.keyword);
    }

    @Override
    protected void initControllers() {
        statisticController = new StatisticGoogleController(this);

        urlController = new UrlGoogleController(this);

        resultCountGoogleController = new ResultCountGoogleController(this);
    }

    @Override
    protected void handleTimeout() {
        if (webBrowser.getResourceLocation() != null
                && !webBrowser.getResourceLocation().startsWith("http://www.google.com/sorry/")) {
            //                                webBrowser.stopLoading();
            //                                webBrowser.navigate(webBrowser.getResourceLocation());
            //                                System.out.println("page reloaded...");

            // sometimes the page stops after the captcha is input. It seems to be solved by changing to onLocationChanged
            if (runningController.getLastSearchUrl() != null
                    && runningController.getLastSearchUrl().startsWith("http://www.google.com/sorry/")) {
                webBrowser.navigate(webBrowser.getResourceLocation());
            } else {
                runningController.research();
            }
        }
    }
}
