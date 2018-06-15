package pizzk.android.ptr.api;

import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingParent;

import pizzk.android.ptr.constant.RefreshOwner;

public interface IRefreshLayout extends IRefreshView, NestedScrollingParent, NestedScrollingChild {

    /**
     * 获取核心组件
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
     * 获取一级内容部布局
     */
    IRefreshView getOneLevel();

    /**
     * 获取二级内容部布局
     */
    IRefreshView getTwoLevel();

    /**
     * 停止刷新
     *
     * @param success 是否调用成功
     */
    void stopRefresh(boolean success);

    /**
     * 开始刷新
     *
     * @param owner 调用刷新所属对象
     */
    void startRefresh(RefreshOwner owner);
}
