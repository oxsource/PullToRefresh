package pizzk.android.ptr.constant;

public enum RefreshState {
    //无状态
    NONE("NONE"),
    //展开中
    OPENING("OPENING"),
    //释放可刷新
    ACTIVE("ACTIVE"),
    //刷新中
    REFRESHING("REFRESHING"),
    //关闭中
    CLOSING("CLOSING");

    public final String plain;

    RefreshState(String value) {
        this.plain = value;
    }
}
