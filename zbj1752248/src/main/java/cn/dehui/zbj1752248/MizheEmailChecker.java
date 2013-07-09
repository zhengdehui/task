package cn.dehui.zbj1752248;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.swing.JLabel;
import javax.swing.JProgressBar;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class MizheEmailChecker extends EmailChecker {

    private static final String CHECK_URL_TEMPLATE = "http://www.mizhe.com/member/available.html?t=";

    public static int           sleepTime          = 100;

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
        String token = getToken();

        String url = CHECK_URL_TEMPLATE + System.currentTimeMillis();
        HttpPost httpPost = new HttpPost(url);
        setFireFoxHeaders(httpPost);
        httpPost.setHeader("Referer", "http://www.mizhe.com/member/register.html");
        httpPost.setHeader("X-Requested-With", "XMLHttpRequest");

        // email=123314025%40qq.com&_csrf_token=3ee778e6cad8b99ebf8d23416790f5a8
        StringEntity stringEntity = new StringEntity(String.format("email=%s&_csrf_token=%s",
                email.replaceAll("@", "%40"), token), "UTF-8");
        stringEntity.setContentType("application/x-www-form-urlencoded; charset=UTF-8");
        httpPost.setEntity(stringEntity);

        HttpResponse response = client.execute(httpPost);
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != 200) {
            throw new Exception(String.format("Fanhuan Status Code: %d, email: %s", statusCode, email));
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        // {"result":false,"message":" \u8be5\u90ae\u7bb1\u5df2\u7ecf\u88ab\u4f7f\u7528\u4e86 "}
        String line = br.readLine();
        br.close();

        return line.contains("\"result\":true");
    }

    String getToken() throws Exception {
        String url = "http://www.mizhe.com/member/register.html";
        HttpGet httpGet = new HttpGet(url);
        setFireFoxHeaders(httpGet);

        HttpResponse response = client.execute(httpGet);
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != 200) {
            throw new Exception(url + " unreachable!");
        }

        String html = EntityUtils.toString(response.getEntity());

        int keyIndex = html.indexOf("g.__t__");
        int begin = html.indexOf("\"", keyIndex) + 1;
        int end = html.indexOf("\"", begin);

        return html.substring(begin, end);
    }

    private String getEmailParamStr(String email) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        //        params.add(new BasicNameValuePair("identifier", email));
        params.add(new BasicNameValuePair("username", "zhengdehui"));
        params.add(new BasicNameValuePair("email", email));
        params.add(new BasicNameValuePair("password", "1qaz2wsx"));
        params.add(new BasicNameValuePair("checkcode", ""));
        params.add(new BasicNameValuePair("regpage", "http://passport.fanhuan.com/reg/"));
        params.add(new BasicNameValuePair("_", System.currentTimeMillis() + ""));

        return URLEncodedUtils.format(params, "utf-8");
    }

    @Override
    protected String getCategory() {
        return "/mmmm";
    }

    @Override
    protected long getPauseTime() {
        return sleepTime;
    }

    public static void main(String[] args) throws Exception {
        EmailChecker checker = new MizheEmailChecker(null, null, null, null);

        System.out.println(checker.check("76778165@qq.com"));
        System.out.println(checker.check("123314025@qq.com"));
    }
}
