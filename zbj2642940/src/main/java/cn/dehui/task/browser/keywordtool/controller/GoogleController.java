package cn.dehui.task.browser.keywordtool.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.tags.Div;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.TableColumn;
import org.htmlparser.tags.TableRow;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.visitors.NodeVisitor;

import chrriis.dj.nativeswing.swtimpl.components.WebBrowserAdapter;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserEvent;
import cn.dehui.task.browser.keywordtool.controller.util.AdwordsKeywordInfo;
import cn.dehui.task.browser.keywordtool.controller.util.Status;

/**
 * @author Christopher Deckers
 */
public class GoogleController extends Controller {

    private static final String __C_REGEX       = "__c=\\d+";

    private static final String __U_REGEX       = "__u=\\d+";

    private static Pattern      cPattern        = Pattern.compile(__C_REGEX);

    private static Pattern      uPattern        = Pattern.compile(__U_REGEX);

    private static final String KEYWORD_URL_TPL = "https://adwords.google.com/o/Targeting/Explorer?__o=kp&ideaRequestType=KEYWORD_IDEAS&%s&%s";

    @Override
    protected WebBrowserAdapter getWebBrowserListener() {
        return new WebBrowserAdapter() {

            @Override
            public void loadingProgressChanged(WebBrowserEvent e) {
                super.loadingProgressChanged(e);
                if (webBrowser.getLoadingProgress() == 100) {
                    execute();
                    //                    SwingUtilities.invokeLater(runnable);
                }
                //                if (webBrowser.getResourceLocation().startsWith("https://adwords.google.com/o/Targeting/Explorer")) {
                //                    System.out.println(webBrowser.getLoadingProgress());
                //                }
            }

            private void execute() {
                String location = webBrowser.getResourceLocation();

                System.out.println("Loaded: " + location);
                if (location.startsWith("https://accounts.google.com/ServiceLogin")) {
                    if (status == Status.UNSTARRED) {
                        doLogin();
                    }
                } else if (location.startsWith("https://adwords.google.com/cm/CampaignMgmt")) {
                    if (status == Status.UNSTARRED || status == Status.WAIT_FOR_CM) {
                        navigateToKeywordTool(location);
                    }
                } else if (location.startsWith("https://adwords.google.com/o/Targeting/Explorer")) {

                    if (status == Status.WAIT_FOR_TARGET_TOOL) {

                        status = Status.EMTER_TARGET_TOOL;

                        Runnable thread = new Runnable() {
                            @Override
                            public void run() {
                                //                                    sleep(5000);
                                //                                    webBrowser.stopLoading();
                                while (started) {

                                    if (keywordIndex == keywordList.size()) {
                                        System.out.println("All done.");
                                        break;
                                    }

                                    String keyword = keywordList.get(keywordIndex++);
                                    System.out.printf("[%s] started, number: %d.\r\n", keyword, keywordIndex - 1);

                                    waitUntilLoadingFinished(false);

                                    if (status == Status.EMTER_TARGET_TOOL) {
                                        initFuncs();
                                        initSearchConfig();
                                    }

                                    // input keyword
                                    String js = "var textareas=document.getElementsByTagName('textarea');"
                                            + "for(var i=0;i<textareas.length;i++){"
                                            + "if(textareas[i].parentNode.children[0].innerHTML=='One per line'){textareas[i].value='"
                                            + keyword + "';break;}" + "}";
                                    executeJavascript(js);

                                    // input Include terms
                                    js = "findByTagInnerHTML('a','Remove all').click(); "
                                            + "var div=findByTagInnerHTML('div','Include terms (0)');"
                                            + "var inputParent=div.parentNode.parentNode.children[1].children[0].children[0];"
                                            + "inputParent.children[0].value='" + keyword + "';"
                                            + "inputParent.children[1].click();";
                                    executeJavascript(js);

                                    // click search
                                    js = "findAndClick('button','Search');";
                                    executeJavascript(js);

                                    status = Status.SEARCHING_RESULT;

                                    System.out.printf("[%s] searching.\r\n", keyword);

                                    waitUntilLoadingFinished(false);

                                    // click 'Keyword ideas'
                                    js = "findAndClick('span','Keyword ideas')";
                                    executeJavascript(js);

                                    waitUntilLoadingFinished(false);

                                    String initLabel = executeJavascriptWithResult(
                                            "return getLabelAndClickAndNext(false,false);").toString();
                                    //                                    System.out.println(initLabel);

                                    String[] labelSlices = initLabel.split("</*b>");
                                    // change to 100 items
                                    if (!labelSlices[3].equals(labelSlices[5]) && !"100".equals(labelSlices[3])) {
                                        executeJavascript("getLabelAndClickAndNext(true,false);");
                                        executeJavascript("findAndClick('div','100 items');");
                                    }

                                    List<AdwordsKeywordInfo> infos = new ArrayList<AdwordsKeywordInfo>();
                                    boolean meetEnd = false;
                                    do {
                                        if (!waitUntilLoadingFinished(false)) {
                                            meetEnd = false;
                                            break;
                                        }
                                        //collect data
                                        String label = executeJavascriptWithResult(
                                                "return getLabelAndClickAndNext(false,false);").toString();
                                        System.out.printf("[%s] data collecting: %s \r\n", keyword,
                                                label.replaceAll("</*b>", ""));
                                        infos.addAll(collectData());

                                        meetEnd = Boolean.parseBoolean(executeJavascriptWithResult("return meetEnd();")
                                                .toString());

                                        if (meetEnd) {
                                            System.out.printf("[%s] finished.\r\n", keyword);
                                            output(infos, keyword);
                                            break;
                                        } else {
                                            System.out.printf("[%s] next page.\r\n", keyword);
                                            executeJavascript("getLabelAndClickAndNext(false,true)");
                                        }
                                    } while (started);

                                    if (!started) {
                                        // the keyword not done
                                        if (!meetEnd) {
                                            keywordIndex--;
                                        }
                                        status = Status.UNSTARRED;
                                        System.out.printf("<STOP> next keyword: %s, index: %d \r\n",
                                                keywordList.get(keywordIndex), keywordIndex);
                                    }
                                }
                            }
                        };
                        new Thread(thread).start();
                    }
                }
            }

        };
    }

