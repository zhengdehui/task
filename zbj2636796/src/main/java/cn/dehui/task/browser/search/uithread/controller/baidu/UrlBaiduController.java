package cn.dehui.task.browser.search.uithread.controller.baidu;

import java.io.BufferedWriter;

import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import cn.dehui.task.browser.search.uithread.controller.manager.ControllerManager;
import cn.dehui.task.browser.search.uithread.controller.manager.IUrlController;

public class UrlBaiduController extends BaiduController implements IUrlController {

    protected int          maxUrlPerKeyword = URL_TO_SEARCH_COUNT;

    private BufferedWriter bw;

    public UrlBaiduController(ControllerManager controllerManager) {
        super(controllerManager);
    }

    //    private void printToFile() {
    //        try {
    //            String keywordToWrite = keyword.charAt(0) == '\"' && keyword.charAt(keyword.length() - 1) == '\"' ? keyword
    //                    : '\"' + keyword + '\"';
    //            bw.write(keywordToWrite);
    //            int len = Math.min(maxUrlForOneKeyword, urlList.size());
    //            for (int i = 0; i < len; i++) {
    //                String url = urlList.get(i).toString();
    //                url = getRealUrl(url);
    //                bw.write(",\"" + url + "\"");
    //            }
    //            bw.write("\r\n");
    //        } catch (IOException e) {
    //            e.printStackTrace();
    //        }
    //    }

    public void setBw(BufferedWriter bw) {
        this.bw = bw;
    }

    @Override
    public void setMaxUrlPerKeyword(int maxUrlPerKeyword) {
        this.maxUrlPerKeyword = maxUrlPerKeyword;
    }

    @Override
    protected void handleSearchResult(JWebBrowser webBrowser) {
        switch (status) {
            case KEYWORD_SEARCHING:
                searchContext.urlList.addAll(collectUrls());
                if (searchContext.urlList.size() < maxUrlPerKeyword && !meetEnd()) {
                    webBrowser.executeJavascript(nextPageJs);
                } else {
                    System.out.printf("Used time: %d ms\r\n", System.currentTimeMillis() - timestamp);
                    callback.execute();
                }
                break;
            default:
                break;
        }
    }
}
