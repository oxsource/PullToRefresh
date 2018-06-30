package pizzk.android.ptr.api;

import pizzk.android.ptr.constant.RefreshOwner;

public class RefreshControl {
    //无限拉动属性
    private boolean isHeadInfiniteDrag = true;
    private boolean isFootInfiniteDrag = true;
    //less属性
    private boolean isHeadLess = false;
    private boolean isFootLess = false;
    //刷新监听器
    private RefreshListener listener;
    //其他
    private final IRefreshKernel kernel;
    private final IRefreshLayout layout;

    public RefreshControl(IRefreshKernel kernel, IRefreshLayout layout) {
        this.kernel = kernel;
        this.layout = layout;
    }

    /**
     * 停止刷新
     *
     * @param success 是否调用成功
     */
    public void stopRefresh(boolean success) {
        kernel.onStopRefresh(success);
    }

    /**
     * 开始刷新
     *
     * @param owner 调用刷新所属对象
     */
    public void startRefresh(RefreshOwner owner) {
        if (RefreshOwner.NONE != kernel.getOwner()) return;
        layout.getView().post(() -> kernel.onStartRefresh(owner));
    }

    public void setListener(RefreshListener listener) {
        this.listener = listener;
    }

    public RefreshListener getListener() {
        return listener;
    }

    public boolean isHeadInfiniteDrag() {
        return isHeadInfiniteDrag;
    }

    public void setHeadInfiniteDrag(boolean headInfiniteDrag) {
        isHeadInfiniteDrag = headInfiniteDrag;
    }

    public boolean isFootInfiniteDrag() {
        return isFootInfiniteDrag;
    }

    public void setFootInfiniteDrag(boolean footInfiniteDrag) {
        isFootInfiniteDrag = footInfiniteDrag;
    }

    public boolean isHeadLess() {
        return isHeadLess;
    }

    public void setHeadLess(boolean headLess) {
        isHeadLess = headLess;
    }

    public boolean isFootLess() {
        return isFootLess;
    }

    public void setFootLess(boolean footLess) {
        isFootLess = footLess;
    }
}
