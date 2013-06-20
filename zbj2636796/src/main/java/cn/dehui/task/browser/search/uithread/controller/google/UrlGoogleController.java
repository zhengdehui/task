package cn.dehui.task.browser.search.uithread.controller.google;

import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import cn.dehui.task.browser.search.uithread.controller.manager.ControllerManager;
import cn.dehui.task.browser.search.uithread.controller.manager.IUrlController;

public class UrlGoogleController extends GoogleController implements IUrlController {

    protected int maxUrlPerKeyword;

    public UrlGoogleController(ControllerManager controllerManager) {
        super(controllerManager);
    }

    @Override
    protected void handleSearchResult(final JWebBrowser webBrowser) {
        switch (status) {
            case KEYWORD_SEARCHING:
                searchContext.urlList.addAll(collectUrls());
                if (searchContext.urlList.size() < maxUrlPerKeyword && !meetEnd()) {
                    //                    webBrowser.executeJavascript(nextPageJs);
                    nextPage();
                } else {
                    System.out.printf("Used time: %d ms\r\n", System.currentTimeMillis() - timestamp);
                    callback.execute();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void setMaxUrlPerKeyword(int maxUrlPerKeyword) {
        this.maxUrlPerKeyword = maxUrlPerKeyword;
    }
}
