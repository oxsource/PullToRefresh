package pizzk.android.ptr.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Size;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import pizzk.android.ptr.api.IRefreshAttach;
import pizzk.android.ptr.api.IRefreshKernel;
import pizzk.android.ptr.api.IRefreshLayout;
import pizzk.android.ptr.api.IRefreshView;
import pizzk.android.ptr.api.RefreshControl;
import pizzk.android.ptr.constant.RefreshOwner;
import pizzk.android.ptr.constant.RefreshState;
import pizzk.android.ptr.wrapper.AttachWrapper;
import pizzk.android.ptr.wrapper.TargetWrapper;

final public class RefreshLayout extends ViewGroup implements IRefreshLayout {
    //核心组件接口
    private IRefreshAttach mHeadWidget;
    private IRefreshAttach mFootWidget;
    private IRefreshView mOneLevel;
    private IRefreshView mTwoLevel;
    //嵌套滑动属性
    private NestedScrollingParentHelper mNspHelper;
    private NestedScrollingChildHelper mNscHelper;
    //核心组件
    private RefreshKernel mKernel;
    //滑动属性
    private boolean initScrollFlag = true;
    private int mLastY;

    public RefreshLayout(Context context) {
        this(context, null, 0);
    }

    public RefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mNspHelper = new NestedScrollingParentHelper(this);
        mNscHelper = new NestedScrollingChildHelper(this);
        mKernel = new RefreshKernel(this);
        setNestedScrollingEnabled(true);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() > 3) {
            throw new IllegalStateException("RefreshLayout at most has three child view");
        }
        if (getChildCount() < 1) {
            throw new IllegalStateException("RefreshLayout at least has two child view");
        }
        View view;
        //绑定Header
        view = getChildAt(0);
        mHeadWidget = (view instanceof IRefreshAttach) ? (IRefreshAttach) view : new AttachWrapper(view);
        //绑定Content
        view = getChildAt(1);
        mOneLevel = (view instanceof IRefreshView) ? (IRefreshView) view : new TargetWrapper(view);
        if (3 == getChildCount()) {
            //绑定Footer
            view = getChildAt(2);
            mFootWidget = (view instanceof IRefreshAttach) ? (IRefreshAttach) view : new AttachWrapper(view);
        }
        mTwoLevel = mOneLevel;
    }

    public void setTwoLevel(View view) {
        if (null == view) return;
        mTwoLevel = (view instanceof IRefreshView) ? (IRefreshView) view : new TargetWrapper(view);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        int height = getMeasuredHeight();
        int headHeight = mHeadWidget.getView().getMeasuredHeight();
        height += headHeight;
        height += null != mFootWidget ? mFootWidget.getView().getMeasuredHeight() : 0;
        setMeasuredDimension(getMeasuredWidth(), height);
        //初始隐藏头部
        if (!initScrollFlag) return;
        initScrollFlag = false;
        scrollTo(0, headHeight);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int pLeft = getPaddingLeft(), pTop = getPaddingTop();
        final int pRight = getPaddingRight(), pBottom = getPaddingBottom();
        final int boundX = right - pRight, boundY = bottom - pBottom;
        final int ltX = pLeft + left;
        int currentTop = pTop + top;
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            int vRight = Math.min((ltX + view.getMeasuredWidth()), boundX);
            int vBottom = Math.min((currentTop + view.getMeasuredHeight()), boundY);
            view.layout(ltX, currentTop, vRight, vBottom);
            currentTop = vBottom;
        }
        mKernel.onLayoutChanged();
    }

    /***NestedScrollingParent***/
    @Override
    public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, int nestedScrollAxes) {
        return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedScrollAccepted(@NonNull View child, @NonNull View target, int axes) {
        mNspHelper.onNestedScrollAccepted(child, target, axes);
        startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
    }

    @Override
    public void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed) {
        int consumedY = mKernel.onPreScroll(dy);
        mKernel.onScroll(consumedY);
        consumed[1] = consumedY;
    }

    @Override
    public void onStopNestedScroll(@NonNull View child) {
        stopNestedScroll();
    }

    @Override
    public boolean onNestedPreFling(@NonNull View target, float velocityX, float velocityY) {
        if (mKernel.onPreFling(velocityX, velocityY)) {
            mKernel.onFling(velocityX, velocityY);
            return true;
        }
        return super.onNestedPreFling(target, velocityX, velocityY);
    }

    /**
     * NestedScrollingChild
     */
    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        mNscHelper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return mNscHelper.isNestedScrollingEnabled();
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return mNscHelper.startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        mNscHelper.stopNestedScroll();
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return mNscHelper.hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, @Nullable
    @Size(value = 2) int[] offsetInWindow) {
        return mNscHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, @Nullable @Size(value = 2) int[] consumed, @Nullable @Size
            (value = 2) int[] offsetInWindow) {
        return mNscHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean onNestedFling(@NonNull View target, float velocityX, float velocityY, boolean consumed) {
        return dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return mNscHelper.dispatchNestedPreFling(velocityX, velocityY);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return mNscHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mKernel.isTouchLock()) {
            return true;
        }
        int action = ev.getAction();
        if (MotionEvent.ACTION_DOWN == action) {
            mLastY = (int) ev.getRawY();
        } else if (MotionEvent.ACTION_MOVE == action) {
            int offsetY = (int) (ev.getRawY() - mLastY);
            return mKernel.useTouchIntercept(offsetY);
        }
        return super.onInterceptTouchEvent(ev);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if (MotionEvent.ACTION_MOVE == action) {
            int currentY = (int) event.getRawY();
            int dy = currentY - mLastY;
            int moving = mKernel.onPreScroll(-dy);
            mKernel.onScroll(moving);
            mLastY = currentY;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if (MotionEvent.ACTION_UP == action) {
            if (RefreshState.OPENING == mKernel.getState()) {
                mKernel.onStopRefresh(false);
            } else if (RefreshState.ACTIVE == mKernel.getState()) {
                mKernel.onStartRefresh(mKernel.getOwner());
            } else if (RefreshState.REFRESHING == mKernel.getState()) {
                mKernel.notifyStateChanged();
            }
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    protected void onDetachedFromWindow() {
        mKernel.onDestroy();
        super.onDetachedFromWindow();
    }

    @Override
    public IRefreshKernel getKernel() {
        return mKernel;
    }

    @Override
    public IRefreshAttach getHeader() {
        return mHeadWidget;
    }

    @Override
    public IRefreshAttach getFooter() {
        return mFootWidget;
    }

    @Override
    public IRefreshView getOneLevel() {
        return mOneLevel;
    }

    @Override
    public IRefreshView getTwoLevel() {
        return mTwoLevel;
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public void setAttach(IRefreshAttach attach, RefreshOwner owner) {
        if (RefreshState.NONE != getKernel().getState()) return;
        if (null == attach || null == attach.getView()) return;
        if (owner == RefreshOwner.FOOTER) {
            if (null == mFootWidget) return;
            View old = mFootWidget.getView();
            int index = indexOfChild(old);
            if (index < 0) return;
            removeView(old);
            addView(attach.getView(), index);
            mFootWidget = attach;
        } else if (owner == RefreshOwner.HEADER) {
            if (null == mHeadWidget) return;
            View old = mHeadWidget.getView();
            int index = indexOfChild(old);
            if (index < 0) return;
            removeView(old);
            addView(attach.getView(), index);
            mHeadWidget = attach;
        } else {
            return;
        }
        requestLayout();
        invalidate();
    }
}
