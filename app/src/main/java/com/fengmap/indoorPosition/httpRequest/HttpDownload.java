package com.fengmap.indoorPosition.httpRequest;


import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import org.apache.commons.httpclient.util.HttpURLConnection;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * Created by ACER on 2018/1/16.
 */

public class HttpDownload {

    private int FILESIZE = 1024;

    /**
     * @param urlStr   文件地址
     * @param path     文件保存路径
     * @param fileName 文件名
     * @return 文件的绝对路径
     */
    public String downFile(Context context, String urlStr, String path, String fileName) {

        InputStream inputStream = null;
        String filePath = null;

        try {
            //判断文件是否存在
            filePath = Environment.getExternalStorageDirectory()+
                    path +fileName;
            File file = new File(filePath);
            if (file.exists()) {
                Toast.makeText(context, "未找到文件！",
                        Toast.LENGTH_SHORT).show();
            } else {
                //得到io流
                inputStream = getInputStreamFromURL(urlStr);
                //从input流中将文件写入SD卡中
                File resultFile = write2SDFromInput(filePath, inputStream);
                if (resultFile != null) {

                    filePath = resultFile.getPath();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null)
                    inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return filePath;
    }

    /**
     * 根据URL得到输入流
     *
     * @param urlStr
     * @return
     */
    public InputStream getInputStreamFromURL(String urlStr) {

        HttpURLConnection urlConn;
        InputStream inputStream = null;
        URL url;
        try {
            url = new URL(urlStr);
            urlConn = (HttpURLConnection) url.openConnection();
            inputStream = urlConn.getInputStream();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return inputStream;
    }

    /**
     * 将一个InputStream里面的数据写入到SD卡中
     *
     *
     * @param filePath 文件保存的名字
     * @param input 文件输入流
     * @return 文件
     */
    public File write2SDFromInput(String filePath,
                                  InputStream input) {
        File file = null;
        OutputStream output = null;
        try {

            file = new File(filePath);
            File parentFile = file.getParentFile();
            parentFile.mkdirs();
            file.createNewFile();

            // 开启输出流，准备写入文件
            output = new FileOutputStream(file);
            // 缓冲区
            byte[] buffer = new byte[FILESIZE];
            int count;
            while ((count = input.read(buffer)) != -1) {
                // 这里，请一定按该方式写入文件，不然时而会出现文件写入错误，数据丢失问题
                output.write(buffer, 0, count);
            }
            output.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                output.close();
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }
}
