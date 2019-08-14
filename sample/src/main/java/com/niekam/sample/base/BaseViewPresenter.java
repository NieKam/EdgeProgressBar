package com.niekam.sample.base;

import androidx.annotation.Nullable;

public abstract class BaseViewPresenter<V> {

    private @Nullable
    V mView;

    public final void attachView(V view) {
        if (view == null) {
            return;
        }

        mView = view;
        onViewAttached();
    }

    protected void onViewAttached() {
    }

    public final void detachView() {
        onViewDetached();
        mView = null;
    }

    protected void onViewDetached() {
    }

    public @Nullable
    V getView() {
        return mView;
    }
}
