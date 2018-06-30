package pizzk.android.ptr.wrapper;

import android.view.View;

import pizzk.android.ptr.api.IRefreshLayout;
import pizzk.android.ptr.api.IRefreshAttach;

public class AttachWrapper implements IRefreshAttach {
    private View mView;

    public AttachWrapper(View view) {
        mView = view;
    }

    @Override
    public View getView() {
        return mView;
    }

    @Override
    public int getActivateValue() {
        return 100;
    }

    @Override
    public int getReboundTime() {
        return 250;
    }

    @Override
    public int getFinishHintTime() {
        return 1500;
    }

    @Override
    public int onDamping(int current, int offset) {
        return (int) (0.35f * offset);
    }

    @Override
    public boolean autoCloseLess() {
        return true;
    }

    @Override
    public void onDragging(IRefreshLayout layout, float percent) {

    }

    @Override
    public void onDestroy() {

    }
}
