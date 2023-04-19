package com.theoplayer.demo.simpleott;

import android.content.Context;
import android.text.Layout;
import android.text.SpannableString;
import android.text.style.AlignmentSpan;
import android.widget.Toast;

import androidx.annotation.StringRes;

public class ToastUtils {

    /**
     * Displays centered toast message.
     *
     * @param context - The current context.
     * @param messageResId - The message resource id to be toasted.
     */
    public static void toastMessage(Context context, @StringRes int messageResId) {
        SpannableString toastMessage = SpannableString.valueOf(context.getString(messageResId));
        toastMessage.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, toastMessage.length(), 0);
        Toast.makeText(context, toastMessage, Toast.LENGTH_LONG).show();
    }

}
