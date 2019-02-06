package com.ludei.devapplib.android.fragments;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.LocalBroadcastManager;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
import android.widget.TextView;

import com.ludei.devapplib.android.CommonConsts;
import com.ludei.devapplib.android.R;
import com.ludei.devapplib.android.adapters.DocumentsCursorAdapter;
import com.ludei.devapplib.android.adapters.ResourcesCursorAdapter;
import com.ludei.devapplib.android.providers.FilesContentProvider;
import com.ludei.devapplib.android.utils.UriHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class DocumentsFragment extends ResourcesFragment {

	public static final String TAG = DocumentsFragment.class.getSimpleName();

    public static final String ACTION_LIST_PATH = DocumentsFragment.class.getSimpleName() + ".ACTION_LIST_PATH";

    public static final String BUNDLE_PATH_KEY = "path";

    private static final String[] PROJECTION = new String[] {
            FilesContentProvider.FILE_ID_COLUMN,
            FilesContentProvider.FILE_URI_COLUMN};

    private LocalBroadcastManager localBroadcastManager = null;
    private File mCurrentPath = Environment.getExternalStorageDirectory();
    private TextView mCurrentPathText;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equalsIgnoreCase(ACTION_LIST_PATH)) {
                hideLaunchButtons();

                Bundle extras = intent.getExtras();
                LoaderManager lm = getLoaderManager();
                lm.restartLoader(LOADER, extras, DocumentsFragment.this);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        IntentFilter filter = new IntentFilter(TAG);
        filter.addAction(ACTION_LIST_PATH);
        localBroadcastManager = LocalBroadcastManager.getInstance(getActivity());
        localBroadcastManager.registerReceiver(mReceiver, filter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        if (container == null) {
            return null;
        }

        return inflater.inflate(R.layout.fragment_resources, null);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser)
            mCurrentPath = Environment.getExternalStorageDirectory();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewGroup navLayout = (ViewGroup) getView().findViewById(R.id.nav_buttons_layout);
        navLayout.setVisibility(View.VISIBLE);

        mCurrentPathText = (TextView) getView().findViewById(R.id.current_dir);

        ImageView upButton = (ImageView) getView().findViewById(R.id.up_button);
        upButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentPath.getParentFile() != null) {
                    mCurrentPath = mCurrentPath.getParentFile();

                    Intent intent = new Intent(DocumentsFragment.TAG);
                    intent.setAction(DocumentsFragment.ACTION_LIST_PATH);
                    intent.putExtra(DocumentsFragment.BUNDLE_PATH_KEY, mCurrentPath.getPath());
                    LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
                }
            }
        });

        if (Build.VERSION.SDK_INT >= 23) {
            this.askExternalStoragePermissionMarshmallow();

        } else {
            // We assume that the access has been granted when installing
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void askExternalStoragePermissionMarshmallow() {
        int hasWriteContactsPermission = getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
            getActivity().requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, CommonConsts.REQUEST_CODE_ASK_PERMISSIONS_EXTERNAL_STORAGE);
            return;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mReceiver != null)
            localBroadcastManager.unregisterReceiver(mReceiver);
    }

    public String fromSortOrderToSQLString(SortOrder sortOrder) {
        String sort = FilesContentProvider.FILE_NAME_COLUMN + " asc";
        switch (sortOrder) {
            case NAME_ASC:
                sort = FilesContentProvider.FILE_NAME_COLUMN + " asc";
                break;

            case NAME_DSC:
                sort = FilesContentProvider.FILE_NAME_COLUMN + " desc";
                break;

            case SIZE_ASC:
                sort = FilesContentProvider.FILE_SIZE_COLUMN + " asc";
                break;

            case SIZE_DSC:
                sort = FilesContentProvider.FILE_SIZE_COLUMN + " desc";
                break;

            case DATE_ASC:
                sort = FilesContentProvider.FILE_LAST_MODIFICATION_DATE_COLUMN + " asc";
                break;

            case DATE_DSC:
                sort = FilesContentProvider.FILE_LAST_MODIFICATION_DATE_COLUMN + " desc";
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
        // Nothing to do
    }

    @Override
    public void renameSelectedItems() {
        // Nothing to do
    }

    @Override
    public ResourcesCursorAdapter createAdapter() {
        ResourcesCursorAdapter adapter = new DocumentsCursorAdapter(getActivity(), null, true);
        adapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence constraint) {
                String select = "(" +
                        FilesContentProvider.FILE_PATH_COLUMN + " = ? AND " +
                        FilesContentProvider.FILE_URI_COLUMN + " LIKE ? )";
                String[] selectArgs = {
                        Uri.parse(mCurrentPath.getAbsolutePath()).toString(),
                        constraint.toString()
                };
                return getActivity().getContentResolver().query(
                        FilesContentProvider.FILE_URI, 			        // URI
                        PROJECTION,                				        // Projection
                        select,                                         // Selection
                        selectArgs,                                     // Selection args
                        fromSortOrderToSQLString(getCurrentOrder())     // Order By
                );
            }
        });

        return adapter;
    }

    @Override
    public void saveListStatus() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(getString(R.string.pref_last_document_position), getSelectedItemPosition());
        editor.putString(getString(R.string.pref_last_document_path), mCurrentPath.getAbsolutePath());
        editor.putInt(getString(R.string.pref_last_document_order), getCurrentOrder().ordinal());
        editor.commit();
    }

    @Override
    public void restoreListStatus() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        SortOrder order = SortOrder.values()[prefs.getInt(getString(R.string.pref_last_document_order), SortOrder.NAME_ASC.ordinal())];
        mCurrentPath = new File(prefs.getString(getString(R.string.pref_last_document_path), Environment.getExternalStorageDirectory().getAbsolutePath()));
        int selectedItemPosition = prefs.getInt(getString(R.string.pref_last_document_position), -1);

        setCurrentOrder(order);
        setSelectedItemPosition(selectedItemPosition);
    }

    @Override
    public void reloadList() {
        Bundle extras = new Bundle();
        extras.putString(BUNDLE_PATH_KEY, mCurrentPath.getAbsolutePath());
        extras.putInt(BUNDLE_ORDER_KEY, getCurrentOrder().ordinal());
        LoaderManager lm = getLoaderManager();
        if (lm.getLoader(LOADER) == null) {
            lm.initLoader(LOADER, extras, this);

        } else {
            lm.restartLoader(LOADER, extras, this);
        }
    }

    @Override
    protected void saveLastUrl() {}

    @Override
    public int getEmptyResource() {
        return R.layout.documents_empty;
    }

    @Override
    public int getMenuResource() {
        return R.menu.documents;
    }

    @Override
    public CursorLoader getLoaderCursor(Bundle bundle) {
        String path = Environment.getExternalStorageDirectory().getPath();
        String sort = FilesContentProvider.FILE_NAME_COLUMN + " asc";
        if (bundle != null) {
            path = bundle.getString(BUNDLE_PATH_KEY) == null ? Environment.getExternalStorageDirectory().getPath() : bundle.getString(BUNDLE_PATH_KEY);
            sort = fromSortOrderToSQLString(bundle.getInt(BUNDLE_ORDER_KEY) == 0 ? SortOrder.NAME_ASC : SortOrder.values()[bundle.getInt(BUNDLE_ORDER_KEY)]);
        }

        mCurrentPath = new File(path);
        mCurrentPathText.setText(mCurrentPath.getAbsolutePath());

        String select = "( " + FilesContentProvider.FILE_PATH_COLUMN + " = ? )";
        String[] selectArgs = { Uri.parse(mCurrentPath.getAbsolutePath()).toString() };
        return new CursorLoader(getActivity(),      // Context
                FilesContentProvider.FILE_URI, 	    // URI
                PROJECTION,                		    // Projection
                select,                             // Selection
                selectArgs,                         // Selection args
                sort); 	                            // Sort
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
        dialog.setTitle(R.string.dialog_docs_delete_title);
        dialog.setMessage(String.format(getString(R.string.dialog_docs_delete_message), message));
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
                String select = "(" +
                        FilesContentProvider.FILE_PATH_COLUMN + " = ? AND " +
                        FilesContentProvider.FILE_URI_COLUMN + " = ? )";
                String[] selectArgs = {
                        Uri.parse(mCurrentPath.getAbsolutePath()).toString(),
                        selectedItems.get(i).toString()
                };
                Cursor cursor = getActivity().getContentResolver().query(
                        FilesContentProvider.FILE_URI,
                        new String[]{ FilesContentProvider.FILE_URI_COLUMN },
                        select,
                        selectArgs,
                        null);

                if (cursor.getCount() > 0) {
                    ContentProviderOperation.Builder builder = ContentProviderOperation.newDelete(FilesContentProvider.FILE_URI);
                    select = "(" + FilesContentProvider.FILE_PATH_COLUMN + " = ? )";
                    selectArgs = new String[]{ selectedItems.get(i).toString() };
                    builder.withSelection(
                            select,
                            selectArgs);
                    batch.add(builder.build());
                }

                cursor.close();
            }
            getActivity().getContentResolver().applyBatch(FilesContentProvider.AUTHORITY, batch);

        } catch (RemoteException e) {
            e.printStackTrace();

        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }

}
