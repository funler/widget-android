package com.github.funler.widget_android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.github.funler.jsbridge.BridgeWebView;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class WidgetViewActivity extends AppCompatActivity {

    private BridgeWebView bridgeWebView;
    private RelativeLayout root;

    private final BroadcastReceiver closeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
            overridePendingTransition(R.anim.scale_up, R.anim.scale_down);
        }
    };

    private final BroadcastReceiver maximizeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            WidgetView.getInstance().setMaximized(true);
            maximize();
        }
    };

    private final BroadcastReceiver restoreReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            WidgetView.getInstance().setMaximized(false);
            minimize();
        }
    };

    @Override
    public void onCreate(Bundle savedStateInstance) {
        super.onCreate(savedStateInstance);
        overridePendingTransition(R.anim.scale_up, R.anim.scale_down);

        root = new RelativeLayout(getBaseContext());
        root.setLayoutParams(new RelativeLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));

        bridgeWebView = WidgetView.getInstance().getBridgeWebView();
        root.addView(bridgeWebView);

        setContentView(root);

        makeFullScreenWithoutSystemUI();
        configureInitialSize();

        registerReceivers();
    }

    @Override
    public void onBackPressed() {
        // do nothing
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        detachBridgeView();
        unregisterReceivers();
    }

    private void makeFullScreenWithoutSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.INVISIBLE);
        }
    }

    private void registerReceivers() {
        registerReceiver(closeReceiver, new IntentFilter("close_widget_view"));
        registerReceiver(maximizeReceiver, new IntentFilter("maximize_widget_view"));
        registerReceiver(restoreReceiver, new IntentFilter("restore_widget_view"));
    }

    private void unregisterReceivers() {
        unregisterReceiver(closeReceiver);
        unregisterReceiver(maximizeReceiver);
        unregisterReceiver(restoreReceiver);
    }

    private void configureInitialSize() {
        WindowManager wm = (WindowManager) WidgetView.getInstance().getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);

        if (WidgetView.getInstance().getRestoreWidth() == 0) {
            WidgetView.getInstance().setRestoreWidth(metrics.widthPixels - 60);
            WidgetView.getInstance().setRestoreHeight(metrics.heightPixels - 100);
        }

        if (WidgetView.getInstance().isMaximized()) {
            maximize();
        } else {
            minimize();
        }
    }

    private void updateLayoutParams(int width, int height) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) bridgeWebView.getLayoutParams();
        if (params == null) {
            params = new RelativeLayout.LayoutParams(width, height);
        } else {
            params.width = width;
            params.height = height;
        }

        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        bridgeWebView.setLayoutParams(params);
    }

    private void detachBridgeView() {
        if (bridgeWebView != null && bridgeWebView.getParent() != null) {
            ((ViewGroup) bridgeWebView.getParent()).removeView(bridgeWebView);
        }
    }

    private void maximize() {
        updateLayoutParams(MATCH_PARENT, MATCH_PARENT);
    }

    private void minimize() {
        updateLayoutParams(
                WidgetView.getInstance().getRestoreWidth(),
                WidgetView.getInstance().getRestoreHeight()
        );
    }
}