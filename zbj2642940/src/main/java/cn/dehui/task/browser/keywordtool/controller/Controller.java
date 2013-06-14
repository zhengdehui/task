package cn.dehui.task.browser.keywordtool.controller;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.swing.SwingUtilities;

import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserAdapter;
import cn.dehui.task.browser.keywordtool.controller.util.Callback;
import cn.dehui.task.browser.keywordtool.controller.util.Status;

/**
 * @author Christopher Deckers
 */
public abstract class Controller implements Runnable {

    protected Status            status     = Status.UNSTARRED;

    protected final JWebBrowser webBrowser = new JWebBrowser();

    protected List<String>      keywordList;

    protected int               keywordIndex;

    protected boolean           started    = false;

    protected String            password   = "19861103";

    protected String            username   = "dhzheng3@gmail.com";

    protected File              outputFolder;

    protected boolean           debug      = false;

    public Controller() {
        webBrowser.setMenuBarVisible(false);
        webBrowser.addWebBrowserListener(getWebBrowserListener());
    }

    protected abstract WebBrowserAdapter getWebBrowserListener();

    protected Object executeJavascriptWithResult(final String js) {

        WithResultRunnable<Object> runnable = new WithResultRunnable<Object>(new Callback<Object>() {
            @Override
            public Object execute() {
                return webBrowser.executeJavascriptWithResult(js);
            }
        });
        try {
            SwingUtilities.invokeAndWait(runnable);
            return runnable.result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    protected void executeJavascript(final String js) {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    webBrowser.executeJavascript(js);
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    protected static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        //        clearSessionCookies();
        started = true;
        webBrowser.navigate("http://cas.baidu.com/?tpl=www2&fromu=http%3A%2F%2Fwww2.baidu.com%2F");
    }

    public void stop() {
        started = false;
        webBrowser.stopLoading();
    }

    public JWebBrowser getWebBrowser() {
        return webBrowser;
    }

    public void setKeywordList(List<String> keywordList) {
        this.keywordList = keywordList;
        keywordIndex = 0;
        status = Status.UNSTARRED;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTitle() {
        return "Adwords";
    }

    public void setOutputFolder(File outputFolder) {
        this.outputFolder = outputFolder;
    }

    static class WithResultRunnable<T> implements Runnable {
        T           result;

        Callback<T> callback;

        WithResultRunnable(Callback<T> callback) {
            this.callback = callback;
        }

        @Override
        public void run() {
            result = callback.execute();
        }
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public static void clearSessionCookies() {
        try {
            Process p = Runtime.getRuntime().exec(
                    new String[] { "cmd", "/c", "RunDll32.exe InetCpl.cpl,ClearMyTracksByProcess 10" });
            int exitValue = p.waitFor();
            System.out.println("clearSessionCookies " + (exitValue == 0 ? "succeed" : "fail"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
