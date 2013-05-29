package cn.dehui.task.browser.controller.google;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserEvent;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserListener;
import cn.dehui.task.browser.controller.Callback;
import cn.dehui.task.browser.controller.Status;

public class StatisticController2 implements Runnable {

    private static final String  GOOGLE_URL             = "http://www.google.com/";

    private static final String  GOOGLE_HK_URL          = "http://www.google.com.hk/";

    private static final int     URL_TO_SEARCH_COUNT    = 10;

    private static final String  UTF8                   = "utf8";

    static String                getResultUrlsByVSPIBJs = "var urls=new Array();"
                                                                + "var vspibs=document.getElementsByClassName('vspib');"
                                                                + "for(var i=0;i<vspibs.length;i++){"
                                                                + "if(vspibs[i].parentNode.parentNode.tagName.toLowerCase()=='li'){url=vspibs[i].parentNode.children[1].children[0].href;urls.push(url);}"
                                                                + "}" + "return urls;";

    static String                nextPageJs             = "document.getElementById('pnnext').click();";

    private JWebBrowser          webBrowser;

    private Status               status                 = Status.UNSTARRED;

    private String               keyword;

    private Map<String, Integer> statisticMap;

    private List<Object>         urlList                = new ArrayList<Object>();

    private int                  urlIndex               = 0;

    private int                  currentUrlResultCount  = 0;

    private Random               random                 = new Random();

    private int                  maxUrlResultCount;

    private int                  waitTime;

    private Callback             callback;

    private File                 outputFolder;

    private WebBrowserAdapter    webBrowserAdapter;

