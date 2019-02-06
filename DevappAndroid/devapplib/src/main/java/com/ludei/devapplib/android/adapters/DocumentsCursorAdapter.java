package com.ludei.devapplib.android.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ludei.devapplib.android.R;
import com.ludei.devapplib.android.fragments.DocumentsFragment;
import com.ludei.devapplib.android.utils.UriHelper;

/**
 * Created by imanolmartin on 27/08/14.
 */
public class DocumentsCursorAdapter extends ResourcesCursorAdapter {

    public DocumentsCursorAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
    }

    public DocumentsCursorAdapter(Context context, Cursor c, int flags) {
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

        Uri uri = Uri.parse(cursor.getString(cursor.getColumnIndex(cursor.getColumnName(RESOURCE_URI_COLUMN_POS))));

        TextView nameView = (TextView) view.findViewById(R.id.item_file_name);
        nameView.setText(UriHelper.getName(uri));

        TextView sizeView = (TextView) view.findViewById(R.id.item_file_size);
        sizeView.setText(UriHelper.getSize(uri));

        TextView dateView = (TextView) view.findViewById(R.id.item_file_date);
        dateView.setText(UriHelper.getLastModificationDate(uri));

        ImageView disclosure = (ImageView) view.findViewById(R.id.item_disclosure_button);
        disclosure.setVisibility(View.INVISIBLE);
        disclosure.setTag(uri);

        if (UriHelper.isDirectory(uri)) {
            disclosure.setVisibility(View.VISIBLE);
            disclosure.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri item = (Uri) v.getTag();

                    Intent intent = new Intent(DocumentsFragment.TAG);
                    intent.setAction(DocumentsFragment.ACTION_LIST_PATH);
                    intent.putExtra(DocumentsFragment.BUNDLE_PATH_KEY, item.getPath());
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                }
            });
        }
    }
}
