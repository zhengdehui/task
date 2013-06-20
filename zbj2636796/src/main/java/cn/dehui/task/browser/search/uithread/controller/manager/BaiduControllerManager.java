package cn.dehui.task.browser.search.uithread.controller.manager;

import javax.swing.JButton;
import javax.swing.JPanel;

import cn.dehui.task.browser.search.uithread.controller.baidu.StatisticBaiduController;
import cn.dehui.task.browser.search.uithread.controller.baidu.UrlBaiduController;

public class BaiduControllerManager extends ControllerManager {

    public BaiduControllerManager(JPanel contentPane) {
        super(contentPane);
    }

    @Override
    public void startCountResult(final JButton button) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void initControllers() {
        statisticController = new StatisticBaiduController(this);
        urlController = new UrlBaiduController(this);
    }

    @Override
    protected void handleTimeout() {
        runningController.research();
    }
}
