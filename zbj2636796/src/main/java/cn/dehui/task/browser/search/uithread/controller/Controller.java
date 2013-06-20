package cn.dehui.task.browser.search.uithread.controller;

import javax.swing.SwingUtilities;

import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserAdapter;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserListener;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserNavigationEvent;
import cn.dehui.task.browser.search.uithread.controller.manager.ControllerManager;
import cn.dehui.task.browser.search.uithread.controller.manager.IResearchController;
import cn.dehui.task.browser.search.util.Callback;
import cn.dehui.task.browser.search.util.WithResultRunnable;

public abstract class Controller implements Runnable, IResearchController {

    protected ControllerManager controllerManager;

    protected JWebBrowser       webBrowser;

    private WebBrowserAdapter   webBrowserAdapter;

    public Controller(ControllerManager controllerManager) {
        this.controllerManager = controllerManager;

        webBrowserAdapter = getWebBrowserAdapter();
    }

    private WebBrowserAdapter getWebBrowserAdapter() {
        return new WebBrowserAdapter() {
            @Override
            public void locationChanged(final WebBrowserNavigationEvent e) {
                super.locationChanged(e);
                beatHeart();
                String newResourceLocation = e.getNewResourceLocation();
                //                System.out.println(newResourceLocation);

                if (!isWantedLocation(newResourceLocation)) {
                    return;
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        boolean pageLoadComplete = false;
                        int count = 0;

                        do {
                            sleep(100);
                            if (getLoadingProgress() == 100) {
                                pageLoadComplete = true;
                                break;
                            }
                        } while (!pageLoadComplete && count++ < 50);

                        if (!pageLoadComplete) {
                            System.err.println("pageLoad timeout");
                        }
                        try {
                            handle(e);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }

                }).start();
            }
        };
    }

    private int getLoadingProgress() {
        WithResultRunnable<Integer> runnable = new WithResultRunnable<Integer>(new Callback<Integer>() {
            @Override
            public Integer execute() {
                return webBrowser.getLoadingProgress();
            }
        });
        try {
            SwingUtilities.invokeAndWait(runnable);
            return runnable.result;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    protected abstract void handle(WebBrowserNavigationEvent e) throws Exception;

    protected abstract boolean isWantedLocation(String newResourceLocation);

    @Override
    public void setWebBrowser(JWebBrowser webBrowser) {
        this.webBrowser = webBrowser;

        WebBrowserListener[] listeners = webBrowser.getWebBrowserListeners();
        for (WebBrowserListener l : listeners) {
            webBrowser.removeWebBrowserListener(l);
        }

        webBrowser.addWebBrowserListener(webBrowserAdapter);
    }

    public JWebBrowser getWebBrowser() {
        return webBrowser;
    }

    protected static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        beatHeart();
    }

    private void beatHeart() {
        controllerManager.setLastBeatHeartTime(System.currentTimeMillis());
    }
}
