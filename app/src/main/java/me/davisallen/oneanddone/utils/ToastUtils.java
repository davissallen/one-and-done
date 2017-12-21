package me.davisallen.oneanddone.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Package Name:   me.davisallen.oneanddone.utils
 * Project:        one-and-done
 * Created by davis, on 12/20/17
 */

public class ToastUtils {

    private static Toast sToast;

    public static void showToast(Context context, String message) {
        sToast.cancel();
        sToast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        sToast.show();
    }
}
