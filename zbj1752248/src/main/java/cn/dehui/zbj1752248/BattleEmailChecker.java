package cn.dehui.zbj1752248;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.htmlparser.Parser;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.tags.InputTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import cn.dehui.zbj1752248.util.AdslUtil;

public class BattleEmailChecker extends EmailChecker {

    private static final String CHECK_URL_TEMPLATE = "https://us.battle.net/account/creation/tos.html";

    public static int           sleepTime          = 100;

    public BattleEmailChecker(List<String> emailList, String outputFolder, CountDownLatch startSignal,
            CountDownLatch doneSignal, JLabel label, JProgressBar bar) {
        super(emailList, outputFolder, startSignal, doneSignal, label, bar);
    }

    public BattleEmailChecker(List<String> emailList, String outputFolder, CountDownLatch startSignal,
            CountDownLatch doneSignal, JLabel label) {
        this(emailList, outputFolder, startSignal, doneSignal, label, null);
    }

    public BattleEmailChecker(List<String> emailList, String outputFolder, CountDownLatch startSignal,
            CountDownLatch doneSignal) {
        this(emailList, outputFolder, startSignal, doneSignal, null, null);
    }

    @Override
    public boolean check(String email) throws Exception {
        HttpGet httpGet = new HttpGet(CHECK_URL_TEMPLATE);
        setHeaders(httpGet);
        HttpResponse response = null;
        try {
            response = client.execute(httpGet);

            // <input id="csrftoken" type="hidden" name="csrftoken" value="d39377dc-899c-4bcc-88a4-aa431bb55961"/>
            HttpEntity entity = response.getEntity();
            Parser parser = Parser.createParser(EntityUtils.toString(entity, "UTF-8"), "UTF8");
            EntityUtils.consume(entity);

            NodeList nodeList = parser.extractAllNodesThatMatch(new HasAttributeFilter("id", "csrftoken"));
            InputTag inputTag = (InputTag) nodeList.elementAt(0);
            String csrftoken = inputTag.getAttribute("value");

            //        System.out.println(csrftoken);

            HttpPost httpPost = new HttpPost(CHECK_URL_TEMPLATE);
            setHeaders(httpPost);
            httpPost.setHeader("Referer",
                    "https://us.battle.net/account/creation/tos.html?ref=https://us.battle.net/account/creation/tos.html");

            email = email.replaceAll("@", "%40");
            StringEntity stringEntity = new StringEntity(
                    String.format(
                            "csrftoken=%s&country=USA&dobMonth=3&dobDay=6&dobYear=1979&firstname=aaa&lastname=aaa&emailAddress=%s&emailAddressConfirmation=%s&password=1qaz2wsy&rePassword=1qaz2wsx&question1=16&answer1=aaa&agreedToToU=true",
                            csrftoken, email, email), "UTF-8");
            stringEntity.setContentType("application/x-www-form-urlencoded");
            httpPost.setEntity(stringEntity);
            response = client.execute(httpPost);

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                throw new Exception(String.format("Battle.net Status Code: %d, email: %s", statusCode, email));
            }

            entity = response.getEntity();
            String postResponse = EntityUtils.toString(entity, "UTF-8");
            EntityUtils.consume(entity);
            //        System.out.println(postResponse);

            parser = Parser.createParser(postResponse, "UTF8");

            // <p class="error.required">請填寫所有必填的欄位。</p>
            // <li class="error.email.unavailable">此電子郵件地址已被使用。</li>
            // <li class="password.nomatch">請輸入正確的密碼。</li>

            if (hasClassElement(parser, "error.required")) {
                // reconnect adsl
                AdslUtil.renewIP();

                throw new Exception("reconnect adsl");
            } else if (hasClassElement(parser, "password.nomatch")) {
                return !hasClassElement(parser, "error.email.unavailable");
            } else {
                // unknown error
                System.out.println(postResponse);
                throw new Exception("unknown error");
            }
        } finally {
            if (response != null && response.getEntity() != null) {
                EntityUtils.consumeQuietly(response.getEntity());
            }
        }
    }

    private boolean hasClassElement(Parser parser, String className) throws ParserException {
        parser.reset();
        NodeList nodeList = parser.extractAllNodesThatMatch(new HasAttributeFilter("class", className));

        return nodeList.size() > 0;
    }

    @Override
    protected String getCategory() {
        return "/battle";
    }

    @Override
    protected long getPauseTime() {
        return sleepTime;
    }

    private TrustManager createAllTrustManager() {
        TrustManager easyTrustManager = new X509TrustManager() {

            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] x509Certificates, String s)
                    throws java.security.cert.CertificateException {
            }

            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] x509Certificates, String s)
                    throws java.security.cert.CertificateException {
            }

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };
        return easyTrustManager;
    }

    public static void main(String[] args) throws Exception {
        EmailChecker checker = new BattleEmailChecker(null, null, null, null);

        System.out.println(checker.check("76778165@qq.com"));
        System.out.println(checker.check("123314025@qq.com"));
        //        NodeList nodeList;
        //        Parser parser = Parser.createParser("<p class=\"password.nomatch\">請輸入正確的密碼。</p>", "UTF8");
        //        nodeList = parser.extractAllNodesThatMatch(new HasAttributeFilter("class", "error.required"));
        //        System.out.println(nodeList.size());
        //        parser.reset();
        //        nodeList = parser.extractAllNodesThatMatch(new HasAttributeFilter("class", "password.nomatch"));
        //        System.out.println(nodeList.size());
    }
}
