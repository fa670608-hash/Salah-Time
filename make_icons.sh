#!/usr/bin/env bash
# resources/icon.png (1024x1024) থেকে Android আইকন বানায়
set -e
if command -v magick >/dev/null 2>&1; then IM=magick; elif command -v convert >/dev/null 2>&1; then IM=convert; else sudo apt-get update -qq && sudo apt-get install -y -qq imagemagick && IM=convert; fi
RES=android/app/src/main/res
BG="#04180F"
declare -A L=( [mdpi]=48 [hdpi]=72 [xhdpi]=96 [xxhdpi]=144 [xxxhdpi]=192 )
declare -A F=( [mdpi]=108 [hdpi]=162 [xhdpi]=216 [xxhdpi]=324 [xxxhdpi]=432 )
for d in mdpi hdpi xhdpi xxhdpi xxxhdpi; do
  dir="$RES/mipmap-$d"; mkdir -p "$dir"
  s=${L[$d]}; f=${F[$d]}; inner=$(( f * 85 / 100 ))
  $IM resources/icon.png -resize ${s}x${s} "$dir/ic_launcher.png"
  $IM resources/icon.png -resize ${s}x${s} \( -size ${s}x${s} xc:black -fill white -draw "circle $((s/2)),$((s/2)) $((s/2)),0" \) \
          -alpha off -compose CopyOpacity -composite "$dir/ic_launcher_round.png"
  $IM -size ${f}x${f} xc:none \( resources/icon.png -resize ${inner}x${inner} \) -gravity center -composite "$dir/ic_launcher_foreground.png"
done
mkdir -p "$RES/values"
cat > "$RES/values/ic_launcher_background.xml" <<XML
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="ic_launcher_background">$BG</color>
</resources>
XML
echo "icons done"
