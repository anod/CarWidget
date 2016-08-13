package com.anod.car.home.backup;

import android.content.ContentValues;
import android.util.JsonWriter;
import android.util.SparseArray;

import com.anod.car.home.model.ShortcutInfo;
import com.anod.car.home.model.ShortcutModel;

import java.io.IOException;

/**
 * @author algavris
 * @date 08/04/2016.
 */
public class ShortcutsJsonWriter {

    public void writeList(JsonWriter shortcutsWriter, SparseArray<ShortcutInfo> shortcuts) throws IOException {
        for (int idx = 0; idx < shortcuts.size(); idx++) {
            int pos = shortcuts.keyAt(idx);
            ShortcutInfo info = shortcuts.get(pos);
            if (info == null) {
                continue;
            }
            shortcutsWriter.beginObject();
            ContentValues values = ShortcutModel.createShortcutContentValues(info);
            shortcutsWriter.name("pos").value(pos);

            for (String key: values.keySet()) {
                Object value = values.get(key);
                if (value != null) {
                    if (value instanceof String) {
                        shortcutsWriter.name(key).value((String)value);
                    } else if (value instanceof Integer) {
                        shortcutsWriter.name(key).value((Integer)value);
                    } else if (value instanceof Boolean) {
                        shortcutsWriter.name(key).value((Boolean)value);
                    } else if (value instanceof byte[]) {
                        byte[] data = (byte[])value;
                        shortcutsWriter.name(key).beginArray();
                        for(int i=0; i<data.length; i++) {
                            shortcutsWriter.value(data[i]);
                        }
                        shortcutsWriter.endArray();
                    } else {
                        throw new RuntimeException("Not implemented: " + value);
                    }
                }
            }
            shortcutsWriter.endObject();
        }
    }
}
