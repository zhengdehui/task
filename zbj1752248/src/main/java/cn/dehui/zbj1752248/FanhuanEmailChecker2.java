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
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

public class FanhuanEmailChecker2 extends EmailChecker {

    //    private static final String CHECK_URL_TEMPLATE = "http://passport.fanhuan.com/ajax/existedIdentifier?";
    private static final String CHECK_URL_TEMPLATE = "http://passport.fanhuan.com/ajax/reg?";

    public static int           sleepTime          = 100;

    public FanhuanEmailChecker2(List<String> emailList, String outputFolder, CountDownLatch startSignal,
            CountDownLatch doneSignal, JLabel label, JProgressBar bar) {
        super(emailList, outputFolder, startSignal, doneSignal, label, bar);
    }

    public FanhuanEmailChecker2(List<String> emailList, String outputFolder, CountDownLatch startSignal,
            CountDownLatch doneSignal, JLabel label) {
        super(emailList, outputFolder, startSignal, doneSignal, label, null);
    }

    public FanhuanEmailChecker2(List<String> emailList, String outputFolder, CountDownLatch startSignal,
            CountDownLatch doneSignal) {
        super(emailList, outputFolder, startSignal, doneSignal, null, null);
    }

    @Override
    public boolean check(String email) throws Exception {
        String url = CHECK_URL_TEMPLATE + getEmailParamStr(email);
        HttpGet httpGet = new HttpGet(url);
        setFireFoxHeaders(httpGet);
        httpGet.setHeader("Referer", "http://passport.fanhuan.com/reg/");
        HttpResponse response = client.execute(httpGet);
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != 200) {
            throw new Exception(String.format("Fanhuan Status Code: %d, email: %s", statusCode, email));
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        // {"Error":null,"Result":{"Succeed":true,"Remark":"123314025@qq.com"}}
        // ({"Error":{"Code":204,,"Msg":"您输入的邮箱已存在！"},"Result":null})
        String line = br.readLine();
        br.close();

        return !line.contains("\"Keyword\":\"existed_email\"");
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
        return "/ffff";
    }

    @Override
    protected long getPauseTime() {
        return sleepTime;
    }
}
