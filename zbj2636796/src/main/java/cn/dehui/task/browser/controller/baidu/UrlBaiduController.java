package cn.dehui.task.browser.controller.baidu;

import java.io.BufferedWriter;
import java.io.IOException;

import javax.swing.SwingUtilities;

import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserAdapter;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserEvent;
import cn.dehui.task.browser.controller.Status;

public class UrlBaiduController extends BaiduController {

    private int            maxUrlForOneKeyword = URL_TO_SEARCH_COUNT;

    private BufferedWriter bw;

    public UrlBaiduController(final JWebBrowser webBrowser) {
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
                url = getRealUrl(url);
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
                                return;
                            } else if (progress == 100 && location.startsWith("http://www.baidu.com/s")) {
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException ex) {
                                    ex.printStackTrace();
                                }
                                webBrowser.stopLoading();
                                try {
                                    switch (status) {
                                        case KEYWORD_SEARCHING:
                                            urlList.addAll(collectUrls());
                                            if (urlList.size() < maxUrlForOneKeyword && !meetEnd()) {
                                                webBrowser.executeJavascript(nextPageJs);
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
                    });
                }
            }
        };
    }

    public void setMaxUrlForOneKeyword(int maxUrlForOneKeyword) {
        this.maxUrlForOneKeyword = maxUrlForOneKeyword;
    }
}
