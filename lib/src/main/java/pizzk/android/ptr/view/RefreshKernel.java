package pizzk.android.ptr.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Scroller;

import pizzk.android.ptr.anim.AnimatorHolder;
import pizzk.android.ptr.anim.AnimatorListener;
import pizzk.android.ptr.api.IRefreshAttach;
import pizzk.android.ptr.api.IRefreshKernel;
import pizzk.android.ptr.api.IRefreshLayout;
import pizzk.android.ptr.api.RefreshListener;
import pizzk.android.ptr.constant.RefreshOwner;
import pizzk.android.ptr.constant.RefreshState;

public class RefreshKernel implements IRefreshKernel {
    //刷新布局接口
    private IRefreshLayout layout;
    private RefreshListener listener;

    //刷新状态
    private RefreshState mState = RefreshState.NONE;
    private RefreshOwner mOwner = RefreshOwner.NONE;

    //是否已经激活
    private boolean isRefreshFlag;
    private boolean isCloseFoot;
    private boolean isTouchLock;
    private boolean isAutoRefresh;

    //动画属性
    private final AnimatorHolder mAnimator;
    //动画结束监听器
    private AnimatorListener mFinishListener = new AnimatorListener() {
        @Override
        public void onAnimationEnd(Animator animation) {
            finishRefresh();
        }
    };
    //滑动更新监听器
    private ValueAnimator.AnimatorUpdateListener mScrollListener = animation -> {
        int current = (Integer) animation.getAnimatedValue();
        View view = layout.getView();
        view.scrollTo(view.getScrollX(), current);
        if (RefreshOwner.NONE == getOwner()) return;
        IRefreshAttach attach = RefreshOwner.HEADER == getOwner() ? layout.getHeader() : layout.getFooter();
        float percent = current / (attach.getView().getMeasuredHeight() / 1.0f);
        attach.onDragging(layout, percent);
    };

    RefreshKernel(@NonNull IRefreshLayout layout) {
        this.layout = layout;
        mAnimator = new AnimatorHolder();
    }

    @Override
    public void setListener(RefreshListener listener) {
        this.listener = listener;
    }

    @Override
    public RefreshState getState() {
        return mState;
    }

    @Override
    public RefreshOwner getOwner() {
        return mOwner;
    }

    @Override
    public boolean useTouchIntercept(int offset) {
        if (layout.getOneLevel().getView().isNestedScrollingEnabled()) {
            return false;
        }
        int scrollY = layout.getView().getScrollY();
        View view = layout.getTwoLevel().getView();
        boolean access;
        if (offset > 0) {
            boolean flag = !view.canScrollVertically(-1);
            access = flag && scrollY >= 0 && scrollY <= getHeadHeight();
        } else {
            access = !view.canScrollVertically(1);
        }
        return access;
    }

    @Override
    public boolean isTouchLock() {
        return isTouchLock;
    }

    @Override
    public boolean isReachTop() {
        View oneLevel = layout.getOneLevel().getView();
        View twoLevel = layout.getTwoLevel().getView();
        //只有一级刷新
        if (oneLevel == twoLevel) {
            return !oneLevel.canScrollVertically(-1);
        }
        //存在二级刷新
        int scrollY = layout.getView().getScrollY();
        boolean flag = !twoLevel.canScrollVertically(-1);
        return scrollY >= 0 && scrollY <= getHeadHeight() && flag;
    }

    @Override
    public boolean isReachBottom() {
        View view = layout.getTwoLevel().getView();
        return !view.canScrollVertically(1);
    }

