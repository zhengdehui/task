package cn.dehui.task.keywordtool.selenium.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.http.client.ClientProtocolException;
import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.Text;
import org.htmlparser.tags.Div;
import org.htmlparser.tags.TableColumn;
import org.htmlparser.tags.TableRow;
import org.htmlparser.util.ParserException;
import org.htmlparser.visitors.NodeVisitor;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import cn.dehui.task.keywordtool.browser.controller.util.Callback;

public class AdwordsController implements Runnable {

    private static final String __C_REGEX           = "__c=\\d+";

    private static final String __U_REGEX           = "__u=\\d+";

    private static Pattern      cPattern            = Pattern.compile(__C_REGEX);

    private static Pattern      uPattern            = Pattern.compile(__U_REGEX);

    private static final String KEYWORD_PLANNER_URL = "https://adwords.google.com/ko/KeywordPlanner/Home";

    private static final String KEYWORD_URL_TPL     = KEYWORD_PLANNER_URL + "?%s&%s&__o=cues";

    private WebDriver           driver;

    protected List<String>      keywordList;

    protected int               keywordIndex;

    protected boolean           started             = false;

    protected String            password            = "19861103";

    protected String            username            = "dhzheng3@gmail.com";

    protected File              outputFolder;

    protected boolean           debug               = false;

    @Override
    public void run() {
        started = true;
        if (driver == null) {
            //            File profileDir = new File("C:/Users/dehui/AppData/Roaming/Mozilla/Firefox/Profiles/uyz0f0u0.default");
            //            if (!profileDir.exists()) {
            //                profileDir.mkdir();
            //            }
            //            FirefoxProfile profile = new FirefoxProfile(profileDir);
            //            profile.setEnableNativeEvents(true);
            //            driver = new FirefoxDriver(profile);
            driver = new FirefoxDriver();
            driver.manage().window().maximize();
        }

        //        driver.get("https://adwords.google.com");
        driver.get(KEYWORD_PLANNER_URL);

        if (driver.getCurrentUrl().startsWith("https://accounts.google.com/ServiceLogin")) {
            doLogin();
        }

        goToKeywordPlanner();

        while (started) {

            if (keywordIndex == keywordList.size()) {
                System.out.println("All done.");
                break;
            }

            if (keywordIndex % 10 == 9) {
                driver.get(driver.getCurrentUrl());
                goToKeywordPlanner();
            }

            String keyword = keywordList.get(keywordIndex++);
            System.out.printf("[%s] started, number: %d.\r\n", keyword, keywordIndex - 1);

            waitUntilLoadingFinished();

            // input keyword
            WebElement keywordInput = driver.findElement(By.id("gwt-debug-search-bar")).findElement(
                    By.id("gwt-debug-keywords-text-area"));
            keywordInput.clear();
            keywordInput.sendKeys(keyword);
            keywordInput.click();

            // input Include terms
            driver.findElement(By.id("gwt-debug-search-page"))
                    .findElement(By.id("gwt-debug-include-exclude-pill-display-text-div")).click();
            WebElement includeTerms = driver.findElement(By.id("gwt-debug-include-exclude-pill-input-editor"))
                    .findElement(By.tagName("textarea"));
            includeTerms.clear();
            includeTerms.sendKeys(keyword);
            driver.findElement(By.id("gwt-debug-search-page"))
                    .findElement(By.id("gwt-debug-include-exclude-pill-display-text-div")).click();

            // click search

            System.out.printf("[%s] searching.\r\n", keyword);

            waitUntilLoadingFinished();

            // click 'Keyword ideas'
            driver.findElement(By.id("gwt-debug-search-page"))
                    .findElement(By.id("gwt-debug-grouping-toggle-KEYWORD_IDEAS")).click();

            waitUntilLoadingFinished();

            WebElement keywordTable = driver.findElement(By.id("gwt-debug-search-page")).findElement(
                    By.id("gwt-debug-keyword-table"));

            String initLabel = keywordTable.findElement(By.id("gwt-debug-idea-paging-range")).getText();
            //            System.out.printf("[%s] data collecting: %s \r\n", keyword, initLabel);
            if ("".equals(initLabel)) {
                System.out.printf("[%s] finished.\r\n", keyword);
                output(new ArrayList<AdwordsKeywordInfo>(), keyword);
                continue;
            }

            String[] labelSlices = initLabel.split(" ");
            // change to 100 items
            if (!labelSlices[2].equals(labelSlices[4]) && !"100".equals(labelSlices[2])) {
                keywordTable.findElement(By.id("gwt-debug-idea-paging-selection")).click();

                driver.findElement(By.className("popupContent")).findElement(By.xpath("div[1]/div[1]/div[7]")).click();
            }

            List<AdwordsKeywordInfo> infos = new ArrayList<AdwordsKeywordInfo>();
            boolean meetEnd = false;
            do {
                waitUntilLoadingFinished();
                if (!started) {
                    break;
                }
                //collect data
                keywordTable = driver.findElement(By.id("gwt-debug-search-page")).findElement(
                        By.id("gwt-debug-keyword-table"));
                String label = keywordTable.findElement(By.id("gwt-debug-idea-paging-range")).getText();
                System.out.printf("[%s] data collecting: %s \r\n", keyword, label);

                String tbodyString = (String) ((JavascriptExecutor) driver).executeScript(
                        "return arguments[0].outerHTML;", keywordTable.findElements(By.tagName("tbody")).get(2));
                infos.addAll(collectData(tbodyString));

                meetEnd = meetEnd(label);

                if (meetEnd) {
                    System.out.printf("[%s] finished.\r\n", keyword);
                    output(infos, keyword);
                    break;
                } else {
                    System.out.printf("[%s] next page.\r\n", keyword);
                    keywordTable.findElement(By.id("gwt-debug-idea-paging-next-content")).click();
                }
            } while (started);

            if (!started) {
                // the keyword not done
                if (!meetEnd) {
                    keywordIndex--;
                }
                System.out.printf("<STOP> next keyword: %s, index: %d \r\n", keywordList.get(keywordIndex),
                        keywordIndex);
            }
        }
    }

