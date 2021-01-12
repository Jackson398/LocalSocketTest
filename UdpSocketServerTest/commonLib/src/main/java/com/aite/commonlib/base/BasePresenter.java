package com.aite.commonlib.base;

import android.content.Context;

import androidx.annotation.NonNull;

public interface BasePresenter {
    void install(@NonNull Context context);
    void uninstall();
}
