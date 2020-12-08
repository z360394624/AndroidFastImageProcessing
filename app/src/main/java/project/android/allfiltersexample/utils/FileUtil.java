package project.android.allfiltersexample.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileUtil {

    private static final String[] INVALID_ZIP_ENTRY_NAME = new String[]{"../", "~/"};

    public static boolean copyAssets(Context context, String fileName, File destFile) {
        if (!TextUtils.isEmpty(fileName) && destFile != null) {
            boolean isSuccess = false;
            AssetManager assetManager = context.getAssets();
            InputStream in = null;
            FileOutputStream out = null;

            try {
                in = assetManager.open(fileName);
                out = new FileOutputStream(destFile);
                copyFile((InputStream)in, (OutputStream)out);
                isSuccess = true;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }

                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            return isSuccess;
        } else {
            return false;
        }
    }

    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];

        int read;
        while((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }

    }

    public static boolean unzip(String zipFile, String targetDir, boolean nomedia) {
        boolean unzipSuccess;
        try {
            unzipSuccess = true;
            unzipWithExeption(zipFile, targetDir, nomedia);
        } catch (Exception e) {
            e.printStackTrace();
            unzipSuccess = false;
        }

        return unzipSuccess;
    }

    public static void unzipWithExeption(String zipFile, String targetDir) throws Exception {
        unzipWithExeption(zipFile, targetDir, false);
    }

    public static void unzipWithExeption(String zipFile, String targetDir, boolean nomedia) throws Exception {
        int BUFFER = 4096;
        BufferedOutputStream dest = null;
        FileInputStream fis = new FileInputStream(zipFile);
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));

        ZipEntry entry;
        try {
            while((entry = zis.getNextEntry()) != null) {
                byte[] data = new byte[BUFFER];
                String strEntry = entry.getName();
                if (!validEntry(strEntry)) {
                    throw new IllegalArgumentException("unsecurity zipfile!");
                }

                File entryFile = new File(targetDir, strEntry);
                if (entry.isDirectory()) {
                    if (!entryFile.exists()) {
                        entryFile.mkdirs();
                    }
                } else {
                    File entryDir = new File(entryFile.getParent());
                    if (!entryDir.exists()) {
                        entryDir.mkdirs();
                    }

                    if (nomedia) {
                        File nomeidiaFile = new File(entryDir, ".nomedia");
                        if (!nomeidiaFile.exists()) {
                            nomeidiaFile.createNewFile();
                        }
                    }

                    try {
                        dest = new BufferedOutputStream(new FileOutputStream(entryFile), BUFFER);

                        int count;
                        while((count = zis.read(data, 0, BUFFER)) != -1) {
                            dest.write(data, 0, count);
                        }

                        dest.flush();
                    } finally {
                        dest.close();
                    }
                }
            }
        } finally {
            zis.close();
        }
    }

    public static boolean validEntry(String name) {
        int i = 0;

        for(int l = INVALID_ZIP_ENTRY_NAME.length; i < l; ++i) {
            if (name.contains(INVALID_ZIP_ENTRY_NAME[i])) {
                return false;
            }
        }

        return true;
    }

    public static File getCacheDirectory(Context context) {
        return context.getExternalCacheDir();
    }
}
