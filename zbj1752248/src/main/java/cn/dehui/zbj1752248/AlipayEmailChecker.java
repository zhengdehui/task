package cn.dehui.zbj1752248;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.swing.JLabel;
import javax.swing.JProgressBar;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

public class AlipayEmailChecker extends EmailChecker {

    private static final String ALIPAY_SLEEP       = "alipay.sleep";

    private static final String CHECK_URL_TEMPLATE = "https://memberprod.alipay.com/account/reg/acctStatusCheck.json";

    public static int           sleepTime          = System.getProperty(ALIPAY_SLEEP) == null ? 500 : Integer
                                                           .parseInt(System.getProperty(ALIPAY_SLEEP));

    public AlipayEmailChecker(List<String> emailList, String outputFolder, CountDownLatch startSignal,
            CountDownLatch doneSignal, JLabel label, JProgressBar bar) {
        super(emailList, outputFolder, startSignal, doneSignal, label, bar);
    }

    public AlipayEmailChecker(List<String> emailList, String outputFolder, CountDownLatch startSignal,
            CountDownLatch doneSignal, JLabel label) {
        this(emailList, outputFolder, startSignal, doneSignal, label, null);
    }

    public AlipayEmailChecker(List<String> emailList, String outputFolder, CountDownLatch startSignal,
            CountDownLatch doneSignal) {
        this(emailList, outputFolder, startSignal, doneSignal, null, null);
    }

    @Override
    public boolean check(String email) throws Exception {
        HttpPost httpPost = new HttpPost(CHECK_URL_TEMPLATE);
        setHeaders(httpPost);
        httpPost.setHeader("Referer", "https://memberprod.alipay.com/account/reg/email.htm");

        // StringEntity stringEntity = new
        // StringEntity(String.format("acctname=%s&acctType=email&_input_charset=utf-8",
        // email.replaceAll("@", "%40")), "UTF-8");
        StringEntity stringEntity = new StringEntity(String.format("accName=%s", email.replaceAll("@", "%40")), "UTF-8");
        stringEntity.setContentType("application/x-www-form-urlencoded; charset=utf-8");
        httpPost.setEntity(stringEntity);
        HttpResponse response = null;
        try {
            response = client.execute(httpPost);

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                throw new Exception(String.format("Alipay Status Code: %d, email: %s", statusCode, email));
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String line = br.readLine();
            br.close();
            //        return line.contains("\"acctstatus\":\"available\"") && line.contains("\"stat\":\"ok\"");
            return line.contains("\"accStatus\":\"available\"") && line.contains("\"stat\":\"ok\"");
        } finally {
            if (response != null && response.getEntity() != null) {
                EntityUtils.consumeQuietly(response.getEntity());
            }
        }
    }

    @Override
    protected String getCategory() {
        return "/aaaa";
    }

    @Override
    protected long getPauseTime() {
        return sleepTime;
    }

}