    StatisticController2(final JWebBrowser webBrowser) {
        this.webBrowser = webBrowser;

        webBrowserAdapter = new WebBrowserAdapter() {
            @Override
            public void loadingProgressChanged(WebBrowserEvent e) {
                super.loadingProgressChanged(e);

                if (webBrowser.getLoadingProgress() > 95) {
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            int progress = webBrowser.getLoadingProgress();
                            String location = webBrowser.getResourceLocation();
                            //                            System.out.println("--> " + location);

                            if (progress > 95 && location.equals(GOOGLE_URL)) {
                                webBrowser.stopLoading();
                                Object hasFehl = webBrowser
                                        .executeJavascriptWithResult("return document.getElementById('fehl')!=null");
                                if (Boolean.parseBoolean(hasFehl.toString())) {
                                    webBrowser.executeJavascript("document.getElementById('fehl').click()");
                                    return;
                                }

                                if (status == Status.UNSTARRED) {
                                    status = Status.KEYWORD_SEARCHING;
                                    //                            JWebBrowser.clearSessionCookies();
                                    searchKeywordInMainPage(keyword);
                                }
                            } else if (progress > 95 && location.startsWith(GOOGLE_HK_URL)) {
                                webBrowser.stopLoading();
                                //                        resetVPN();
                                //                        webBrowser.navigate(GOOGLE_URL);
                                webBrowser.executeJavascript("document.getElementById('fehl').click()");

                            } else if (progress == 100 && location.startsWith("http://www.google.com/sorry/?continue=")) {
                                webBrowser.stopLoading();
                                alert();
                                //                        JOptionPane.showMessageDialog(null, "请输验证码");

                                //http://www.google.com/sorry/?continue=
                                //http://www.google.com/sorry/Captcha?continue=
                                //                        resetVPN();

                                //                        JWebBrowser.clearSessionCookies();
                                //                        try {
                                //                            webBrowser.navigate(URLDecoder.decode(location.split("continue=")[1], UTF8));
                                //                        } catch (UnsupportedEncodingException e1) {
                                //                            e1.printStackTrace();
                                //                        }

                            } else if (progress == 100
                                    && (location.startsWith("http://www.google.com/search") || location
                                            .startsWith("http://www.google.com/webhp"))) {
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException ex) {
                                    ex.printStackTrace();
                                }
                                webBrowser.stopLoading();
                                try {
                                    switch (status) {
                                        case KEYWORD_SEARCHING:
                                            List<String> urls = collectUrls();
                                            urlList.addAll(urls);
                                            if (urlList.size() < URL_TO_SEARCH_COUNT && !meetEnd() && !urls.isEmpty()) {
                                                webBrowser.executeJavascript(nextPageJs);
                                            } else {
                                                status = Status.URL_SEARCHING;
                                                searchUrlInResultPage();
                                                currentUrlResultCount = 0;
                                            }
                                            break;
                                        case URL_SEARCHING:
                                            //1. get all results, calc into <keyword, Map<domain, count>>, inc currentUrlResultCount
                                            List<String> resultUrls = collectUrls();

                                            for (String url : resultUrls) {
                                                if (!url.startsWith("http")) {
                                                    try {
                                                        url = getRealUrl(url);
                                                    } catch (UnsupportedEncodingException e1) {
                                                        e1.printStackTrace();
                                                    }
                                                }

                                                String[] parts = url.split("/", 4);
                                                if (parts.length < 3) {
                                                    System.out.println("结果中有非法URL：" + url);
                                                    continue;
                                                }

                                                String domain = parts[2];

                                                int currentCount = statisticMap.containsKey(domain) ? statisticMap
                                                        .get(domain) : 0;
                                                statisticMap.put(domain, currentCount + 1);
                                            }
                                            currentUrlResultCount += resultUrls.size();

                                            if (currentUrlResultCount > maxUrlResultCount || meetEnd()
                                                    || resultUrls.isEmpty()) {
                                                urlIndex++;
                                                if (urlIndex < URL_TO_SEARCH_COUNT) {
                                                    currentUrlResultCount = 0;
                                                    searchUrlInResultPage();
                                                } else {
                                                    // output
                                                    System.out.println("outputing...");
                                                    printToFile();
                                                    System.out.println("finished");

                                                    callback.execute();
                                                }
                                            } else {
                                                webBrowser.executeJavascript(nextPageJs);
                                            }

                                            break;
                                        default:
                                            break;
                                    }
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    webBrowser.reloadPage();
                                }
                            }
                        }

                    });

                }
            }

        };
    }

    public void initControl() {
        WebBrowserListener[] listeners = webBrowser.getWebBrowserListeners();
        for (WebBrowserListener l : listeners) {
            webBrowser.removeWebBrowserListener(l);
        }
        webBrowser.addWebBrowserListener(webBrowserAdapter);
    }

    private void alert() {
        try {
            Process p = Runtime.getRuntime().exec("alert.bat");
            int exitValue = p.waitFor();
            System.out.println("exitValue: " + exitValue);
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
    }

    private void printToFile() {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(new File(outputFolder, keyword + ".csv")));
            for (Map.Entry<String, Integer> entry : statisticMap.entrySet()) {
                bw.write(entry.getKey() + "," + entry.getValue() + "\r\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String getRealUrl(String url) throws UnsupportedEncodingException {
        String[] urlParts = url.split("&");
        for (String part : urlParts) {
            int index = part.indexOf("url=");
            if (index != -1) {
                return URLDecoder.decode(part.substring(index + 4), UTF8);
            }
        }
        return "";
    }

    private boolean meetEnd() {
        String js = "return document.getElementById('pnnext').innerHTML;";
        Object o = webBrowser.executeJavascriptWithResult(js);
        return o == null;
    }

    private List<String> collectUrls() {
        //        String javascript = "var urls=new Array();"
        //                + "var cites=document.getElementsByTagName('cite');"
        //                + "for(var i=0;i<cites.length;i++){"
        //                + "if(cites[i].style.display=='none') continue;"
        //                + "if(window.getComputedStyle){var style=window.getComputedStyle(cites[i],'');if(style.display=='none') continue;if(style.visibility=='hidden') continue;}"
        //                + "var d=cites[i].parentNode.parentNode.parentNode;"
        //                + "if(d.parentNode.tagName.toLowerCase()=='li'){url=d.children[1].children[0].href;urls.push(url);}"
        //                + "}" + "return urls;";

        //        return (Object[]) webBrowser.executeJavascriptWithResult(getResultUrlsByVSPIBJs);

        String js = "if(document.getElementById('topstuff')){return document.getElementById('topstuff').outerHTML;}else{return '';}";
        String topstuffInnerHtml = (String) webBrowser.executeJavascriptWithResult(js);
        if (topstuffInnerHtml.contains("No results found for")) {
            return new ArrayList<String>();
        }

        js = "return document.getElementById('search').outerHTML;";
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

    private void searchKeywordInMainPage(String keyword) {
        //        String javascript = "var inputs=document.getElementsByTagName('input');" + "for(var i=0;i<inputs.length;i++){"
        //                + "if(inputs[i].type=='text'&&inputs[i].name=='q'){inputs[i].value='" + keyword + "';}"
        //                + "else if(inputs[i].type=='submit'&&inputs[i].value=='Google Search'){inputs[i].click();}" + "}";
        //        String javascript = "var inputs=document.getElementsByTagName('input');"
        //                + "for(var i=0;i<inputs.length;i++){if(inputs[i].type=='text'&&inputs[i].name=='q'){inputs[i].value='"
        //                + keyword + "';break;}}" + "";
        //        String javascript = "document.getElementsByName('q')[0].value='" + keyword + "';"
        //                + "document.getElementsByName('f')[0].submit();";

        webBrowser.executeJavascript("document.getElementsByName('q')[0].value='" + keyword + "';");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        webBrowser.executeJavascript("document.getElementsByName('f')[0].submit();");
    }

    private void searchUrlInResultPage() {
        if (urlIndex >= urlList.size()) {
            // output
            System.out.println("outputing...");
            printToFile();
            System.out.println("finished");

            callback.execute();
            return;
        }

        String url = urlList.get(urlIndex).toString();
        try {
            if (!url.startsWith("http")) {
                url = getRealUrl(url);
            }
            url = URLDecoder.decode(url, UTF8);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("start url: " + url);

        try {
            Thread.sleep(1000 + random.nextInt(Math.abs(waitTime - 1000)));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String javascript = "var inputs=document.getElementsByTagName('input');"
                + "for(var i=0;i<inputs.length;i++){if(inputs[i].type=='text'&&inputs[i].name=='q'){inputs[i].value='\""
                + url + "\"';break;}}" + "document.getElementsByTagName('button')[0].click();";
        //        String javascript = "document.getElementsByName('q')[0].value='" + url + "';"
        //                + "document.getElementsByName('btnG')[0].click();";
        webBrowser.executeJavascript(javascript);
    }

    @Override
    public void run() {
        urlList.clear();
        urlIndex = 0;
        JWebBrowser.clearSessionCookies();
        webBrowser.navigate(GOOGLE_HK_URL);
    }

    public void stop() {
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
        statisticMap = new HashMap<String, Integer>();
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        String s = "http://www.baike.com/wiki/%E7%99%BE%E5%BA%A6";
        //        s = "https%3A%2F%2Fzh.wikipedia.org%2Fzh%2F%25E7%2599%25BE%25E5%25BA%25A6";
        s = "http://www.google.com/search%3Fq%3D%2522http://www.wikihow.com/Make-Money%2522%26ei%3DM355Ub7-GIr1iQK2o4DIAQ%26start%3D100%26sa%3DN%26biw%3D989%26bih%3D439";
        System.out.println(URLDecoder.decode(URLDecoder.decode(s, UTF8), UTF8));

        //        try {
        //            Process p = Runtime.getRuntime().exec("reconnect.bat");
        //            int exitValue = p.waitFor();
        //            System.out.println("exitValue: " + exitValue);
        //        } catch (IOException e1) {
        //            e1.printStackTrace();
        //        } catch (InterruptedException e1) {
        //            e1.printStackTrace();
        //        }

        //        String divString = "<div style=\"\" id=\"search\"><!--a--><h2 class=\"hd\">搜索结果</h2><div id=\"ires\"><ol eid=\"82V5Ub2VLon2iQKH4YDoCw\" id=\"rso\"><li class=\"g\"><!--m--><div class=\"rc\"><span style=\"float:left\"></span><h3 class=\"r\"><a href=\"http://www.youtube.com/watch?v=YhU2Nus5u9c\" onmousedown=\"return rwt(this,'','','','1','AFQjCNHrYC4LtVGkmv9YLvI8qzqjlatgPg','','0CDoQFjAA','','',event)\"><em>How to Make Money</em> Online: 7400 dollars in week <b>...</b> - YouTube</a></h3><div class=\"s\"><div><div class=\"f kv\" style=\"white-space:nowrap\"><cite>www.youtube.com/watch?v=YhU2Nus5u9c</cite><div class=\"action-menu ab_ctl\"><a class=\"clickable-dropdown-arrow ab_button\" id=\"am-b0\" href=\"#\" data-ved=\"0CDsQ7B0wAA\" aria-label=\"结果详情\" jsaction=\"ab.tdd; keydown:ab.hbke; keypress:ab.mskpe\" role=\"button\" aria-haspopup=\"true\" aria-expanded=\"false\"><span class=\"mn-dwn-arw\"></span></a><div data-ved=\"0CDwQqR8wAA\" class=\"action-menu-panel ab_dropdown\" jsaction=\"keydown:ab.hdke; mouseover:ab.hdhne; mouseout:ab.hdhue\" role=\"menu\" tabindex=\"-1\"><ul><li class=\"action-menu-item ab_dropdownitem\" role=\"menuitem\"><a href=\"http://webcache.googleusercontent.com/search?q=cache:rUdo2rBmXToJ:www.youtube.com/watch%3Fv%3DYhU2Nus5u9c+how+to+make+money&amp;cd=1&amp;hl=zh-CN&amp;ct=clnk&amp;gl=us\" onmousedown=\"return rwt(this,'','','','1','AFQjCNH28E0dG8p1NmXwn4plzqljCWjKZw','','0CD0QIDAA','','',event)\" class=\"fl\">网页快照</a></li></ul></div></div><a href=\"http://translate.google.com/translate?hl=zh-CN&amp;sl=en&amp;u=http://www.youtube.com/watch%3Fv%3DYhU2Nus5u9c&amp;prev=/search%3Fq%3Dhow%2Bto%2Bmake%2Bmoney%26biw%3D1280%26bih%3D382\" onmousedown=\"return rwt(this,'','','','1','AFQjCNEZKjhj_BcDdRioSFJWgQDPTll2Gg','','0CD8Q7gEwAA','','',event)\" class=\"fl\">翻译此页</a></div><div class=\"f slp\"></div><span class=\"st\"><span class=\"f\">2013年3月18日 – </span>http://www.pureprofitssystem.com --- check my strategy now ! <em>How to Make Money</em> Online: 7400 dollars in week PureProfitsSystem.com <b>...</b></span></div></div></div><!--n--></li><li class=\"g\"><!--m--><div class=\"rc\"><span style=\"float:left\"></span><h3 class=\"r\"><a href=\"http://www.youtube.com/watch?v=FZOuC8FraM0\" onmousedown=\"return rwt(this,'','','','2','AFQjCNHDwSXZ41mVzpmsafB4URv8sZEuxA','','0CEIQFjAB','','',event)\"><em>How to Make Money</em> from Home - <em>Make Money</em> Online Fast - YouTube</a></h3><div class=\"s\"><div><div class=\"f kv\" style=\"white-space:nowrap\"><cite>www.youtube.com/watch?v=FZOuC8FraM0</cite><div class=\"action-menu ab_ctl\"><a class=\"clickable-dropdown-arrow ab_button\" id=\"am-b1\" href=\"#\" data-ved=\"0CEMQ7B0wAQ\" aria-label=\"结果详情\" jsaction=\"ab.tdd; keydown:ab.hbke; keypress:ab.mskpe\" role=\"button\" aria-haspopup=\"true\" aria-expanded=\"false\"><span class=\"mn-dwn-arw\"></span></a><div data-ved=\"0CEQQqR8wAQ\" class=\"action-menu-panel ab_dropdown\" jsaction=\"keydown:ab.hdke; mouseover:ab.hdhne; mouseout:ab.hdhue\" role=\"menu\" tabindex=\"-1\"><ul><li class=\"action-menu-item ab_dropdownitem\" role=\"menuitem\"><a href=\"http://webcache.googleusercontent.com/search?q=cache:v1mQ1HC0ThUJ:www.youtube.com/watch%3Fv%3DFZOuC8FraM0+how+to+make+money&amp;cd=2&amp;hl=zh-CN&amp;ct=clnk&amp;gl=us\" onmousedown=\"return rwt(this,'','','','2','AFQjCNHxf5DVbQ22aYwbrcVqznsB-PM0cQ','','0CEUQIDAB','','',event)\" class=\"fl\">网页快照</a></li></ul></div></div><a href=\"http://translate.google.com/translate?hl=zh-CN&amp;sl=en&amp;u=http://www.youtube.com/watch%3Fv%3DFZOuC8FraM0&amp;prev=/search%3Fq%3Dhow%2Bto%2Bmake%2Bmoney%26biw%3D1280%26bih%3D382\" onmousedown=\"return rwt(this,'','','','2','AFQjCNF6txaPSgMDrcVe3BTH7_0oZWOS9w','','0CEcQ7gEwAQ','','',event)\" class=\"fl\">翻译此页</a></div><div class=\"f slp\"></div><span class=\"st\"><span class=\"f\">2013年3月29日 – </span>Click Here -- http://financialabsolute.com/ -- Click Here <em>How to Make Money</em> from Home - <em>Make Money</em> Online Fast We want to help you gain <b>...</b></span></div></div></div><!--n--></li><li class=\"g\"><!--m--><div class=\"rc\"><span style=\"float:left\"></span><h3 class=\"r\"><a href=\"http://www.youtube.com/watch?v=FNzF2Z6ZLF8\" onmousedown=\"return rwt(this,'','','','3','AFQjCNETkze1ks0E8SQ4_q67ChO9xcVF3g','','0CEoQFjAC','','',event)\"><em>How to Make</em> Easy <em>Money</em> Online - Fast and Proven Ways To <em>Make</em> <b>...</b></a></h3><div class=\"s\"><div><div class=\"f kv\" style=\"white-space:nowrap\"><cite>www.youtube.com/watch?v=FNzF2Z6ZLF8</cite><div class=\"action-menu ab_ctl\"><a class=\"clickable-dropdown-arrow ab_button\" id=\"am-b2\" href=\"#\" data-ved=\"0CEsQ7B0wAg\" aria-label=\"结果详情\" jsaction=\"ab.tdd; keydown:ab.hbke; keypress:ab.mskpe\" role=\"button\" aria-haspopup=\"true\" aria-expanded=\"false\"><span class=\"mn-dwn-arw\"></span></a><div data-ved=\"0CEwQqR8wAg\" class=\"action-menu-panel ab_dropdown\" jsaction=\"keydown:ab.hdke; mouseover:ab.hdhne; mouseout:ab.hdhue\" role=\"menu\" tabindex=\"-1\"><ul><li class=\"action-menu-item ab_dropdownitem\" role=\"menuitem\"><a href=\"http://webcache.googleusercontent.com/search?q=cache:hwksUOVF7PIJ:www.youtube.com/watch%3Fv%3DFNzF2Z6ZLF8+how+to+make+money&amp;cd=3&amp;hl=zh-CN&amp;ct=clnk&amp;gl=us\" onmousedown=\"return rwt(this,'','','','3','AFQjCNGcK_6BRWDdLTyX4n7vw6lCZrKWUA','','0CE0QIDAC','','',event)\" class=\"fl\">网页快照</a></li></ul></div></div><a href=\"http://translate.google.com/translate?hl=zh-CN&amp;sl=en&amp;u=http://www.youtube.com/watch%3Fv%3DFNzF2Z6ZLF8&amp;prev=/search%3Fq%3Dhow%2Bto%2Bmake%2Bmoney%26biw%3D1280%26bih%3D382\" onmousedown=\"return rwt(this,'','','','3','AFQjCNHISPay_IZD0iMbCLkVhlAb0R9KOQ','','0CE8Q7gEwAg','','',event)\" class=\"fl\">翻译此页</a></div><div class=\"f slp\"></div><span class=\"st\"><span class=\"f\">2013年3月18日 – </span>http://goo.gl/ZdNUR --- Work from Home and <em>Make Money</em> Online! Best Ways to Make a Living Working From Home Online With the <b>...</b></span></div></div></div><!--n--></li><li class=\"g\"><!--m--><div class=\"rc\"><span style=\"float:left\"></span><h3 class=\"r\"><a href=\"http://www.youtube.com/watch?v=Lm8F3XTQuBw\" onmousedown=\"return rwt(this,'','','','4','AFQjCNEiYzvIg-USyEGyQhPG5LMOBIfN3w','','0CFIQtwIwAw','','',event)\">Ways To <em>Earn Money</em> Online | how to make $1000 a day - YouTube</a></h3><div class=\"s\"><div><div class=\"th thb\" style=\"height:65px;width:116px\"><a href=\"http://www.youtube.com/watch?v=Lm8F3XTQuBw\" onmousedown=\"return rwt(this,'','','','4','AFQjCNEiYzvIg-USyEGyQhPG5LMOBIfN3w','','0CFMQuAIwAw','','',event)\"><span class=\"thc\" style=\"top:-11px\"><img src=\"data:image/jpeg;base64,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\" id=\"vidthumb4\" border=\"0\" height=\"87\" width=\"116\"></span><span class=\"vdur thl thlb\">►&nbsp;4:32</span><span class=\"vdur thl thlt\">►&nbsp;4:32</span></a></div></div><div style=\"margin-left:125px\"><div class=\"f kv\" style=\"white-space:nowrap\"><cite>www.youtube.com/watch?v=Lm8F3XTQuBw</cite></div><div class=\"f slp\">2013年3月19日 - 上传者：Mike Fortune</div><span class=\"st\">click here - http://goo.gl/70agQ - click here 5 Easy Ways To <em>Make Money</em> Online Perhaps you are looking for <b>...</b></span></div><div style=\"clear:left\"></div></div></div><!--n--></li><li class=\"g\"><!--m--><div class=\"rc\"><span style=\"float:left\"></span><h3 class=\"r\"><a href=\"http://www.wikihow.com/Make-Money\" onmousedown=\"return rwt(this,'','','','5','AFQjCNHMxH1crXT45dYB7S-aJ_tCTx2laQ','','0CFUQFjAE','','',event)\">13 Ways to <em>Make Money</em> - wikiHow</a></h3><div class=\"s\"><div><div class=\"f kv\" style=\"white-space:nowrap\"><cite>www.wikihow.com/<b>Make</b>-<b>Money</b></cite><div class=\"action-menu ab_ctl\"><a class=\"clickable-dropdown-arrow ab_button\" id=\"am-b4\" href=\"#\" data-ved=\"0CFYQ7B0wBA\" aria-label=\"结果详情\" jsaction=\"ab.tdd; keydown:ab.hbke; keypress:ab.mskpe\" role=\"button\" aria-haspopup=\"true\" aria-expanded=\"false\"><span class=\"mn-dwn-arw\"></span></a><div data-ved=\"0CFcQqR8wBA\" class=\"action-menu-panel ab_dropdown\" jsaction=\"keydown:ab.hdke; mouseover:ab.hdhne; mouseout:ab.hdhue\" role=\"menu\" tabindex=\"-1\"><ul><li class=\"action-menu-item ab_dropdownitem\" role=\"menuitem\"><a href=\"http://webcache.googleusercontent.com/search?q=cache:F2V5PgqE9PkJ:www.wikihow.com/Make-Money+how+to+make+money&amp;cd=5&amp;hl=zh-CN&amp;ct=clnk&amp;gl=us\" onmousedown=\"return rwt(this,'','','','5','AFQjCNFKH2q0zg-AcZK6FwfiI27RqJbvVw','','0CFgQIDAE','','',event)\" class=\"fl\">网页快照</a></li><li class=\"action-menu-item ab_dropdownitem\" role=\"menuitem\"><a href=\"/search?biw=1280&amp;bih=382&amp;q=related:www.wikihow.com/Make-Money+how+to+make+money&amp;tbo=1\" onmousedown=\"return rwt(this,'','','','5','AFQjCNH7K8MwF8zMwpJO4IfeVwQAD6LFRA','','0CFkQHzAE','','',event)\" class=\"fl\">类似结果</a></li></ul></div></div><a href=\"http://translate.google.com/translate?hl=zh-CN&amp;sl=en&amp;u=http://www.wikihow.com/Make-Money&amp;prev=/search%3Fq%3Dhow%2Bto%2Bmake%2Bmoney%26biw%3D1280%26bih%3D382\" onmousedown=\"return rwt(this,'','','','5','AFQjCNFC2_h7qlA2NemDPgAlE6BJ5KpH2w','','0CFsQ7gEwBA','','',event)\" class=\"fl\">翻译此页</a></div><div class=\"f slp\"></div><span class=\"st\"><em>How to Make Money</em>. The secret to making money isn't working at a high-paying job, it's finding creative solutions to people's problems, and it doesn't take a <b>...</b></span><div class=\"osl\"><a href=\"http://www.wikihow.com/Make-Money-Fast\" onmousedown=\"return rwt(this,'','','','5','AFQjCNG3yYnm04v8dZdxvRSLOWkEWAfBIw','','0CF0Q0gIoADAE','','',event)\" class=\"fl\">How to Make Money Fast</a> -&nbsp;<a href=\"http://www.wikihow.com/Make-Money-with-Free-Online-Surveys\" onmousedown=\"return rwt(this,'','','','5','AFQjCNEpG8QOvVbMg3M-dmIOrHBWEYcj2g','','0CF4Q0gIoATAE','','',event)\" class=\"fl\">How to Make Money with Free</a></div></div></div></div><!--n--></li><li class=\"g\"><!--m--><div class=\"rc\"><span style=\"float:left\"></span><h3 class=\"r\"><a href=\"http://www.wikihow.com/Make-Money-Fast\" onmousedown=\"return rwt(this,'','','','6','AFQjCNG3yYnm04v8dZdxvRSLOWkEWAfBIw','','0CGAQFjAF','','',event)\"><em>How to Make Money</em> Fast (with Quiz) - wikiHow</a></h3><div class=\"s\"><div><div class=\"f kv\" style=\"white-space:nowrap\"><cite class=\"bc\">www.wikihow.com › ... › <a href=\"http://www.wikihow.com/Category:Managing-Your-Money\" onmousedown=\"return rwt(this,'','','','6','AFQjCNG_r7gyyDFUJ7Mqc3-nJVxglkF7wA','','0CGIQ6QUoADAF','','',event)\">Managing Your Money</a></cite><div class=\"action-menu ab_ctl\"><a class=\"clickable-dropdown-arrow ab_button\" id=\"am-b5\" href=\"#\" data-ved=\"0CGMQ7B0wBQ\" aria-label=\"结果详情\" jsaction=\"ab.tdd; keydown:ab.hbke; keypress:ab.mskpe\" role=\"button\" aria-haspopup=\"true\" aria-expanded=\"false\"><span class=\"mn-dwn-arw\"></span></a><div data-ved=\"0CGQQqR8wBQ\" class=\"action-menu-panel ab_dropdown\" jsaction=\"keydown:ab.hdke; mouseover:ab.hdhne; mouseout:ab.hdhue\" role=\"menu\" tabindex=\"-1\"><ul><li class=\"action-menu-item ab_dropdownitem\" role=\"menuitem\"><a href=\"http://webcache.googleusercontent.com/search?q=cache:YHPtGcZ0gyYJ:www.wikihow.com/Make-Money-Fast+how+to+make+money&amp;cd=6&amp;hl=zh-CN&amp;ct=clnk&amp;gl=us\" onmousedown=\"return rwt(this,'','','','6','AFQjCNEI5lsbGYjO_wjcIsaiae19XEgcqg','','0CGUQIDAF','','',event)\" class=\"fl\">网页快照</a></li><li class=\"action-menu-item ab_dropdownitem\" role=\"menuitem\"><a href=\"/search?biw=1280&amp;bih=382&amp;q=related:www.wikihow.com/Make-Money-Fast+how+to+make+money&amp;tbo=1\" onmousedown=\"return rwt(this,'','','','6','AFQjCNGaTkGXLJK8bn7KkZZuPxkwhlvkZg','','0CGYQHzAF','','',event)\" class=\"fl\">类似结果</a></li></ul></div></div><a href=\"http://translate.google.com/translate?hl=zh-CN&amp;sl=en&amp;u=http://www.wikihow.com/Make-Money-Fast&amp;prev=/search%3Fq%3Dhow%2Bto%2Bmake%2Bmoney%26biw%3D1280%26bih%3D382\" onmousedown=\"return rwt(this,'','','','6','AFQjCNFHzW6wLrRLP6_NxpiOEVCEVA60wQ','','0CGgQ7gEwBQ','','',event)\" class=\"fl\">翻译此页</a></div><div class=\"f slp\"></div><span class=\"st\">Later, you can read up on long-term ways to <em>make money</em>, reducing expenses, saving, and investing. Otherwise, hurry up and follow these steps so you can <b>...</b></span></div></div></div><!--n--></li><li class=\"g\"><!--m--><div class=\"rc\"><span style=\"float:left\"></span><h3 class=\"r\"><a href=\"http://makemoneyinternett.com/\" onmousedown=\"return rwt(this,'','','','7','AFQjCNHqNalXGuUb5IyH8t3TUASGOiwTGg','','0CGoQFjAG','','',event)\">It is easy to <em>make money</em> in the Internet. Best way to <em>make money</em> <b>...</b></a></h3><div class=\"s\"><div><div class=\"f kv\" style=\"white-space:nowrap\"><cite><b>makemoney</b>internett.com/</cite><div class=\"action-menu ab_ctl\"><a class=\"clickable-dropdown-arrow ab_button\" id=\"am-b6\" href=\"#\" data-ved=\"0CGsQ7B0wBg\" aria-label=\"结果详情\" jsaction=\"ab.tdd; keydown:ab.hbke; keypress:ab.mskpe\" role=\"button\" aria-haspopup=\"true\" aria-expanded=\"false\"><span class=\"mn-dwn-arw\"></span></a><div data-ved=\"0CGwQqR8wBg\" class=\"action-menu-panel ab_dropdown\" jsaction=\"keydown:ab.hdke; mouseover:ab.hdhne; mouseout:ab.hdhue\" role=\"menu\" tabindex=\"-1\"><ul><li class=\"action-menu-item ab_dropdownitem\" role=\"menuitem\"><a href=\"http://webcache.googleusercontent.com/search?q=cache:i_GIKRnmmhMJ:makemoneyinternett.com/+how+to+make+money&amp;cd=7&amp;hl=zh-CN&amp;ct=clnk&amp;gl=us\" onmousedown=\"return rwt(this,'','','','7','AFQjCNH9ivvNCZ0O5l21hepXESzyN0_H-A','','0CG0QIDAG','','',event)\" class=\"fl\">网页快照</a></li></ul></div></div><a href=\"http://translate.google.com/translate?hl=zh-CN&amp;sl=en&amp;u=http://makemoneyinternett.com/&amp;prev=/search%3Fq%3Dhow%2Bto%2Bmake%2Bmoney%26biw%3D1280%26bih%3D382\" onmousedown=\"return rwt(this,'','','','7','AFQjCNEUdIaZDrITIwrfVlxD3mitNwNyrw','','0CG8Q7gEwBg','','',event)\" class=\"fl\">翻译此页</a></div><div class=\"f slp\"></div><span class=\"st\">It is easy to <em>make money</em> in the Internet. You will be able to <em>make money</em> sitting in your favorite chair and spend time on yourself. The Internet is available to <b>...</b></span></div></div></div><!--n--></li><li class=\"g\"><!--m--><div class=\"rc\"><span style=\"float:left\"></span><h3 class=\"r\"><a href=\"http://gizmodo.com/5995121/how-to-make-money-on-your-lunch-break\" onmousedown=\"return rwt(this,'','','','8','AFQjCNHKjO2HtK26V3joZPMzrE-E25YU1Q','','0CHEQFjAH','','',event)\"><em>How to Make Money</em> on Your Lunch Break - Gizmodo</a></h3><div class=\"s\"><div><div class=\"f kv\" style=\"white-space:nowrap\"><cite>gizmodo.com/.../<b>how-to-make</b>-<b>money</b>-on-your-lunch-break</cite><div class=\"action-menu ab_ctl\"><a class=\"clickable-dropdown-arrow ab_button\" id=\"am-b7\" href=\"#\" data-ved=\"0CHIQ7B0wBw\" aria-label=\"结果详情\" jsaction=\"ab.tdd; keydown:ab.hbke; keypress:ab.mskpe\" role=\"button\" aria-haspopup=\"true\" aria-expanded=\"false\"><span class=\"mn-dwn-arw\"></span></a><div data-ved=\"0CHMQqR8wBw\" class=\"action-menu-panel ab_dropdown\" jsaction=\"keydown:ab.hdke; mouseover:ab.hdhne; mouseout:ab.hdhue\" role=\"menu\" tabindex=\"-1\"><ul><li class=\"action-menu-item ab_dropdownitem\" role=\"menuitem\"><a href=\"http://webcache.googleusercontent.com/search?q=cache:YmHSEAh3H3oJ:gizmodo.com/5995121/how-to-make-money-on-your-lunch-break+how+to+make+money&amp;cd=8&amp;hl=zh-CN&amp;ct=clnk&amp;gl=us\" onmousedown=\"return rwt(this,'','','','8','AFQjCNEnB-OU6gsgsYy84z3n5Bp7-w_kPQ','','0CHQQIDAH','','',event)\" class=\"fl\">网页快照</a></li></ul></div></div><a href=\"http://translate.google.com/translate?hl=zh-CN&amp;sl=en&amp;u=http://gizmodo.com/5995121/how-to-make-money-on-your-lunch-break&amp;prev=/search%3Fq%3Dhow%2Bto%2Bmake%2Bmoney%26biw%3D1280%26bih%3D382\" onmousedown=\"return rwt(this,'','','','8','AFQjCNE1ZxOsiOGi6vfqgcU0HESoSwgqiw','','0CHYQ7gEwBw','','',event)\" class=\"fl\">翻译此页</a></div><div class=\"f slp\"></div><span class=\"st\"><span class=\"f\">4 days ago – </span>How do you spend your lunch break? Taking a turn round the park? Checking Facebook? Catching up on the work you should've done in the <b>...</b></span></div></div></div><!--n--></li><li class=\"g\"><!--m--><div class=\"rc\"><span style=\"float:left\"></span><h3 class=\"r\"><a href=\"http://financialhighway.com/ways-to-make-more-money/\" onmousedown=\"return rwt(this,'','','','9','AFQjCNFw_eJgWYXc6FrnYbW6JaXZCGL_AA','','0CHkQFjAI','','',event)\">30+ Ways To <em>Make</em> More <em>Money</em> - Financial Highway</a></h3><div class=\"s\"><div><div class=\"f kv\" style=\"white-space:nowrap\"><cite>financialhighway.com/ways-to-<b>make</b>-more-<b>money</b>/</cite><div class=\"action-menu ab_ctl\"><a class=\"clickable-dropdown-arrow ab_button\" id=\"am-b8\" href=\"#\" data-ved=\"0CHoQ7B0wCA\" aria-label=\"结果详情\" jsaction=\"ab.tdd; keydown:ab.hbke; keypress:ab.mskpe\" role=\"button\" aria-haspopup=\"true\" aria-expanded=\"false\"><span class=\"mn-dwn-arw\"></span></a><div data-ved=\"0CHsQqR8wCA\" class=\"action-menu-panel ab_dropdown\" jsaction=\"keydown:ab.hdke; mouseover:ab.hdhne; mouseout:ab.hdhue\" role=\"menu\" tabindex=\"-1\"><ul><li class=\"action-menu-item ab_dropdownitem\" role=\"menuitem\"><a href=\"http://webcache.googleusercontent.com/search?q=cache:lB2V2FfIn84J:financialhighway.com/ways-to-make-more-money/+how+to+make+money&amp;cd=9&amp;hl=zh-CN&amp;ct=clnk&amp;gl=us\" onmousedown=\"return rwt(this,'','','','9','AFQjCNGZ29rvGy_Z4WTOcYI5I16NUYQLnw','','0CHwQIDAI','','',event)\" class=\"fl\">网页快照</a></li></ul></div></div><a href=\"http://translate.google.com/translate?hl=zh-CN&amp;sl=en&amp;u=http://financialhighway.com/ways-to-make-more-money/&amp;prev=/search%3Fq%3Dhow%2Bto%2Bmake%2Bmoney%26biw%3D1280%26bih%3D382\" onmousedown=\"return rwt(this,'','','','9','AFQjCNHmNdESFoXWNRv_aELiPSJrSxDi_g','','0CH4Q7gEwCA','','',event)\" class=\"fl\">翻译此页</a></div><div class=\"f slp\"></div><span class=\"st\">How to make more money doesn't mean a high paying job, just some creativity and capitalizing on opportunities. Here are 30+ ways to <em>make money</em>.</span></div></div></div><!--n--></li><li class=\"g\"><!--m--><div class=\"rc\"><span style=\"float:left\"></span><h3 class=\"r\"><a href=\"http://www.forbes.com/sites/deborahljacobs/2011/12/14/how-to-make-money-without-a-job/\" onmousedown=\"return rwt(this,'','','','10','AFQjCNFAasaMQ3iTgutkta3Q20QDnttf0w','','0CIABEBYwCQ','','',event)\"><em>How To Make Money</em> Without A Job - Forbes</a></h3><div class=\"s\"><div><div class=\"th thb\" style=\"height:44px;width:44px\"><a href=\"/search?q=how+to+make+money&amp;biw=1280&amp;bih=382&amp;tbs=ppl_ids:--105147298486201187029-,ppl_nps:Deborah+Jacobs,ppl_aut:1\" onmousedown=\"return rwt(this,'','','','10','AFQjCNFv1e1p4ASiYfqg3qV7Z8Cv_dJjDw','','0CIIBEP0WMAk','','',event)\"><img src=\"data:image/jpeg;base64,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\" id=\"apthumb9\" border=\"0\" height=\"44\" width=\"44\"></a></div></div><div style=\"margin-left:53px\"><div class=\"f kv\" style=\"white-space:nowrap\"><cite>www.forbes.com/.../<b>how-to-make</b>-<b>money</b>-without-a-jo...</cite><div class=\"action-menu ab_ctl\"><a class=\"clickable-dropdown-arrow ab_button\" id=\"am-b9\" href=\"#\" data-ved=\"0CIMBEOwdMAk\" aria-label=\"结果详情\" jsaction=\"ab.tdd; keydown:ab.hbke; keypress:ab.mskpe\" role=\"button\" aria-haspopup=\"true\" aria-expanded=\"false\"><span class=\"mn-dwn-arw\"></span></a><div data-ved=\"0CIQBEKkfMAk\" class=\"action-menu-panel ab_dropdown\" jsaction=\"keydown:ab.hdke; mouseover:ab.hdhne; mouseout:ab.hdhue\" role=\"menu\" tabindex=\"-1\"><ul><li class=\"action-menu-item ab_dropdownitem\" role=\"menuitem\"><a href=\"http://webcache.googleusercontent.com/search?q=cache:gcqnUzo5I_QJ:www.forbes.com/sites/deborahljacobs/2011/12/14/how-to-make-money-without-a-job/+how+to+make+money&amp;cd=10&amp;hl=zh-CN&amp;ct=clnk&amp;gl=us\" onmousedown=\"return rwt(this,'','','','10','AFQjCNFlaByWaWDsBE3oh2cF69ll5_uCKA','','0CIUBECAwCQ','','',event)\" class=\"fl\">网页快照</a></li></ul></div></div><a href=\"http://translate.google.com/translate?hl=zh-CN&amp;sl=en&amp;u=http://www.forbes.com/sites/deborahljacobs/2011/12/14/how-to-make-money-without-a-job/&amp;prev=/search%3Fq%3Dhow%2Bto%2Bmake%2Bmoney%26biw%3D1280%26bih%3D382\" onmousedown=\"return rwt(this,'','','','10','AFQjCNEDqM1n79Gd0aeESN9J3P7e7UJBCA','','0CIcBEO4BMAk','','',event)\" class=\"fl\">翻译此页</a></div><div class=\"f\"><div></div><a class=\"authorship_link\" href=\"/search?q=how+to+make+money&amp;biw=1280&amp;bih=382&amp;tbs=ppl_ids:--105147298486201187029-,ppl_nps:Deborah+Jacobs,ppl_aut:1\" onmousedown=\"return rwt(this,'','','','10','AFQjCNFv1e1p4ASiYfqg3qV7Z8Cv_dJjDw','','0CIoBEJ8WMAk','','',event)\">作者：Deborah Jacobs</a> - <a class=\"authorship_link\" href=\"https://plus.google.com/105147298486201187029\" onmousedown=\"return rwt(this,'','','','10','AFQjCNFbVae-4PyxeQRDekp_kgwowyFK4g','','0CIsBEOsRMAk','','',event)\"><span>在 291 个 Google+ 圈子中</span></a></div><span class=\"st\"><span class=\"f\">2011年12月14日 – </span>Whether you want to start something on the side or say goodbye to corporate life forever, here are some tips for starting your own business.</span></div><div style=\"clear:left\"></div></div></div><!--n--></li></ol></div><!--z--></div>";
        //
        //        Parser parser;
        //        try {
        //            parser = new Parser(divString);
        //
        //            TagNameFilter acriptTagFilter = new TagNameFilter("li");
        //
        //            HasAttributeFilter hasIdFilter = new HasAttributeFilter("id");
        //            HasAttributeFilter hasClassFilter = new HasAttributeFilter("class", "g");
        //
        //            NodeList liList = parser.extractAllNodesThatMatch(new AndFilter(new NodeFilter[] { acriptTagFilter,
        //                    new NotFilter(hasIdFilter), hasClassFilter }));
        //
        //            ExtractUrlVistor v = new ExtractUrlVistor();
        //            for (int j = 0; j < liList.size(); j++) {
        //                liList.elementAt(j).accept(v);
        //            }
        //
        //            for (String s : v.urlList) {
        //                System.out.println(s);
        //            }
        //
        //        } catch (ParserException e) {
        //            e.printStackTrace();
        //        }
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

    public void setMaxUrlResultCount(int maxUrlResultCount) {
        this.maxUrlResultCount = maxUrlResultCount;
    }

    public void setWaitTime(int waitTime) {
        this.waitTime = waitTime;
    }

    public void setAction(Callback callback) {
        this.callback = callback;
    }

    public void setOutputPath(File outputFolder) {
        this.outputFolder = outputFolder;
    }
}
