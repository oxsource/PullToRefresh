package pizzk.android.ptr.view;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import pizzk.android.ptr.R;
import pizzk.android.ptr.api.IRefreshAttach;
import pizzk.android.ptr.api.IRefreshKernel;
import pizzk.android.ptr.api.IRefreshLayout;
import pizzk.android.ptr.constant.RefreshOwner;
import pizzk.android.ptr.constant.RefreshState;

public class RefreshAttach extends RelativeLayout implements IRefreshAttach {
    //正常视图属性
    private View vRefresh;
    private ImageView vArrow;
    private TextView vContent;
    private ImageView vLoading;
    private ImageView vFinished;
    //使用FootLess和HeadLess的布局
    private View vAttachLess;

    //刷新状态属性
    private RefreshState lastState;
    private boolean finishFlag = false;
    //是否自动关闭less
    private boolean autoCloseLess;

    //状态提示文字
    private String hintOpening;
    private String hintActive;
    private String hintRefreshing;
    private String hintFinished;
    private String hintLess;
    //类型属性
    private RefreshOwner owner;
    //时间属性
    private int msRebound;
    private int msFinished;
    //触发刷新的临界值
    private int activeValue;
    //阻尼系数
    private float damping;
    private final float screenHeight;

    public RefreshAttach(Context context) {
        this(context, null);
    }

    public RefreshAttach(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.refresh_widget_layout, this);
        vRefresh = findViewById(R.id.rl_refresh);
        vArrow = findViewById(R.id.iv_refresh_arrow);
        vContent = findViewById(R.id.tv_refresh_text);
        vFinished = findViewById(R.id.iv_refresh_finished);
        vLoading = findViewById(R.id.iv_refresh_loading);
        vAttachLess = findViewById(R.id.rl_attach_less);
        vAttachLess.setVisibility(GONE);
        TextView tvAttachLess = findViewById(R.id.tv_attach_less);
        //自定义属性
        TypedArray ta = context.getTheme().obtainStyledAttributes(attrs, R.styleable.RefreshAttach, 0, 0);
        hintOpening = ta.getString(R.styleable.RefreshAttach_hint_opening);
        hintActive = ta.getString(R.styleable.RefreshAttach_hint_active);
        hintRefreshing = ta.getString(R.styleable.RefreshAttach_hint_refresh);
        hintFinished = ta.getString(R.styleable.RefreshAttach_hint_finish);
        msRebound = ta.getInt(R.styleable.RefreshAttach_ms_rebound, 250);
        msFinished = ta.getInt(R.styleable.RefreshAttach_ms_finish_hint, 1000);
        int msLoadingDuration = ta.getInt(R.styleable.RefreshAttach_ms_loading_duration, 1000);
        hintLess = ta.getString(R.styleable.RefreshAttach_hint_less);
        tvAttachLess.setText(getHintLess());
        int defaultActive = (int) getResources().getDimension(R.dimen.head_activate_value);
        activeValue = (int) ta.getDimension(R.styleable.RefreshAttach_active_value, defaultActive);
        damping = ta.getFloat(R.styleable.RefreshAttach_damping, 0.35f);
        autoCloseLess = ta.getBoolean(R.styleable.RefreshAttach_auto_close_less, true);
        Drawable dwDirection = ta.getDrawable(R.styleable.RefreshAttach_icon_direction);
        Drawable dwFinished = ta.getDrawable(R.styleable.RefreshAttach_icon_finish);
        Drawable dwLoading = ta.getDrawable(R.styleable.RefreshAttach_icon_loading);
        ta.recycle();
        //图标设置
        if (null != dwDirection) vArrow.setImageDrawable(dwDirection);
        if (null != dwFinished) vFinished.setImageDrawable(dwFinished);
        if (null != dwLoading) vLoading.setImageDrawable(dwLoading);
        //加载动画
        ObjectAnimator animator = ObjectAnimator.ofFloat(vLoading, "rotation", 0, 360)
                .setDuration(msLoadingDuration);
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.start();
        screenHeight = getResources().getDisplayMetrics().heightPixels;
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public int getActivateValue() {
        return activeValue;
    }

    @Override
    public int getReboundTime() {
        return msRebound;
    }

    @Override
    public int getFinishHintTime() {
        return msFinished;
    }

    @Override
    public int onDamping(int current, int offset) {
        float reduce = 0.0f;
        if (isUseForHeader() && current < 0) {
            reduce = -current / screenHeight;
        } else if (!isUseForHeader() && current > getMeasuredHeight()) {
            reduce = current / screenHeight;
        }
        reduce = (1.0f - reduce * 1.2f);
        return (int) (damping * reduce * offset);
    }

