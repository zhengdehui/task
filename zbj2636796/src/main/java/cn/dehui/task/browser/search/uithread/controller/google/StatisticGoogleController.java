package cn.dehui.task.browser.search.uithread.controller.google;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import cn.dehui.task.browser.search.uithread.controller.manager.ControllerManager;
import cn.dehui.task.browser.search.uithread.controller.manager.IStatisticController;
import cn.dehui.task.browser.search.util.Status;
import cn.dehui.task.browser.search.util.Utils;

public class StatisticGoogleController extends UrlGoogleController implements IStatisticController {

    /**
     * 记录统计到哪个URl
     */
    private int    urlIndex              = 0;

    /**
     * 记录当前的url已经统计了多少条记录，内部用来辅助统计的变量
     */
    private int    currentUrlResultCount = 0;

    private int    maxResultPerUrl;

    private Random random                = new Random();

    private int    waitTime;

    private long   timestamp             = 0;

    public StatisticGoogleController(ControllerManager controllerManager) {
        super(controllerManager);
    }

    @Override
    protected List<String> collectUrls() {
        String js = "if(document.getElementById('topstuff')){return document.getElementById('topstuff').outerHTML;}else{return '';}";
        String topstuffInnerHtml = (String) webBrowser.executeJavascriptWithResult(js);
        if (topstuffInnerHtml.contains("No results found for")) {
            return new ArrayList<String>();
        }
        return super.collectUrls();
    }

    @Override
    public void run() {
        urlIndex = 0;
        super.run();
    }

    private void renewBrowser() {
        // renew browser
        controllerManager.renewWebBrowser();
        getWebBrowser().navigate(HTTPS_GOOGLE_URL);
    }

    @Override
    protected void handleEnterGoogleSite(final JWebBrowser webBrowser) {
        if (status == Status.URL_SEARCHING) {
            timestamp = System.currentTimeMillis();
            searchUrlInResultPage();
            return;
        }

        super.handleEnterGoogleSite(webBrowser);
    }

    private void searchUrlInResultPage() {
        if (urlIndex >= searchContext.urlList.size()) {
            callback.execute();
            return;
        }

        String url = searchContext.urlList.get(urlIndex);

        url = Utils.removeHeadFootForUrl(Utils.decodeUrl(url));
        System.out.printf("start url: (%d) %s \r\n", urlIndex, url);

        sleep(1000 + (Math.abs(waitTime - 1000) == 0 ? 0 : random.nextInt(Math.abs(waitTime - 1000))));

        searchKeywordInMainPage(url);

        //        String javascript = "var inputs=document.getElementsByTagName('input');"
        //                + "for(var i=0;i<inputs.length;i++){if(inputs[i].type=='text'&&inputs[i].name=='q'){inputs[i].value='\"%s\"';break;}}";
        //        webBrowser.executeJavascript(String.format(javascript, url));
        //        sleep(500);
        //        webBrowser.executeJavascript("document.getElementsByTagName('form')[0].submit();");
    }

    @Override
    public void setMaxResultPerUrl(int maxResultPerUrl) {
        this.maxResultPerUrl = maxResultPerUrl;
    }

    @Override
    public void setWaitTime(int waitTime) {
        this.waitTime = waitTime;
    }

    @Override
    protected void handleSearchResult(final JWebBrowser webBrowser) {
        switch (status) {
            case KEYWORD_SEARCHING:
                List<String> urls = collectUrls();
                searchContext.urlList.addAll(urls);
                if (searchContext.urlList.size() < maxUrlPerKeyword && !meetEnd() && !urls.isEmpty()) {
                    //                    webBrowser.executeJavascript(nextPageJs);
                    nextPage();
                } else {
                    status = Status.URL_SEARCHING;
                    renewBrowser();
                    currentUrlResultCount = 0;
                }
                break;
            case URL_SEARCHING:
                //1. get all results, calc into <keyword, Map<domain, count>>, inc currentUrlResultCount
                List<String> resultUrls = collectUrls();

                for (String url : resultUrls) {

                    // URL should be like this: http://www.aaa.com/bbb
                    String[] parts = url.split("/", 4);
                    if (parts.length < 3) {
                        System.out.println(url);
                    }

                    // 只统计域名
                    String domain = parts[2];

                    int currentCount = searchContext.statisticMap.containsKey(domain) ? searchContext.statisticMap
                            .get(domain) : 0;
                    searchContext.statisticMap.put(domain, currentCount + 1);
                }
                currentUrlResultCount += resultUrls.size();

                if (currentUrlResultCount > maxResultPerUrl || meetEnd() || resultUrls.isEmpty()) {
                    urlIndex++;
                    if (urlIndex < maxUrlPerKeyword) {
                        currentUrlResultCount = 0;
                        System.out.println("Used time: " + (System.currentTimeMillis() - timestamp));
                        renewBrowser();
                    } else {
                        System.out.printf("Used time: %d ms\r\n", System.currentTimeMillis() - timestamp);
                        callback.execute();
                    }
                } else {
                    //                    getWebBrowser().executeJavascript(nextPageJs);
                    nextPage();
                }
                break;
            default:
                break;
        }
    }
}
