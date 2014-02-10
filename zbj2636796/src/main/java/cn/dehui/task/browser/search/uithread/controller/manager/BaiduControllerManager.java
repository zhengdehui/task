package cn.dehui.task.browser.search.uithread.controller.manager;

import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import cn.dehui.task.browser.search.uithread.controller.SearchContext;
import cn.dehui.task.browser.search.uithread.controller.baidu.ResultCountBaiduController;
import cn.dehui.task.browser.search.uithread.controller.baidu.StatisticBaiduController;
import cn.dehui.task.browser.search.uithread.controller.baidu.UrlBaiduController;
import cn.dehui.task.browser.search.uithread.ui.SearchFrame;
import cn.dehui.task.browser.search.util.Callback;
import cn.dehui.task.browser.search.util.Status;
import cn.dehui.task.browser.search.util.Utils;

public class BaiduControllerManager extends ControllerManager {

    private ResultCountBaiduController resultCountBaiduController;

    public BaiduControllerManager(JPanel contentPane) {
        super(contentPane);
    }

    @Override
    public void startCountResult(final JButton button) {
        Utils.clearSessionCookies();
        initBufferedWriterForSiteCount();

        String url = keywordList.get(keywordIndexForSiteCount);
        SearchContext sc = new SearchContext("\"" + Utils.removeHeadFootForUrl(url) + "\"");

        resultCountBaiduController.setWebBrowser(webBrowser);
        resultCountBaiduController.setStatus(Status.UNSTARRED);
        resultCountBaiduController.setSearchContext(sc);
        resultCountBaiduController.setAction(new Callback<Void>() {
            @Override
            public Void execute() {
                SearchContext searchContext = resultCountBaiduController.getSearchContext();
                outputSiteCount(searchContext);

                keywordIndexForSiteCount++;

                if (keywordIndexForSiteCount < keywordList.size()) {
                    resultCountBaiduController.setStatus(Status.UNSTARRED);
                    String url = keywordList.get(keywordIndexForSiteCount);
                    searchContext = new SearchContext("\"" + Utils.removeHeadFootForUrl(url) + "\"");
                    resultCountBaiduController.setSearchContext(searchContext);

                    refreshBrowserAndCookie(keywordIndexForSiteCount);

                    SwingUtilities.invokeLater(resultCountBaiduController);
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
        SwingUtilities.invokeLater(resultCountBaiduController);
        runningController = resultCountBaiduController;
        System.out.printf("Searching URL: (%d) %s. ", keywordIndexForSiteCount, sc.keyword);

    }

    @Override
    protected void initControllers() {
        statisticController = new StatisticBaiduController(this);
        urlController = new UrlBaiduController(this);
        resultCountBaiduController = new ResultCountBaiduController(this);
    }

    @Override
    protected void handleTimeout() {
        runningController.research();
    }
}
