package com.example.kadir.agricultureprojectclientside.ShortCut;

import android.app.Activity;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

public class ShortCut {

    public static void displayMessageToast(Context context, String displayMessage) {
        Toast.makeText(context, displayMessage, Toast.LENGTH_LONG).show();
    }

    public static void hideSoftKeyboard(Activity activity) {
        if(activity == null || activity.getCurrentFocus() == null ||activity.getCurrentFocus().getWindowToken() == null ) return;
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }
}
