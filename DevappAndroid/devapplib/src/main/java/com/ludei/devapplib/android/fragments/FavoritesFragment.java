package com.ludei.devapplib.android.fragments;

import android.app.AlertDialog;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FilterQueryProvider;

import com.ludei.devapplib.android.R;
import com.ludei.devapplib.android.adapters.FavoritesCursorAdapter;
import com.ludei.devapplib.android.adapters.ResourcesCursorAdapter;
import com.ludei.devapplib.android.providers.FavoritesContentProvider;
import com.ludei.devapplib.android.utils.UriHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class FavoritesFragment extends ResourcesFragment {

	public static final String TAG = FavoritesFragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        if (container == null) {
            return null;
        }

        return inflater.inflate(R.layout.fragment_resources, null);
    }

    public String fromSortOrderToSQLString(SortOrder sortOrder) {
        String sort = FavoritesContentProvider.FAVORITES_NAME_COLUMN + " asc";
        switch (sortOrder) {
            case NAME_ASC:
                sort = FavoritesContentProvider.FAVORITES_NAME_COLUMN + " asc";
                break;

            case NAME_DSC:
                sort = FavoritesContentProvider.FAVORITES_NAME_COLUMN + " desc";
                break;

            case DATE_ASC:
                sort = FavoritesContentProvider.FAVORITES_DATE_COLUMN + " asc";
                break;

            case DATE_DSC:
                sort = FavoritesContentProvider.FAVORITES_DATE_COLUMN + " desc";
                break;
        }

        return sort;
    }

    @Override
    public Uri getItemAtPosition(int index) {
        Uri uri = null;
        ResourcesCursorAdapter adapter = getAdapter();
        if(adapter != null) {
            Cursor cursor = (Cursor) adapter.getItem(getSelectedItemPosition());
            if (cursor != null && index >= 0 && index < cursor.getCount())
                uri = Uri.parse(cursor.getString(cursor.getColumnIndex(cursor.getColumnName(ResourcesCursorAdapter.RESOURCE_URI_COLUMN_POS))));
        }

        return uri;
    }

    @Override
    public ArrayList<Uri> getSelectedItems(HashMap<Integer, Boolean> selection) {
        ArrayList<Uri> selectedItems = new ArrayList<Uri>();
        Set<Integer> selectedIndexes = selection.keySet();
        for (Iterator<Integer> it = selectedIndexes.iterator(); it.hasNext();) {
            int index = it.next();
            if (selection.get(index)) {
                ResourcesCursorAdapter adapter = getAdapter();
                if(adapter != null) {
                    Cursor cursor = (Cursor) adapter.getItem(index);
                    Uri uri = Uri.parse(cursor.getString(cursor.getColumnIndex(cursor.getColumnName(ResourcesCursorAdapter.RESOURCE_URI_COLUMN_POS))));
                    selectedItems.add(uri);
                }
            }
        }

        return selectedItems;
    }

    @Override
    public void deleteAllItems() {
        getActivity().getContentResolver().delete(FavoritesContentProvider.FAVORITES_URI, null, null);
    }

    @Override
    public void renameSelectedItems() {
        int len = getList().getCount();
        HashMap<Integer, Boolean> selection = new HashMap<Integer, Boolean>();
        SparseBooleanArray checked = getList().getCheckedItemPositions();
        for (int i = 0; i < len; i++) {
            if (checked.get(i)) {
                selection.put(i, checked.get(i));
            }
        }
        final ArrayList<Uri> selectedItems = getSelectedItems(selection);
        for (Iterator<Uri> it = selectedItems.iterator(); it.hasNext();) {
            final Uri uri = it.next();
            String name = uri.toString();

            final EditText contentView = new EditText(getActivity());
            contentView.setHint(name);

            final AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
            dialog.setView(contentView);
            dialog.setTitle(R.string.dialog_favorite_rename_title);
            dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Do nothing
                }
            });
            dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dialog.show();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (contentView.getText().toString().isEmpty()) {
                        renameItem(uri, uri.toString());
                        reloadList();
                        dialog.dismiss();

                    } else {
                        renameItem(uri, contentView.getText().toString());
                        reloadList();
                        dialog.dismiss();
                    }
                }
            });
        }
    }

    @Override
    public ResourcesCursorAdapter createAdapter() {
        ResourcesCursorAdapter adapter = new FavoritesCursorAdapter(getActivity(), null, true);
        adapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence constraint) {
                String[] projection = new String[] {
                        FavoritesContentProvider.FAVORITES_ID_COLUMN,
                        FavoritesContentProvider.FAVORITES_URI_COLUMN,
                        FavoritesContentProvider.FAVORITES_NAME_COLUMN,
                        FavoritesContentProvider.FAVORITES_DATE_COLUMN
                };
                String select = "( " +
                        FavoritesContentProvider.FAVORITES_URI_COLUMN + " LIKE ? )";
                String[] selectArgs = {
                        "%" + constraint.toString() + "%"
                };
                return getActivity().getContentResolver().query(
                        FavoritesContentProvider.FAVORITES_URI,                     // URI
                        projection,                				                    // Projection
                        select,                                                     // Selection
                        selectArgs,                                                 // Selection args
                        fromSortOrderToSQLString(getCurrentOrder())                 // Order By
                );
            }
        });

        return adapter;
    }

    @Override
    public void saveListStatus() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(getString(R.string.pref_last_favorite_position), getSelectedItemPosition());
        editor.putInt(getString(R.string.pref_last_favorite_order), getCurrentOrder().ordinal());
        editor.commit();
    }

    @Override
    public void restoreListStatus() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        SortOrder order = SortOrder.values()[prefs.getInt(getString(R.string.pref_last_favorite_order), SortOrder.NAME_ASC.ordinal())];
        int selectedItemPosition = prefs.getInt(getString(R.string.pref_last_favorite_position), -1);

        setCurrentOrder(order);
        setSelectedItemPosition(selectedItemPosition);
    }

    @Override
    public void reloadList() {
        Bundle extras = new Bundle();
        extras.putInt(BUNDLE_ORDER_KEY, getCurrentOrder().ordinal());
        LoaderManager lm = getLoaderManager();
        if (lm.getLoader(LOADER) == null) {
            lm.initLoader(LOADER, extras, this);

        } else {
            lm.restartLoader(LOADER, extras, this);
            lm.restartLoader(LOADER, extras, this);
        }
    }

    @Override
    protected void saveLastUrl() {}

    @Override
    public int getEmptyResource() {
        return R.layout.favorites_empty;
    }

    @Override
    public int getMenuResource() {
        return R.menu.favorites;
    }

    @Override
    public CursorLoader getLoaderCursor(Bundle bundle) {
        String sort = FavoritesContentProvider.FAVORITES_NAME_COLUMN + " asc";
        if (bundle != null) {
            sort = fromSortOrderToSQLString(bundle.getInt(BUNDLE_ORDER_KEY) == 0 ? SortOrder.NAME_ASC : SortOrder.values()[bundle.getInt(BUNDLE_ORDER_KEY)]);
        }

        String[] projection = {
                FavoritesContentProvider.FAVORITES_ID_COLUMN,
                FavoritesContentProvider.FAVORITES_URI_COLUMN,
                FavoritesContentProvider.FAVORITES_NAME_COLUMN,
                FavoritesContentProvider.FAVORITES_DATE_COLUMN
        };
        return new CursorLoader(getActivity(),                                  // Context
                FavoritesContentProvider.FAVORITES_URI,                         // URI
                projection,                		                                // Projection
                null,                                                           // Selection
                null,                                                           // Selection args
                sort); 	                                                        // Sort
    }

    @Override
    public void deleteSelectedItems() {
        int len = getList().getCount();
        HashMap<Integer, Boolean> selection = new HashMap<Integer, Boolean>();
        SparseBooleanArray checked = getList().getCheckedItemPositions();
        for (int i = 0; i < len; i++) {
            if (checked.get(i)) {
                selection.put(i, checked.get(i));
            }
        }

        final ArrayList<Uri> selectedItems = getSelectedItems(selection);
        StringBuilder message = new StringBuilder("\n");
        for (Iterator<Uri> it = selectedItems.iterator(); it.hasNext();) {
            Uri uri = it.next();
            String name = UriHelper.getName(uri);
            message.append("\n\t- " + name);
        }

        AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
        dialog.setTitle(R.string.dialog_favorite_delete_title);
        dialog.setMessage(String.format(getString(R.string.dialog_favorite_delete_message), message));
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteItems(selectedItems);
                reloadList();
                dialog.dismiss();
            }
        });
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void deleteItems(ArrayList<Uri> selectedItems) {
        try {
            final ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
            int noOfModels = selectedItems.size();
            for (int i = 0; i < noOfModels; i++) {
                String[] projection = {
                        FavoritesContentProvider.FAVORITES_URI_COLUMN
                };
                String select = "(" +
                        FavoritesContentProvider.FAVORITES_URI_COLUMN + " = ?)";
                String[] selectArgs = {
                        selectedItems.get(i).toString()
                };
                Cursor cursor = getActivity().getContentResolver().query(
                        FavoritesContentProvider.FAVORITES_URI,
                        projection,
                        select,
                        selectArgs,
                        null);

                if (cursor.getCount() > 0) {
                    ContentProviderOperation.Builder builder = ContentProviderOperation.newDelete(FavoritesContentProvider.FAVORITES_URI);
                    select = "(" +
                            FavoritesContentProvider.FAVORITES_URI_COLUMN + " = ? )";
                    selectArgs = new String[]{
                            selectedItems.get(i).toString()
                    };
                    builder.withSelection(
                            select,
                            selectArgs);
                    batch.add(builder.build());
                }

                cursor.close();
            }
            getActivity().getContentResolver().applyBatch(FavoritesContentProvider.AUTHORITY, batch);

        } catch (RemoteException e) {
            e.printStackTrace();

        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }

    public void renameItem(Uri uri, String name) {
        String select = "(" + FavoritesContentProvider.FAVORITES_URI_COLUMN + " LIKE ? )";
        String[] selectArgs = { "%" + uri.toString() + "%" };

        ContentValues cv = new ContentValues();
        cv.put(FavoritesContentProvider.FAVORITES_URI_COLUMN, uri.toString());
        cv.put(FavoritesContentProvider.FAVORITES_NAME_COLUMN, name);
        getActivity().getContentResolver().update(FavoritesContentProvider.FAVORITES_URI, cv, select, selectArgs);
    }

}
