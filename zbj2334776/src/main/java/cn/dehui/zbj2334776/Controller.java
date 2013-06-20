package cn.dehui.zbj2334776;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;

public class Controller implements Runnable {

    List<TranslationPair>        pairList;

    int                          cursor = 0;

    boolean                      stoped = false;

    private Language             from;

    private Language             to;

    private Callback<Controller> endCallback;

    private File                 inputFile;

    private Callback<String>     startCallback;

    Controller(File inputFile, Language from, Language to) throws IOException {
        this.inputFile = inputFile;
        this.pairList = readFile(inputFile);
        this.from = from;
        this.to = to;
    }

    Controller(File inputFile, Language from, Language to, Callback<String> startCallback,
            Callback<Controller> endCallback) throws IOException {
        this(inputFile, from, to);
        this.startCallback = startCallback;
        this.endCallback = endCallback;
    }

    private List<TranslationPair> readFile(File inputFile) throws IOException {
        List<TranslationPair> pairList = new ArrayList<Controller.TranslationPair>();
        StringBuffer sb = new StringBuffer();
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "GBK"));
        int tmp;
        char c;
        while ((tmp = br.read()) != -1) {
            c = (char) tmp;
            sb.append(c);
            if (c == ';' || c == '。') {
                pairList.add(new TranslationPair(sb.toString()));
                sb.delete(0, sb.length());
            }
        }

        br.close();
        return pairList;
    }

    public void run() {
        if (cursor == 0) {
            startCallback.execute(toString());
        }
        stoped = false;
        while (!stoped) {
            if (cursor < pairList.size()) {
                TranslationPair pair = pairList.get(cursor);
                cursor++;

                if (pair.fromText.contains("&nbsp;")) {
                    if (pair.fromText.equals("&nbsp;")) {
                        pair.toText = pair.fromText;
                    } else {
                        pair.fromText = pair.fromText.substring(0, pair.fromText.length() - 6);
                        translate(pair);
                        pair.fromText += "&nbsp;";
                        pair.toText += "&nbsp;";
                    }
                } else {
                    translate(pair);
                }
            } else {
                export();
                endCallback.execute(this);
                break;
            }
        }
    }

    private void export() {
        BufferedWriter bw = null;
        try {
            File dir = new File(inputFile.getParent() + "/" + to.name());
            if (!dir.exists()) {
                dir.mkdir();
            }

            String originalName = FilenameUtils.getBaseName(inputFile.getName());
            String translatedName = Translate.execute(originalName, from, to);

            bw = new BufferedWriter(new FileWriter(new File(dir, translatedName.replaceAll("[/\\\\:*?\"<>|]", "")
                    + ".txt")));

            for (TranslationPair pair : pairList) {
                bw.write(pair.toText);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void translate(TranslationPair pair) {
        boolean translated = false;
        do {
            try {
                pair.toText = Translate.execute(pair.fromText, from, to);
                translated = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } while (!translated);
        //        lineCallback.execute();
    }

    public void pause() {
        stoped = true;
    }

    static class TranslationPair {

        String fromText;

        String toText;

        TranslationPair(String fromText) {
            this.fromText = fromText;
        }
    }

    public int getCursor() {
        return cursor;
    }

    public Language getFrom() {
        return from;
    }

    public Language getTo() {
        return to;
    }

    @Override
    public String toString() {
        return inputFile.getName() + "(" + to.name() + ")";
    }

    public static void main(String[] args) throws IOException {
        //        Translate.setClientId("mstranslate1");
        //        Translate.setClientSecret("Mjx2OuqDvIvE5JeCCc0iTZRK23eUHgbmVQcPrzR7VN8=");
        //
        //        new Controller(new File("C:/Users/dehui/Downloads/蜂胶 2012-12-19/病好了还要继续服蜂胶么？.txt"),
        //                Language.CHINESE_SIMPLIFIED, Language.ENGLISH).run();

        //        String translatedName = "sfh*?\"";
        //
        //        System.out.println(translatedName.replaceAll("[/\\\\:*?\"<>|]", ""));
    }
}
