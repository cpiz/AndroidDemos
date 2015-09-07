package com.cpiz.android.utils;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 文件zip压缩工具类
 *
 * Created by caijw on 2015/7/8.
 */
public class FileZipUtil {
    private static final String TAG = "FileZipUtil";

    /**
     * 压缩单个文件到zip
     * @param originalFilePath  待压缩的原文件路径
     * @param zippedFilePath    生成的压缩文件路径
     * @return 成功返回true，失败false
     */
    public static boolean zipFile(String originalFilePath, String zippedFilePath) {
        return zipFile(new File(originalFilePath), new File(zippedFilePath));
    }

    /**
     * 压缩单个文件到zip
     * @param originalFile  待压缩的原文件
     * @param zippedFile    生成的压缩文件
     * @return 成功返回true，失败false
     */
    public static boolean zipFile(File originalFile, File zippedFile) {
        List<File> originalFiles = new ArrayList<>(1);
        originalFiles.add(originalFile);
        return zipFiles(originalFiles, zippedFile) > 0;
    }

    /**
     * 压缩多个文件到一个zip文件
     * @param originalFilePaths 待压缩的原文件路径列表
     * @param zippedFilePath    生成的压缩文件路径
     * @return 成功压缩的文件数
     */
    public static int zipFiles(List<String> originalFilePaths, String zippedFilePath) {
        List<File> originalFiles = new ArrayList<>(originalFilePaths.size());
        for (String path : originalFilePaths) {
            originalFiles.add(new File(path));
        }

        return zipFiles(originalFiles, new File(zippedFilePath));
    }

    /**
     * 压缩多个文件到一个zip文件
     * @param originalFiles 待压缩的原文件列表
     * @param zippedFile    生成的压缩文件
     * @return 成功压缩的文件数
     */
    public static int zipFiles(List<File> originalFiles, File zippedFile) {
        int zippedFileCount = 0;
        try {
            ZipOutputStream outZip = new ZipOutputStream(new FileOutputStream(zippedFile));

            for (File originalFile : originalFiles) {
                try {
                    FileInputStream inputStream = new FileInputStream(originalFile);

                    ZipEntry zipEntry = new ZipEntry(originalFile.getName());
                    outZip.putNextEntry(zipEntry);

                    int len;
                    byte[] buffer = new byte[10 * 1024];
                    while ((len = inputStream.read(buffer)) != -1) {
                        outZip.write(buffer, 0, len);
                    }

                    inputStream.close();
                    outZip.closeEntry();

                    zippedFileCount++;
                } catch (FileNotFoundException ex) {
                    Log.e(TAG, ex.toString(), ex);
                    continue;
                }
            }

            outZip.finish();
            outZip.close();
        } catch (IOException ex) {
            Log.e(TAG, ex.toString(), ex);
            return 0;
        }

        return zippedFileCount;
    }
}
