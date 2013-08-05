package cn.dehui.zbj1752248;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JLabel;
import javax.swing.JProgressBar;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

/**
 * 超过20次封IP
 * @author dehui
 */
@Deprecated
public class FanliEmailChecker extends EmailChecker {

    private static final String CHECK_URL_TEMPLATE = "http://passport.51fanli.com/Reg/ajaxCheckname?action=useremail&";

    private Pattern             pattern            = Pattern.compile("\"status\":(\\d+)");

    private Random              random             = new Random();

    public FanliEmailChecker(List<String> emailList, String outputFolder, CountDownLatch startSignal,
            CountDownLatch doneSignal, JLabel label, JProgressBar bar) {
        super(emailList, outputFolder, startSignal, doneSignal, label, bar);
        retryTimes = 1;
    }

    public FanliEmailChecker(List<String> emailList, String outputFolder, CountDownLatch startSignal,
            CountDownLatch doneSignal, JLabel label) {
        super(emailList, outputFolder, startSignal, doneSignal, label, null);
        retryTimes = 1;
    }

    public FanliEmailChecker(List<String> emailList, String outputFolder, CountDownLatch startSignal,
            CountDownLatch doneSignal) {
        super(emailList, outputFolder, startSignal, doneSignal, null, null);
        retryTimes = 1;
    }

    /**
     * "10001":{t:"notice",txt:"注册失败，请重试！",ele:$submit},
    "10002":{t:"error",txt:"邮箱格式错误。",ele:$ue},
    "10004":{t:"notice",txt:"该邮箱已注册返利网，请<a href='http://passport.51fanli.com/login?go_url={0}' onclick='Ftrack(\"/virtual/www/home_navigation_reg\", \"zhijiedenglu\");' target='_blank'>直接登录</a>",ele:$ue},
    "10005":{t:"error",txt:"用户名只允许字母,数字,汉字和下划线！",ele:$un},
    "10006":{t:"notice",txt:"用户名长度不能小于3或大于25，请重新输入！",ele:$un},
    "10008":{t:"notice",txt:"注册失败，请重试！",ele:$submit},
    "10009":{t:"error",txt:"为了帐户安全，密码长度不能小于8位，请重输！",ele:$pw},
    "10010":{t:"error",txt:"为了帐户安全，密码必须包含数字和英文，请重输！",ele:$pw},
    "10011":{t:"notice",txt:"为了帐户安全，密码和邮箱或用户名不能一致，请重输！",ele:$pw},
    "10012":{t:"notice",txt:"密码输入不一致。",ele:$cpw},
    "10014":{t:"error",txt:"注册IP超过限制。",ele:$submit},
    "10015":{t:"error",txt:"密码中包含非法字符，请重新输入！",ele:$pw},
    "10019":{t:"error",txt:"中国雅虎邮箱已停止服务，请更换其他邮箱注册！",ele:$ue},
    "10024":{t:"error",txt:"用户名不能用纯数字，请重新输入！",ele:$un}
     */
    @Override
    public boolean check(String email) throws Exception {
        String url = CHECK_URL_TEMPLATE + getEmailParamStr(email)
                + "&jsoncallback=jQuery17209032433573078179_1373652107635&_=" + System.currentTimeMillis();
        HttpGet httpGet = new HttpGet(url);
        setHeaders(httpGet);
        httpGet.setHeader("Referer", "http://passport.51fanli.com/reg?action=yes&go_url=http%3A%2F%2F51fanli.com%2F");
        httpGet.setHeader("X-Requested-With", "XMLHttpRequest");
        HttpResponse response = null;
        try {
            response = client.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                throw new Exception(String.format("51Fanli Status Code: %d, email: %s", statusCode, email));
            }

            String json = EntityUtils.toString(response.getEntity(), "utf8");
            Matcher matcher = pattern.matcher(json);
            if (matcher.find()) {
                String status = matcher.group(1);

                if ("10003".equals(status) || "10007".equals(status)) {
                    return true;
                } else if ("10004".equals(status)) {
                    return false;
                } else {
                    throw new Exception("51Fanli error, code: " + status);
                }
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
        return 1000 + random.nextInt(2000);
    }

    public static final void main(String[] args) throws Exception {
        EmailChecker checker = new FanliEmailChecker(null, null, null, null);

        System.out.println(checker.check("123314025@qq.com"));
        System.out.println(checker.check("dhzheng3@gmail.com"));
    }
}
