package com.ludei.devapplib.android.tasks;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileUnzipAsyncTask extends FileAsyncTask {
	
	public FileUnzipAsyncTask(Context context, String outputPath, FileAsyncTaskListener listener) {
		super(context, outputPath, listener);
	}

	@Override
	protected FileTaskResult doInBackground(String... params) {
		FileTaskResult result = new FileTaskResult(false, null, "");
		try {
			_dirChecker(mOutputPath, "");
					
			File file = new File(params[0]);
			long size = file.length();
			long progress = 0;
			
			FileInputStream fin = new FileInputStream(params[0]);
			ZipInputStream zin = new ZipInputStream(fin);
			ZipEntry ze = null;
			final byte buffer[] = new byte[16 * 1024];
			while ((ze = zin.getNextEntry()) != null) {
				if (isCancelled()) {
					zin.closeEntry();
		    		break;
				}
				
				Log.v("Decompress", "Unzipping " + ze.getName() + " " + mOutputPath);

				if (ze.isDirectory()) {
					_dirChecker(mOutputPath, ze.getName());
					
				} else {
                    File parentPath = new File(ze.getName()).getParentFile();
                    if (parentPath != null)
                        _dirChecker(mOutputPath, parentPath.getAbsolutePath());

				    int lenght = 0;
					FileOutputStream fout = new FileOutputStream(mOutputPath + File.separator + ze.getName());					
				    while ((lenght = zin.read(buffer)) > 0) {
				    	fout.write(buffer, 0, lenght);
				    	progress += lenght;
				    	
				    	publishProgress(size, progress);
				    }

					zin.closeEntry();
					fout.flush();
					fout.close();
				}
			}
			zin.close();
			
			result = new FileTaskResult(true, mOutputPath, null);
			
		} catch (Exception e) {
			result = new FileTaskResult(false, null, e.getLocalizedMessage());
		}
		
		return result;
	}
	
	private static void _dirChecker(String location, String dir) {
		File f = new File(location + File.separator + dir);

		if (f != null && !f.isDirectory()) {
			f.mkdirs();
		}
	}

}