    private void initSearchConfig() {
        // select Only show ideas closely related to my search terms
        String js = "var inputs=document.getElementsByTagName('input');"
                + "for(var i=0;i<inputs.length;i++){"
                + "if(inputs[i].type=='checkbox'&&inputs[i].parentNode.children[1].innerHTML=='Only show ideas closely related to my search terms'){inputs[i].click();break;}"
                + "}";
        executeJavascript(js);

        // open advanced option
        js = "findAndClick('a','Advanced Options and Filters');";
        executeJavascript(js);

        // select country & language
        js = "var divs=document.getElementsByTagName('div');" + "for(var i=0;i<divs.length;i++){"
                + "if(divs[i].innerHTML==' Locations and languages '){"
                + "var selectDiv=divs[i].parentNode.children[2];" + "selectDiv.children[0].value='US';"
                + "selectDiv.children[1].value='en';" + "break;" + "}" + "}";
        executeJavascript(js);

        // close advanced option
        js = "findAndClick('a','Advanced Options and Filters');";
        executeJavascript(js);

        // select exact
        js = "var box=findByTagInnerHTML('span','Broad').parentNode.children[0];"
                + "if(box.hasAttribute('checked')) box.click();"
                + "box=findByTagInnerHTML('span','[Exact]').parentNode.children[0];"
                + "if(!box.hasAttribute('checked')) box.click();"
                + "box=findByTagInnerHTML('span','\"Phrase\"').parentNode.children[0];"
                + "if(box.hasAttribute('checked')) box.click();";
        executeJavascript(js);
    }

    private void output(List<AdwordsKeywordInfo> infos, String keyword) {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(new File(outputFolder, keyword + ".csv")));

            bw.write("Keyword,Competition,Global Monthly Searches,Local Monthly Searches (United States)\r\n");

