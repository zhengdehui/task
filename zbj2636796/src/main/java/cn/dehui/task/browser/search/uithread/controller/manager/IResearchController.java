package cn.dehui.task.browser.search.uithread.controller.manager;

import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;

public interface IResearchController extends Runnable {

    void setWebBrowser(JWebBrowser webBrowser);

    void research();

    abstract String getLastSearchUrl();

    void stop();
}
