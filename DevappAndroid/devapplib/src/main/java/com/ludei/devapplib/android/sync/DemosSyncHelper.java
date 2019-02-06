package com.ludei.devapplib.android.sync;

import android.accounts.Account;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ludei.devapplib.android.CommonConsts;
import com.ludei.devapplib.android.Consts;
import com.ludei.devapplib.android.models.Demo;
import com.ludei.devapplib.android.providers.DemosContentProvider;
import com.ludei.devapplib.android.utils.FileUtils;
import com.ludei.devapplib.android.utils.GsonRequest;
import com.ludei.devapplib.android.utils.NetworkUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

/**
 * Helper class implementing the synchronization logic.
 */
public class DemosSyncHelper {

    public static final String TAG = DemosSyncHelper.class.getCanonicalName();
	
	public static final String LAST_DEMO_RECEIVED = "com.ludei.devapplib.android.sync.extras.LAST_SYNC";

	private Context mContext;
	private RequestQueue mQueue;
    private SSLContext mSslContext = null;

    class MyHostnameVerifier implements HostnameVerifier {

        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    class MyHurlStack extends HurlStack {

        public MyHurlStack(UrlRewriter urlRewriter, SSLSocketFactory sslSocketFactory) {
            super(urlRewriter, sslSocketFactory);
        }

        @Override
        protected HttpURLConnection createConnection(URL url) throws IOException {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            if ("https".equals(url.getProtocol()) && mSslContext.getSocketFactory() != null) {
                ((HttpsURLConnection)connection).setHostnameVerifier(new MyHostnameVerifier());
            }

            return connection;
        }
    }
	
	public DemosSyncHelper(Context context) {
		mContext = context;

        try {
            mSslContext = SSLContext.getInstance("TLS");
            TrustManager tm = new NetworkUtils.MyTrustManager();
            mSslContext.init(null, new TrustManager[]{tm}, null);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();

        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        mQueue = Volley.newRequestQueue(mContext, new MyHurlStack(null, mSslContext.getSocketFactory()));
	}
	
	public void performSync(SyncResult syncResult, Account account) {
        if (Consts.USE_LOCAL_JSON)
            performSyncLocal(syncResult, account);
        else
		    mQueue.add(createDemoListRequest(syncResult));
	}

    public void performSyncLocal(SyncResult syncResult, Account account) {
        try {
            Type demoType = new TypeToken<Collection<Demo>>(){}.getType();
            Collection<Demo> demos = new Gson().fromJson(FileUtils.readFileFromAssets(mContext, "demos.json"), demoType);

            for (Iterator<Demo> it = demos.iterator(); it.hasNext();) {
                Demo demo = it.next();
                if (!demo.web) {
                    demo.github_url = demo.github_url.replace(".git", "/archive/master.zip");
                    final ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
                    String select = "(" + DemosContentProvider.DEMO_NAME_COLUMN + " = ? )";
                    String[] selectArgs = {demo.name};
                    Cursor cursor = mContext.getContentResolver().query(
                            DemosContentProvider.DEMO_URI,
                            new String[]{
                                    DemosContentProvider.DEMO_NAME_COLUMN
                            },
                            select,
                            selectArgs,
                            null);
                    if (cursor.getCount() == 0) {
                        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(DemosContentProvider.DEMO_URI);
                        builder.withValues(demo.toContentValues());
                        batch.add(builder.build());

                    } else {
                        ContentProviderOperation.Builder builder = ContentProviderOperation.newUpdate(DemosContentProvider.DEMO_URI);
                        builder.withSelection(DemosContentProvider.DEMO_NAME_COLUMN + " = ?", new String[]{demo.name});
                        ContentValues values = demo.toContentValues();
                        values.remove(DemosContentProvider.DEMO_NAME_COLUMN);
                        builder.withValues(values);
                        batch.add(builder.build());
                    }
                    cursor.close();
                    mContext.getContentResolver().applyBatch(DemosContentProvider.AUTHORITY, batch);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();

        } catch (RemoteException e) {
            e.printStackTrace();

        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }
	
	private GsonRequest<Demo[]> createDemoListRequest(SyncResult syncResult) {
		return new GsonRequest<Demo[]>(
                CommonConsts.COCOON_TEMPLATES_API_URL,
				Demo[].class,
				null,
				new DemoListResponseListener(),
				new DemoListErrorListener(syncResult));
	}
	
	class DemoListResponseListener implements Response.Listener<Demo[]>{
		
		@Override
		public void onResponse(Demo[] demos) {
            new AsyncTask<Demo[], Void, Void>() {

                @Override
                protected Void doInBackground(Demo[]... params) {
                    Demo[] demos = params[0];
                    try {
                        int responseLength = demos.length;
                        if(responseLength > 0) {
                            final ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
                            int noOfModels = demos.length;
                            for (int i = 0; i < noOfModels; i++) {
                                String select = "(" + DemosContentProvider.DEMO_NAME_COLUMN + " = ? )";
                                String[] selectArgs = { demos[i].name };
                                Cursor cursor = mContext.getContentResolver().query(
                                        DemosContentProvider.DEMO_URI,
                                        new String[] {
                                                DemosContentProvider.DEMO_NAME_COLUMN
                                        },
                                        select,
                                        selectArgs,
                                        null);
                                if (cursor.getCount() == 0) {
                                    ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(DemosContentProvider.DEMO_URI);
                                    builder.withValues(demos[i].toContentValues());
                                    batch.add(builder.build());

                                } else {
                                    ContentProviderOperation.Builder builder = ContentProviderOperation.newUpdate(DemosContentProvider.DEMO_URI);
                                    builder.withSelection(DemosContentProvider.DEMO_NAME_COLUMN + " = ?", new String[]{demos[i].name});
                                    ContentValues values = demos[i].toContentValues();
                                    values.remove(DemosContentProvider.DEMO_NAME_COLUMN);
                                    builder.withValues(values);
                                    batch.add(builder.build());
                                }
                                cursor.close();
                            }
                            mContext.getContentResolver().applyBatch(DemosContentProvider.AUTHORITY, batch);
                        }

                    } catch (RemoteException e) {
                        e.printStackTrace();

                    } catch (OperationApplicationException e) {
                        e.printStackTrace();
                    }

                    return null;
                }

            }.execute(demos);
		}
	}
	
	class DemoListErrorListener implements Response.ErrorListener{
		
		private SyncResult syncResult;
		
		public DemoListErrorListener(SyncResult syncResult) {
			this.syncResult = syncResult;
		}

		@Override
		public void onErrorResponse(VolleyError error) {
            Log.e(TAG, error.networkResponse.toString());

            if(error.networkResponse != null){
				switch (error.networkResponse.statusCode) {
				case 403:
					++syncResult.stats.numAuthExceptions;
					break;
				case 400:
					break;
				default:
					break;
				}
			}else{
				
			}
		}
	}

}