            for (AdwordsKeywordInfo info : infos) {
                bw.write(info.toString() + "\r\n");
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

    private List<AdwordsKeywordInfo> collectData() {
        String js = "var dfns=document.getElementsByTagName('dfn');"
                + "for(var i=0;i<dfns.length;i++){"
                + "if(dfns[i].innerHTML.search(/^Keyword ideas \\(\\d+\\)$/)!=-1){"
                + "return dfns[i].parentNode.parentNode.parentNode.parentNode.children[2].children[0].children[1].children[2].outerHTML;"
                + "}" + "} return null;";
        String tbodyString = (String) executeJavascriptWithResult(js);

        if (debug) {
            System.out.println("javascript:" + js);
            System.out.println("tbody:" + tbodyString);
        }

        if (tbodyString == null) {
            return new ArrayList<AdwordsKeywordInfo>();
        }

        try {
            Parser parser = new Parser(tbodyString);
            ExtractKeywordInfoVistor v = new ExtractKeywordInfoVistor();
            parser.visitAllNodesWith(v);

            return v.results;

        } catch (ParserException e) {
            e.printStackTrace();
            return new ArrayList<AdwordsKeywordInfo>();
        }
    }

    private void navigateToKeywordTool(String CampaignMgmtUrl) {
        //        sleep(5000);
        //        webBrowser.stopLoading();
        //        waitUntilLoadingFinished(true);
        //        String js = "var tds=document.getElementsByTagName('td');"
        //                + "for(var i=0;i<tds.length;i++){"
        //                + "if(tds[i].getAttribute('gwtdebugid')&&tds[i].getAttribute('gwtdebugid')=='aw-cues-bar-reports-menu'){tds[i].click();}"
        //                + "if(tds[i].getAttribute('gwtdebugid')&&tds[i].getAttribute('gwtdebugid')=='aw-cues-item-keyword-tool'){tds[i].click();break;}"
        //                + "}";
        //        webBrowser.executeJavascript(js);

        String __c = "";
        Matcher matcher = cPattern.matcher(CampaignMgmtUrl);
        if (matcher.find()) {
            __c = matcher.group(0);
        } else {
            System.out.println("Cannot find __c for url: " + CampaignMgmtUrl);
        }

        String __u = "";
        matcher = uPattern.matcher(CampaignMgmtUrl);
        if (matcher.find()) {
            __u = matcher.group(0);
        } else {
            System.out.println("Cannot find __u for url: " + CampaignMgmtUrl);
        }

        webBrowser.navigate(String.format(KEYWORD_URL_TPL, __c, __u));
        System.out.println("Going to Keyword tool...");
        status = Status.WAIT_FOR_TARGET_TOOL;
    }

    private void initFuncs() {
        String existKeywordResultFunc = "function existKeywordResult(kw){"
                + "var bs=document.getElementsByTagName('b');"
                + "for(var i=0;i<bs.length;i++){if(bs[i].innerHTML==kw){return true;}}" + "return false;" + "}";
        executeJavascript(existKeywordResultFunc);

        String findAndClickFunc = "function findAndClick(tag, text){" + "var spans=document.getElementsByTagName(tag);"
                + "for(var i=0;i<spans.length;i++){if(spans[i].innerHTML==text){spans[i].click();return;}}" + "}";
        executeJavascript(findAndClickFunc);

        String findByTagInnerHTMLFunc = "function findByTagInnerHTML(tag, text){"
                + "var spans=document.getElementsByTagName(tag);"
                + "for(var i=0;i<spans.length;i++){if(spans[i].innerHTML==text){return spans[i];}} return null;}";
        executeJavascript(findByTagInnerHTMLFunc);

        String getLabelAndClickAndNextFunc = "function getLabelAndClickAndNext(toClick,toNext){"
                + "var label=null;"
                + "var dfns=document.getElementsByTagName('dfn');"
                + "for(var i=0;i<dfns.length;i++){"
                + "if(dfns[i].innerHTML.search(/^Keyword ideas \\(\\d+\\)$/)!=-1){"
                + "label=dfns[i].parentNode.parentNode.parentNode.children[3].children[0].children[0].children[0].innerHTML;"
                + "if(toClick){dfns[i].parentNode.parentNode.parentNode.children[3].children[0].children[0].children[1].click();}"
                + "if(toNext){dfns[i].parentNode.parentNode.parentNode.children[3].children[0].children[0].children[2].children[1].click(); return null;}"
                + "break;}" + "} return label;}";
        executeJavascript(getLabelAndClickAndNextFunc);

        String meetEndFunc = "function meetEnd(){" + "var currentIndexText=getLabelAndClickAndNext(false,false);"
                + "var indexes=currentIndexText.split(/<\\/*b>/);" + "return indexes[3]==indexes[5];" + "}";
        executeJavascript(meetEndFunc);
    }

    private void doLogin() {
        String js = "if(document.getElementById('Email')) document.getElementById('Email').value='%s'; "
                + "if(document.getElementById('Passwd')) document.getElementById('Passwd').value='%s'; "
                + "if(document.getElementById('PersistentCookie')) document.getElementById('PersistentCookie').click(); "
                + "if(document.getElementById('signIn')) document.getElementById('signIn').click();";
        webBrowser.executeJavascript(String.format(js, username, password));
        System.out.println("Going to CampaignMgmt...");
        status = Status.WAIT_FOR_CM;
    }

    private boolean waitUntilLoadingFinished(boolean isInGuiThread) {
        if (!started) {
            return started;
        }
        sleep(1000);
        String js = "return document.getElementById('initialLoading').getAttribute('style').indexOf('display: none')!=-1";
        boolean condition = false;
        while (!condition) {
            if (!started) {
                return started;
            }
            sleep(1000);
            //            System.out.println(webBrowser.executeJavascriptWithResult(
            //                    "return document.getElementById('initialLoading').getAttribute('style')").toString());
            if (isInGuiThread) {
                condition = Boolean.parseBoolean(webBrowser.executeJavascriptWithResult(js).toString());
            } else {
                condition = Boolean.parseBoolean(executeJavascriptWithResult(js).toString());
            }
        }
        if (!started) {
            return started;
        }
        sleep(1000);
        return true;

        //        System.out.println("[END]waitUntilLoadingFinished");
    }

    @Override
    public void run() {
        //        clearSessionCookies();
        started = true;
        webBrowser.navigate("http://adwords.google.com/");
    }

    @Override
    public void stop() {
        started = false;
        webBrowser.stopLoading();
    }

    private static class ExtractKeywordInfoVistor extends NodeVisitor {

        List<AdwordsKeywordInfo> results = new ArrayList<AdwordsKeywordInfo>();

        boolean                  inTr    = false;

        int                      tdIndex = -1;

        AdwordsKeywordInfo       currentKeywordInfo;

        /**
         * Called for each <code>Tag</code> visited.
         * @param tag The tag being visited.
         */
        @Override
        public void visitTag(Tag tag) {
            if (tag instanceof TableRow) {
                inTr = true;
                return;
            }

            if (inTr && tag instanceof TableColumn) {
                tdIndex++;
                if (tdIndex == 0) {
                    currentKeywordInfo = new AdwordsKeywordInfo();
                }
                return;
            }

            if (tdIndex == 1 && tag instanceof LinkTag) {
                NodeList children = tag.getChildren();
                for (int i = 0; i < children.size(); i++) {
                    Node node = children.elementAt(i);
                    if (node instanceof TextNode) {
                        currentKeywordInfo.keyword += node.getText();
                    }
                }
                return;
            }

            if (tdIndex == 2 && tag instanceof Div && tag.getChildren().size() == 1
                    && tag.getChildren().elementAt(0) instanceof TextNode) {
                if (tag.getAttribute("title") == null) {
                    currentKeywordInfo.competition = tag.getChildren().elementAt(0).getText();
                } else {
                    currentKeywordInfo.competition = tag.getAttribute("title");
                }
                return;
            }

            if (tdIndex == 3 && tag instanceof Div && tag.getChildren().size() == 1
                    && tag.getChildren().elementAt(0) instanceof TextNode) {
                String text = tag.getChildren().elementAt(0).getText();
                if ("&lt; 10".equals(text)) {
                    text = "0";
                }
                currentKeywordInfo.globalMonthlySearches = text;
                return;
            }
            if (tdIndex == 4 && tag instanceof Div && tag.getChildren().size() == 1
                    && tag.getChildren().elementAt(0) instanceof TextNode) {
                String text = tag.getChildren().elementAt(0).getText();
                if ("&lt; 10".equals(text)) {
                    text = "0";
                }
                currentKeywordInfo.localMonthlySearches = text;

                inTr = false;
                results.add(currentKeywordInfo);
                tdIndex = -1;
                return;
            }
        }
    }

    @Override
    public String getTitle() {
        return "Adwords";
    }

    /* Standard main method to try that test as a standalone application. */
    public static void main(String[] args) {
        //        NativeInterface.open();
        //        UIUtils.setPreferredLookAndFeel();
        //        SwingUtilities.invokeLater(new Runnable() {
        //            @Override
        //            public void run() {
        //                GoogleController c = new GoogleController();
        //                c.setKeywordList(Arrays.asList("earn money", "save money"));
        //
        //                JFrame frame = new JFrame("DJ Native Swing Test");
        //                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //                frame.getContentPane().add(c.getWebBrowser(), BorderLayout.CENTER);
        //                frame.setSize(1280, 600);
        //                frame.setLocationByPlatform(true);
        //                frame.setVisible(true);
        //                frame.setLocation(0, 0);
        //
        //                c.run();
        //            }
        //        });
        //        NativeInterface.runEventPump();

        //        String s = "<b>1</b> - <b>100</b> of <b>181</b>";
        //        String[] ss = s.split("</*b>");
        //        for (String s1 : ss) {
        //            System.out.println(s1);
        //        }

        //        String tbodyStr = "<tbody><tr class=\"sNX\" __gwt_subrow=\"0\" __gwt_row=\"0\"><td align=\"left\" class=\"sMX sOX sPX \"><div style=\"outline-style: none;\" __gwt_cell=\"cell-gwt-uid-2653\"><div id=\"gwt-debug-column-SELECTION-row-0-0\"><input class=\"sHR\" type=\"checkbox\"/></div></div></td><td align=\"left\" class=\"sMX sOX \"><div style=\"outline-style: none;\" __gwt_cell=\"cell-gwt-uid-2654\"><div id=\"gwt-debug-column-KEYWORD-row-0-1\"><span style=\"white-space: nowrap;\"><span></span><span><a class=\"sJR\" gwtuirendered=\"gwt-uid-2655\">[<b>playing game</b>]</a></span><span></span></span></div></div></td><td align=\"right\" class=\"sMX sOX sJY \"><div style=\"outline-style: none;\" __gwt_cell=\"cell-gwt-uid-2656\"><div id=\"gwt-debug-column-AVERAGE_TARGETED_MONTHLY_SEARCHES-row-0-2\">91</div></div></td></tr><tr class=\"sNY\" __gwt_subrow=\"0\" __gwt_row=\"1\"><td align=\"left\" class=\"sMX sOY sPX \"><div style=\"outline-style: none;\" __gwt_cell=\"cell-gwt-uid-2653\"><div id=\"gwt-debug-column-SELECTION-row-1-0\"><input class=\"sHR\" type=\"checkbox\"/></div></div></td><td align=\"left\" class=\"sMX sOY \"><div style=\"outline-style: none;\" __gwt_cell=\"cell-gwt-uid-2654\"><div id=\"gwt-debug-column-KEYWORD-row-1-1\"><span style=\"white-space: nowrap;\"><span></span><span><a class=\"sJR\" gwtuirendered=\"gwt-uid-2657\">[role <b>playing game</b>]</a></span><span></span></span></div></div></td><td align=\"right\" class=\"sMX sOY sJY \"><div style=\"outline-style: none;\" __gwt_cell=\"cell-gwt-uid-2656\"><div id=\"gwt-debug-column-AVERAGE_TARGETED_MONTHLY_SEARCHES-row-1-2\">720</div></div></td></tr><tr class=\"sNX\" __gwt_subrow=\"0\" __gwt_row=\"2\"><td align=\"left\" class=\"sMX sOX sPX \"><div style=\"outline-style: none;\" __gwt_cell=\"cell-gwt-uid-2653\"><div id=\"gwt-debug-column-SELECTION-row-2-0\"><input class=\"sHR\" type=\"checkbox\"/></div></div></td><td align=\"left\" class=\"sMX sOX \"><div style=\"outline-style: none;\" __gwt_cell=\"cell-gwt-uid-2654\"><div id=\"gwt-debug-column-KEYWORD-row-2-1\"><span style=\"white-space: nowrap;\"><span></span><span><a class=\"sJR\" gwtuirendered=\"gwt-uid-2658\">[game playing]</a></span><span></span></span></div></div></td><td align=\"right\" class=\"sMX sOX sJY \"><div style=\"outline-style: none;\" __gwt_cell=\"cell-gwt-uid-2656\"><div id=\"gwt-debug-column-AVERAGE_TARGETED_MONTHLY_SEARCHES-row-2-2\">91</div></div></td></tr></tbody>";
        //        Parser parser;
        //        try {
        //            parser = new Parser(tbodyStr);
        //            ExtractKeywordInfoVistor v = new ExtractKeywordInfoVistor();
        //            parser.visitAllNodesWith(v);
        //
        //            for (KeywordInfo info : v.results) {
        //                System.out.println(info.toString());
        //            }
        //        } catch (ParserException e) {
        //            e.printStackTrace();
        //        }

        String url = "https://adwords.google.com/cm/CampaignMgmt?__c=2012996510&__u=6295170950#r.ONLINE&app=cm";

        String regex = "__u=\\d+";

        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(url);

        System.out.println(matcher.find());

        System.out.println(matcher.group(0));
    }
}
