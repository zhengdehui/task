package cn.dehui.task.browser.controller.baidu;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.SwingUtilities;

import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserAdapter;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserEvent;
import cn.dehui.task.browser.controller.Status;

public class StatisticBaiduController extends BaiduController {

    private Map<String, Integer> statisticMap;

    private int                  urlIndex              = 0;

    private int                  currentUrlResultCount = 0;

    private Random               random                = new Random();

    private int                  maxUrlResultCount;

    private int                  waitTime;

    private File                 outputFolder;

    public StatisticBaiduController(final JWebBrowser webBrowser) {
        super(webBrowser);
    }

    /**
     * 输出统计结果
     */
    private void printToFile() {
        System.out.print("outputing...");
        String format = "\"%s\",\"%d\"\r\n";
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                    new File(outputFolder, keyword + ".csv")), "GBK"));
            for (Map.Entry<String, Integer> entry : statisticMap.entrySet()) {
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
        System.out.println("finished");
    }

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

    @Override
    protected void restartPage() {
        System.out.println("restart searching keyword: " + keyword);
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                stop();
                initControl();
                setStatus(Status.UNSTARRED);
                StatisticBaiduController.this.run();
            }
        });
    }

    @Override
    public void setKeyword(String keyword) {
        this.keyword = keyword;
        statisticMap = new HashMap<String, Integer>();
    }

    private void searchUrlInResultPage() {
        if (urlIndex >= Math.min(URL_TO_SEARCH_COUNT, urlList.size())) {
            // output
            printToFile();

            callback.execute();
            return;
        }

        String url = urlList.get(urlIndex).toString();
        url = getRealUrl(url);
        int begin = url.indexOf("//") + 2;
        int end = url.charAt(url.length() - 1) == '/' ? url.length() - 1 : url.length();
        url = url.substring(begin, end);
        System.out.printf("start url: (%d) %s \r\n", urlIndex, url);

        if (waitTime > 0) {
            try {
                Thread.sleep(random.nextInt(waitTime));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        searchKeywordInMainPage("\"" + url + "\"");
    }

    public void setMaxUrlResultCount(int maxUrlResultCount) {
        this.maxUrlResultCount = maxUrlResultCount;
    }

    public void setWaitTime(int waitTime) {
        this.waitTime = waitTime;
    }

    public void setOutputPath(File outputFolder) {
        this.outputFolder = outputFolder;
    }

    @Override
    protected WebBrowserAdapter getWebBrowserAdapter() {
        return new WebBrowserAdapter() {
            @Override
            public void loadingProgressChanged(WebBrowserEvent e) {
                super.loadingProgressChanged(e);

                if (webBrowser.getLoadingProgress() > PROGRESS) {
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            int progress = webBrowser.getLoadingProgress();
                            String location = webBrowser.getResourceLocation();
                            updateLocation(location);
                            //                            System.out.println("--> " + location);

                            if (progress > PROGRESS && location.equals(BAIDU_URL)) {
                                webBrowser.stopLoading();
                                if (status == Status.UNSTARRED) {
                                    status = Status.KEYWORD_SEARCHING;
                                    searchKeywordInMainPage(keyword);
                                }
                            } else if (progress == 100 && (location.startsWith("http://www.baidu.com/s"))) {
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException ex) {
                                    ex.printStackTrace();
                                }
                                webBrowser.stopLoading();
                                try {
                                    switch (status) {
                                        case KEYWORD_SEARCHING:
                                            List<String> urls = collectUrls();
                                            urlList.addAll(urls);
                                            if (urlList.size() < URL_TO_SEARCH_COUNT && !meetEnd() && !urls.isEmpty()) {
                                                webBrowser.executeJavascript(nextPageJs);
                                            } else {
                                                status = Status.URL_SEARCHING;
                                                searchUrlInResultPage();
                                                currentUrlResultCount = 0;
                                            }
                                            break;
                                        case URL_SEARCHING:
                                            //1. get all results, calc into <keyword, Map<domain, count>>, inc currentUrlResultCount
                                            List<String> resultUrls = collectUrls();

                                            for (String url : resultUrls) {
                                                url = getRealUrl(url);

                                                String[] parts = url.split("/", 4);
                                                if (parts.length < 3) {
                                                    System.out.println("结果中有非法URL：" + url);
                                                    continue;
                                                }

                                                String domain = parts[2];

                                                int currentCount = statisticMap.containsKey(domain) ? statisticMap
                                                        .get(domain) : 0;
                                                statisticMap.put(domain, currentCount + 1);
                                            }
                                            currentUrlResultCount += resultUrls.size();

                                            if (currentUrlResultCount > maxUrlResultCount || meetEnd()
                                                    || resultUrls.isEmpty()) {
                                                urlIndex++;
                                                //                                                System.out.println("urlIndex: " + urlIndex);
                                                if (urlIndex < URL_TO_SEARCH_COUNT) {
                                                    currentUrlResultCount = 0;
                                                    searchUrlInResultPage();
                                                } else {
                                                    // output
                                                    printToFile();

                                                    callback.execute();
                                                }
                                            } else {
                                                webBrowser.executeJavascript(nextPageJs);
                                            }
                                            break;
                                        default:
                                            break;
                                    }
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    restartPage();
                                }
                            }
                        }
                    });
                }
            }
        };
    }
}
