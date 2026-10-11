# Salah Time – GitHub দিয়ে APK বানানো

## Repo-তে যা থাকবে
```
.github/workflows/build-apk.yml   ← অটো APK বিল্ড
www/index.html                    ← এপের কোড (উইজেট bridge সহ)
widget/                           ← হোমস্ক্রিন উইজেটের Android কোড
scripts/                          ← আইকন + প্যাচ স্ক্রিপ্ট
resources/icon.png                ← তোমার লোগো
package.json, capacitor.config.json
google-services.json              ← তুমি নিজে যোগ করবে (নিচে দেখো)
```
`android/` ফোল্ডার commit করতে হবে না, GitHub নিজেই বানিয়ে নেয়।

## ধাপ
1. সব ফাইল নতুন GitHub repo-তে আপলোড করো (`.github` ফোল্ডারসহ)।
2. তোমার `google-services.json` repo-র একদম root-এ আপলোড করো (package.json এর পাশে)।
   - Repo **private** হলে এটাই যথেষ্ট।
   - Repo **public** হলে ফাইলটা আপলোড না করে: Settings → Secrets and variables → Actions → New secret →
     নাম `GOOGLE_SERVICES_JSON_B64`, মান = ফাইলের base64 (`base64 -w0 google-services.json`)।
3. Actions ট্যাব → "Build APK" → Run workflow (অথবা main-এ push করলেই চলে)।
4. ৫–১০ মিনিট পরে রান শেষ হলে নিচে Artifacts থেকে `salah-time-apk` ডাউনলোড করো, জিপের ভেতরে `app-debug.apk`।

## উইজেট বসানো
এপ ইনস্টল করে একবার খুলে নামাজের পেজে যাও। তারপর হোমস্ক্রিনে Long press → Widgets → "সালাহ টাইম"।

## জানা কথা
- এটা debug APK, নিজের ফোনে/বন্ধুদের দেওয়ার জন্য ঠিক আছে। Play Store-এর জন্য signed release বিল্ড লাগবে।
- Xiaomi/Oppo ফোনে এপকে "Autostart" ও "No restrictions" দিলে উইজেট ও নোটিফিকেশন ঠিক চলে।
