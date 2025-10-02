#!/usr/bin/env bash
set -euo pipefail

# Generates folder_* layered drawable variants for each baseline_* icon.
# Foreground baseline icon is inset inside the folder outline for better visual hierarchy.
# Usage: ./scripts/generate_folder_icons.sh [--force]
#   --force : overwrite existing folder_* drawables to apply current inset settings.
#
# Environment overrides (optional):
#   INSET_TOP (default 16dp)
#   INSET_BOTTOM (default 12dp)
#   INSET_LEFT (default 12dp)
#   INSET_RIGHT (default 12dp)
#   BACKGROUND (default outline_folder_24)
#   DRAWABLE_DIR (default iconpack/src/main/res/drawable)
#
# Notes:
# - baseline_folder_24 is skipped (acts as the plain folder icon itself)
# - Insets are applied via layer-list <item> attributes (android:top/left/right/bottom)

FORCE=0
if [[ ${1:-} == "--force" ]]; then
  FORCE=1
fi

DRAWABLE_DIR="${DRAWABLE_DIR:-iconpack/src/main/res/drawable}"
BACKGROUND="${BACKGROUND:-outline_folder_24}"
INSET_TOP="${INSET_TOP:-16dp}"
INSET_BOTTOM="${INSET_BOTTOM:-12dp}"
INSET_LEFT="${INSET_LEFT:-12dp}"
INSET_RIGHT="${INSET_RIGHT:-12dp}"

if [ ! -f "$DRAWABLE_DIR/$BACKGROUND.xml" ]; then
  echo "Missing $BACKGROUND.xml background vector in $DRAWABLE_DIR. Aborting." >&2
  exit 1
fi

created=0
skipped=0
updated=0

for f in "$DRAWABLE_DIR"/baseline_*.xml; do
  [ -e "$f" ] || continue
  base_file=$(basename "$f")
  base_name="${base_file%.xml}" # e.g. baseline_music_note_24
  if [ "$base_name" = "baseline_folder_24" ]; then
    skipped=$((skipped+1))
    continue
  fi
  suffix="${base_name#baseline_}" # music_note_24
  out_name="folder_${suffix}"
  out_file="$DRAWABLE_DIR/${out_name}.xml"

  if [ -f "$out_file" ] && [ $FORCE -eq 0 ]; then
    skipped=$((skipped+1))
    continue
  fi

  existed_before=0
  if [ -f "$out_file" ]; then
    existed_before=1
  fi

  cat > "$out_file" <<EOF
<?xml version="1.0" encoding="utf-8"?>
<layer-list xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:drawable="@drawable/${BACKGROUND}" />
    <item android:drawable="@drawable/${base_name}"
        android:top="${INSET_TOP}"
        android:bottom="${INSET_BOTTOM}"
        android:left="${INSET_LEFT}"
        android:right="${INSET_RIGHT}" />
</layer-list>
EOF

  if [ $existed_before -eq 1 ]; then
    updated=$((updated+1))
    echo "Updated $out_name.xml"
  else
    created=$((created+1))
    echo "Created $out_name.xml"
  fi

done

echo "Folder icon generation complete. Created: $created Updated: $updated Skipped(existing & not forced or excluded): $skipped"
