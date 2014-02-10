package cn.dehui.task.keywordtool.browser.ui;

import javax.swing.SwingUtilities;

import chrriis.common.UIUtils;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import cn.dehui.task.keywordtool.browser.controller.Controller;
import cn.dehui.task.keywordtool.browser.controller.GoogleController;

public class AdwordsKeywordToolFrame extends KeywordToolFrameWithBrowser {

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        NativeInterface.open();
        UIUtils.setPreferredLookAndFeel();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                AdwordsKeywordToolFrame frame = new AdwordsKeywordToolFrame();
                frame.setSize(1280, 600);
                frame.setLocationByPlatform(true);
                frame.setVisible(true);
            }
        });
        NativeInterface.runEventPump();
    }

    @Override
    protected Controller createController() {
        return new GoogleController();
    }

    @Override
    protected String getConfigFile() {
        return "adwords.txt";
    }
}
