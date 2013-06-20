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

public class FanhuanEmailChecker extends EmailChecker {

    //    private static final String CHECK_URL_TEMPLATE = "http://www.fanhuan.com/ajax/registered?";
    private static final String CHECK_URL_TEMPLATE = "http://passport.fanhuan.com/ajax/existedIdentifier?";

    public FanhuanEmailChecker(List<String> emailList, String outputFolder, CountDownLatch startSignal,
            CountDownLatch doneSignal, JLabel label, JProgressBar bar) {
        super(emailList, outputFolder, startSignal, doneSignal, label, bar);
    }

    public FanhuanEmailChecker(List<String> emailList, String outputFolder, CountDownLatch startSignal,
            CountDownLatch doneSignal, JLabel label) {
        super(emailList, outputFolder, startSignal, doneSignal, label, null);
    }

    public FanhuanEmailChecker(List<String> emailList, String outputFolder, CountDownLatch startSignal,
            CountDownLatch doneSignal) {
        super(emailList, outputFolder, startSignal, doneSignal, null, null);
    }

    @Override
    public boolean check(String email) throws Exception {
        String url = CHECK_URL_TEMPLATE + getEmailParamStr(email);
        HttpGet httpGet = new HttpGet(url);
        setFireFoxHeaders(httpGet);
        HttpResponse response = client.execute(httpGet);
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != 200) {
            throw new Exception(String.format("Fanhuan Status Code: %d, email: %s", statusCode, email));
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String line = br.readLine();
        br.close();
        //        return !Boolean.parseBoolean(line);
        return line.contains("\"Succeed\":false");
    }

    private String getEmailParamStr(String email) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("identifier", email));
        params.add(new BasicNameValuePair("_", System.currentTimeMillis() + ""));

        return URLEncodedUtils.format(params, "utf-8");
    }

    @Override
    protected String getCategory() {
        return "/ffff";
    }

    @Override
    protected long getPauseTime() {
        return 100;
    }

    public static final void main(String[] args) throws Exception {
        EmailChecker checker = new FanhuanEmailChecker2(null, null, null, null);

        System.out.println(checker.check("123314025@qq.com"));
        System.out.println(checker.check("dhzheng3@gmail.com"));
    }
}
