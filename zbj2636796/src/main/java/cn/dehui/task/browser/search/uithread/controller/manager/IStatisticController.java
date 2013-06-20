package cn.dehui.task.browser.search.uithread.controller.manager;

public interface IStatisticController extends IUrlController {

    void setMaxResultPerUrl(int maxResultPerUrl);

    void setWaitTime(int waitTime);
}
