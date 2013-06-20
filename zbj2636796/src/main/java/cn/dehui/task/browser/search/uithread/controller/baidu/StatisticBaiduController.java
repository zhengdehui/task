package cn.dehui.task.browser.search.uithread.controller.baidu;

import java.util.List;
import java.util.Random;

import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import cn.dehui.task.browser.search.uithread.controller.manager.ControllerManager;
import cn.dehui.task.browser.search.uithread.controller.manager.IStatisticController;
import cn.dehui.task.browser.search.util.Status;
import cn.dehui.task.browser.search.util.Utils;

public class StatisticBaiduController extends UrlBaiduController implements IStatisticController {

    private int    urlIndex              = 0;

    private int    currentUrlResultCount = 0;

    private Random random                = new Random();

    private int    maxResultPerUrl;

    private int    waitTime;

    public StatisticBaiduController(ControllerManager controllerManager) {
        super(controllerManager);
    }

    /**
     * 输出统计结果
     */
    //    private void printToFile() {
    //        System.out.print("outputing...");
    //        String format = "\"%s\",\"%d\"\r\n";
    //        BufferedWriter bw = null;
    //        try {
    //            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
    //                    new File(outputFolder, keyword + ".csv")), "GBK"));
    //            for (Map.Entry<String, Integer> entry : statisticMap.entrySet()) {
    //                bw.write(String.format(format, entry.getKey(), entry.getValue()));
    //            }
    //        } catch (IOException e) {
    //            e.printStackTrace();
    //        } finally {
    //            if (bw != null) {
    //                try {
    //                    bw.close();
    //                } catch (IOException e) {
    //                    e.printStackTrace();
    //                }
    //            }
    //        }
    //        System.out.println("finished");
    //    }

    @Override
    protected List<String> collectUrls() {
        //        String js = "var ps=document.getElementsByTagName('p');for(var i=0;i<ps.length;i++){if(ps[i].innerHTML.indexOf('没有找到该URL')!=-1) return true;} return false;";
        //        Object containNoResult = webBrowser.executeJavascriptWithResult(js);
        //        if (Boolean.parseBoolean(containNoResult.toString())) {
        //            return new ArrayList<String>();
        //        }
        return super.collectUrls();
    }

    @Override
    public void run() {
        urlIndex = 0;
        super.run();
    }

    private void searchUrlInResultPage() {
        if (urlIndex >= Math.min(URL_TO_SEARCH_COUNT, searchContext.urlList.size())) {
            // output
            //            printToFile();

            callback.execute();
            return;
        }

        String url = searchContext.urlList.get(urlIndex).toString();
        //        url = getRealUrl(url);
        //        int begin = url.indexOf("//") + 2;
        //        int end = url.charAt(url.length() - 1) == '/' ? url.length() - 1 : url.length();
        //        url = url.substring(begin, end);

        url = Utils.removeHeadFootForUrl(url);
        System.out.printf("start url: (%d) %s .", urlIndex, url);

        if (waitTime > 0) {
            sleep(random.nextInt(waitTime));
        }

        searchKeywordInMainPage("\"" + url + "\"");
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
    protected void handleSearchResult(JWebBrowser webBrowser) {
        switch (status) {
            case KEYWORD_SEARCHING:
                List<String> urls = collectUrls();
                searchContext.urlList.addAll(urls);
                if (searchContext.urlList.size() < URL_TO_SEARCH_COUNT && !meetEnd() && !urls.isEmpty()) {
                    webBrowser.executeJavascript(nextPageJs);
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
                    //                    url = getRealUrl(url);

                    String[] parts = url.split("/", 4);
                    if (parts.length < 3) {
                        System.out.println("结果中有非法URL：" + url);
                        continue;
                    }

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
                    webBrowser.executeJavascript(nextPageJs);
                }
                break;
            default:
                break;
        }
    }

    private void renewBrowser() {
        // renew browser
        controllerManager.renewWebBrowser();
        getWebBrowser().navigate(BAIDU_URL);
    }

    @Override
    protected void enterMainPage() {
        if (status == Status.URL_SEARCHING) {
            timestamp = System.currentTimeMillis();
            searchUrlInResultPage();
            return;
        }

        super.enterMainPage();
    }
}
