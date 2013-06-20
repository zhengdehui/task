package cn.dehui.task.browser.search.uithread.controller.google;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

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
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserNavigationEvent;
import cn.dehui.task.browser.search.uithread.controller.Controller;
import cn.dehui.task.browser.search.uithread.controller.SearchContext;
import cn.dehui.task.browser.search.uithread.controller.manager.ControllerManager;
import cn.dehui.task.browser.search.util.Callback;
import cn.dehui.task.browser.search.util.Status;
import cn.dehui.task.browser.search.util.Utils;

public abstract class GoogleController extends Controller {

    protected static final String GOOGLE_URL                  = "http://www.google.com/";

    protected static final String GOOGLE_HK_URL               = "http://www.google.com.hk/";

    protected static final int    PROGRESS                    = 95;

    protected static final String URL_ENCODING                = "utf8";

    private static final String   clickSearchBtnJs            = "document.getElementsByTagName('form')[0].submit();";

    private static final String   inputKeywordInMainPageJsTpl = "var is=document.getElementsByTagName('input');for(var i=0;i<is.length;i++){if(is[i].type=='text'){is[i].value='%s';}}";

    private static final String   searchResultOuterHtmlJs     = "return document.getElementById('search').outerHTML;";

    private static final String   nextBtnInnerHtmlJs          = "return document.getElementById('pnnext').innerHTML;";

    private static final String   clickGoogleDotComLinkJs     = "document.getElementById('fehl').click()";

    private static final String   googleDotComLinkExistJs     = "return document.getElementById('fehl')!=null";

    protected static final String nextPageJs                  = "document.getElementById('pnnext').click();";

    private static final String   getNextPageUrlJs            = "return document.getElementById('pnnext').href;";

    protected Status              status                      = Status.UNSTARRED;

    protected SearchContext       searchContext;

    protected Callback<Void>      callback;

    protected long                timestamp;

    private String                lastSearchUrl;

    //    private BufferedWriter        bw;

    public GoogleController(ControllerManager controllerManager) {
        super(controllerManager);

        //        try {
        //            bw = new BufferedWriter(new FileWriter("debug.log"));
        //        } catch (Exception e) {
        //            e.printStackTrace();
        //        }
    }

    @Override
    protected boolean isWantedLocation(String newResourceLocation) {
        return newResourceLocation != null
                && (GOOGLE_URL.equals(newResourceLocation)
                        || newResourceLocation.startsWith(GOOGLE_URL + "sorry/?continue=") || newResourceLocation
                            .startsWith(GOOGLE_URL + "search?"));
    }

