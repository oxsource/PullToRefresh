package pizzk.android.ptr.api;

/**
 * 刷新附件接口(Head或Foot作为附件)
 */
public interface IRefreshAttach extends IRefreshView {

    /**
     * 获取触发临界值
     */
    int getActivateValue();

    /**
     * 获取回弹时间（单位：毫秒）
     *
     * @return 回弹时间
     */
    int getReboundTime();

    /**
     * 获取结束提示等待时间（单位：毫秒）
     *
     * @return 等待时间
     */
    int getFinishHintTime();

    /**
     * 阻尼回调
     *
     * @param current 当前位置
     * @param offset  偏移量
     * @return 阻尼效果处理后的值
     */
    int onDamping(int current, int offset);

    /**
     * 是否自动关闭less
     */
    boolean autoCloseLess();

    /**
     * 移动过程中的回调
     *
     * @param layout  所属刷新布局接口
     * @param percent 可当前移动距离站可移动总距离的百分比
     */
    void onDragging(IRefreshLayout layout, float percent);

    /**
     * 视图销毁时的回调
     */
    void onDestroy();
}
