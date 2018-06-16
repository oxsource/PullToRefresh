package pizzk.android.ptr.api;

import android.support.annotation.NonNull;

import pizzk.android.ptr.constant.RefreshOwner;

public interface RefreshListener {
    void onRefresh(@NonNull RefreshOwner owner);
}