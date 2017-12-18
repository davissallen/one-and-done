package me.davisallen.oneanddone.utils;

import android.app.Activity;
import android.view.inputmethod.InputMethodManager;

/**
 * Package Name:   me.davisallen.oneanddone.utils
 * Project:        one-and-done
 * Created by davis, on 12/17/17
 */

public class KeyboardUtils {

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(
                    activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

}
