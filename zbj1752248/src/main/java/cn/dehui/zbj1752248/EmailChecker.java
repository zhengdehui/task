package cn.dehui.zbj1752248;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.conn.BasicClientConnectionManager;
import org.apache.http.message.AbstractHttpMessage;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

public abstract class EmailChecker extends Thread {

    protected HttpClient   client;

    private List<String>   emailList;

    private String         outputFolder;

    private BufferedWriter rightEmailWriter;

    private BufferedWriter wrongEmailWriter;

    private BufferedWriter errorEmailWriter;

    private int            retryTimes    = 10;

    private int            retryInterval = 100;

    private CountDownLatch startSignal;

    private CountDownLatch doneSignal;

    private JLabel         label;

    private JProgressBar   bar;

    private DateFormat     df            = new SimpleDateFormat("MMddHHmmss");

    EmailChecker(List<String> emailList, String outputFolder, CountDownLatch startSignal, CountDownLatch doneSignal,
            JLabel label, JProgressBar bar) {
        this.emailList = emailList;
        this.outputFolder = outputFolder;
        this.startSignal = startSignal;
        this.doneSignal = doneSignal;
        this.label = label;
        this.bar = bar;

        if (bar != null) {
            bar.setMaximum(emailList.size());
        }

        client = createHttpClient();
    }

    @Override
    public void run() {
        try {
            startSignal.await();

            System.out.println(getCategory() + " start...");
            long currentTime = System.currentTimeMillis();
            String dateStr = df.format(new Date());
            rightEmailWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(getFolder()
                    + String.format("/未%s.txt", dateStr))));
            wrongEmailWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(getFolder()
                    + String.format("/已%s.txt", dateStr))));
            errorEmailWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(getFolder()
                    + String.format("/错%s.txt", dateStr))));

            for (int j = 0; j < emailList.size(); j++) {
                String email = emailList.get(j);
                for (int i = 1; i <= retryTimes; i++) {
                    try {
                        boolean result = check(email);
                        writeFile(email, result);
                        sleep(getPauseTime());
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.err.printf("第%d个email的第%d次失败\r\n", j + 1, i);
                        if (i == retryTimes) {
                            errorEmailWriter.write(email + "\r\n");
                        }
                        sleep(retryInterval * i);
                        continue;
                    }
                }
                if (bar != null) {
                    bar.setValue(j + 1);
                }
            }
            long usedTime = System.currentTimeMillis() - currentTime;
            if (label != null) {
                label.setText(String.format("用时：%d秒", usedTime / 1000));
            }
            System.out.printf("%s: %d ms\r\n", getCategory(), usedTime);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                rightEmailWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                wrongEmailWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                errorEmailWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            doneSignal.countDown();
        }
    }

    protected abstract long getPauseTime();

    private void writeFile(String email, boolean result) throws IOException {
        BufferedWriter br = result ? rightEmailWriter : wrongEmailWriter;
        br.write(email + "\r\n");
    }

    /**
     * return true=可以注册，false==已经被注册
     */
    protected abstract boolean check(String email) throws Exception;

    private String getFolder() {
        String folderStr = outputFolder + getCategory();

        File file = new File(folderStr);
        if (!file.exists()) {
            file.mkdir();
        }
        return folderStr;
    }

    protected abstract String getCategory();

    public HttpClient createHttpClient() {
        HttpParams params = createHttpParams();

        BasicClientConnectionManager connectionManager = createConnectionManager();

        DefaultHttpClient httpClient = new DefaultHttpClient(connectionManager, params);

        if (System.getProperty("http.proxyHost") != null) {
            HttpHost proxy = new HttpHost(System.getProperty("http.proxyHost"), Integer.parseInt(System
                    .getProperty("http.proxyPort")));
            httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
        }

        httpClient.setRedirectStrategy(new DefaultRedirectStrategy() {
            @Override
            public boolean isRedirected(HttpRequest request, HttpResponse response,
                    org.apache.http.protocol.HttpContext context) {
                boolean isRedirect = false;
                try {
                    isRedirect = super.isRedirected(request, response, context);
                } catch (ProtocolException e) {
                    e.printStackTrace();
                }
                if (!isRedirect) {
                    int responseCode = response.getStatusLine().getStatusCode();
                    if (responseCode == 301 || responseCode == 302) {
                        return true;
                    }
                }
                return isRedirect;
            }
        });

        return httpClient;
    }

    private BasicClientConnectionManager createConnectionManager() {
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));

        try {
            SSLContext sslcontext = SSLContext.getInstance("TLS");
            sslcontext.init(null, new TrustManager[] { createAllTrustManager() }, null);
            Scheme httpsScheme = new Scheme("https", 443, new SSLSocketFactory(sslcontext,
                    SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER));
            schemeRegistry.register(httpsScheme);
        } catch (Exception e) {
            e.printStackTrace();
        }

        BasicClientConnectionManager connectionManager = new BasicClientConnectionManager(schemeRegistry);
        return connectionManager;
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

    private HttpParams createHttpParams() {
        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setUserAgent(params, "Mozilla/5.0 (Windows NT 6.2; rv:22.0) Gecko/20100101 Firefox/22.0");
        HttpConnectionParams.setConnectionTimeout(params, 5000);
        HttpConnectionParams.setSoTimeout(params, 10000);
        HttpClientParams.setCookiePolicy(params, CookiePolicy.BROWSER_COMPATIBILITY);
        return params;
    }

    protected void setFireFoxHeaders(AbstractHttpMessage abstractHttpMessage) {
        abstractHttpMessage
                .setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.2; rv:22.0) Gecko/20100101 Firefox/22.0");
        abstractHttpMessage.setHeader("Accept", "*/*");
        abstractHttpMessage.setHeader("Accept-Language", "zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
        abstractHttpMessage.setHeader("Accept-Charset", "GB2312,utf-8;q=0.7,*;q=0.7");

        abstractHttpMessage.setHeader("Keep-Alive", "300");
        abstractHttpMessage.setHeader("Connection", "keep-alive");
        abstractHttpMessage.setHeader("Cache-Control", "no-cache");
    }

    public void setDoneSignal(CountDownLatch doneSignal) {
        this.doneSignal = doneSignal;
    }
}
