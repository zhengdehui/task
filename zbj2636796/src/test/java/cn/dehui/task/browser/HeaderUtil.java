package cn.dehui.task.browser;

import org.apache.http.message.AbstractHttpMessage;

public class HeaderUtil {

    public static void setFireFoxHeaders(AbstractHttpMessage abstractHttpMessage) {
        abstractHttpMessage.setHeader("User-Agent", "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.2; Trident/6.0)");
        abstractHttpMessage.setHeader("Accept", "*/*");
        abstractHttpMessage.setHeader("Accept-Language", "zh-CN");
        abstractHttpMessage.setHeader("Accept-Encoding", "gzip, deflate");
    }

}
