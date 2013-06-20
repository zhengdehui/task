package cn.dehui.task.browser.search.uithread.ui;

import javax.swing.SwingUtilities;

import chrriis.common.UIUtils;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import cn.dehui.task.browser.search.uithread.controller.manager.ControllerManager;
import cn.dehui.task.browser.search.uithread.controller.manager.GoogleControllerManager;

public class GoogleSearchFrame extends SearchFrame {

    private static final long serialVersionUID = 375677196545349189L;

    @Override
    protected ControllerManager getControllerManager() {
        return new GoogleControllerManager(contentPane);
    }

    @Override
    protected String getWaitTime() {
        return "2000";
    }

    @Override
    protected String getBrowserTitle() {
        return "Google Search";
    }

    @Override
    protected boolean isSiteSearchEnabled() {
        return true;
    }

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        NativeInterface.open();
        UIUtils.setPreferredLookAndFeel();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                GoogleSearchFrame frame = new GoogleSearchFrame();
                frame.setSize(1280, 600);
                frame.setLocationByPlatform(true);
                frame.setVisible(true);
            }
        });
        NativeInterface.runEventPump();
    }
}
