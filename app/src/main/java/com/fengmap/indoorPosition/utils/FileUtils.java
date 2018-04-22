package com.fengmap.indoorPosition.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;

import com.fengmap.android.data.FMDataManager;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * @Email hezutao@fengmap.com
 * @Version 2.0.0
 * @Description 文件操作类
 */
public class FileUtils {

    /**
     * 主题文件类型
     */
    public static final String FILE_TYPE_THEME = ".theme";

    /**
     * 地图文件类型
     */
    public static final String FILE_TYPE_MAP = ".fmap";

    /**
     * 默认地图
     */
    public static final String DEFAULT_MAP_ID = "iotlab";

    /**
     * 默认主题
     */
    public static final String DEFAULT_THEME_ID = "iotlab";

    /**
     * 通过主题id获取主题路径
     *
     * @param themeId 主题id
     * @return
     */
    public static String getThemePath(String themeId) {
        String themePath = FMDataManager.getFMThemeResourceDirectory() + themeId + FILE_TYPE_THEME;
        return themePath;
    }

    /**
     * 通过地图id获取地图文件路径
     *
     * @param mapId 地图id
     * @return
     */
    public static String getMapPath(String mapId) {
        String mapPath = FMDataManager.getFMMapResourceDirectory() + mapId + File.separator + mapId + FILE_TYPE_MAP;
        return mapPath;
    }

    /**
     * 获取默认地图文件路径
     *
     * @param context 上下文
     * @return
     */
    public static String getDefaultMapPath(Context context) {
        String srcFile = DEFAULT_MAP_ID + FILE_TYPE_MAP;
        String destFile = getMapPath(DEFAULT_MAP_ID);
        try {
            FileUtils.copyAssetsToSdcard(context, srcFile, destFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return destFile;
    }

    /**
     * 获取默认地图主题路径
     *
     * @param context 上下文
     * @return
     */
    public static String getDefaultThemePath(Context context) {
        return getThemePath(context, DEFAULT_THEME_ID);
    }

    /**
     * 获取本地主题路径
     *
     * @param context 上下文
     * @param themeId 主题名称
     * @return
     */
    public static String getThemePath(Context context, String themeId) {
        String path = getThemePath(themeId);
        File file = new File(path);
        if (!file.exists()) {
            copyAssetsThemeToSdcard(context);
        }
        return path;
    }

    /**
     * 将assets目录下theme.zip主题复制、解压到sdcard中
     *
     * @param context 上下文
     */
    public static void copyAssetsThemeToSdcard(Context context) {
        String srcFileName = "iotlab.zip";
        String themeDir = FMDataManager.getFMThemeResourceDirectory();
        String destFileName = themeDir + srcFileName;
        try {
            copyAssetsToSdcard(context, srcFileName, destFileName);
            // 解压压缩包文件并删除主题压缩包文件
            ZipUtils.unZipFolder(destFileName, themeDir);
            deleteDirectory(destFileName);

            // 遍历目录是否存在主题文件,不存在则解压
            File themeFile = new File(themeDir);
            File[] files = themeFile.listFiles();

            String extension = ".zip";
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(extension)) {
                    File f = new File(file.getName().replace(extension, ""));
                    String fileDir = file.getAbsolutePath();
                    if (!f.exists()) {
                        ZipUtils.unZipFolder(fileDir, themeDir);
                    }
                    deleteDirectory(fileDir);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除文件
     *
     * @param fileDir 文件夹路径
     * @return
     */
    public static boolean deleteDirectory(String fileDir) {
        if (fileDir == null) {
            return false;
        }

        File file = new File(fileDir);
        if (file == null || !file.exists()) {
            return false;
        }

        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i].getAbsolutePath());
                } else {
                    files[i].delete();
                }
            }
        }

