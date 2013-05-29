package cn.dehui.task.browser.search.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.SwingUtilities;

import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserAdapter;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserEvent;
import cn.dehui.task.browser.controller.Status;

public class StatisticGoogleController extends GoogleController {

    private Map<String, Integer> statisticMap;

    private int                  urlIndex              = 0;

    private int                  currentUrlResultCount = 0;

    private Random               random                = new Random();

    private int                  maxUrlResultCount;

    private int                  waitTime;

    private File                 outputFolder;

    public StatisticGoogleController(final JWebBrowser webBrowser) {
        super(webBrowser);
    }

    private void printToFile() {
        System.out.print("outputing...");
        String format = "\"%s\",\"%d\"\r\n";
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(new File(outputFolder, keyword + ".csv")));
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
        String js = "if(document.getElementById('topstuff')){return document.getElementById('topstuff').outerHTML;}else{return '';}";
        String topstuffInnerHtml = (String) executeJavascriptWithResult(js);
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

    @Override
    protected void restartPage() {
        System.out.println("restart searching keyword: " + keyword);
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                stop();
                initControl();
                setStatus(Status.UNSTARRED);
                StatisticGoogleController.this.run();
            }
        });
    }

    @Override
    public void setKeyword(String keyword) {
        this.keyword = keyword;
        statisticMap = new HashMap<String, Integer>();
    }

    private void searchUrlInResultPage() {
        if (urlIndex >= urlList.size()) {
            // output
            printToFile();
            callback.execute();
            return;
        }

        String url = urlList.get(urlIndex).toString();
        try {
            if (!url.startsWith("http")) {
                url = getRealUrl(url);
            }
            url = URLDecoder.decode(url, URL_ENCODING);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.printf("start url: (%d) %s \r\n", urlIndex, url);

        sleep(1000 + random.nextInt(Math.abs(waitTime - 1000)));
        String javascript = "var inputs=document.getElementsByTagName('input');"
                + "for(var i=0;i<inputs.length;i++){if(inputs[i].type=='text'&&inputs[i].name=='q'){inputs[i].value='\""
                + url + "\"';break;}}" + "document.getElementsByTagName('button')[0].click();";
        //        String javascript = "document.getElementsByName('q')[0].value='" + url + "';"
        //                + "document.getElementsByName('btnG')[0].click();";
        executeJavascript(javascript);
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

                if (webBrowser.getLoadingProgress() == 100) {
                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            String location = getLocation();
                            updateLocation(location);
                            //                            System.out.println("--> " + location);

                            if (location.equals(GOOGLE_URL)) {
                                Object hasFehl = executeJavascriptWithResult("return document.getElementById('fehl')!=null");
                                if (Boolean.parseBoolean(hasFehl.toString())) {
                                    executeJavascript("document.getElementById('fehl').click()");
                                    return;
                                }

                                if (status == Status.UNSTARRED) {
                                    status = Status.KEYWORD_SEARCHING;
                                    searchKeywordInMainPage(keyword);
                                }
                            } else if (location.startsWith(GOOGLE_HK_URL)) {
                                //                                webBrowser.navigate(GOOGLE_URL);
                                executeJavascript("document.getElementById('fehl').click()");

                            } else if (location.startsWith("www.google.com/sorry/?continue=")) {
                                //                                webBrowser.stopLoading();
                                alert();
                            } else if (location.startsWith("www.google.com/search")) {
                                //                                sleep(1000);
                                try {
                                    switch (status) {
                                        case KEYWORD_SEARCHING:
                                            List<String> urls = collectUrls();
                                            urlList.addAll(urls);
                                            if (urlList.size() < URL_TO_SEARCH_COUNT && !meetEnd() && !urls.isEmpty()) {
                                                executeJavascript(nextPageJs);
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
                                                if (!url.startsWith("http")) {
                                                    try {
                                                        url = getRealUrl(url);
                                                    } catch (UnsupportedEncodingException e1) {
                                                        e1.printStackTrace();
                                                    }
                                                }

                                                String[] parts = url.split("/", 4);
                                                if (parts.length < 3) {
                                                    System.out.println(url);
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
                                                executeJavascript(nextPageJs);
                                            }
                                            break;
                                        default:
                                            break;
                                    }
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    //                                    webBrowser.reloadPage();
                                    restartPage();
                                }
                            }
                        }
                    }).start();
                }
            }
        };
    }
}
