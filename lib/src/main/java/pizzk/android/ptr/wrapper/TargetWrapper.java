package pizzk.android.ptr.wrapper;

import android.view.View;

import pizzk.android.ptr.api.IRefreshView;

public class TargetWrapper implements IRefreshView {
    private View mView;

    public TargetWrapper(View view) {
        mView = view;
    }

    @Override
    public View getView() {
        return mView;
    }
}
