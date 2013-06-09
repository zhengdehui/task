package cn.dehui.task.browser;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

public class Captcha {

    private DefaultHttpClient client;

    public Captcha() {
        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setUserAgent(params, "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2; Trident/6.0)");
        client = new DefaultHttpClient(params);
    }

    public String saveCaptcha(String url, String referer) throws ClientProtocolException, IOException {
        HttpGet httpGet = new HttpGet(url);
        HeaderUtil.setFireFoxHeaders(httpGet);

        httpGet.setHeader("Referer", referer);
        HttpResponse resp = client.execute(httpGet);
        InputStream is = resp.getEntity().getContent();

        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(System.currentTimeMillis() + ".jpg"));
        int len;
        byte[] bytes = new byte[1024];
        while ((len = is.read(bytes)) != -1) {
            bos.write(bytes, 0, len);
        }

        bos.close();
        is.close();

        List<Cookie> cookies = client.getCookieStore().getCookies();
        for (Cookie cookie : cookies) {
            if ("GDSESS".equals(cookie.getName())) {
                return cookie.getName() + "=" + cookie.getValue();
            }
        }
        return null;
    }

    public static void main(String[] args) throws ClientProtocolException, IOException {
        Captcha captcha = new Captcha();
        captcha.saveCaptcha(
                "http://www.google.com.hk/sorry/image?id=5514747270865174035&hl=zh-CN",
                "http://www.google.com.hk/sorry/?continue=http%3A%2F%2Fwww.google.com.hk%2Fsearch%3Fq%3D%252Furl%253Fsa%253Dt%2526rct%253Dj%2526q%253D%2525E7%252599%2525BE%2525E5%2525BA%2525A6%2526source%253Dweb%2526cd%253D11%2526cad%253Drja%2526ved%253D0CGcQFjAK%2526url%253Dhttps%25253A%25252F%25252Fzh.wikipedia.org%25252Fzh%25252F%252525E7%25252599%252525BE%252525E5%252525BA%252525A6%2526ei%253DudN3Uf-eIcbL0QGD1YHoDA%2526usg%253DAFQjCNHPg9iYw5ykq-6QMAIP_69N6uT_Ig%26ie%3Dutf-8%26oe%3Dutf-8%26aq%3Dt%26rls%3Dorg.mozilla%3Azh-CN%3Aofficial%26client%3Dfirefox-a%26channel%3Dfflb&id=8602555891035988640&captcha=28595&submit=%E6%8F%90%E4%BA%A4");
    }
}
