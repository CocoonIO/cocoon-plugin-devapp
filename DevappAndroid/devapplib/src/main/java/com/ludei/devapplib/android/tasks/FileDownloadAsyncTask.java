package com.ludei.devapplib.android.tasks;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.List;

public class FileDownloadAsyncTask extends FileAsyncTask {
	
	public FileDownloadAsyncTask(Context context, String outputPath, FileAsyncTaskListener listener) {
		super(context, outputPath, listener);
	}
	
	@Override
	protected FileTaskResult doInBackground(String... params) {
		FileTaskResult result = new FileTaskResult(false, null, "");
		try {
			final URL downloadFileUrl = new URL(params[0]);
			final HttpURLConnection connection = (HttpURLConnection) downloadFileUrl.openConnection();
		    connection.setRequestMethod("GET");
		    connection.setConnectTimeout(10000);
		    connection.setReadTimeout(60000);
		    connection.connect();
		    
		    long size = 0;
		    List<String> values = connection.getHeaderFields().get("content-Length");
    		if (values != null && !values.isEmpty()) {
    		    String sLength = (String) values.get(0);
    		    if (sLength != null) {
    		    	size = Long.parseLong(sLength);
    		    }
    		}

		    int index = downloadFileUrl.toString().lastIndexOf('/');
		    String filename = downloadFileUrl.toString().substring(index+1);
		    final File outputFile = new File(mOutputPath + File.separator + URLDecoder.decode(filename, "ASCII"));
		    outputFile.createNewFile();
		    
		    final FileOutputStream outputStream = new FileOutputStream(outputFile);
		    final byte buffer[] = new byte[16 * 1024];

		    final InputStream inputStream = connection.getInputStream();
		    
		    long progress = 0;
		    int lenght = 0;
		    while ((lenght = inputStream.read(buffer)) > 0) {
		    	if (isCancelled())
		    		break;
		    	
		        outputStream.write(buffer, 0, lenght);
		        progress += lenght;
		        
		        publishProgress(size, progress);
		    }
		    outputStream.flush();
		    outputStream.close();
		    
		    result = new FileTaskResult(true, outputFile.getAbsolutePath(), null);
		    
		} catch (Exception e) {
			result = new FileTaskResult(false, null, e.getLocalizedMessage());
		}
		
	    return result;
	}

}
