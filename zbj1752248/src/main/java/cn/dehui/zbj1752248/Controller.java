package cn.dehui.zbj1752248;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class Controller {

    private String       emailFilePath = "C:/workspace/zbj1752248/1000个邮箱测试速度.txt";

    private String       outputFolder  = "C:/workspace/zbj1752248/output";

    private List<String> emailList;

    public void readFile() {
        //build emailList;
        emailList = new ArrayList<String>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(emailFilePath));

            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    emailList.add(line);
                }
            }

            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //        emailList.add("123314025@qq.com");
        //        emailList.add("dhzheng3@gmail.com");
    }

    public List<String> getEmailList() {
        return emailList;
    }

    public String getOutputFolder() {
        return outputFolder;
    }

    public static final void main(String[] args) throws InterruptedException {
        Controller c = new Controller();
        c.readFile();

        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(1);

        //        new EgouEmailChecker(c.getEmailList(), c.getOutputFolder(), startSignal, doneSignal).start();
        //        new FanhuanEmailChecker(c.getEmailList(), c.getOutputFolder(), startSignal, doneSignal).start();
        //        new FanliEmailChecker(c.getEmailList(), c.getOutputFolder(), startSignal, doneSignal).start();
        //        new FiveOneBiEmailChecker(c.getEmailList(), c.getOutputFolder(), startSignal, doneSignal).start();
        new AlipayEmailChecker(c.getEmailList(), c.getOutputFolder(), startSignal, doneSignal).start();

        startSignal.countDown(); // let all threads proceed
        doneSignal.await(); // wait for all to finish
    }
}
