package com.anod.car.home.backup;

import android.content.ContentValues;
import android.util.JsonWriter;
import android.util.SparseArray;

import com.anod.car.home.model.Shortcut;
import com.anod.car.home.model.ShortcutIcon;
import com.anod.car.home.model.ShortcutModel;
import com.anod.car.home.model.ShortcutsContainerModel;

import java.io.IOException;

/**
 * @author algavris
 * @date 08/04/2016.
 */
public class ShortcutsJsonWriter {

    public void writeList(JsonWriter shortcutsWriter, SparseArray<Shortcut> shortcuts, ShortcutsContainerModel model) throws IOException {
        for (int idx = 0; idx < shortcuts.size(); idx++) {
            int pos = shortcuts.keyAt(idx);
            Shortcut info = shortcuts.get(pos);
            if (info == null) {
                continue;
            }
            shortcutsWriter.beginObject();
            ShortcutIcon icon = model.loadIcon(info.id);
            ContentValues values = ShortcutModel.createShortcutContentValues(info, icon);
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
