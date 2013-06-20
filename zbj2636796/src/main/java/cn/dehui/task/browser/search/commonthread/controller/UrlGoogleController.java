package cn.dehui.task.browser.search.commonthread.controller;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URLDecoder;

import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserAdapter;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserEvent;
import cn.dehui.task.browser.search.util.Status;

public class UrlGoogleController extends GoogleController {

    private int            maxUrlForOneKeyword = URL_TO_SEARCH_COUNT;

    private BufferedWriter bw;

    public UrlGoogleController(final JWebBrowser webBrowser) {
        super(webBrowser);
    }

    private void printToFile() {
        try {
            String keywordToWrite = keyword.charAt(0) == '\"' && keyword.charAt(keyword.length() - 1) == '\"' ? keyword
                    : '\"' + keyword + '\"';
            bw.write(keywordToWrite);
            int len = Math.min(maxUrlForOneKeyword, urlList.size());
            for (int i = 0; i < len; i++) {
                String url = urlList.get(i).toString();
                try {
                    if (!url.startsWith("http")) {
                        url = getRealUrl(url);
                    }
                    url = URLDecoder.decode(url, URL_ENCODING);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                bw.write(",\"" + url + "\"");
            }
            bw.write("\r\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setBw(BufferedWriter bw) {
        this.bw = bw;
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
                                return;
                            } else if (location.startsWith(GOOGLE_HK_URL)) {
                                navigate("http://" + GOOGLE_URL);
                                return;
                            } else if (location.startsWith("www.google.com/sorry/?continue=")) {
                                alert();
                            } else if (location.startsWith("www.google.com/search")) {
                                try {
                                    switch (status) {
                                        case KEYWORD_SEARCHING:
                                            urlList.addAll(collectUrls());
                                            if (urlList.size() < maxUrlForOneKeyword && !meetEnd()) {
                                                executeJavascript(nextPageJs);
                                            } else {
                                                printToFile();
                                                callback.execute();
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

                    }).start();
                }
            }
        };
    }

    public void setMaxUrlForOneKeyword(int maxUrlForOneKeyword) {
        this.maxUrlForOneKeyword = maxUrlForOneKeyword;
    }

    public static void main(String[] args) {
        System.out.println("https://www.google.com/dfadsfgfdg".replaceAll("https?://", ""));
        System.out.println("http://www.google.com/dfadsfgfdg".replaceAll("https?://", ""));
        System.out.println("http://www.google.com/".replaceAll("https?://", ""));
        System.out.println("http://www.google.com".replaceAll("https?://", ""));

    }
}
