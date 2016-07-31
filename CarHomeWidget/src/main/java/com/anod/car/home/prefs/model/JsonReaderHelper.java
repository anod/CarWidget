package com.anod.car.home.prefs.model;

import android.support.v4.util.SimpleArrayMap;
import android.util.JsonReader;
import android.util.JsonToken;

import info.anodsplace.android.log.AppLog;

import java.io.IOException;

/**
 * @author algavris
 * @date 09/04/2016.
 */
public class JsonReaderHelper {

    public static void readValues(JsonReader reader, SimpleArrayMap<String, JsonToken> types, ChangeableSharedPreferences prefs) throws IOException {

        while (reader.hasNext()) {
            String name = reader.nextName();
            if (!types.containsKey(name)) {
                AppLog.e("No type for name: "+name);
                reader.skipValue();
                continue;
            }
            JsonToken type = types.get(name);
            final boolean isNull = reader.peek() == JsonToken.NULL;
            boolean skipped = false;
            if (type == JsonToken.BOOLEAN) {
                prefs.putChange(name, reader.nextBoolean());
            } else if (type == JsonToken.STRING) {
                prefs.putChange(name, isNull ? null : reader.nextString());
            } else if (type == JsonToken.NUMBER) {
                prefs.putChange(name, isNull ? null : reader.nextInt());
            } else {
                AppLog.e("Unknown type: "+type+" for name: "+name);
                skipped = true;
                reader.skipValue();
            }
            if (isNull && !skipped) {
                reader.nextNull();
            }
        }

    }
}
