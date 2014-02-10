package cn.dehui.task.browser.search.uithread.controller.google;

import java.awt.AWTException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
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
import cn.dehui.task.browser.search.util.DMDLLV2.FastVerCode;
import cn.dehui.task.browser.search.util.Status;
import cn.dehui.task.browser.search.util.Utils;

public abstract class GoogleController extends Controller {

    private static final String   GET_ELEMENT_BY_TEXT_FUNC    = "function getElementByText(tagName, text){"
                                                                      + "var tags=document.getElementsByTagName(tagName);"
                                                                      + "for(var i=0;i<tags.length;i++){if(tags[i].innerHTML==text){return tags[i];}}"
                                                                      + "return null;" + "}";

    private static String         captchaPassword;

    private static String         captchaUserName;

    public static final String    HTTPS_GOOGLE_URL            = "https://www.google.com/";

    public static final String    HTTP_GOOGLE_URL             = "http://www.google.com/";

    protected static final int    PROGRESS                    = 95;

    protected static final String URL_ENCODING                = "utf8";

    private static final String   submitFormJs                = "document.getElementsByTagName('form')[0].submit();";

    private static final String   inputKeywordInMainPageJsTpl = "var is=document.getElementsByTagName('input');for(var i=0;i<is.length;i++){if(is[i].type=='text'){is[i].value='%s';}}";

    private static final String   inputCaptchaJsTpl           = "document.getElementById('captcha').value='%s';";

    private static final String   clickSubmitBtnJs            = "document.getElementsByName('submit')[0].click()";

    private static final String   isRedirectPageJs            = "return document.body.innerHTML.indexOf('Redirecting')==0;";

    private static final String   searchResultOuterHtmlJs     = "return document.getElementById('search').outerHTML;";

    private static final String   nextBtnInnerHtmlJs          = "return document.getElementById('pnnext').innerHTML;";

    //    private static final String   clickGoogleDotComLinkJs     = "document.getElementById('fehl').click()";
    private static final String   clickGoogleDotComLinkJs     = "getElementByText('a','Google.com in English').click()";

    //    private static final String   googleDotComLinkExistJs     = "return document.getElementById('fehl')!=null";
    private static final String   googleDotComLinkExistJs     = "return getElementByText('a','Google.com in English')!=null";

    protected static final String nextPageJs                  = "document.getElementById('pnnext').click();";

    private static final String   getNextPageUrlJs            = "return document.getElementById('pnnext').href;";

    protected Status              status                      = Status.UNSTARRED;

    protected SearchContext       searchContext;

    protected Callback<Void>      callback;

    protected long                timestamp;

    private String                lastSearchUrl;

    private static Robot          robot;

    private String                workerName                  = null;

    static {
        try {
            robot = new Robot();

            Properties properties = new Properties();
            properties.load(new FileInputStream("config.ini"));

            captchaUserName = properties.getProperty("captcha.username", "baby2321");
            captchaPassword = properties.getProperty("captcha.password", "1234567abc");
        } catch (AWTException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
                && (HTTPS_GOOGLE_URL.equals(newResourceLocation)
                        || newResourceLocation.contains(".google.com/sorry/") || newResourceLocation
                            .startsWith(HTTPS_GOOGLE_URL + "search?"));
    }

    @Override
    protected void handle(WebBrowserNavigationEvent e) throws InterruptedException, InvocationTargetException {
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                String location = getWebBrowser().getResourceLocation();

                // detect duplicate entry
                if (!location.equals(HTTPS_GOOGLE_URL)) {
                    if (location.equals(lastSearchUrl)) {
                        System.err.println("old location: " + lastSearchUrl);
                        System.err.println("new location: " + location);
                        return;
                    }

                    if (location.startsWith(HTTPS_GOOGLE_URL + "search?") && lastSearchUrl != null
                            && lastSearchUrl.startsWith(HTTPS_GOOGLE_URL + "search?")
                            && isOldStart(location, lastSearchUrl)) {
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
                if (location.equals(HTTPS_GOOGLE_URL)) {
                    workerName = null;
                    handleEnterGoogleSite(getWebBrowser());
                } else if (location.contains(".google.com/sorry/")) {
                    if ((Boolean) getWebBrowser().executeJavascriptWithResult(isRedirectPageJs)) {
                        return;
                    }

                    if (workerName != null) {
                        System.out.println("report captcha error: " + workerName);
                        FastVerCode.INSTANCE.ReportError(captchaUserName, workerName);
                    }
                    handleCaptcha();
                } else if (location.startsWith(HTTPS_GOOGLE_URL + "search?")) {
                    workerName = null;
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
        webBrowser.executeJavascript(GET_ELEMENT_BY_TEXT_FUNC);
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

    protected void handleCaptcha() {

        Point browserLocation = webBrowser.getNativeComponent().getLocationOnScreen();
        Rectangle rect = new Rectangle((int) (browserLocation.getX()) + 30, (int) (browserLocation.getY()) + 113, 202,
                72);
        BufferedImage img = robot.createScreenCapture(rect);
        //        System.out.println("img done");
        try {
            //            File captchaFile = new File("google_search_captcha.png");
            //            ImageIO.write(img, "png", captchaFile);

            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            ImageIO.write(img, "jpg", buf);
            byte[] data = buf.toByteArray();

            String[] parts = null;
            do {
                System.out.println("Getting captcha...");
                String result = FastVerCode.INSTANCE.RecByte(data, data.length, captchaUserName, captchaPassword);

                if (result == null) {
                    continue;
                }

                if ("No Money!".equals(result)) {
                    JOptionPane.showMessageDialog(webBrowser, "后台没点数了", "后台没点数了", JOptionPane.ERROR_MESSAGE);
                    return;
                } else if ("No Reg!".equals(result)) {
                    JOptionPane.showMessageDialog(webBrowser, "验证码账号没注册", "验证码账号没注册", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                parts = result.split("\\|!\\|", 2);
            } while (parts == null || parts.length < 2);
            String captcha = parts[0];
            workerName = parts[1];
            System.out.printf("captcha: %s, worker: %s\r\n", captcha, workerName);

            submitCaptcha(captcha);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //        webBrowser.executeJavascript("alert('请输入验证码')");
        //        try {
        //            Process p = Runtime.getRuntime().exec("alert.bat");
        //            int exitValue = p.waitFor();
        //            //            System.out.println("exitValue: " + exitValue);
        //        } catch (Exception e1) {
        //            e1.printStackTrace();
        //        }
    }

    private void submitCaptcha(String captcha) {
        getWebBrowser().executeJavascript(String.format(inputCaptchaJsTpl, captcha));
        sleep(100);
        getWebBrowser().executeJavascript(clickSubmitBtnJs);
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
        getWebBrowser().executeJavascript(submitFormJs);
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
        webBrowser.navigate(HTTPS_GOOGLE_URL);
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

    public static void main(String[] args) {
        String s = "kiests|!|87325570&dmhd25";
        String[] parts = s.split("\\|!\\|");
        for (String ss : parts) {
            System.out.println(ss);
        }
    }
}
