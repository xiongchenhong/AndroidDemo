package com.xch.shortcuts;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * 在各个版本创建和删除桌面快捷方式的方法
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    private static final String UNINSTALL_ACTION = "com.android.launcher.action.UNINSTALL_SHORTCUT";
    private static final String INSTALL_ACTION = "com.android.launcher.action.INSTALL_SHORTCUT";
    public static final String CALLBACK_INTENT_ACTION = "com.xch.action.install_shortcut";

    private ShortcutManager mShortcutManager;
    private Intent actionIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.baidu.com/"));
    private BroadcastReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.create).setOnClickListener(this);
        findViewById(R.id.delete).setOnClickListener(this);
        registerReceiver();
    }

    private void registerReceiver() {
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "onReceive: shortcut created");
            }
        };
        registerReceiver(mReceiver, new IntentFilter(CALLBACK_INTENT_ACTION));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.create:
                create(actionIntent);
                break;
            case R.id.delete:
                delete(actionIntent);
                break;
        }
    }

    private void create(Intent actionIntent) {
        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.N_MR1) {
            mShortcutManager = (ShortcutManager) getSystemService(SHORTCUT_SERVICE);
            if (mShortcutManager != null && mShortcutManager.isRequestPinShortcutSupported()) {
                ShortcutInfo info = new ShortcutInfo.Builder(this, "id1")
                        .setIcon(Icon.createWithResource(this, R.drawable.feature_share_qq))
                        .setLongLabel("long")
                        .setShortLabel("8.0 name")
                        .setIntent(actionIntent)
                        .build();
                Intent intent = new Intent(CALLBACK_INTENT_ACTION); // 用户点击确认后的回调intent
                PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                mShortcutManager.requestPinShortcut(info, pendingIntent.getIntentSender());
            }
        } else {
            Intent.ShortcutIconResource icon = Intent.ShortcutIconResource.fromContext(this, R.drawable.feature_share_qq);
            Intent intent = new Intent(INSTALL_ACTION);
            intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, icon);
            intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, actionIntent);
            intent.putExtra("duplicate", false);
            intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "7.0 name");
            sendBroadcast(intent);
        }
    }

    private void delete(Intent actionIntent) {
        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.N_MR1) {
            List<ShortcutInfo> pinnedShortcuts = mShortcutManager.getPinnedShortcuts();
            List<ShortcutInfo> dynamicShortcuts = mShortcutManager.getDynamicShortcuts();
            List<String> list = new ArrayList<>();
            list.add("id1");
            mShortcutManager.disableShortcuts(list, "id1 is disabled");
        } else {
            Intent intent = new Intent(UNINSTALL_ACTION);
            intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, actionIntent);
            intent.putExtra("duplicate", false);
            intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "7.0 name");
            sendBroadcast(intent);
        }
    }

    @Override
    protected void onDestroy() {
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
        super.onDestroy();
    }
}
