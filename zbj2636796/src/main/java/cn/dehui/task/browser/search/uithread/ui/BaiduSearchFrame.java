package cn.dehui.task.browser.search.uithread.ui;

import javax.swing.SwingUtilities;

import chrriis.common.UIUtils;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import cn.dehui.task.browser.search.uithread.controller.manager.BaiduControllerManager;
import cn.dehui.task.browser.search.uithread.controller.manager.ControllerManager;

public class BaiduSearchFrame extends SearchFrame {

    private static final long serialVersionUID = 375677196545349189L;

    @Override
    protected ControllerManager getControllerManager() {
        return new BaiduControllerManager(contentPane);
    }

    @Override
    protected String getWaitTime() {
        return "0";
    }

    @Override
    protected String getBrowserTitle() {
        return "百度搜索";
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
                BaiduSearchFrame frame = new BaiduSearchFrame();
                frame.setSize(1280, 600);
                frame.setLocationByPlatform(true);
                frame.setVisible(true);
            }
        });
        NativeInterface.runEventPump();
    }
}
