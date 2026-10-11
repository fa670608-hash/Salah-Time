#!/usr/bin/env python3
"""cap add android এর পর চালাও: উইজেট কোড বসায়, Manifest ও MainActivity প্যাচ করে।"""
import shutil, pathlib, re, sys

APP_ID = "com.bjdfarhma.salah"
A = pathlib.Path("android/app/src/main")

# 1) উইজেটের java + res কপি
shutil.copytree("widget/app/src/main", A, dirs_exist_ok=True)

# 2) MainActivity: প্লাগিন রেজিস্টার
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

# 3) AndroidManifest প্যাচ
mf = A / "AndroidManifest.xml"
x = mf.read_text(encoding="utf-8")
if "com.salahtime.widget" not in x:
    snip = pathlib.Path("widget/AndroidManifest-snippet.xml").read_text(encoding="utf-8")
    ifilter = re.search(r'<!-- \(১\).*?-->\s*(<intent-filter>.*?</intent-filter>)', snip, re.S).group(1)
    recv = snip[snip.index('<receiver'):]
    if "</activity>" not in x or "</application>" not in x:
        sys.exit("Manifest structure unexpected")
    x = x.replace("</activity>", ifilter + "\n        </activity>", 1)
    x = x.replace("</application>", recv + "\n    </application>", 1)
    perms = ""
    for p in ("RECEIVE_BOOT_COMPLETED", "POST_NOTIFICATIONS"):
        if f"android.permission.{p}" not in x:
            perms += f'    <uses-permission android:name="android.permission.{p}" />\n'
    x = x.replace("</manifest>", perms + "</manifest>")
    mf.write_text(x, encoding="utf-8")
print("android patched OK")