        file.delete();
        return true;
    }

    /**
     * 复制assets下文件到sdcard下文件
     *
     * @param context
     * @param srcFileName  复制源文件
     * @param destFileName 复制至sdcard文件
     * @throws IOException
     */
    public static void copyAssetsToSdcard(Context context, String srcFileName, String destFileName) throws IOException {
        File file = new File(destFileName);
        File parentFile = file.getParentFile();
        if (parentFile != null & !parentFile.exists()) {
            parentFile.mkdirs();
        }

        if (!file.exists()) {
            file.createNewFile();
        } else {
            return;
        }

        InputStream is = context.getAssets().open(srcFileName);
        OutputStream os = new FileOutputStream(destFileName);

        byte[] buffer = new byte[1024];
        int byteCount = 0;
        while ((byteCount = is.read(buffer)) != -1) {
            os.write(buffer, 0, byteCount);
        }
        os.flush();
        is.close();
        os.close();
    }

    /**
     * 第一种方法：
     * 将输入的用户名和密码保存在这个应用的某个文件中
     * @param context Activity的上面的某一层是Context,所以传值过来的是一个Activity,此处可以写成Context
     * @param name 输入的用户名
     * @param pass 输入的密码
     */
    public static void saveToFile(Context context, String name, String pass) {
        File dir = context.getFilesDir(); //查找这个应用下的所有文件所在的目录
        FileWriter writer;
        try {
            writer = new FileWriter(dir.getAbsolutePath() + "/userinfo.txt");
            writer.append(name + "," + pass);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从这个应用下的文件中读取保存的用户名和密码,再次登录时自动显示在输入框中
     * @param context
     * @param tname
     * @param tpass
     */
    public static void readFromFile(Context context, EditText tname, EditText tpass) {
        File dir = context.getFilesDir();//目录为：/data/data/com.etc.login/files
        FileReader reader;
        try {
            reader = new FileReader(dir.getAbsolutePath() + "/userinfo.txt");
            BufferedReader breader = new BufferedReader(reader);
            String line = breader.readLine();
            String strs[] = line.split(",");
            tname.setText(strs[0]);
            tpass.setText(strs[1]);
            breader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 第二种方法：
     * 将输入的用户名和密码保存到sdcard中
     * @param context
     * @param name
     * @param pass
     */
    public static void savtToSDCard(Context context, String name, String pass) {
        File sdcardDir = Environment.getExternalStorageDirectory();
        Log.d("mytag",sdcardDir.toString());//目录为：/storage/emulated/0
        /**
         * 但是使用cmd工具查找文件时，不再这个目录下，而是在/mnt/sdcard目录下
         *
         */
        FileWriter writer;
        try {
            writer = new FileWriter(sdcardDir.getAbsolutePath() + "/userinfo.txt");
            writer.append(name + "," + pass);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * 从sdcard中读取保存的用户名和密码
     * @param context
     * @param tname
     * @param tpass
     */
    public static void readFromSDCard(Context context, EditText tname, EditText tpass) {
        File sdcardDir = Environment.getExternalStorageDirectory();
        FileReader reader;
        try {
            reader = new FileReader(sdcardDir.getAbsolutePath() + "/userinfo.txt");
            BufferedReader breader = new BufferedReader(reader);
            String line = breader.readLine();
            String strs[] = line.split(",");
            tname.setText(strs[0]);
            tpass.setText(strs[1]);
            breader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 第三种方法：
     * 使用文件api进行保存用户名和密码，不必得到对应的目录
     * @param context
     * @param name
     * @param pass
     */
    public static void saveToFile2(Context context, String name, String pass) {
        try {
            FileOutputStream out = context.openFileOutput("userinfo2.txt",context.MODE_PRIVATE);
            OutputStreamWriter writer = new OutputStreamWriter(out);
            writer.append(name+","+pass);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * 使用文件api进行读取保存的用户名和密码
     * @param context
     * @param tname
     * @param tpass
     */
    public static void readFromFile2(Context context, EditText tname, EditText tpass) {
        try {
            FileInputStream in = context.openFileInput("userinfo2.txt");
            InputStreamReader reader = new InputStreamReader(in);
            BufferedReader breader = new BufferedReader(reader);
            String line = breader.readLine();
            String strs[] = line.split(",");
            tname.setText(strs[0]);
            tpass.setText(strs[1]);
            breader.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    /**
     * 最为通用的第四种方法：
     * 使用SharedPreference进行保存用户名和密码，可以实现拆分，不必手动的以，为分隔符进行拆分，
     * 如果用，进行拆分，那么一旦用户登录的时候输入了，号，程序就会出错，现在以Map的形式保存用户名和密码，
     * 就不用再担心这个问题
     * @param context
     * @param name
     * @param pass
     */
    public static void saveToPre(Context context, String name, String pass) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("userinfo",context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("name",name);
        editor.putString("pass",pass);
        editor.commit();
    }

    public static void saveToPre(Context context, Bitmap pic) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("userinfo",context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("pic",convertIconToString(pic));
        editor.commit();
    }
    /**
     * 使用SharedPreference进行读取保存的用户名和密码
     * @param context
     * @param tname
     * @param tpass
     */
    public static void readFromPre(Context context, EditText tname, EditText tpass) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("userinfo",context.MODE_PRIVATE);
        String name = sharedPreferences.getString("name","");
        String pass = sharedPreferences.getString("pass","");
        if (!name.equals("")) tname.setText(name);
        if (!pass.equals("")) tpass.setText(pass);
    }

    public static void readFromPre(Context context, ImageView portrait) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("userinfo",context.MODE_PRIVATE);
        String portraitString = sharedPreferences.getString("pic","");
        if (!portraitString.equals("")) portrait.setImageBitmap(convertStringToIcon(portraitString));
    }

    /**
     * 图片转成string
     *
     * @param bitmap
     * @return
     */
    public static String convertIconToString(Bitmap bitmap)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();// outputstream
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] appicon = baos.toByteArray();// 转为byte数组
        return Base64.encodeToString(appicon, Base64.DEFAULT);

    }

    /**
     * string转成bitmap
     *
     * @param st
     */
    public static Bitmap convertStringToIcon(String st)
    {
        // OutputStream out;
        Bitmap bitmap = null;
        try
        {
            // out = new FileOutputStream("/sdcard/aa.jpg");
            byte[] bitmapArray;
            bitmapArray = Base64.decode(st, Base64.DEFAULT);
            bitmap =
                    BitmapFactory.decodeByteArray(bitmapArray, 0,
                            bitmapArray.length);
            // bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            return bitmap;
        }
        catch (Exception e)
        {
            return null;
        }
    }

}
