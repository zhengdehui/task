package cn.dehui.zbj1752248;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.swing.JLabel;
import javax.swing.JProgressBar;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.InputTag;
import org.htmlparser.tags.ParagraphTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

public class MizheEmailChecker extends EmailChecker {

    private static final String UTF8         = "utf8";

    private static final String REGISTER_URL = "http://www.mizhe.com/member/register.html";

    private static final String LOGIN_URL    = "http://www.mizhe.com/member/login.html";

    public static int           sleepTime    = 100;

    public MizheEmailChecker(List<String> emailList, String outputFolder, CountDownLatch startSignal,
            CountDownLatch doneSignal, JLabel label, JProgressBar bar) {
        super(emailList, outputFolder, startSignal, doneSignal, label, bar);
    }

    public MizheEmailChecker(List<String> emailList, String outputFolder, CountDownLatch startSignal,
            CountDownLatch doneSignal, JLabel label) {
        super(emailList, outputFolder, startSignal, doneSignal, label, null);
    }

    public MizheEmailChecker(List<String> emailList, String outputFolder, CountDownLatch startSignal,
            CountDownLatch doneSignal) {
        super(emailList, outputFolder, startSignal, doneSignal, null, null);
    }

    @Override
    public boolean check(String email) throws Exception {
        CheckCodePair pair = getCheckCodeAndSign();

        return checkByMobileLogin(email, pair);
    }

    private boolean checkByMobileLogin(String email, CheckCodePair pair) throws UnsupportedEncodingException,
            IOException, ClientProtocolException, Exception, ParserException {
        HttpPost httpPost = new HttpPost(LOGIN_URL);
        setHeaders(httpPost);
        httpPost.setHeader("Referer", LOGIN_URL);
        httpPost.setHeader("Origin", "http://www.mizhe.com");

        StringEntity stringEntity = new StringEntity(getPayload(email, pair), UTF8);
        stringEntity.setContentType("application/x-www-form-urlencoded");
        httpPost.setEntity(stringEntity);

        HttpResponse response = null;
        try {
            response = client.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                throw new Exception(String.format("Mizhe Status Code: %d, email: %s", statusCode, email));
            }

            HttpEntity entity = response.getEntity();
            Parser parser = Parser.createParser(EntityUtils.toString(entity, UTF8), UTF8);

            TagNameFilter tagNameFilter = new TagNameFilter("p");
            HasAttributeFilter hasErrorFilter = new HasAttributeFilter("class", "error-msg");
            AndFilter andFilter = new AndFilter(tagNameFilter, hasErrorFilter);

            NodeList nodeList = parser.extractAllNodesThatMatch(andFilter);
            if (nodeList.size() == 0) {
                throw new Exception(String.format("Mizhe check error, email: %s", email));
            }

            ParagraphTag paragraphTag = (ParagraphTag) nodeList.elementAt(0);
            String text = paragraphTag.getChildrenHTML();
            return "用户名不存在".equals(text);
        } finally {
            if (response != null && response.getEntity() != null) {
                EntityUtils.consumeQuietly(response.getEntity());
            }
        }
    }

    private CheckCodePair getCheckCodeAndSign() throws Exception {
        HttpResponse response = null;
        try {
            HttpGet httpGet = new HttpGet(REGISTER_URL);
            setHeaders(httpGet);

            response = client.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            if (statusCode != 200) {
                throw new Exception(REGISTER_URL + " unreachable!");
            }

            Parser parser = Parser.createParser(EntityUtils.toString(entity, UTF8), UTF8);
            TagNameFilter tagNameFilter = new TagNameFilter("input");

            HasAttributeFilter hasSignFilter = new HasAttributeFilter("name", "sign");
            HasAttributeFilter hasCheckCodeFilter = new HasAttributeFilter("name", "checkcode");

            OrFilter orFilter = new OrFilter(hasSignFilter, hasCheckCodeFilter);

            AndFilter andFilter = new AndFilter(tagNameFilter, orFilter);

            NodeList nodeList = parser.extractAllNodesThatMatch(andFilter);

            CheckCodePair pair = new CheckCodePair();

            for (int i = 0; i < 2; i++) {
                InputTag input = (InputTag) nodeList.elementAt(i);
                if (input.getAttribute("name").equals("sign")) {
                    pair.sign = input.getAttribute("value");
                } else {
                    pair.checkCode = input.getAttribute("value");
                }
            }
            return pair;
        } finally {
            if (response != null && response.getEntity() != null) {
                EntityUtils.consumeQuietly(response.getEntity());
            }
        }
    }

    private class CheckCodePair {
        String sign;

        String checkCode;
    }

    private String getPayload(String email, CheckCodePair pair) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        //        params.add(new BasicNameValuePair("identifier", email));
        params.add(new BasicNameValuePair("done", ""));
        params.add(new BasicNameValuePair("sign", pair.sign));
        params.add(new BasicNameValuePair("email", email));
        params.add(new BasicNameValuePair("passwd", System.currentTimeMillis() + ""));
        params.add(new BasicNameValuePair("checkcode", pair.checkCode));

        return URLEncodedUtils.format(params, UTF8);
    }

    @Override
    protected String getCategory() {
        return "/mmmm";
    }

    @Override
    protected long getPauseTime() {
        return sleepTime;
    }

    @Override
    protected String getUserAgent() {
        return "Mozilla/5.0 (iPhone; CPU iPhone OS 5_0 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9A334 Safari/7534.48.3";
    }

    public static void main(String[] args) throws Exception {
        EmailChecker checker = new MizheEmailChecker(null, null, null, null);

        System.out.println(checker.check("76778165@qq.com"));
        System.out.println(checker.check("123314025@qq.com"));
        System.out.println(checker.check("dhzheng3@foxmail.com"));
        System.out.println(checker.check("k_zheng@21cn.com"));
    }
}
