package com.salahtime.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;

/** উইজেট ৩: তারিখ + চলমান ওয়াক্ত + শর্টকাট বাটন */
public class WidgetC extends android.appwidget.AppWidgetProvider {
    @Override public void onUpdate(Context c, AppWidgetManager m, int[] ids) {
        SalahWidgetUpdater.updateAll(c);
        SalahWidgetUpdater.schedule(c);
    }
    @Override public void onEnabled(Context c) { SalahWidgetUpdater.schedule(c); }
}