    @Override
    public boolean autoCloseLess() {
        return autoCloseLess;
    }

    @Override
    public void onDragging(IRefreshLayout layout, float percent) {
        IRefreshKernel kernel = layout.getKernel();
        owner = kernel.getOwner();
        if (lastState == kernel.getState()) return;
        switch (kernel.getState()) {
            case ACTIVE:
                vContent.setText(getHintActive());
                setIconVisibility(VISIBLE, GONE, GONE);
                changeArrowDirection(true);
                break;
            case REFRESHING:
                vContent.setText(getHintRefreshing());
                setIconVisibility(GONE, GONE, VISIBLE);
                finishFlag = true;
                break;
            case CLOSING:
                vContent.setText(finishFlag ? getHintFinished() : getHintOpening());
                setIconVisibility(finishFlag ? GONE : VISIBLE, finishFlag ? VISIBLE : GONE, GONE);
                finishFlag = false;
                break;
            case OPENING:
            case NONE:
                vContent.setText(getHintOpening());
                setIconVisibility(VISIBLE, GONE, GONE);
                changeArrowDirection(false);
                if (kernel.isOwnerLess(owner)) {
                    vRefresh.setVisibility(GONE);
                    vAttachLess.setVisibility(TextUtils.isEmpty(hintLess) ? GONE : VISIBLE);
                } else {
                    vRefresh.setVisibility(VISIBLE);
                    vAttachLess.setVisibility(GONE);
                }
                break;
        }
        lastState = kernel.getState();
    }

    @Override
    public void onDestroy() {
        if (vArrow.getAnimation() != null) {
            vArrow.getAnimation().cancel();
        }
        if (vLoading.getAnimation() != null) {
            vLoading.getAnimation().cancel();
        }
    }

    public ImageView getDragView() {
        return vArrow;
    }

    public TextView getContentView() {
        return vContent;
    }

    public ImageView getLoadingView() {
        return vLoading;
    }

    public ImageView getFinishedView() {
        return vFinished;
    }

    public String getHintOpening() {
        return TextUtils.isEmpty(hintOpening) ? isUseForHeader() ? "下拉刷新" : "上拉加载" : hintOpening;
    }

    public void setHintOpening(String hintOpening) {
        this.hintOpening = hintOpening;
    }

    public String getHintActive() {
        return TextUtils.isEmpty(hintActive) ? "该放手了" : hintActive;
    }

    public void setHintActive(String hintActive) {
        this.hintActive = hintActive;
    }

    public String getHintRefreshing() {
        return TextUtils.isEmpty(hintRefreshing) ? isUseForHeader() ? "正在刷新" : "正在加载" : hintRefreshing;
    }

    public void setHintRefreshing(String hintRefreshing) {
        this.hintRefreshing = hintRefreshing;
    }

    public String getHintFinished() {
        return TextUtils.isEmpty(hintFinished) ? isUseForHeader() ? "刷新完成" : "加载完成" : hintFinished;
    }


    public void setHintFinished(String hintFinished) {
        this.hintFinished = hintFinished;
    }

    public String getHintLess() {
        return TextUtils.isEmpty(hintLess) ? "" : hintLess;
    }

    public void setHintLess(String text) {
        this.hintLess = text;
    }

    public boolean isUseForHeader() {
        return RefreshOwner.HEADER == owner;
    }

    /**
     * 设置回弹动画时间
     */
    public void setMsResilience(int msResilience) {
        if (msResilience < 0) return;
        this.msRebound = msResilience;
    }

    /**
     * 设置完成等待时间
     */
    public void setMsFinished(int msFinished) {
        if (msFinished < 0) return;
        this.msFinished = msFinished;
    }

    /**
     * 设置刷新触发值
     */
    public void setActiveValue(int activeValue) {
        if (activeValue < 0) return;
        this.activeValue = activeValue;
    }

    //控制图标显示与隐藏
    private void setIconVisibility(int arrow, int success, int loading) {
        vArrow.setVisibility(arrow);
        vFinished.setVisibility(success);
        vLoading.setVisibility(loading);
    }

    //改变箭头方向
    private void changeArrowDirection(boolean up) {
        float value = isUseForHeader() == up ? 0f : -180f;
        String propertyName = "rotation";
        ObjectAnimator.ofFloat(vArrow, propertyName, value, (-180f - value)).setDuration(100).start();
    }
}
