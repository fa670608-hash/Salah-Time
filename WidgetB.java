package com.salahtime.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;

/** উইজেট ২: তারিখ + ৫ ওয়াক্তের সময় + লাইভ কাউন্টডাউন */
public class WidgetB extends android.appwidget.AppWidgetProvider {
    @Override public void onUpdate(Context c, AppWidgetManager m, int[] ids) {
        SalahWidgetUpdater.updateAll(c);
        SalahWidgetUpdater.schedule(c);
    }
    @Override public void onEnabled(Context c) { SalahWidgetUpdater.schedule(c); }
}
