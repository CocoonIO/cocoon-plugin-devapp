package com.ludei.devapplib.android.tasks;

public class FileTaskResult {
	
	private boolean mSuccess;
	private String mOutpuPath;
	private String mErrorMsg;
	
	public FileTaskResult(boolean success, String outputPath, String errorMsg) {
		mSuccess = success;
		mOutpuPath = outputPath;
		mErrorMsg = errorMsg;
	}
	
	public boolean isSuccess() {
		return mSuccess;
	}

	public String getOutpuPath() {
		return mOutpuPath;
	}

	public String getErrorMsg() {
		return mErrorMsg;
	}
	
}
