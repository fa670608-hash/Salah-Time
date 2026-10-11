package com.salahtime.widget;

import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

/** JS থেকে নামাজের সময় নিয়ে উইজেটে পাঠায়: Capacitor.Plugins.SalahWidget.update({json}) */
@CapacitorPlugin(name = "SalahWidget")
public class SalahWidgetPlugin extends Plugin {
    @PluginMethod
    public void update(PluginCall call) {
        String json = call.getString("json");
        if (json == null) { call.reject("json missing"); return; }
        SalahWidgetUpdater.save(getContext(), json);
        SalahWidgetUpdater.updateAll(getContext());
        SalahWidgetUpdater.schedule(getContext());
        call.resolve();
    }
}
