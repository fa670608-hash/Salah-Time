package com.salahtime.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;

/** উইজেট ১: বড় তারিখ + সূর্যোদয়/সূর্যাস্ত + ৫ ওয়াক্তের আইকন */
public class WidgetA extends android.appwidget.AppWidgetProvider {
    @Override public void onUpdate(Context c, AppWidgetManager m, int[] ids) {
        SalahWidgetUpdater.updateAll(c);
        SalahWidgetUpdater.schedule(c);
    }
    @Override public void onEnabled(Context c) { SalahWidgetUpdater.schedule(c); }
}
