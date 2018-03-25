package com.dachen.creator.utils;

import com.dachen.creator.OpenFile;

import java.io.*;

public class FileUtils {

    public static final boolean copyFile(File old, File newFile){
        if(!old.exists()||newFile.exists()) return false;
        try {
            FileInputStream reader = new FileInputStream(old);
            FileOutputStream writer = new FileOutputStream(newFile);
            byte[] buffer = new byte[1024];
            int lenght = -1;

            while ((lenght = reader.read(buffer)) >0){
                writer.write(buffer,0,lenght);
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

    public static final boolean delete(File file){
        try {
            if (file.exists()) return file.delete();
        }catch (Exception e){
            return false;
        }
        return false;
    }

    public static final String read(File file){

        if(!file.exists()) return "";
        String result = "";
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder sb = new StringBuilder();
            String temp = null;
            while ((temp = reader.readLine())!=null){
                sb.append(temp+"\n");
            }
            reader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
    public static final boolean write(String data,File file){
        if(file.exists()) file.delete();
        if(!file.getParentFile().exists()) file.getParentFile().mkdirs();
        try {
            PrintWriter writer = new PrintWriter(file);
            writer.write(data);
            writer.flush();
            writer.close();
            return true;
        } catch (FileNotFoundException e) {
            return false;
        }
    }
}