    @Override
    public int onPreScroll(int dy) {
        int consumedY = 0;
        int scrollY = layout.getView().getScrollY();
        View hView = layout.getHeader().getView();
        View fView = null == layout.getFooter() ? null : layout.getFooter().getView();
        int hHeight = hView.getMeasuredHeight();
        int fHeight = null == fView ? 0 : fView.getMeasuredHeight();
        //开始判断处理
        if (hView.isEnabled() && RefreshOwner.FOOTER != mOwner) {
            int offsetTopLimit = hHeight - scrollY;
            if (isReachTop() && dy < 0) {
                //头部下拉
                consumedY = layout.getHeader().onDamping(scrollY, dy);
                mOwner = RefreshOwner.HEADER;
            } else if (dy > 0 && offsetTopLimit > 0) {
                //头部上拉
                consumedY = Math.min(offsetTopLimit, dy);
                mOwner = RefreshOwner.HEADER;
            }
        }
        if (null != fView && fView.isEnabled() && RefreshOwner.HEADER != mOwner) {
            if (isReachBottom() && dy > 0) {
                //底部上拉
                int pullY = Math.min(fHeight, dy);
                //非刷新状态下拉有阻尼效果
                boolean noDamping = layout.getFooter().isLess() && !layout.getFooter().autoCloseLess();
                int offset = scrollY - hHeight - fHeight;
                consumedY = noDamping ? pullY : layout.getFooter().onDamping(offset, pullY);
                mOwner = RefreshOwner.FOOTER;
            } else if (dy < 0 && scrollY > hHeight) {
                //底部下拉
                consumedY = (scrollY + dy) < hHeight ? (hHeight - scrollY) : dy;
                mOwner = RefreshOwner.FOOTER;
            }
        }
        return consumedY;
    }

    @Override
    public void onScroll(int scroll) {
        IRefreshAttach attach = getAttach();
        if (null == attach) return;
        View layoutView = layout.getView();
        layoutView.scrollBy(0, scroll);
        //计算相对滑动距离
        int distance = attach.getView().getMeasuredHeight();
        if (RefreshOwner.FOOTER == getOwner()) {
            distance += getHeadHeight();
        }
        distance -= layoutView.getScrollY();
        if (RefreshState.CLOSING != getState() && RefreshState.REFRESHING != getState()) {
            int trigger = attach.getActivateValue();
            boolean opening = RefreshOwner.FOOTER == getOwner() ? distance > trigger : distance < trigger;
            mState = opening ? RefreshState.OPENING : RefreshState.ACTIVE;
            isRefreshFlag = RefreshState.ACTIVE == mState;
        }
        float percent = distance / (attach.getView().getMeasuredHeight() / 1.0f);
        attach.onDragging(layout, percent);
    }

    @Override
    public boolean onPreFling(float velocityX, float velocityY) {
        int currentY = layout.getView().getScrollY();
        int headHeight = getHeadHeight();
        boolean value = false;
        //开始判断处理
        if (velocityY > 0 && layout.getHeader().getView().isEnabled()) {
            //头部处于显示状态
            value = currentY >= 0 && currentY < headHeight;
        }
        if (!value) {
            View fView = null == layout.getFooter() ? null : layout.getFooter().getView();
            if (velocityY < 0 && null != fView && fView.isEnabled()) {
                int totalHeight = headHeight + fView.getMeasuredHeight();
                //底部处于显示状态
                value = currentY > headHeight && currentY < totalHeight;
            }
        }
        return value;
    }

    @Override
    public void onFling(float velocityX, float velocityY) {
        View view = layout.getView();
        Scroller scroller = new Scroller(view.getContext());
        scroller.fling(view.getScrollX(), view.getScrollY(), (int) velocityX,
                (int) velocityY, 0, 0, Integer.MIN_VALUE,
                Integer.MAX_VALUE);
        if (!scroller.computeScrollOffset()) return;
        mAnimator.abort(null);
        int scrollY = view.getScrollY();
        int targetY = scrollY + scroller.getFinalY();
        mAnimator.times(0, scroller.getDuration()).values(scrollY, targetY);
        mAnimator.start(null, mScrollListener);
    }

    //布局发生变化时的回调
    @Override
    public void onLayoutChanged() {
        if (RefreshOwner.FOOTER == getOwner()) {
            if (!isCloseFoot) return;
            //利用RefreshLayout布局回调通知Footer关闭
            isCloseFoot = false;
            int delay = layout.getFooter().getFinishHintTime();
            layout.getView().postDelayed(this::finishRefresh, delay);
        }
    }

