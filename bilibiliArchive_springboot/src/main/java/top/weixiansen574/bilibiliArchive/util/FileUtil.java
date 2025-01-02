package top.weixiansen574.bilibiliArchive.util;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class FileUtil {
    public static void outputToFile(InputStream inputStream, File file) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        byte[] buffer = new byte[4096];
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
            fileOutputStream.write(buffer, 0, read);
        }
        fileOutputStream.flush();
        fileOutputStream.close();
        inputStream.close();
    }

    public static void copyFile(File source, File target) throws IOException {
        try (FileInputStream fis = new FileInputStream(source);
             FileOutputStream fos = new FileOutputStream(target)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
        }
    }

    public static void outputToFile(InputStream inputStream, String path) throws IOException {
        outputToFile(inputStream, new File(path));
    }

    public static boolean outputToFile(byte[] bytes, File path) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(path);
            fileOutputStream.write(bytes);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static void deleteDirs(File file) {
        if (file.isFile()) {
            file.delete();
        } else {
            File[] childFilePath = file.listFiles();
            if (childFilePath != null) {
                for (File child : childFilePath) {
                    deleteDirs(child);
                }
                file.delete();
            }
        }
    }

    public static byte[] readAll(File file) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(new FileInputStream(file));
        return dataInputStream.readAllBytes();
    }

    public static String readAllToString(File file) throws IOException {
        return new String(readAll(file), StandardCharsets.UTF_8);
    }


    public static File getOrCreateDir(File parent, String child) throws IOException {
        File file = new File(parent, child);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                throw new IOException("mkdir " + file.getAbsolutePath() + " failed");
            }
        }
        return file;
    }

    public static void deleteOneFile(File file) throws IOException {
        if (file.exists()) {
            if (!file.delete()) {
                throw new IOException("delete " + file.getAbsolutePath() + " failed");
            }
        }
    }

    public static void deleteOneIfExists(File file){
        if (file.exists()){
            if (!file.delete()){
                throw new RuntimeException("delete " + file.getAbsolutePath() + " failed");
            }
        }
    }

    /**
     * 计算文件或文件夹所占用的存储空间
     *
     * @param path 文件或文件夹路径
     * @return 总大小（字节），如果路径不存在则返回 0
     */
    public static long calculateSize(File path) {
        if (!path.exists()) {
            return 0; // 路径不存在，返回 0
        }
        return getSize(path);
    }

    private static long getSize(File file) {
        if (file.isFile()) {
            return file.length(); // 单个文件直接返回大小
        }

        long totalSize = 0;
        File[] files = file.listFiles();
        if (files != null) { // 防止文件夹被限制访问导致 null
            for (File f : files) {
                totalSize += getSize(f); // 递归计算子文件或子文件夹的大小
            }
        }
        return totalSize;
    }

}
