package cn.dehui.task.keywordtool.browser.ui;

import javax.swing.SwingUtilities;

import chrriis.common.UIUtils;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import cn.dehui.task.keywordtool.browser.controller.BaiduController;
import cn.dehui.task.keywordtool.browser.controller.Controller;

public class BaiduKeywordToolFrame extends KeywordToolFrameWithBrowser {

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        NativeInterface.open();
        UIUtils.setPreferredLookAndFeel();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                BaiduKeywordToolFrame frame = new BaiduKeywordToolFrame();
                frame.setSize(1280, 600);
                frame.setLocationByPlatform(true);
                frame.setVisible(true);
            }
        });
        NativeInterface.runEventPump();
    }

    @Override
    protected Controller createController() {
        return new BaiduController();
    }

    @Override
    protected String getConfigFile() {
        return "fengchao.txt";
    }
}
