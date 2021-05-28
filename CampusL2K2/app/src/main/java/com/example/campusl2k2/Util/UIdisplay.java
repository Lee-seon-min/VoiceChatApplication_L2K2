package com.example.campusl2k2.Util;

import android.content.Context;
import android.widget.Toast;

public class UIdisplay {
    static public void showMessage(Context ctx, String msg){
        Toast.makeText(ctx,msg, Toast.LENGTH_SHORT).show();
    }
}