    @Override
    protected void handle(WebBrowserNavigationEvent e) throws InterruptedException, InvocationTargetException {
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                String location = getWebBrowser().getResourceLocation();

                // detect duplicate entry
                if (!location.equals(GOOGLE_URL)) {
                    if (location.equals(lastSearchUrl)) {
                        System.err.println("old location: " + lastSearchUrl);
                        System.err.println("new location: " + location);
                        return;
                    }

                    if (location.startsWith(GOOGLE_URL + "search?") && lastSearchUrl != null
                            && lastSearchUrl.startsWith(GOOGLE_URL + "search?") && isOldStart(location, lastSearchUrl)) {
                        System.err.println("old location: " + lastSearchUrl);
                        System.err.println("new location: " + location);
                        return;
                    }
                }
                lastSearchUrl = location;
                //                            try {
                //                                bw.write(progress + " - " + location + "\r\n");
                //                                bw.flush();
                //                            } catch (IOException e) {
                //                                e.printStackTrace();
                //                            }
                if (location.equals(GOOGLE_URL)) {
                    handleEnterGoogleSite(getWebBrowser());
                } else if (location.startsWith(GOOGLE_URL + "sorry/?continue=")) {
                    alert();
                } else if (location.startsWith(GOOGLE_URL + "search?")) {
                    sleep(1000);
                    try {
                        handleSearchResult(getWebBrowser());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        research();
                    }
                }
            }
        });
    }

    private boolean isOldStart(String location, String lastSearchUrl) {
        String locationStart = getStart(location);
        String lastSearchUrlStart = getStart(lastSearchUrl);
        int newStart = Integer.parseInt(locationStart);
        return newStart > 0 && newStart <= Integer.parseInt(lastSearchUrlStart);
    }

    private String getStart(String location) {
        int index = location.indexOf("?");

        String paramStr = location.substring(index + 1);

        String[] params = paramStr.split("&");
        for (String param : params) {
            if (param.startsWith("start=")) {
                return param.substring("start=".length());
            }
        }
        return "0";
    }

    protected abstract void handleSearchResult(final JWebBrowser webBrowser);

    protected void handleEnterGoogleSite(final JWebBrowser webBrowser) {
        //        System.out.println("handleEnterGoogleSite");
        webBrowser.stopLoading();
        Object hasFehl = webBrowser.executeJavascriptWithResult(googleDotComLinkExistJs);
        if (Boolean.parseBoolean(hasFehl.toString())) {
            webBrowser.executeJavascript(clickGoogleDotComLinkJs);

            //            String addr = (String) webBrowser
            //                    .executeJavascriptWithResult("return document.getElementById('fehl').href;");
            //            System.out.println(addr);
            //            webBrowser.navigate(addr);
            return;
        }

        if (status == Status.UNSTARRED) {
            status = Status.KEYWORD_SEARCHING;
            //            System.out.println("start searching...");
            searchKeywordInMainPage(searchContext.keyword);
        }
    }

    protected boolean meetEnd() {
        return getWebBrowser().executeJavascriptWithResult(nextBtnInnerHtmlJs) == null;
    }

    protected void alert() {
        //        webBrowser.executeJavascript("alert('请输入验证码')");
        try {
            Process p = Runtime.getRuntime().exec("alert.bat");
            int exitValue = p.waitFor();
            //            System.out.println("exitValue: " + exitValue);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    protected List<String> collectUrls() {
        //        long timestamp = System.currentTimeMillis();
        String divString = (String) getWebBrowser().executeJavascriptWithResult(searchResultOuterHtmlJs);

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

            // get redirect urls
            for (int i = 0; i < v.urlList.size(); i++) {
                String url = v.urlList.get(i);
                if (!url.startsWith("http")) {
                    try {
                        url = Utils.getRealUrl(url);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                v.urlList.set(i, url);
            }

            //            System.out.println("collectUrls used time: " + (System.currentTimeMillis() - timestamp));
            return v.urlList;

        } catch (ParserException e) {
            e.printStackTrace();
            return new ArrayList<String>();
        }

    }

    protected void searchKeywordInMainPage(String keyword) {
        getWebBrowser().executeJavascript(String.format(inputKeywordInMainPageJsTpl, keyword.replaceAll("'", "\\\\'")));
        sleep(500);
        getWebBrowser().executeJavascript(clickSearchBtnJs);
    }

    protected String getLocation() {
        String location = getWebBrowser().getResourceLocation();
        return location == null ? null : location.replaceAll("http[s]?://", "http://");
    }

    protected void nextPage() {
        JWebBrowser webBrowser = getWebBrowser();
        String nextPageUrl = (String) webBrowser.executeJavascriptWithResult(getNextPageUrlJs);
        if (nextPageUrl != null) {
            webBrowser.navigate(nextPageUrl);
        } else {
            webBrowser.executeJavascript(nextPageJs);
        }
    }

    @Override
    public void run() {
        super.run();
        timestamp = System.currentTimeMillis();
        webBrowser.stopLoading();
        webBrowser.navigate(GOOGLE_URL);
    }

    @Override
    public void stop() {
        status = Status.STOPPED;
        webBrowser.stopLoading();
        //        WebBrowserListener[] listeners = webBrowser.getWebBrowserListeners();
        //        for (WebBrowserListener l : listeners) {
        //            webBrowser.removeWebBrowserListener(l);
        //        }
    }

    public void setAction(Callback<Void> callback) {
        this.callback = callback;
    }

    public void setSearchContext(SearchContext searchContext) {
        this.searchContext = searchContext;
    }

    public SearchContext getSearchContext() {
        return searchContext;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    static class ExtractUrlVistor extends NodeVisitor {

        private static final String H3       = "h3";

        private static final String CLASS    = "class";

        private static final String RC       = "rc";

        static final int            MEET_DIV = 1;

        static final int            MEET_H3  = 2;

        List<String>                urlList  = new ArrayList<String>();

        int                         status   = 0;

        /**
         * Called for each <code>Tag</code> visited.
         * @param tag The tag being visited.
         */
        @Override
        public void visitTag(Tag tag) {
            if (tag instanceof Div && RC.equals(tag.getAttribute(CLASS))) {
                status = MEET_DIV;
            } else if (status == MEET_DIV && tag.getTagName().toLowerCase().equals(H3)) {
                status = MEET_H3;
            } else if (status == MEET_H3 && tag instanceof LinkTag) {
                urlList.add(((LinkTag) tag).extractLink());
                status = 0;
            }
        }
    }

    @Override
    public void research() {
        stop();

        System.out.printf("Re-searching keyword: %s.", searchContext.keyword);
        //        Utils.clearSessionCookies();
        searchContext.clear();
        setStatus(Status.UNSTARRED);
        run();
    }

    @Override
    public String getLastSearchUrl() {
        return lastSearchUrl;
    }
}