    private void output(List<AdwordsKeywordInfo> infos, String keyword) {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(new File(outputFolder, keyword + ".csv")));

            bw.write("Keyword,Competition,Avg. Monthly Searches\r\n");

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

    private boolean meetEnd(String label) {
        String[] labelSlices = label.split(" ");
        return labelSlices[2].equals(labelSlices[4]);
    }

    private List<AdwordsKeywordInfo> collectData(String tbodyString) {
        if (debug) {
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

    private void waitUntilLoadingFinished() {
        final long time = System.currentTimeMillis() + 100;
        WebDriverWait myWait = new WebDriverWait(driver, 10);
        ExpectedCondition<Boolean> conditionToCheck = new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver input) {
                try {
                    return System.currentTimeMillis() > time
                            && driver.findElement(By.id("initialLoading")).getAttribute("style")
                                    .contains("display: none");
                } catch (NoSuchElementException e) {
                    return false;
                }
            }
        };
        myWait.until(conditionToCheck);
    }

    private void goToKeywordPlanner() {

        Callback<Void> clickSplashPanel = new Callback<Void>() {
            @Override
            public Void execute() {
                driver.findElement(By.id("gwt-debug-splash-panel-search-selection-input")).click();
                return null;
            }
        };

        waitUntil(clickSplashPanel);

        // select United States
        WebElement splashPanel = driver.findElement(By.id("gwt-debug-splash-panel-form"));
        splashPanel.findElement(By.id("gwt-debug-location-pill-display-text-div")).click();
        WebElement locationInputEditor = driver.findElement(By.id("gwt-debug-location-pill-input-editor"));
        locationInputEditor.findElement(By.linkText("Remove all")).click();
        locationInputEditor.findElement(By.id("gwt-debug-geo-search-box")).sendKeys("United States");

        Callback<Void> selectLocation = new Callback<Void>() {
            @Override
            public Void execute() {
                driver.findElement(By.id("gwt-debug-geo-suggestions-pop-up"))
                        .findElement(By.id("gwt-debug-geotargets-table")).findElement(By.linkText("United States"))
                        .click();
                return null;
            }
        };
        waitUntil(selectLocation);
        splashPanel.click();

        // select option
        splashPanel = driver.findElement(By.id("gwt-debug-splash-panel-form"));
        splashPanel.findElement(By.id("gwt-debug-keyword-options-pill-display-text-div")).click();

        Callback<Void> selectOption = new Callback<Void>() {
            @Override
            public Void execute() {
                driver.findElement(
                        By.xpath("id('gwt-debug-keyword-options-pill-input-editor')/div[1]/div[1]/div[1]/div[1]"))
                        .click();
                return null;
            }
        };
        waitUntil(selectOption);
        splashPanel.click();

        splashPanel.findElement(By.id("gwt-debug-keywords-text-area")).sendKeys("hello");
        splashPanel.findElement(By.id("gwt-debug-search-button-content")).click();
    }

