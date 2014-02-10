package cn.dehui.zbj1752248;

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
import org.apache.http.util.EntityUtils;

/**
 * 超过60次封IP，但不报错，永远返回未注册
 * @author dehui
 */
public class FanhuanEmailChecker extends EmailChecker {

    public static int           sleepTime          = System.getProperty("fanhuan.sleep") == null ? 100 : Integer
                                                           .parseInt(System.getProperty("fanhuan.sleep"));

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
        setHeaders(httpGet);
        httpGet.setHeader("Referer", "http://passport.fanhuan.com/reg/");
        httpGet.setHeader("X-Requested-With", "XMLHttpRequest");
        HttpResponse response = null;
        try {
            response = client.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                throw new Exception(String.format("Fanhuan Status Code: %d, email: %s", statusCode, email));
            }

            String line = EntityUtils.toString(response.getEntity(), "utf8");
            return line.contains("\"Succeed\":false");
        } finally {
            if (response != null && response.getEntity() != null) {
                EntityUtils.consumeQuietly(response.getEntity());
            }
        }
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
        return sleepTime;
    }

    public static final void main(String[] args) throws Exception {
        EmailChecker checker = new FanhuanEmailChecker(null, null, null, null);

        System.out.println(checker.check("123314025@qq.com"));
        System.out.println(checker.check("dhzheng3@gmail.com"));
    }
}
