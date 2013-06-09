package cn.dehui.task.browser.search.uithread.controller.google;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.SwingUtilities;

import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.NotFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.Div;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.visitors.NodeVisitor;

import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserAdapter;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserListener;
import cn.dehui.task.browser.search.uithread.controller.util.Callback;
import cn.dehui.task.browser.search.uithread.controller.util.Status;

public abstract class GoogleController implements Runnable {

    protected static final String GOOGLE_URL          = "www.google.com/";

    protected static final String GOOGLE_HK_URL       = "www.google.com.hk/";

    protected static final int    URL_TO_SEARCH_COUNT = 10;

    protected static final int    PROGRESS            = 95;

    protected static final String URL_ENCODING        = "utf8";

    //    protected static String       getResultUrlsByVSPIBJs = "var urls=new Array();"
    //                                                                 + "var vspibs=document.getElementsByClassName('vspib');"
    //                                                                 + "for(var i=0;i<vspibs.length;i++){"
    //                                                                 + "if(vspibs[i].parentNode.parentNode.tagName.toLowerCase()=='li'){url=vspibs[i].parentNode.children[1].children[0].href;urls.push(url);}"
    //                                                                 + "}" + "return urls;";

    protected static String       nextPageJs          = "document.getElementById('pnnext').click();";

    protected JWebBrowser         webBrowser;

    protected Status              status              = Status.UNSTARRED;

    protected String              keyword;

    protected List<String>        urlList             = new ArrayList<String>();

    protected Callback            callback;

    protected WebBrowserAdapter   webBrowserAdapter;

    private TimerTask             timerTask;

    private Timer                 timer;

    private boolean               isScheduled         = false;

    private String                lastLocation        = null;

    private long                  lastChangeTime      = -1;

    public GoogleController(final JWebBrowser webBrowser) {
        this.webBrowser = webBrowser;

        webBrowserAdapter = getWebBrowserAdapter();
    }

    protected abstract WebBrowserAdapter getWebBrowserAdapter();

    protected void updateLocation(String location) {
        if (lastLocation != null) {
            if (!lastLocation.equals(location)) {
                lastChangeTime = System.currentTimeMillis();
            }
        }
        lastLocation = location;
    }

    protected boolean meetEnd() {
        String js = "return document.getElementById('pnnext').innerHTML;";
        Object o = webBrowser.executeJavascriptWithResult(js);
        return o == null;
    }

    public void initControl() {
        WebBrowserListener[] listeners = webBrowser.getWebBrowserListeners();
        for (WebBrowserListener l : listeners) {
            webBrowser.removeWebBrowserListener(l);
        }
        webBrowser.addWebBrowserListener(webBrowserAdapter);
    }

    protected void alert() {
        //        webBrowser.executeJavascript("alert('请输入验证码')");
        try {
            Process p = Runtime.getRuntime().exec("alert.bat");
            int exitValue = p.waitFor();
            System.out.println("exitValue: " + exitValue);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    protected String getRealUrl(String url) throws UnsupportedEncodingException {
        String[] urlParts = url.split("&");
        for (String part : urlParts) {
            int index = part.indexOf("url=");
            if (index != -1) {
                return URLDecoder.decode(part.substring(index + 4), URL_ENCODING);
            }
        }
        return "";
    }

    protected List<String> collectUrls() {
        String js = "return document.getElementById('search').outerHTML;";
        String divString = (String) webBrowser.executeJavascriptWithResult(js);

        if (divString == null) {
            return new ArrayList<String>();
        }

        try {
            Parser parser = new Parser(divString);

            TagNameFilter acriptTagFilter = new TagNameFilter("li");

            HasAttributeFilter hasIdFilter = new HasAttributeFilter("id");
            HasAttributeFilter hasClassFilter = new HasAttributeFilter("class", "g");

            NodeList liList = parser.extractAllNodesThatMatch(new AndFilter(new NodeFilter[] { acriptTagFilter,
                    new NotFilter(hasIdFilter), hasClassFilter }));

            ExtractUrlVistor v = new ExtractUrlVistor();
            for (int j = 0; j < liList.size(); j++) {
                liList.elementAt(j).accept(v);
            }

            return v.urlList;

        } catch (ParserException e) {
            e.printStackTrace();

            return new ArrayList<String>();
        }

    }

    protected void searchKeywordInMainPage(String keyword) {
        webBrowser
                .executeJavascript("var is=document.getElementsByTagName('input');for(var i=0;i<is.length;i++){if(is[i].type=='text'){is[i].value='"
                        + keyword + "';}}");
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        webBrowser.executeJavascript("document.getElementsByTagName('form')[0].submit();");
    }

    protected String getLocation() {
        return webBrowser.getResourceLocation().replaceAll("http[s]?://", "");
    }

    @Override
    public void run() {
        urlList.clear();
        webBrowser.stopLoading();
        clearSessionCookies();
        webBrowser.navigate("http://" + GOOGLE_HK_URL);

        if (!isScheduled) {
            timer = new Timer(true);
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    if (lastChangeTime > 0 && System.currentTimeMillis() - lastChangeTime > 60000
                            && lastLocation != null && !lastLocation.startsWith("http://www.google.com/sorry/")) {
                        restartPage();
                    }
                }
            };
            timer.schedule(timerTask, 1000, 5000);
            isScheduled = true;
        }
    }

    protected void restartPage() {
        System.out.println("researching keyword: " + keyword);
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                stop();
                initControl();
                setStatus(Status.UNSTARRED);
                lastChangeTime = System.currentTimeMillis();
                GoogleController.this.run();
            }
        });
    }

    public void stop() {
        if (isScheduled) {
            timerTask.cancel();
            timer.cancel();
            isScheduled = false;
        }
        WebBrowserListener[] listeners = webBrowser.getWebBrowserListeners();
        for (WebBrowserListener l : listeners) {
            webBrowser.removeWebBrowserListener(l);
        }
        webBrowser.stopLoading();
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public void setAction(Callback callback) {
        this.callback = callback;
    }

    static class ExtractUrlVistor extends NodeVisitor {

        static final int MEET_DIV = 1;

        static final int MEET_H3  = 2;

        List<String>     urlList  = new ArrayList<String>();

        int              status   = 0;

        /**
         * Called for each <code>Tag</code> visited.
         * @param tag The tag being visited.
         */
        @Override
        public void visitTag(Tag tag) {
            if (tag instanceof Div && "rc".equals(tag.getAttribute("class"))) {
                status = MEET_DIV;
            } else if (status == MEET_DIV && tag.getTagName().toLowerCase().equals("h3")) {
                status = MEET_H3;
            } else if (status == MEET_H3 && tag instanceof LinkTag) {
                urlList.add(((LinkTag) tag).extractLink());
                status = 0;
            }
        }
    }

    public static void clearSessionCookies() {
        Process p;
        try {
            p = Runtime.getRuntime().exec(
                    new String[] { "cmd", "/c", "RunDll32.exe InetCpl.cpl,ClearMyTracksByProcess 10" });
            int exitValue = p.waitFor();
            System.out.print("clearSessionCookies " + (exitValue == 0 ? "succeed. " : "fail. "));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        clearSessionCookies();
    }

    public void setWebBrowser(JWebBrowser webBrowser) {
        this.webBrowser = webBrowser;
    }
}
