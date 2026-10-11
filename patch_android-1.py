#!/usr/bin/env python3
"""cap add android এর পর চালাও: root-এ থাকা উইজেট ফাইলগুলো Android প্রজেক্টে বসায়, Manifest ও MainActivity প্যাচ করে।"""
import shutil, pathlib, re, sys, glob, os

APP_ID = "com.bjdfarhma.salah"
A = pathlib.Path("android/app/src/main")

def put(files, dest):
    dest.mkdir(parents=True, exist_ok=True)
    for f in files:
        shutil.copy(f, dest / os.path.basename(f))

# 1) Java ফাইল
java = ["SalahWidgetPlugin.java", "SalahWidgetUpdater.java", "TickReceiver.java",
        "WidgetA.java", "WidgetB.java", "WidgetC.java"]
put(java, A / "java/com/salahtime/widget")

# 2) res ফাইল
put(sorted(glob.glob("salah_widget_?.xml")), A / "res/layout")
put(sorted(glob.glob("salah_widget_?_info.xml")), A / "res/xml")
put(sorted(glob.glob("sw_*.xml")), A / "res/drawable")

# 3) MainActivity: প্লাগিন রেজিস্টার
main = A / "java" / pathlib.Path(*APP_ID.split(".")) / "MainActivity.java"
main.parent.mkdir(parents=True, exist_ok=True)
main.write_text(f"""package {APP_ID};

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;
import com.salahtime.widget.SalahWidgetPlugin;

public class MainActivity extends BridgeActivity {{
    @Override
    public void onCreate(Bundle savedInstanceState) {{
        registerPlugin(SalahWidgetPlugin.class);
        super.onCreate(savedInstanceState);
    }}
}}
""", encoding="utf-8")

# 4) AndroidManifest প্যাচ
mf = A / "AndroidManifest.xml"
x = mf.read_text(encoding="utf-8")
if "com.salahtime.widget" not in x:
    snip = pathlib.Path("AndroidManifest-snippet.xml").read_text(encoding="utf-8")
    ifilter = re.search(r'<!-- \(১\).*?-->\s*(<intent-filter>.*?</intent-filter>)', snip, re.S).group(1)
    recv = snip[snip.index('<receiver'):]
    if "</activity>" not in x or "</application>" not in x:
        sys.exit("Manifest structure unexpected")
    x = x.replace("</activity>", ifilter + "\n        </activity>", 1)
    x = x.replace("</application>", recv + "\n    </application>", 1)
    perms = ""
    for p in ("RECEIVE_BOOT_COMPLETED", "POST_NOTIFICATIONS", "ACCESS_COARSE_LOCATION", "ACCESS_FINE_LOCATION"):
        if f"android.permission.{p}" not in x:
            perms += f'    <uses-permission android:name="android.permission.{p}" />\n'
    x = x.replace("</manifest>", perms + "</manifest>")
    mf.write_text(x, encoding="utf-8")
print("android patched OK")
