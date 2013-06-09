package cn.dehui.task.browser.search.uithread.controller.google2;

import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import cn.dehui.task.browser.search.uithread.controller.ControllerManager;

public class UrlGoogleController2 extends GoogleController2 {

    protected int maxUrlPerKeyword;

    public UrlGoogleController2(ControllerManager controllerManager) {
        super(controllerManager);
    }

    @Override
    protected void handleSearchResult(final JWebBrowser webBrowser) {
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

    public void setMaxUrlPerKeyword(int maxUrlPerKeyword) {
        this.maxUrlPerKeyword = maxUrlPerKeyword;
    }
}
