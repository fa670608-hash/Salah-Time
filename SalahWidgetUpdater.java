package com.salahtime.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.widget.RemoteViews;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import com.bjdfarhma.salah.R;

public class SalahWidgetUpdater {

    static final String PREFS = "salah_widget", KEY = "data";
    static final String[] NAMES = {"ফজর", "যুহর", "আসর", "মাগরিব", "ইশা"};
    static final String[] EMOJI = {"\uD83C\uDF05", "\u2600\uFE0F", "\uD83C\uDF24\uFE0F", "\uD83C\uDF07", "\uD83C\uDF19"};
    // JS থেকে আসা ৬টা সময়: [ফজর, সূর্যোদয়, যুহর, আসর, মাগরিব, ইশা] -> ৫ ওয়াক্তের index
    static final int[] IDX = {0, 2, 3, 4, 5};
    static final int[] CELL = {R.id.c0, R.id.c1, R.id.c2, R.id.c3, R.id.c4};
    static final int[] NAME = {R.id.n0, R.id.n1, R.id.n2, R.id.n3, R.id.n4};
    static final int[] TIME = {R.id.t0, R.id.t1, R.id.t2, R.id.t3, R.id.t4};
    static final int[] EMO = {R.id.e0, R.id.e1, R.id.e2, R.id.e3, R.id.e4};
    static final int[] BTN = {R.id.b0, R.id.b1, R.id.b2, R.id.b3, R.id.b4};
    static final String[] BTN_EMOJI = {"\uD83D\uDCD6", "\uD83E\uDDED", "\uD83D\uDCFF", "\uD83E\uDD32", "\uD83D\uDD4C"};
    static final String[] BTN_TAB = {"quran", "qibla", "tasbih", "dua", "prayer"};

    /* ---------- data ---------- */
    static class Day { String ymd, hday, hrest, greg; long[] t = new long[6]; }
    static class Data { String loc; List<Day> days = new ArrayList<>(); }
    static class State {
        Day today; int hi; String label, range; long target;
    }

    static void save(Context c, String json) {
        c.getSharedPreferences(PREFS, 0).edit().putString(KEY, json).apply();
    }

    static Data load(Context c) {
        try {
            String s = c.getSharedPreferences(PREFS, 0).getString(KEY, null);
            if (s == null) return null;
            JSONObject o = new JSONObject(s);
            Data d = new Data();
            d.loc = o.optString("loc");
            JSONArray a = o.getJSONArray("days");
            for (int i = 0; i < a.length(); i++) {
                JSONObject j = a.getJSONObject(i);
                Day x = new Day();
                x.ymd = j.optString("ymd"); x.hday = j.optString("hday");
                x.hrest = j.optString("hrest"); x.greg = j.optString("greg");
                JSONArray t = j.getJSONArray("t");
                for (int k = 0; k < 6; k++) x.t[k] = t.getLong(k);
                d.days.add(x);
            }
            return d.days.isEmpty() ? null : d;
        } catch (Exception e) { return null; }
    }

    /* ---------- formatting ---------- */
    static String bn(String s) {
        StringBuilder b = new StringBuilder();
        for (char ch : s.toCharArray()) b.append(ch >= '0' && ch <= '9' ? (char) ('\u09E6' + (ch - '0')) : ch);
        return b.toString();
    }

