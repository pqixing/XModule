package com.dachen.creator.utils

import com.intellij.openapi.vfs.VirtualFile;

import java.io.*;

public class FileUtils {

    public static final boolean copyFile(File old, File newFile) {
        if (!old.exists() || newFile.exists()) return false;
        try {
            FileInputStream reader = new FileInputStream(old);
            FileOutputStream writer = new FileOutputStream(newFile);
            byte[] buffer = new byte[1024];
            int lenght = -1;

            while ((lenght = reader.read(buffer)) > 0) {
                writer.write(buffer, 0, lenght);
            }
            writer.flush();
            writer.close();
            reader.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static final boolean delete(File file) {
        try {
            if (file.exists()) return file.delete();
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    public static final String readForOne(File file) {

        String read = read(file);
//        delete(file);
        return read;
    }

    public static final String read(File file) {

        if (!file.exists()) return "";
        return file.text
    }

    public static final boolean write(String data, File file) {
        if (file.exists()) file.delete();
        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
        file.write(data)
    }

    public static final boolean saveConfig(String key, String value) {
        def f = getConfigFile()
        def p = new Properties()
        if (f.exists()) {
            p.load(f.newInputStream())
        }
        p.put(key, value)
        p.store(f.newOutputStream(),"utf-8")
    }

    public static final String readConfig(String key, String value) {
        def f = getConfigFile()
        if (f.exists()) {
            def p = new Properties()
            p.load(f.newInputStream())
            def v = p.getProperty(key)
            if (v != null) return v
        }
        return value
    }

    public static final File getConfigFile() {
        return new File(System.getenv("HOME"), ".ide.config")
    }
}
