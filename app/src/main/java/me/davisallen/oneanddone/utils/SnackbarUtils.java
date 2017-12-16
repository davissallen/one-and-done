package me.davisallen.oneanddone.utils;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;

/**
 * Package Name:   me.davisallen.oneanddone.utils
 * Project:        one-and-done
 * Created by davis, on 12/16/17
 */

public class SnackbarUtils {

    public static void showSnackbar(Context context, int stringId) {
        String message = context.getString(stringId);
        Snackbar snackbar = Snackbar.make(new CoordinatorLayout(context), message, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

}
