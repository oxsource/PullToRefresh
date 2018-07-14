package pizzk.android.ptr.anim;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.support.annotation.MainThread;

public class AnimatorHolder extends ValueAnimator {

    @MainThread
    public AnimatorHolder times(int delay, int duration) {
        setStartDelay(delay);
        setDuration(duration);
        return this;
    }

    @MainThread
    public AnimatorHolder values(int... values) {
        setIntValues(values);
        return this;
    }

    @MainThread
    public void start(Animator.AnimatorListener listener, ValueAnimator.AnimatorUpdateListener updateListener) {
        setRepeatCount(0);
        if (null != listener) {
            addListener(listener);
        }
        if (null != updateListener) {
            addUpdateListener(updateListener);
        }
        start();
    }

    @MainThread
    public void abort(Animator.AnimatorListener listener) {
        removeAllUpdateListeners();
        if (null != listener) {
            removeListener(listener);
        }
        if (isStarted() || isRunning()) {
            cancel();
        }
    }

    @MainThread
    public boolean working() {
        return isStarted() || isRunning();
    }
}
