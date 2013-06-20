package cn.dehui.task.browser.search.uithread.controller.manager;

import cn.dehui.task.browser.search.uithread.controller.SearchContext;
import cn.dehui.task.browser.search.util.Callback;
import cn.dehui.task.browser.search.util.Status;

public interface IUrlController extends IResearchController {

    void setMaxUrlPerKeyword(int maxUrlPerKeyword);

    void setStatus(Status status);

    void setSearchContext(SearchContext searchContext);

    SearchContext getSearchContext();

    void setAction(Callback<Void> callback);

}