    @Override
    public void onStartRefresh(RefreshOwner owner) {
        if (RefreshOwner.NONE == owner) return;
        mOwner = owner;
        IRefreshAttach attach = getAttach();
        if (null == attach) return;
        if (RefreshState.REFRESHING == getState()) return;
        if (attach.isLess()) {
            onStopRefresh(false);
        } else {
            isAutoRefresh = RefreshState.NONE == getState();
            mState = RefreshState.REFRESHING;
            notifyStateChanged();
            if (null != listener) listener.onRefresh(owner);
        }
    }

    @Override
    public void onStopRefresh(boolean success) {
        if (RefreshOwner.NONE == getOwner()) return;
        if (RefreshState.CLOSING != getState() && RefreshState.NONE != getState()) {
            mState = RefreshState.CLOSING;
            isRefreshFlag = success;
            notifyStateChanged();
        }
    }

    @Override
    public void notifyStateChanged() {
        IRefreshAttach attach = getAttach();
        if (null == attach) return;
        int targetY = 0, animationDelay = 0;
        int scrollY = layout.getView().getScrollY();
        if (RefreshState.CLOSING == getState()) {
            if (RefreshOwner.HEADER == getOwner()) {
                targetY = getHeadHeight();
                if (isRefreshFlag) {
                    isRefreshFlag = false;
                    animationDelay = attach.getFinishHintTime();
                    float percent = scrollY / (attach.getView().getMeasuredHeight() / 1.0f);
                    attach.onDragging(layout, percent);
                }
            } else if (RefreshOwner.FOOTER == getOwner()) {
                if (isRefreshFlag) {
                    isRefreshFlag = false;
                    targetY = 0;
                    if (attach.getFinishHintTime() > 0) {
                        float percent = scrollY / (attach.getView().getMeasuredHeight() / 1.0f);
                        attach.onDragging(layout, percent);
                    }
                    isCloseFoot = true;
                    isTouchLock = true;
                } else {
                    targetY = attach.isLess() && !attach.autoCloseLess() ? 0 : getHeadHeight();
                }
            }
        } else if (RefreshState.REFRESHING == getState()) {
            targetY = attach.getView().getMeasuredHeight() - attach.getActivateValue();
            if (RefreshOwner.FOOTER == getOwner()) {
                targetY += getHeadHeight();
                if (!isAutoRefresh && targetY >= scrollY) targetY = 0;
            } else {
                //向上拉回时不进行回弹
                if (!isAutoRefresh && targetY <= scrollY) targetY = 0;
            }
            isAutoRefresh = false;
        }
        if (0 == targetY) return;
        //执行动画
        isTouchLock = true;
        mAnimator.abort(mFinishListener);
        mAnimator.times(animationDelay, attach.getReboundTime()).values(scrollY, targetY);
        mAnimator.start(mFinishListener, mScrollListener);
    }

    @Override
    public void onDestroy() {
        mAnimator.abort(mFinishListener);
        if (null != layout.getHeader()) {
            layout.getHeader().onDestroy();
        }
        if (null != layout.getFooter()) {
            layout.getFooter().onDestroy();
        }
    }

    //结束刷新状态重置
    private void finishRefresh() {
        IRefreshAttach attach = getAttach();
        if (null == attach) return;
        isTouchLock = false;
        if (RefreshState.CLOSING == getState()) {
            if (RefreshOwner.FOOTER == getOwner()) {
                View layoutView = layout.getView();
                int headHeight = getHeadHeight();
                int dy = layoutView.getScrollY() - headHeight;
                layoutView.scrollTo(0, headHeight);
                layout.getTwoLevel().getView().scrollBy(0, dy);
            }
            mOwner = RefreshOwner.NONE;
            mState = RefreshState.NONE;
            attach.onDragging(layout, 0);
        }
    }

    //通过状态获取刷新附件
    private IRefreshAttach getAttach() {
        if (RefreshOwner.NONE == getOwner()) return null;
        IRefreshAttach attach = getOwner() == RefreshOwner.HEADER ? layout.getHeader() : layout.getFooter();
        if (null != attach && !attach.getView().isEnabled()) {
            attach = null;
        }
        return attach;
    }

    //获取头部高度
    private int getHeadHeight() {
        return layout.getHeader().getView().getMeasuredHeight();
    }
}