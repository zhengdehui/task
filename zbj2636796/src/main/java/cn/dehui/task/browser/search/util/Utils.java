package cn.dehui.task.browser.search.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class Utils {

    private static final String   URL_ENCODING    = "utf8";

    private static final String[] clearCookiesCmd = new String[] { "cmd", "/c",
            "RunDll32.exe InetCpl.cpl,ClearMyTracksByProcess 10" };

    public static String getRealUrl(String url) throws UnsupportedEncodingException {
        String[] urlParts = url.split("&");
        for (String part : urlParts) {
            int index = part.indexOf("url=");
            if (index != -1) {
                return URLDecoder.decode(part.substring(index + 4), URL_ENCODING);
            }
        }
        return "";
    }

    public static String decodeUrl(String url) {
        try {
            return URLDecoder.decode(url, URL_ENCODING);
        } catch (Exception e) {
            //            e.printStackTrace();
            return url;
        }
    }

    public static String removeHeadFootForUrl(String url) {
        String res = url.replaceAll("http[s]?://", "");
        if (res.startsWith("www.")) {
            res = res.substring(4);
        }
        return res.charAt(res.length() - 1) == '/' ? res.substring(0, res.length() - 1) : res;
    }

    public static void clearSessionCookies() {
        try {
            Process p = Runtime.getRuntime().exec(clearCookiesCmd);
            int exitValue = p.waitFor();
            System.out.print("clearSessionCookies " + (exitValue == 0 ? "succeed. " : "fail. "));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
