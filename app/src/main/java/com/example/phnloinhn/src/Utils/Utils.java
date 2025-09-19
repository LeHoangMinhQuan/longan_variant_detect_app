package com.example.phnloinhn.src.Utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.example.phnloinhn.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class Utils {
    private static AlertDialog loadingDialog;

    /**
     * Show loading dialog
     * @param context Activity or Fragment context
     * @param message Optional message (nullable)
     */
    public static void showLoading(Context context, String message) {
        if (context == null) return;

        if (loadingDialog == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View dialogView = inflater.inflate(R.layout.dialog_loading, null);

            TextView textView = dialogView.findViewById(R.id.progress_text);
            if (message != null && !message.isEmpty()) {
                textView.setText(message);
                textView.setVisibility(View.VISIBLE);
            } else {
                textView.setVisibility(View.GONE);
            }

            loadingDialog = new MaterialAlertDialogBuilder(context)
                    .setView(dialogView)
                    .setCancelable(false)
                    .create();
        }

        if (!loadingDialog.isShowing()) {
            loadingDialog.show();
        }
    }

    /**
     * Hide loading dialog
     */
    public static void hideLoading() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
            loadingDialog = null; // reset
        }
    }
}
