package com.ludei.devapplib.android.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ludei.devapplib.android.R;

import java.text.SimpleDateFormat;

/**
 * Created by imanolmartin on 27/08/14.
 */
public class HistoryCursorAdapter extends ResourcesCursorAdapter {

    public static final int HISTORY_DATE_COLUMN_POS = 2;

    public HistoryCursorAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
    }

    public HistoryCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View retView = inflater.inflate(R.layout.list_item_resource, parent, false);

        return retView;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        super.bindView(view, context, cursor);

        double lastAccessTime = cursor.getDouble(cursor.getColumnIndex(cursor.getColumnName(HISTORY_DATE_COLUMN_POS)));

        TextView sizeView = (TextView) view.findViewById(R.id.item_file_size);
        sizeView.setVisibility(View.GONE);

        ImageView disclosure = (ImageView) view.findViewById(R.id.item_disclosure_button);
        disclosure.setVisibility(View.GONE);

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String lastModificationDate = format.format(lastAccessTime);
        TextView dateView = (TextView) view.findViewById(R.id.item_file_date);
        dateView.setText(lastModificationDate);
    }
}