    private void waitUntil(final Callback<Void> callback) {
        WebDriverWait myWait = new WebDriverWait(driver, 10);
        ExpectedCondition<Boolean> conditionToCheck = new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver input) {
                try {
                    callback.execute();
                    return true;
                } catch (NoSuchElementException e) {
                    return false;
                }
            }
        };
        myWait.until(conditionToCheck);
    }

    private void doLogin() {
        driver.findElement(By.id("Email")).sendKeys(username);
        driver.findElement(By.id("Passwd")).sendKeys(password);
        driver.findElement(By.id("signIn")).click();
    }

    public void stop() {
        started = false;
    }

    public void destroy() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    public void setKeywordList(List<String> keywordList) {
        this.keywordList = keywordList;
        keywordIndex = 0;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setOutputFolder(File outputFolder) {
        this.outputFolder = outputFolder;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    private static class ExtractKeywordInfoVistor extends NodeVisitor {

        List<AdwordsKeywordInfo> results = new ArrayList<AdwordsKeywordInfo>();

        boolean                  inTr    = false;

        int                      tdIndex = -1;

        AdwordsKeywordInfo       currentKeywordInfo;

        @Override
        public void visitStringNode(Text string) {
            if (tdIndex == 1) {
                String text = string.getText().trim();
                if ("&lt; 10".equals(text)) {
                    text = "0";
                }
                currentKeywordInfo.avgMonthlySearches = text;
                return;
            }

            if (tdIndex == 2) {
                currentKeywordInfo.competition = string.getText();

                inTr = false;
                results.add(currentKeywordInfo);
                tdIndex = -1;
                return;
            }
        }

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

            if (tdIndex == 0 && tag instanceof Div && ((Div) tag).getAttribute("title") != null) {
                currentKeywordInfo.keyword = ((Div) tag).getAttribute("title");
                return;
            }
        }
    }

    public static void main(String[] args) throws ClientProtocolException, IOException, ParserException {
        //        HttpGet httpGet = new HttpGet(
        //                "https://storage.cloud.google.com/planner-download/download-eff3f411-7e91-462a-951c-fd8780c3c676.csv");
        //
        //        HttpClient client = new DefaultHttpClient();
        //
        //        HttpResponse httpResponse = client.execute(httpGet);
        //
        //        System.out.println(EntityUtils.toString(httpResponse.getEntity()));

        String s = "<tbody style=\"\"><tr __gwt_row=\"0\" __gwt_subrow=\"0\" class=\"spLX spN-\"><td class=\"spKX spMX spNX \" align=\"left\"><div style=\"outline-style:none;\" __gwt_cell=\"cell-gwt-uid-3011\"><div id=\"gwt-debug-column-KEYWORD-row-0-0\"><div dir=\"auto\" class=\"spL-\" title=\"rogers jewelry store\">rogers jewelry store</div></div></div></td><td class=\"spKX spMX \" align=\"right\"><div style=\"outline-style:none;\" __gwt_cell=\"cell-gwt-uid-3012\"><div id=\"gwt-debug-column-SEARCH_VOLUME-row-0-1\"><span class=\"spK-\"></span> 110</div></div></td><td class=\"spKX spMX \" align=\"left\"><div style=\"outline-style:none;\" __gwt_cell=\"cell-gwt-uid-3013\"><div id=\"gwt-debug-column-COMPETITION-row-0-2\">High</div></div></td><td class=\"spKX spMX \" align=\"right\"><div style=\"outline-style:none;\" __gwt_cell=\"cell-gwt-uid-3014\"><div id=\"gwt-debug-column-SUGGESTED_BID-row-0-3\">CN¥7.37</div></div></td><td class=\"spKX spMX \" align=\"right\"><div style=\"outline-style:none;\" __gwt_cell=\"cell-gwt-uid-3015\"><div id=\"gwt-debug-column-AD_IMPRESSION_SHARE-row-0-4\">0%</div></div></td><td class=\"spKX spMX spHY \" align=\"center\"><div style=\"outline-style:none;\" __gwt_cell=\"cell-gwt-uid-3016\"><div id=\"gwt-debug-column-SAVE-row-0-5\"><div style=\"position:relative\"><div class=\"spHX\"><div class=\"spI-\"></div></div><div id=\"gwt-debug-action\" class=\"spFX spH-\" tabindex=\"0\"></div><div class=\"spIX spG-\">ACCOUNT</div></div></div></div></td></tr><tr __gwt_row=\"1\" __gwt_subrow=\"0\" class=\"spLY spN-\"><td class=\"spKX spMY spNX \" align=\"left\"><div style=\"outline-style:none;\" __gwt_cell=\"cell-gwt-uid-3011\"><div id=\"gwt-debug-column-KEYWORD-row-1-0\"><div dir=\"auto\" class=\"spL-\" title=\"jewelry stores in rogers ar\">jewelry stores in rogers ar</div></div></div></td><td class=\"spKX spMY \" align=\"right\"><div style=\"outline-style:none;\" __gwt_cell=\"cell-gwt-uid-3012\"><div id=\"gwt-debug-column-SEARCH_VOLUME-row-1-1\"><span class=\"spK-\"></span> 70</div></div></td><td class=\"spKX spMY \" align=\"left\"><div style=\"outline-style:none;\" __gwt_cell=\"cell-gwt-uid-3013\"><div id=\"gwt-debug-column-COMPETITION-row-1-2\">High</div></div></td><td class=\"spKX spMY \" align=\"right\"><div style=\"outline-style:none;\" __gwt_cell=\"cell-gwt-uid-3014\"><div id=\"gwt-debug-column-SUGGESTED_BID-row-1-3\">CN¥12.82</div></div></td><td class=\"spKX spMY \" align=\"right\"><div style=\"outline-style:none;\" __gwt_cell=\"cell-gwt-uid-3015\"><div id=\"gwt-debug-column-AD_IMPRESSION_SHARE-row-1-4\">0%</div></div></td><td class=\"spKX spMY spHY \" align=\"center\"><div style=\"outline-style:none;\" __gwt_cell=\"cell-gwt-uid-3016\"><div id=\"gwt-debug-column-SAVE-row-1-5\"><div style=\"position:relative\"><div class=\"spHX\"><div class=\"spI-\"></div></div><div id=\"gwt-debug-action\" class=\"spFX spH-\" tabindex=\"0\"></div><div class=\"spIX spG-\">ACCOUNT</div></div></div></div></td></tr><tr __gwt_row=\"2\" __gwt_subrow=\"0\" class=\"spLX spN-\"><td class=\"spKX spMX spNX \" align=\"left\"><div style=\"outline-style:none;\" __gwt_cell=\"cell-gwt-uid-3011\"><div id=\"gwt-debug-column-KEYWORD-row-2-0\"><div dir=\"auto\" class=\"spL-\" title=\"rogers and holland jewelry\">rogers and holland jewelry</div></div></div></td><td class=\"spKX spMX \" align=\"right\"><div style=\"outline-style:none;\" __gwt_cell=\"cell-gwt-uid-3012\"><div id=\"gwt-debug-column-SEARCH_VOLUME-row-2-1\"><span class=\"spK-\"></span> 70</div></div></td><td class=\"spKX spMX \" align=\"left\"><div style=\"outline-style:none;\" __gwt_cell=\"cell-gwt-uid-3013\"><div id=\"gwt-debug-column-COMPETITION-row-2-2\">Low</div></div></td><td class=\"spKX spMX \" align=\"right\"><div style=\"outline-style:none;\" __gwt_cell=\"cell-gwt-uid-3014\"><div id=\"gwt-debug-column-SUGGESTED_BID-row-2-3\">CN¥0.00</div></div></td><td class=\"spKX spMX \" align=\"right\"><div style=\"outline-style:none;\" __gwt_cell=\"cell-gwt-uid-3015\"><div id=\"gwt-debug-column-AD_IMPRESSION_SHARE-row-2-4\">0%</div></div></td><td class=\"spKX spMX spHY \" align=\"center\"><div style=\"outline-style:none;\" __gwt_cell=\"cell-gwt-uid-3016\"><div id=\"gwt-debug-column-SAVE-row-2-5\"><div style=\"position:relative\"><div class=\"spHX\"><div class=\"spI-\"></div></div><div id=\"gwt-debug-action\" class=\"spFX spH-\" tabindex=\"0\"></div><div class=\"spIX spG-\">ACCOUNT</div></div></div></div></td></tr><tr __gwt_row=\"3\" __gwt_subrow=\"0\" class=\"spLY spN-\"><td class=\"spKX spMY spNX \" align=\"left\"><div style=\"outline-style:none;\" __gwt_cell=\"cell-gwt-uid-3011\"><div id=\"gwt-debug-column-KEYWORD-row-3-0\"><div dir=\"auto\" class=\"spL-\" title=\"rogers and hollands jewelry\">rogers and hollands jewelry</div></div></div></td><td class=\"spKX spMY \" align=\"right\"><div style=\"outline-style:none;\" __gwt_cell=\"cell-gwt-uid-3012\"><div id=\"gwt-debug-column-SEARCH_VOLUME-row-3-1\"><span class=\"spK-\"></span> 20</div></div></td><td class=\"spKX spMY \" align=\"left\"><div style=\"outline-style:none;\" __gwt_cell=\"cell-gwt-uid-3013\"><div id=\"gwt-debug-column-COMPETITION-row-3-2\">Low</div></div></td><td class=\"spKX spMY \" align=\"right\"><div style=\"outline-style:none;\" __gwt_cell=\"cell-gwt-uid-3014\"><div id=\"gwt-debug-column-SUGGESTED_BID-row-3-3\">CN¥0.00</div></div></td><td class=\"spKX spMY \" align=\"right\"><div style=\"outline-style:none;\" __gwt_cell=\"cell-gwt-uid-3015\"><div id=\"gwt-debug-column-AD_IMPRESSION_SHARE-row-3-4\">0%</div></div></td><td class=\"spKX spMY spHY \" align=\"center\"><div style=\"outline-style:none;\" __gwt_cell=\"cell-gwt-uid-3016\"><div id=\"gwt-debug-column-SAVE-row-3-5\"><div style=\"position:relative\"><div class=\"spHX\"><div class=\"spI-\"></div></div><div id=\"gwt-debug-action\" class=\"spFX spH-\" tabindex=\"0\"></div><div class=\"spIX spG-\">ACCOUNT</div></div></div></div></td></tr></tbody>";

        Parser parser = new Parser(s);
        ExtractKeywordInfoVistor v = new ExtractKeywordInfoVistor();
        parser.visitAllNodesWith(v);

        System.out.println(v.results.size());
    }
}
