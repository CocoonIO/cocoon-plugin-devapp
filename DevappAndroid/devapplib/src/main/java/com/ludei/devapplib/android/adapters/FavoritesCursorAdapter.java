package com.ludei.devapplib.android.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ludei.devapplib.android.R;
import com.ludei.devapplib.android.fragments.DocumentsFragment;

import java.text.SimpleDateFormat;

/**
 * Created by imanolmartin on 27/08/14.
 */
public class FavoritesCursorAdapter extends ResourcesCursorAdapter {

    public static final int FAVORITES_NAME_COLUMNS_POS = 2;
    public static final int FAVORITES_DATE_COLUMNS_POS = 3;

    public FavoritesCursorAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
    }

    public FavoritesCursorAdapter(Context context, Cursor c, int flags) {
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

        String name = cursor.getString(cursor.getColumnIndex(cursor.getColumnName(FAVORITES_NAME_COLUMNS_POS)));
        double date = cursor.getDouble(cursor.getColumnIndex(cursor.getColumnName(FAVORITES_DATE_COLUMNS_POS)));

        TextView nameView = (TextView) view.findViewById(R.id.item_file_name);
        nameView.setText(name);

        TextView sizeView = (TextView) view.findViewById(R.id.item_file_size);
        sizeView.setVisibility(View.GONE);

        ImageView disclosure = (ImageView) view.findViewById(R.id.item_disclosure_button);
        disclosure.setVisibility(View.GONE);

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");
        String lastModificationDate = format.format(date);
        TextView dateView = (TextView) view.findViewById(R.id.item_file_date);
        dateView.setText(lastModificationDate);

        final ImageView trash = (ImageView) view.findViewById(R.id.thrash);
        trash.setVisibility(View.GONE);

        final ImageView rename = (ImageView) view.findViewById(R.id.rename);
        rename.setEnabled(true);
        rename.setVisibility(View.VISIBLE);
        rename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DocumentsFragment.TAG);
                intent.setAction(DocumentsFragment.ACTION_RENAME);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }
        });
    }
}
