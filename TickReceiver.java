package com.salahtime.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** ওয়াক্ত বদলের সময়, বুটের পর, সময়/তারিখ বদলালে উইজেট রিফ্রেশ করে */
public class TickReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context c, Intent i) {
        SalahWidgetUpdater.updateAll(c);
        SalahWidgetUpdater.schedule(c);
    }
}
