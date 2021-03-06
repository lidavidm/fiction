package com.lithiumli.fiction.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.EditText;

import de.keyboardsurfer.android.widget.crouton.Style;

import com.lithiumli.fiction.R;

public class UiUtils {
    public static final Style STYLE_INFO = (new Style.Builder(Style.INFO))
        .setBackgroundDrawable(R.drawable.ab_texture_tile_fiction_crouton)
        .setTileEnabled(true)
        .build();

    public abstract static class Dialog {
        AlertDialog mDialog;

        public Dialog(int titleResId, Context context) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(titleResId);
            final EditText text = new EditText(context);
            builder.setView(text);
            builder.setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        onDialogDismissed(true, text.getText().toString());
                    }
                });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mDialog.dismiss();
                        onDialogDismissed(false, null);
                    }
                });
            mDialog = builder.create();
        }

        public void show() {
            mDialog.show();
        }

        public abstract void onDialogDismissed(boolean positive, String input);
    }
}
