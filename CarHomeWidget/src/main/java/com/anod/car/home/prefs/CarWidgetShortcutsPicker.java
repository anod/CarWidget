package com.anod.car.home.prefs;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.content.res.ResourcesCompat;
import android.view.View;
import android.widget.AdapterView;

import com.anod.car.home.R;
import com.anod.car.home.prefs.ActivityPicker.PickAdapter.Item;
import com.anod.car.home.utils.AppPermissions;
import com.anod.car.home.utils.BackgroundTask;
import com.anod.car.home.utils.IntentUtils;

import java.util.ArrayList;
import java.util.List;

public class CarWidgetShortcutsPicker extends ActivityPicker {
    private final static int REQUEST_PICK_CONTACT = 100;

    private static final int ITEMS_NUM = 5;


    private static final int[] ICONS = {
            R.drawable.ic_launcher_carwidget,
            R.drawable.ic_call_white_24dp,
            R.drawable.ic_media_play_pause,
            R.drawable.ic_media_next,
            R.drawable.ic_media_prev
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.car_widget_shortcuts);
    }

    @Override
    protected List<Item> getItems() {
        List<PickAdapter.Item> items = new ArrayList<PickAdapter.Item>();
        Resources r = getResources();
        String[] titles = r.getStringArray(R.array.carwidget_shortcuts);
        for (int i = 0; i < ITEMS_NUM; i++) {
            Intent intent = IntentUtils.createPickShortcutLocalIntent(i, titles[i], ICONS[i], this);
            PickAdapter.Item item = new PickAdapter.Item(this, titles[i], ResourcesCompat.getDrawable(r, ICONS[i], null),
                    intent);
            items.add(item);
        }
        return items;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position == IntentUtils.IDX_DIRECT_CALL)
        {
            if (AppPermissions.isGranted(this, Manifest.permission.READ_CONTACTS)) {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                startActivityForResult(intent, REQUEST_PICK_CONTACT);
            } else {
                AppPermissions.request(this, new String[] { Manifest.permission.READ_CONTACTS }, AppPermissions.REQUEST_READ_CONTACTS);
                setResult(RESULT_CANCELED);
                finish();
            }
            return;
        }
        super.onItemClick(parent, view, position, id);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PICK_CONTACT)
        {
            if (resultCode == RESULT_OK) {
                BackgroundTask.execute(new BackgroundTask.Worker<Uri, Intent>(data.getData(), this) {

                    @Override
                    public Intent run(Uri uri, Context context) {
                        return IntentUtils.createDirectCallIntent(uri, context);
                    }

                    @Override
                    public void finished(Intent intent, Context context) {
                        if (intent != null) {
                            setResult(RESULT_OK, intent);
                        } else {
                            setResult(RESULT_CANCELED);
                        }
                        finish();
                    }
                });
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
