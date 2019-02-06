package com.ludei.devapplib.android.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by imanolmartin on 20/08/14.
 */
public class FileUtils {

    public static ArrayList<File> listFiles(String rootFolder, String[] extensions, final boolean includeDirs) {
        final ArrayList<String> exts = new ArrayList<String>(Arrays.asList(extensions));
        final ArrayList<File> result = new ArrayList<File>();
        File dir = new File(rootFolder);
        dir.listFiles(new FileFilter() {

            @Override
            public boolean accept(File file) {
                if (file.isDirectory()) {
                    if (includeDirs) {
                        result.add(file);
                        return true;
                    }

                } else if (file.isFile()) {
                    int index = file.getName().lastIndexOf(".");
                    if (index == -1)
                        return false;

                    String extension = file.getName().substring(index + 1);
                    if (exts.contains(extension)) {
                        result.add(file);
                        return true;
                    }
                }

                return false;
            }
        });

        return result;
    }

    public static String getIndex(String rootFolder) {
        ArrayList<File> files = listFiles(rootFolder, new String[]{"html", "htm"}, true);
        for (File file : files) {
            if (file.getName().equalsIgnoreCase("index.html"))
                return "index.html";
            else if (file.getName().equalsIgnoreCase("index.htm"))
                return "index.htm";
        }

        return "";
    }

    public static String getFirstIndex(String rootFolder) {
        File dir = new File(rootFolder);
        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                if (s.equalsIgnoreCase("index.htm") ||
                        s.equalsIgnoreCase("index.html"))
                    return true;

                return false;
            }
        });

        if (files.length > 0) {
            return files[0].getAbsolutePath();

        } else {
            File[] dirs = dir.listFiles();
            for (int i=0; i<dirs.length; i++) {
                if (dirs[i].isDirectory()) {
                    String index = getFirstIndex(dirs[i].getAbsolutePath());
                    if (index != null)
                        return index;
                }
            }
        }

        return null;
    }

    public static boolean deleteFile(String path) {
        File file = new File(path);

        return deleteFile(file);
    }

    public static boolean deleteFile(File file) {
        if (file.isFile()) {
            return file.delete();
        }

        if (file.isDirectory()) {
            String[] children = file.list();
            for (int i = 0; i < children.length; i++) {
                deleteFile(new File(file, children[i]));
            }

            file.delete();

            return true;
        }

        return false;
    }

    public static String getOutputPath(Context context) {
        return context.getFilesDir().getAbsolutePath();
    }

    public static void copyFromAssetsFilesDir(Context context, String srcPath, String dstPath) throws Exception {
        int firstSeparator = srcPath.indexOf("/"); // Remove the path's www folder
        String outputRelativePath = srcPath.substring(firstSeparator + 1);
        String outputPath = dstPath + File.separator + outputRelativePath;
        try {
            InputStream is =  context.getAssets().open(srcPath);
            copyToFilesDir(context, outputPath, is);

        } catch (FileNotFoundException fnf) {
            try {
                File f = new File(outputPath);
                f.mkdirs();

                String[] list = context.getAssets().list(srcPath);
                for (String file : list) {
                    copyFromAssetsFilesDir(context, srcPath + File.separator + file, dstPath);
                }

            } catch (FileNotFoundException fnf2) {
                Log.e(FileUtils.class.getSimpleName(), "Can't read " + outputPath + " file: ", fnf2);
            }
        }
    }

    public static void copyToFilesDir(Context context, String path, InputStream is) throws IOException {
        int size;
        byte[] buffer = new byte[2048];

        FileOutputStream fout = new FileOutputStream(path);
        BufferedOutputStream bufferOut = new BufferedOutputStream(fout, buffer.length);

        while ((size = is.read(buffer, 0, buffer.length)) != -1) {
            bufferOut.write(buffer, 0, size);
        }
        bufferOut.flush();
        bufferOut.close();
        is.close();
        fout.close();
    }

    /**
     * Read a file from the internal assets as a String
     * @param context
     * @param path
     * @return
     * @throws IOException
     */
    public static String readFileFromAssets (final Context context, final String path) throws IOException {
        StringBuilder buf = new StringBuilder();
        InputStream json = context.getAssets().open(path);
        BufferedReader in = new BufferedReader(new InputStreamReader(json, "UTF-8"));
        String str;

        while ((str=in.readLine()) != null) {
            buf.append(str);
        }

        in.close();

        return buf.toString();
    }

}
