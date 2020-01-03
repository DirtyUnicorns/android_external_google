package com.google.android.settings.connecteddevice.dock;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.database.Cursor;
import java.util.ArrayList;
import java.util.List;

public class DockAsyncQueryHandler extends AsyncQueryHandler {
    private OnQueryListener mListener = null;

    public interface OnQueryListener {
        void onQueryComplete(int i, List<DockDevice> list);
    }

    public DockAsyncQueryHandler(ContentResolver cr) {
        super(cr);
    }

    public static List<DockDevice> parseCursorToDockDevice(Cursor cursor) {
        ArrayList<DockDevice> devices = new ArrayList();
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                devices.add(new DockDevice(cursor.getString(cursor.getColumnIndex("dockId")),
                        cursor.getString(cursor.getColumnIndex("dockName"))));
            }
        }
        return devices;
    }

    public void onQueryComplete(int token, Object cookie, Cursor cursor) {
        super.onQueryComplete(token, cookie, cursor);
        if (mListener != null) {
            mListener.onQueryComplete(token, parseCursorToDockDevice(cursor));
        }
    }

    public void setOnQueryListener(OnQueryListener listener) {
        mListener = listener;
    }
}
