package com.trichain.wifishare.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.trichain.wifishare.R;
import com.trichain.wifishare.retrofit.ApiService;
import com.trichain.wifishare.retrofit.RetrofitClient;

import java.util.Objects;

public class util {

    private static final String TAG = "util";
    public static final String BASE_URL = "http://google.com/";

    /*Instantiate retrofit API client*/
    public static ApiService getApiService() {
        return RetrofitClient.getClient(BASE_URL).create(ApiService.class);
    }

    public static void hideView(View v, boolean withAnimation) {
        if (v.getVisibility() == View.VISIBLE) {
            if (withAnimation)
                v.animate()
                        .alpha(0f)
                        .setDuration(350)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                v.setVisibility(View.GONE);
                            }
                        });
            else v.setVisibility(View.GONE);
        }
    }

    public static void hideViewInvisible(View v, boolean withAnimation) {
        if (v.getVisibility() == View.VISIBLE) {
            if (withAnimation)
                v.animate()
                        .alpha(0f)
                        .setDuration(350)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                v.setVisibility(View.INVISIBLE);
                            }
                        });
            else v.setVisibility(View.INVISIBLE);
        }
    }

    public static void showView(View v, boolean withAnimation) {
        if (v.getVisibility() == View.GONE || v.getVisibility() == View.INVISIBLE) {
            if (withAnimation) {
                v.setAlpha(0f);
                v.setVisibility(View.VISIBLE);
                v.animate()
                        .alpha(1f)
                        .setDuration(350)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                v.setVisibility(View.VISIBLE);
                            }
                        });
            } else {
                v.setVisibility(View.VISIBLE);
            }
        }
    }


    /*Glide load image into view*/
    public static void loadImage(ImageView imageView, String url) {
        Log.e(TAG, "loadImage: Glide: attempting to load image: " + url);

        try {
            Glide.with(imageView)
                    .load(url)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imageView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*Get the ;ine number of the calling line for this method*/
    public static int getLineNumber() {
        return ___8drrd3148796d_Xaf();
    }

    /**
     * This methods name is ridiculous on purpose to prevent any other method
     * names in the stack trace from potentially matching this one.
     *
     * @return The line number of the code that called the method that called
     * this method(Should only be called by getLineNumber()).
     * @author Brian_Entei
     */
    private static int ___8drrd3148796d_Xaf() {
        boolean thisOne = false;
        int thisOneCountDown = 1;
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : elements) {
            String methodName = element.getMethodName();
            int lineNum = element.getLineNumber();
            if (thisOne && (thisOneCountDown == 0)) {
                return lineNum;
            } else if (thisOne) {
                thisOneCountDown--;
            }
            if (methodName.equals("___8drrd3148796d_Xaf")) {
                thisOne = true;
            }
        }
        return -1;
    }

    private static void showDialog(Activity activity, String message, String body, String btnPostiveText,
                                   String btnNegativeText, View.OnClickListener onClickListener) {
    }

}