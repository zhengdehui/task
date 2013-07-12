package cn.dehui.zbj1752248;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import javax.swing.JLabel;
import javax.swing.JProgressBar;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

public class FiveOneBiEmailChecker extends EmailChecker {

    private static final String CHECK_URL_TEMPLATE = "http://www.51bi.com/checkMail.ok?email=%s&ts=%s";

    private DateFormat          dateFormat         = new SimpleDateFormat(
                                                           "EEE'%20'MMM'%20'dd'%20'yyyy'%20'HH:mm:ss'%20GMT'Z",
                                                           Locale.ENGLISH);

    public FiveOneBiEmailChecker(List<String> emailList, String outputFolder, CountDownLatch startSignal,
            CountDownLatch doneSignal, JLabel label, JProgressBar bar) {
        super(emailList, outputFolder, startSignal, doneSignal, label, bar);
    }

    public FiveOneBiEmailChecker(List<String> emailList, String outputFolder, CountDownLatch startSignal,
            CountDownLatch doneSignal, JLabel label) {
        super(emailList, outputFolder, startSignal, doneSignal, label, null);
    }

    public FiveOneBiEmailChecker(List<String> emailList, String outputFolder, CountDownLatch startSignal,
            CountDownLatch doneSignal) {
        super(emailList, outputFolder, startSignal, doneSignal, null, null);
    }

    @Override
    public boolean check(String email) throws Exception {
        String url = String.format(CHECK_URL_TEMPLATE, email, dateFormat.format(new Date()));
        HttpGet httpGet = new HttpGet(url);
        setHeaders(httpGet);
        HttpResponse response = null;
        try {
            response = client.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                throw new Exception(String.format("51Bi Status Code: %d, email: %s", statusCode, email));
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String line = br.readLine();
            br.close();
            return !"exist".equals(line);
        } finally {
            if (response != null && response.getEntity() != null) {
                EntityUtils.consumeQuietly(response.getEntity());
            }
        }
    }

    @Override
    protected String getCategory() {
        return "/bbbb";
    }

    @Override
    protected long getPauseTime() {
        return 100;
    }
}
