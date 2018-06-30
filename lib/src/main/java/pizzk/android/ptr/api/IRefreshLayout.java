package pizzk.android.ptr.api;

import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingParent;

import pizzk.android.ptr.constant.RefreshOwner;

public interface IRefreshLayout extends IRefreshView, NestedScrollingParent, NestedScrollingChild {

    /**
     * 获取核心处理器
     */
    IRefreshKernel getKernel();

    /**
     * 获取头部
     */
    IRefreshAttach getHeader();

    /**
     * 获取尾部
     */
    IRefreshAttach getFooter();

    /**
     * 设置附件
     */
    void setAttach(IRefreshAttach attach, RefreshOwner owner);

    /**
     * 获取一级内容部布局
     */
    IRefreshView getOneLevel();

    /**
     * 获取二级内容部布局
     */
    IRefreshView getTwoLevel();
}
