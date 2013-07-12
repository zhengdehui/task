package cn.dehui.zbj1752248;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.swing.JLabel;
import javax.swing.JProgressBar;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;

public class EgouEmailChecker extends EmailChecker {

    private static final String EGOU_SLEEP         = "egou.sleep";

    private static final String CHECK_URL_TEMPLATE = "http://fanxian.egou.com/checkEmail.do?email=%s";

    public static int           sleepTime          = System.getProperty(EGOU_SLEEP) == null ? 100 : Integer
                                                           .parseInt(System.getProperty(EGOU_SLEEP));

    public EgouEmailChecker(List<String> emailList, String outputFolder, CountDownLatch startSignal,
            CountDownLatch doneSignal, JLabel label, JProgressBar bar) {
        super(emailList, outputFolder, startSignal, doneSignal, label, bar);
    }

    public EgouEmailChecker(List<String> emailList, String outputFolder, CountDownLatch startSignal,
            CountDownLatch doneSignal, JLabel label) {
        super(emailList, outputFolder, startSignal, doneSignal, label, null);
    }

    public EgouEmailChecker(List<String> emailList, String outputFolder, CountDownLatch startSignal,
            CountDownLatch doneSignal) {
        super(emailList, outputFolder, startSignal, doneSignal, null, null);
    }

    @Override
    public boolean check(String email) throws Exception {
        String url = String.format(CHECK_URL_TEMPLATE, email);
        HttpPost httpPost = new HttpPost(url);
        setHeaders(httpPost);
        HttpResponse response = null;
        try {
            response = client.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                throw new Exception(String.format("Egou Status Code: %d, email: %s", statusCode, email));
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String line = br.readLine();
            br.close();
            return Boolean.parseBoolean(line);
        } finally {
            if (response != null && response.getEntity() != null) {
                EntityUtils.consumeQuietly(response.getEntity());
            }
        }
    }

    @Override
    protected String getCategory() {
        return "/eeee";
    }

    @Override
    protected long getPauseTime() {
        return sleepTime;
    }

    public static final void main(String[] args) throws Exception {
        EgouEmailChecker checker = new EgouEmailChecker(null, null, null, null);

        System.out.println(checker.check("123314025@qq.com"));
        System.out.println(checker.check("dhzheng3@gmail.com"));
    }
}
