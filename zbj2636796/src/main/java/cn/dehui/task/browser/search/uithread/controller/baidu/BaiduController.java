package cn.dehui.task.browser.search.uithread.controller.baidu;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.TableColumn;
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

public abstract class BaiduController extends Controller {

    protected static final String BAIDU_URL           = "http://www.baidu.com/";

    protected static final String GB2312              = "GB2312";

    protected static final String UTF8                = "UTF8";

    protected static final int    URL_TO_SEARCH_COUNT = 10;

    protected static String       nextPageJs          = "var as=document.getElementsByTagName('a');for(var i=as.length-1;i>=0;i--){if(as[i].innerHTML=='下一页&gt;'){as[i].click();break;}}";

    protected Status              status              = Status.UNSTARRED;

    private String                lastSearchUrl       = null;

    protected long                timestamp;

    protected SearchContext       searchContext;

    protected Callback<Void>      callback;

    public BaiduController(ControllerManager controllerManager) {
        super(controllerManager);
    }

    protected boolean meetEnd() {
        String js = "var as=document.getElementsByTagName('a');for(var i=as.length-1;i>=0;i--){if(as[i].innerHTML=='下一页&gt;') return false;}return true;";
        Object o = webBrowser.executeJavascriptWithResult(js);
        return Boolean.parseBoolean(o.toString());
    }

    protected String getRealUrl(String url) {
        String redirectUrl = getRedirectUrl(url);

        try {
            String decode = URLDecoder.decode(redirectUrl, GB2312);
            if (isMessyCode(decode)) {
                decode = URLDecoder.decode(redirectUrl, UTF8);
            }
            return decode;
        } catch (Exception e) {
            //            e.printStackTrace();
            return redirectUrl;
        }
    }

    private String getRedirectUrl(String url) {
        HttpURLConnection con = null;
        do {
            try {
                con = (HttpURLConnection) (new URL(url).openConnection());
                con.setInstanceFollowRedirects(false);
                con.connect();
                int responseCode = con.getResponseCode();
                if (responseCode == 302) {
                    return con.getHeaderField("Location");
                } else {
                    return url;
                }
            } catch (Exception e) {
                //                System.err.printf("URL redirect error, retry: %s\r\n", url);
            } finally {
                if (con != null) {
                    try {
                        con.getInputStream().close();
                    } catch (IOException e) {
                        //                        System.err.println("Connection close error");
                    }
                    con.disconnect();
                }
            }
        } while (true);
    }

    protected List<String> collectUrls() {
        String js = "return document.getElementById('content_left').outerHTML;";
        String divString = (String) webBrowser.executeJavascriptWithResult(js);

        if (divString == null) {
            return new ArrayList<String>();
        }

        try {
            Parser parser = new Parser(divString);

            TagNameFilter tableTagFilter = new TagNameFilter("table");

            HasAttributeFilter hasTplFilter = new HasAttributeFilter("tpl", "se_st_default");

            NodeList tableList = parser.extractAllNodesThatMatch(new AndFilter(new NodeFilter[] { tableTagFilter,
                    hasTplFilter }));

            ExtractUrlVistor v = new ExtractUrlVistor();
            for (int j = 0; j < tableList.size(); j++) {
                tableList.elementAt(j).accept(v);
            }

            return v.urlList;

        } catch (ParserException e) {
            e.printStackTrace();

            return new ArrayList<String>();
        }

    }

    protected void searchKeywordInMainPage(String keyword) {
        webBrowser.executeJavascript("document.getElementById('kw').value='" + keyword + "';");
        sleep(500);
        webBrowser.executeJavascript("document.getElementById('su').click();");
    }

    @Override
    public void run() {
        super.run();
        timestamp = System.currentTimeMillis();
        webBrowser.stopLoading();
        webBrowser.navigate(BAIDU_URL);
    }

