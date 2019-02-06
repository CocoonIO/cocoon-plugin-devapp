package com.ludei.devapplib.android.tasks;

import android.content.Context;
import android.os.AsyncTask;

public abstract class FileAsyncTask extends AsyncTask<String, Long, FileTaskResult> {

	public interface FileAsyncTaskListener {
		
		public void onFileTaskStarted(FileAsyncTask task);
		public void onFileTaskProgress(FileAsyncTask task, long size, long progress);
		public void onFileTaskFinished(FileAsyncTask task, String ouputPath);
		public void onFileTaskError(FileAsyncTask task, String errorMsg);
        public void onFileTaskCancelled(FileAsyncTask task);
		
	}
	
	protected Context mContext;
	protected FileAsyncTaskListener mListener;
	protected String mOutputPath;
    protected boolean mFinished;
	
	public FileAsyncTask(Context context, String outputPath, FileAsyncTaskListener listener) {
		mContext = context;
		mListener = listener;
		mOutputPath = outputPath;
        mFinished = false;
	}
	
	@Override
	protected void onPreExecute() {
		if (mListener != null)
	    	mListener.onFileTaskStarted(this);
    }
	
	@Override
	protected void onProgressUpdate(Long... values) {
		if (mListener != null)
	    	mListener.onFileTaskProgress(this, values[0], values[1]);
    }
	
	@Override
	protected void onPostExecute(FileTaskResult result) {
        mFinished = true;
		if (mListener != null) {
			if (result.isSuccess())
				mListener.onFileTaskFinished(this, result.getOutpuPath());
			else
				mListener.onFileTaskError(this, result.getErrorMsg());
		}
    }
	
	@Override
	protected void onCancelled(FileTaskResult result) {
        mFinished = true;
        if (mListener != null) {
        	mListener.onFileTaskCancelled(this);
        }
    }

    public boolean isFinished() {
        return  mFinished;
    }
	
}
