package cn.dehui.zbj1752248;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;

public class AlipayEmailChecker extends EmailChecker {

    private static final String ALIPAY_SLEEP       = "alipay.sleep";

    private static final String CHECK_URL_TEMPLATE = "https://memberprod.alipay.com/account/reg/acctStatusCheck.json";

    public static int           sleepTime          = System.getProperty(ALIPAY_SLEEP) == null ? 500 : Integer
                                                           .parseInt(System.getProperty(ALIPAY_SLEEP));

    public AlipayEmailChecker(List<String> emailList, String outputFolder, CountDownLatch startSignal,
            CountDownLatch doneSignal, JLabel label, JProgressBar bar) {
        super(emailList, outputFolder, startSignal, doneSignal, label, bar);

        addHttpsSupport();
    }

    public AlipayEmailChecker(List<String> emailList, String outputFolder, CountDownLatch startSignal,
            CountDownLatch doneSignal, JLabel label) {
        this(emailList, outputFolder, startSignal, doneSignal, label, null);
    }

    public AlipayEmailChecker(List<String> emailList, String outputFolder, CountDownLatch startSignal,
            CountDownLatch doneSignal) {
        this(emailList, outputFolder, startSignal, doneSignal, null, null);
    }

    private void addHttpsSupport() {
        try {
            TrustManager easyTrustManager = createAllTrustManager();

            SSLContext sslcontext = SSLContext.getInstance("TLS");
            sslcontext.init(null, new TrustManager[] { easyTrustManager }, null);
            SSLSocketFactory sf = new SSLSocketFactory(sslcontext);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            Scheme sch = new Scheme("https", sf, 443);

            client.getConnectionManager().getSchemeRegistry().register(sch);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean check(String email) throws Exception {
        HttpPost httpPost = new HttpPost(CHECK_URL_TEMPLATE);
        setFireFoxHeaders(httpPost);
        httpPost.setHeader("Referer", "https://memberprod.alipay.com/account/reg/email.htm");

        // StringEntity stringEntity = new
        // StringEntity(String.format("acctname=%s&acctType=email&_input_charset=utf-8",
        // email.replaceAll("@", "%40")), "UTF-8");
        StringEntity stringEntity = new StringEntity(String.format("accName=%s", email.replaceAll("@", "%40")), "UTF-8");
        stringEntity.setContentType("application/x-www-form-urlencoded; charset=utf-8");
        httpPost.setEntity(stringEntity);
        HttpResponse response = client.execute(httpPost);

        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != 200) {
            throw new Exception(String.format("Alipay Status Code: %d, email: %s", statusCode, email));
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String line = br.readLine();
        br.close();
        //        return line.contains("\"acctstatus\":\"available\"") && line.contains("\"stat\":\"ok\"");
        return line.contains("\"accStatus\":\"available\"") && line.contains("\"stat\":\"ok\"");
    }

    @Override
    protected String getCategory() {
        return "/aaaa";
    }

    @Override
    protected long getPauseTime() {
        return sleepTime;
    }

    private TrustManager createAllTrustManager() {
        TrustManager easyTrustManager = new X509TrustManager() {

            public void checkClientTrusted(java.security.cert.X509Certificate[] x509Certificates, String s)
                    throws java.security.cert.CertificateException {
            }

            public void checkServerTrusted(java.security.cert.X509Certificate[] x509Certificates, String s)
                    throws java.security.cert.CertificateException {
            }

            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };
        return easyTrustManager;
    }
}