    @Override
    public void stop() {
        status = Status.STOPPED;
        webBrowser.stopLoading();
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setAction(Callback callback) {
        this.callback = callback;
    }

    class ExtractUrlVistor extends NodeVisitor {

        static final int MEET_TD = 1;

        static final int MEET_H3 = 2;

        List<String>     urlList = new ArrayList<String>();

        int              status  = 0;

        /**
         * Called for each <code>Tag</code> visited.
         * @param tag The tag being visited.
         */
        @Override
        public void visitTag(Tag tag) {
            if (tag instanceof TableColumn) {
                status = MEET_TD;
            } else if (status == MEET_TD && tag.getTagName().toLowerCase().equals("h3")) {
                status = MEET_H3;
            } else if (status == MEET_H3 && tag instanceof LinkTag) {
                String link = ((LinkTag) tag).extractLink();
                //                System.out.println(link);
                urlList.add(getRealUrl(link));
                status = 0;
            }
        }
    }

    /**
     * 判断是否为乱码
     *
     * @param str
     * @return
     */
    public static boolean isMessyCode(String str) {
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            // 当从Unicode编码向某个字符集转换时，如果在该字符集中没有对应的编码，则得到0x3f（即问号字符?）
            //从其他字符集向Unicode编码转换时，如果这个二进制数在该字符集中没有标识任何的字符，则得到的结果是0xfffd
            //System.out.println("--- " + (int) c);
            if (c == 0xfffd) {
                // 存在乱码
                //System.out.println("存在乱码 " + (int) c);
                return true;
            }
        }
        return false;
    }

    @Override
    public void research() {
        stop();
        System.out.printf("Re-searching keyword: %s.", searchContext.keyword);
        searchContext.clear();
        setStatus(Status.UNSTARRED);
        run();
    }

    @Override
    public String getLastSearchUrl() {
        return lastSearchUrl;
    }

    @Override
    protected void handle(WebBrowserNavigationEvent e) throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                String location = getWebBrowser().getResourceLocation();

                // detect duplicate entry
                if (!location.equals(BAIDU_URL)) {
                    if (location.equals(lastSearchUrl)) {
                        System.err.println("old location: " + lastSearchUrl);
                        System.err.println("new location: " + location);
                        return;
                    }

                    //                    if (location.startsWith(GOOGLE_URL + "search?") && lastSearchUrl != null
                    //                            && lastSearchUrl.startsWith(GOOGLE_URL + "search?") && isOldStart(location, lastSearchUrl)) {
                    //                        System.err.println("old location: " + lastSearchUrl);
                    //                        System.err.println("new location: " + location);
                    //                        return;
                    //                    }
                }
                lastSearchUrl = location;
                if (location.equals(BAIDU_URL)) {
                    enterMainPage();
                } else if (location.startsWith("http://www.baidu.com/s")) {
                    sleep(1000);
                    webBrowser.stopLoading();
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

    protected void enterMainPage() {
        if (status == Status.UNSTARRED) {
            status = Status.KEYWORD_SEARCHING;
            searchKeywordInMainPage(searchContext.keyword);
        }
    }

    protected abstract void handleSearchResult(final JWebBrowser webBrowser);

    @Override
    protected boolean isWantedLocation(String newResourceLocation) {
        return true;
    }

    public void setSearchContext(SearchContext searchContext) {
        this.searchContext = searchContext;
    }

    public SearchContext getSearchContext() {
        return searchContext;
    }

    public static void main(String[] args) throws MalformedURLException, IOException {
        String url = "http://www.baidu.com/link?url=PJRuW5N7IO_XNFkAsWPoMno5z2bwn1rHrQkJSoR9D0lU9KF-t-UKhw_p_rvNwdVJviJTnH9PcbkrYoEpMg2pzzzaAPCv-6VrkWABkvqSME7";
        HttpURLConnection con = (HttpURLConnection) (new URL(url).openConnection());
        con.setInstanceFollowRedirects(false);
        con.connect();
        int responseCode = con.getResponseCode();
        System.out.println(responseCode);
        String location = con.getHeaderField("Location");
        System.out.println(location);

        con.getInputStream().close();
        con.disconnect();

        //        System.out.println(URLDecoder.decode(location, "GB2312"));
        //        System.out.println(isMessyCode(URLDecoder.decode(location, "GB2312")));
        System.out.println(URLDecoder.decode(location, "utf8"));
        System.out.println(isMessyCode(URLDecoder.decode(location, "utf8")));

        System.out.println(URLDecoder.decode("http://www.xici.net/t_%BB%E9%C7%EC%B2%DF%BB%AE.htm", "GB2312"));
        System.out.println(isMessyCode(URLDecoder
                .decode("http://www.xici.net/t_%BB%E9%C7%EC%B2%DF%BB%AE.htm", "GB2312")));
        System.out.println(URLDecoder.decode("http://www.xici.net/t_%BB%E9%C7%EC%B2%DF%BB%AE.htm", "utf8"));
        System.out
                .println(isMessyCode(URLDecoder.decode("http://www.xici.net/t_%BB%E9%C7%EC%B2%DF%BB%AE.htm", "utf8")));
    }
}
