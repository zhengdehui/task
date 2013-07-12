package cn.dehui.zbj1752248;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JLabel;
import javax.swing.JProgressBar;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class FanliEmailChecker extends EmailChecker {

    private static final String CHECK_URL_TEMPLATE = "http://passport.51fanli.com/Reg/ajaxCheckname?action=useremail&";

    private Pattern             pattern            = Pattern.compile("\"status\":(\\d+)");

    public FanliEmailChecker(List<String> emailList, String outputFolder, CountDownLatch startSignal,
            CountDownLatch doneSignal, JLabel label, JProgressBar bar) {
        super(emailList, outputFolder, startSignal, doneSignal, label, bar);
    }

    public FanliEmailChecker(List<String> emailList, String outputFolder, CountDownLatch startSignal,
            CountDownLatch doneSignal, JLabel label) {
        super(emailList, outputFolder, startSignal, doneSignal, label, null);
    }

    public FanliEmailChecker(List<String> emailList, String outputFolder, CountDownLatch startSignal,
            CountDownLatch doneSignal) {
        super(emailList, outputFolder, startSignal, doneSignal, null, null);
    }

    @Override
    public boolean check(String email) throws Exception {
        String url = CHECK_URL_TEMPLATE + getEmailParamStr(email) + "&" + new Date().getTime();
        HttpPost httpPost = new HttpPost(url);
        setHeaders(httpPost);
        HttpResponse response = null;
        try {
            response = client.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                throw new Exception(String.format("51Fanli Status Code: %d, email: %s", statusCode, email));
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

            String json = br.readLine();
            br.close();

            Matcher matcher = pattern.matcher(json);
            if (matcher.find()) {
                String status = matcher.group(1);
                return "10003".equals(status) || "10007".equals(status);
            } else {
                return false;
            }
        } finally {
            if (response != null && response.getEntity() != null) {
                EntityUtils.consumeQuietly(response.getEntity());
            }
        }
    }

    private String getEmailParamStr(String email) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("name", email));

        return URLEncodedUtils.format(params, "utf-8");
    }

    @Override
    protected String getCategory() {
        return "/5555";
    }

    @Override
    protected long getPauseTime() {
        return 10;
    }

}
