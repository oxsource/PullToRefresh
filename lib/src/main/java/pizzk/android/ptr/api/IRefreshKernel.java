package pizzk.android.ptr.api;

import pizzk.android.ptr.constant.RefreshOwner;
import pizzk.android.ptr.constant.RefreshState;

/**
 * NestScroll和TouchEvent处理核心的接口
 */
public interface IRefreshKernel {
    /**
     * 获取控制器
     */
    RefreshControl getControl();

    /**
     * 获取当前刷新状态
     */
    RefreshState getState();

    /**
     * 获取刷新持有者
     */
    RefreshOwner getOwner();

    /**
     * 判断内容是否滚动至顶部
     */
    boolean isReachTop();

    /**
     * 判断内容是否滚动至底部
     */
    boolean isReachBottom();

    /**
     * 判断是够使用Touch拦截机制
     *
     * @param offset 目标滑动偏移量
     */
    boolean useTouchIntercept(int offset);

    /**
     * 是否需要将Touch锁定
     */
    boolean isTouchLock();

    /**
     * 判断Owner是否Less
     */
    boolean isOwnerLess(RefreshOwner owner);

    /**
     * 滑动事件预处理
     *
     * @param dy 滑动偏移量
     * @return 消耗偏移量
     */
    int onPreScroll(int dy);

    /**
     * 滑动事件处理
     *
     * @param scroll 滑动距离
     */
    void onScroll(int scroll);


    /**
     * 滚动事件预处理
     *
     * @param velocityX X轴加速度
     * @param velocityY 轴加速度
     * @return 是否处理滚动事件
     */
    boolean onPreFling(float velocityX, float velocityY);

    /**
     * 滚动事件处理
     *
     * @param velocityX X轴加速度
     * @param velocityY 轴加速度
     */
    void onFling(float velocityX, float velocityY);

    /**
     * 布局发生变化时的回调
     */
    void onLayoutChanged();

    /**
     * 显示刷新回调
     *
     * @param owner 头部或尾部触发刷新
     */
    void onStartRefresh(RefreshOwner owner);

    /**
     * 关闭刷新
     */
    void onStopRefresh(boolean success);

    /**
     * 状态调整
     */
    void notifyStateChanged();

    /**
     * 销毁
     */
    void onDestroy();
}
