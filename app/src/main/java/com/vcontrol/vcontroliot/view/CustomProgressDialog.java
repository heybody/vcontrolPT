package com.vcontrol.vcontroliot.view;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.KeyEvent;
import android.widget.TextView;

import com.vcontrol.vcontroliot.R;
import com.vcontrol.vcontroliot.log.Log;

/**
 * 说明：
 *
 */
public class CustomProgressDialog extends Dialog
{
    private static final String TAG = "CustomProgressDialog";

    public CustomProgressDialog(Context context, String strMessage)
    {
        this(context, R.style.CustomProgressDialog, strMessage);
    }

    public CustomProgressDialog(Context context, int theme, String strMessage)
    {
        super(context, theme);
        this.setContentView(R.layout.customprogressdialog);
        setCanceledOnTouchOutside(true);
        this.getWindow().getAttributes().gravity = Gravity.CENTER;
        TextView tvMsg = (TextView) this.findViewById(R.id.id_tv_loadingmsg);
        if (tvMsg != null)
        {
            tvMsg.setText(strMessage);
        }
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        Log.debug(TAG, "handleKeyDown::keyCode:" + keyCode);
        return true;
    }
}