    static String fmt(long ms) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(ms);
        int h = c.get(Calendar.HOUR);
        if (h == 0) h = 12;
        return bn(String.format(Locale.US, "%02d:%02d", h, c.get(Calendar.MINUTE)));
    }

    static String todayYmd() {
        Calendar c = Calendar.getInstance();
        return c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-" + c.get(Calendar.DAY_OF_MONTH);
    }

    /* ---------- current prayer logic ---------- */
    static State compute(Data d) {
        if (d == null) return null;
        long now = System.currentTimeMillis();
        String ty = todayYmd();
        int di = -1;
        for (int i = 0; i < d.days.size(); i++) if (d.days.get(i).ymd.equals(ty)) di = i;
        if (di < 0) { // ডাটা পুরনো হয়ে গেলে কাছাকাছি দিনটা নিই
            di = 0;
            for (int i = 0; i < d.days.size(); i++) if (d.days.get(i).t[0] <= now) di = i;
        }
        State s = new State();
        s.today = d.days.get(di);
        for (int k = Math.max(0, di - 1); k <= Math.min(d.days.size() - 1, di + 1); k++) {
            Day x = d.days.get(k);
            long nextFajr = (k + 1 < d.days.size()) ? d.days.get(k + 1).t[0] : x.t[0] + 86400000L;
            long[][] w = {
                {x.t[0], x.t[1]}, {x.t[2], x.t[3]}, {x.t[3], x.t[4]}, {x.t[4], x.t[5]}, {x.t[5], nextFajr}
            };
            for (int p = 0; p < 5; p++) {
                if (now >= w[p][0] && now < w[p][1]) {
                    s.hi = p; s.target = w[p][1];
                    s.label = NAMES[p] + " শেষ হতে বাকি";
                    s.range = NAMES[p] + " " + fmt(w[p][0]) + "-" + fmt(w[p][1]);
                    return s;
                }
            }
        }
        // সূর্যোদয় থেকে যুহরের আগ পর্যন্ত: যুহরকে হাইলাইট
        s.hi = 1; s.target = s.today.t[2];
        s.label = "যুহর শুরু হতে বাকি";
        s.range = "যুহর " + fmt(s.today.t[2]) + "-" + fmt(s.today.t[3]);
        return s;
    }

    /* ---------- build ---------- */
    static PendingIntent open(Context c, String tab) {
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("salahtime://tab/" + tab));
        i.setPackage(c.getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return PendingIntent.getActivity(c, tab.hashCode(), i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    static void txt(RemoteViews rv, int id, String v) { rv.setTextViewText(id, v); }

    static void cells(RemoteViews rv, State s, boolean emoji, boolean times) {
        for (int i = 0; i < 5; i++) {
            rv.setTextViewText(NAME[i], NAMES[i]);
            if (emoji) rv.setTextViewText(EMO[i], EMOJI[i]);
            if (times && s != null) rv.setTextViewText(TIME[i], fmt(s.today.t[IDX[i]]));
            rv.setInt(CELL[i], "setBackgroundResource", (s != null && s.hi == i) ? R.drawable.sw_cell_on : 0);
        }
    }

    static RemoteViews buildA(Context c, State s) {
        RemoteViews rv = new RemoteViews(c.getPackageName(), R.layout.salah_widget_a);
        rv.setOnClickPendingIntent(R.id.root, open(c, "prayer"));
        if (s == null) { txt(rv, R.id.tvHijri, "এপটি একবার খুলুন"); return rv; }
        txt(rv, R.id.tvDay, s.today.hday);
        txt(rv, R.id.tvHijri, s.today.hrest);
        txt(rv, R.id.tvGreg, s.today.greg);
        txt(rv, R.id.tvSunrise, "সূর্যোদয় " + fmt(s.today.t[1]));
        txt(rv, R.id.tvSunset, "সূর্যাস্ত " + fmt(s.today.t[4]));
        cells(rv, s, true, false);
        return rv;
    }

    static RemoteViews buildB(Context c, State s) {
        RemoteViews rv = new RemoteViews(c.getPackageName(), R.layout.salah_widget_b);
        rv.setOnClickPendingIntent(R.id.root, open(c, "prayer"));
        if (s == null) { txt(rv, R.id.tvHijri, "এপটি একবার খুলুন"); return rv; }
        txt(rv, R.id.tvDay, s.today.hday);
        txt(rv, R.id.tvHijri, s.today.hrest);
        txt(rv, R.id.tvSunsetLbl, "সূর্যাস্ত");
        txt(rv, R.id.tvSunset, fmt(s.today.t[4]));
        cells(rv, s, false, true);
        txt(rv, R.id.tvLabel, s.label);
        long base = SystemClock.elapsedRealtime() + (s.target - System.currentTimeMillis());
        rv.setChronometer(R.id.chrono, base, null, true);
        if (Build.VERSION.SDK_INT >= 24) rv.setChronometerCountDown(R.id.chrono, true);
        return rv;
    }

    static RemoteViews buildC(Context c, State s) {
        RemoteViews rv = new RemoteViews(c.getPackageName(), R.layout.salah_widget_c);
        rv.setOnClickPendingIntent(R.id.root, open(c, "prayer"));
        for (int i = 0; i < 5; i++) {
            txt(rv, BTN[i], BTN_EMOJI[i]);
            rv.setOnClickPendingIntent(BTN[i], open(c, BTN_TAB[i]));
        }
        if (s == null) { txt(rv, R.id.tvHijri, "এপটি একবার খুলুন"); return rv; }
        txt(rv, R.id.tvHijri, s.today.hday + " " + s.today.hrest.replace("হিজরি", "হি."));
        txt(rv, R.id.tvGreg, s.today.greg);
        txt(rv, R.id.tvSunrise, "সূর্যোদয় " + fmt(s.today.t[1]));
        txt(rv, R.id.tvSunset, "সূর্যাস্ত " + fmt(s.today.t[4]));
        txt(rv, R.id.tvRange, s.range);
        return rv;
    }

    static void push(Context c, AppWidgetManager m, Class<?> cls, int type, State s) {
        int[] ids = m.getAppWidgetIds(new ComponentName(c, cls));
        for (int id : ids) {
            RemoteViews rv = type == 0 ? buildA(c, s) : type == 1 ? buildB(c, s) : buildC(c, s);
            m.updateAppWidget(id, rv);
        }
    }

    public static void updateAll(Context c) {
        try {
            AppWidgetManager m = AppWidgetManager.getInstance(c);
            State s = compute(load(c));
            push(c, m, WidgetA.class, 0, s);
            push(c, m, WidgetB.class, 1, s);
            push(c, m, WidgetC.class, 2, s);
        } catch (Exception ignored) { }
    }

    /* ---------- next refresh (ওয়াক্ত বদল / মাঝরাত) ---------- */
    public static void schedule(Context c) {
        long now = System.currentTimeMillis();
        long next = now + 3 * 3600000L; // সর্বোচ্চ ৩ ঘণ্টা পর আবার
        Data d = load(c);
        if (d != null) for (Day x : d.days) for (long t : x.t) if (t > now && t < next) next = t;
        Calendar mid = Calendar.getInstance();
        mid.add(Calendar.DAY_OF_MONTH, 1);
        mid.set(Calendar.HOUR_OF_DAY, 0); mid.set(Calendar.MINUTE, 0); mid.set(Calendar.SECOND, 5);
        if (mid.getTimeInMillis() < next) next = mid.getTimeInMillis();
        next += 1500;

        Intent i = new Intent(c, TickReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(c, 7001, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        if (Build.VERSION.SDK_INT >= 23) am.setAndAllowWhileIdle(AlarmManager.RTC, next, pi);
        else am.set(AlarmManager.RTC, next, pi);
    }
}
