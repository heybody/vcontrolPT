package com.vcontrol.vcontroliot.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.vcontrol.vcontroliot.R;
import com.vcontrol.vcontroliot.log.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

/**
 * Created by linxi on 2017/5/10.
 */

public class FTPManager
{

    private static final String TAG = FTPManager.class.getSimpleName();
    private static FTPManager ftpManager;


    private String The_local_file_exist;

    private String The_server_file_exist;
    private String file_been_downloaded;
    private String Remote_file_exists;
    private String The_name_is;

    private String Local_file_exists;
    private String successfully_deleted;
    private String Start_redownload;

    private String Download_progress;
    private String file__downloaded_successfully;
    private String File_download_failed;

    private String local_file_name;
    private String Server_file_storage_path;
    private String server_not_exist;
    private String server_delete_reupload;
    private String Upload_progress;

    FTPClient ftpClient = null;


    public static FTPManager getFtpManager()
    {
        if (ftpManager == null)
        {
            ftpManager = new FTPManager();
        }
        return ftpManager;
    }

    public FTPManager()
    {
        ftpClient = new FTPClient();
    }

    // 连接到ftp服务器
    public synchronized boolean connect() throws Exception
    {
        Log.info(TAG,"connect::");
        boolean bool = false;
        if (ftpClient.isConnected())
        {//判断是否已登陆
            ftpClient.disconnect();
        }
        ftpClient.setDataTimeout(20000);//设置连接超时时间
        ftpClient.setControlEncoding("utf-8");
        ftpClient.connect(ConfigParams.FTP_SERVER_IP, ConfigParams.FTP_SERVER_PORT);
        if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode()))
        {
            if (ftpClient.login(ConfigParams.FTP_SERVER_ACCOUNT, ConfigParams.FTP_SERVER_PSD))
            {
                bool = true;
                Log.info(TAG,"connect::success");
            }
        }
        return bool;
    }

    // 创建文件夹
    public boolean createDirectory(String path) throws Exception
    {
        boolean bool = false;
        String directory = path.substring(0, path.lastIndexOf("/") + 1);
        int start = 0;
        int end = 0;
        if (directory.startsWith("/"))
        {
            start = 1;
        }
        end = directory.indexOf("/", start);
        while (true)
        {
            String subDirectory = directory.substring(start, end);
            if (!ftpClient.changeWorkingDirectory(subDirectory))
            {
                ftpClient.makeDirectory(subDirectory);
                ftpClient.changeWorkingDirectory(subDirectory);
                bool = true;
            }
            start = end + 1;
            end = directory.indexOf("/", start);
            if (end == -1)
            {
                break;
            }
        }
        return bool;
    }

    // 实现上传文件的功能
    public synchronized boolean uploadFile(String localPath, String serverPath, Context context)
            throws Exception
    {
        // 上传文件之前，先判断本地文件是否存在
        File localFile = new File(localPath);
        The_local_file_exist = context.getString(R.string.The_local_file_exist);
        local_file_name = context.getString(R.string.local_file_name);
        Server_file_storage_path = context.getString(R.string.Server_file_storage_path);
        server_not_exist = context.getString(R.string.server_not_exist);
        server_delete_reupload = context.getString(R.string.server_delete_reupload);
        Upload_progress = context.getString(R.string.Upload_progress);
        if (!localFile.exists())
        {
            Log.info(TAG,The_local_file_exist);//(TAG,"本地文件不存在");
            return false;
        }
        Log.info(TAG,local_file_name + localFile.getName());
        createDirectory(serverPath); // 如果文件夹不存在，创建文件夹
        Log.info(TAG,Server_file_storage_path + serverPath + localFile.getName());
        String fileName = localFile.getName();
        // 如果本地文件存在，服务器文件也在，上传文件，这个方法中也包括了断点上传
        long localSize = localFile.length(); // 本地文件的长度
        FTPFile[] files = ftpClient.listFiles(fileName);
        long serverSize = 0;
        if (files.length == 0)
        {
            Log.info(TAG,server_not_exist);
            serverSize = 0;
        }
        else
        {
            serverSize = files[0].getSize(); // 服务器文件的长度
        }
        if (localSize <= serverSize)
        {
            if (ftpClient.deleteFile(fileName))
            {
                Log.info(TAG,server_delete_reupload);
                serverSize = 0;
            }
        }
        RandomAccessFile raf = new RandomAccessFile(localFile, "r");
        // 进度
        long step = localSize / 100;
        long process = 0;
        long currentSize = 0;
        // 好了，正式开始上传文件
        ftpClient.enterLocalPassiveMode();
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        ftpClient.setRestartOffset(serverSize);
        raf.seek(serverSize);
        OutputStream output = ftpClient.appendFileStream(fileName);
        byte[] b = new byte[1024];
        int length = 0;
        while ((length = raf.read(b)) != -1)
        {
            output.write(b, 0, length);
            currentSize = currentSize + length;
            if (currentSize / step != process)
            {
                process = currentSize / step;
                if (process % 10 == 0)
                {
                    Log.info(TAG,"上传进度：" + process);
                }
            }
        }
        output.flush();
        output.close();
        raf.close();
        if (ftpClient.completePendingCommand())
        {
            Log.info(TAG,"文件上传成功");
            return true;
        }
        else
        {
            Log.info(TAG,"文件上传失败");
            return false;
        }
    }

    // 实现下载文件功能，可实现断点下载
    public synchronized boolean downloadFile(String localPath, String serverPath,Context context)
            throws Exception
    {
        The_server_file_exist = context.getString(R.string.The_server_file_exist);
        file_been_downloaded = context.getString(R.string.file_been_downloaded);
        Remote_file_exists = context.getString(R.string.Remote_file_exists);
        The_name_is = context.getString(R.string.The_name_is);

        Local_file_exists = context.getString(R.string.Local_file_exists);
        successfully_deleted = context.getString(R.string.successfully_deleted);
        Start_redownload = context.getString(R.string.Start_redownload);

        Download_progress = context.getString(R.string.Download_progress);
        file__downloaded_successfully = context.getString(R.string.file__downloaded_successfully);
        File_download_failed = context.getString(R.string.File_download_failed);
        if (ftpClient == null)
        {
            Log.info(TAG,"ftpClient is null");
            return false;
        }
        // 先判断服务器文件是否存在
        FTPFile[] files = ftpClient.listFiles(serverPath);
        if (files.length == 0)
        {
            Log.info(TAG,The_server_file_exist);//(TAG,"服务器文件不存在");
            return false;
        }
        Log.info(TAG,Remote_file_exists + The_name_is + files[0].getName());//(TAG,"远程文件存在,名字为：" + files[0].getName());
        localPath = localPath + files[0].getName();
        // 接着判断下载的文件是否能断点下载
        long serverSize = files[0].getSize(); // 获取远程文件的长度
        File localFile = new File(localPath);
        long localSize = 0;
        if (localFile.exists())
        {
            localSize = localFile.length(); // 如果本地文件存在，获取本地文件的长度
            if (localSize >= serverSize)
            {
                Log.info(TAG,file_been_downloaded);//(TAG,"文件已经下载完了");
                File file = new File(localPath);
                file.delete();
                Log.info(TAG,Local_file_exists + successfully_deleted + Start_redownload);//(TAG,"本地文件存在，删除成功，开始重新下载");
                return false;
            }
        }
        // 进度
        long step = serverSize / 100;
        long process = 0;
        long currentSize = 0;
        // 开始准备下载文件
        ftpClient.enterLocalActiveMode();
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        OutputStream out = new FileOutputStream(localFile, true);
        ftpClient.setRestartOffset(localSize);
        InputStream input = ftpClient.retrieveFileStream(serverPath);

        if (input == null)
        {
            return false;
        }

        byte[] b = new byte[1024];
        int length = 0;
        while ((length = input.read(b)) != -1)
        {
            out.write(b, 0, length);
            currentSize = currentSize + length;
            if (currentSize / step != process)
            {
                process = currentSize / step;
                if (process % 10 == 0)
                {
                    Log.info(TAG,Download_progress + process);//(TAG,"下载进度：" + process);

                }
            }
        }
        out.flush();
        out.close();
        input.close();
        // 此方法是来确保流处理完毕，如果没有此方法，可能会造成现程序死掉
        if (ftpClient.completePendingCommand())
        {
            Log.info(TAG,file__downloaded_successfully);//(TAG,"文件下载成功");

            return true;
        }
        else
        {
            Log.info(TAG,File_download_failed);//(TAG,"文件下载失败");
            return false;
        }
    }


    /**
     * 根据url下载图片在指定的文件
     *
     * @param urlStr
     * @param loacaFile
     * @return
     */
    public boolean downloadImgByUrl(String urlStr,FTPFile remoteFile, File loacaFile)
    {
        if (ftpClient == null)
        {
            return false;
        }
        Log.info(TAG,"downloadImgByUrl::urlStr:" + urlStr + ",remoteFile" + remoteFile.getSize() + ",loacaFile:" + loacaFile.getAbsolutePath());
        FileOutputStream fos = null;
        InputStream is = null;
        try
        {
//            URL url = new URL(urlStr);
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//
//            is = conn.getInputStream();

            // 开始准备下载文件
            ftpClient.enterLocalActiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.setRestartOffset(remoteFile.getSize());
            is = ftpClient.retrieveFileStream(urlStr);

            fos = new FileOutputStream(loacaFile);
            byte[] buf = new byte[512];
            int len = 0;
            while ((len = is.read(buf)) != -1)
            {
                fos.write(buf, 0, len);
            }
            fos.flush();
            return true;

        } catch (Exception e)
        {
            e.printStackTrace();
        } finally
        {
            try
            {
                if (is != null)
                    is.close();
            } catch (IOException e)
            {
            }

            try
            {
                if (fos != null)
                    fos.close();
            } catch (IOException e)
            {
            }
        }

        return false;

    }

    /**
     * 根据url下载图片在指定的文件
     *
     * @param urlStr
     * @param imageview
     * @return
     */
    public Bitmap downloadImgByUrl(String urlStr,FTPFile remoteFile, ImageView imageview)
    {
        if (ftpClient == null)
        {
            return null;
        }
        Log.info(TAG,"downloadImgByUrl::urlStr:" + urlStr + ",remoteFile" + remoteFile.getSize());
        FileOutputStream fos = null;
        InputStream is = null;
        try
        {
//            URL url = new URL(urlStr);
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // 开始准备下载文件
            ftpClient.enterLocalActiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.setRestartOffset(remoteFile.getSize());
            is = ftpClient.retrieveFileStream(urlStr);

//            is = new BufferedInputStream(conn.getInputStream());
//            is.mark(is.available());

            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            Bitmap bitmap = BitmapFactory.decodeStream(is, null, opts);

            //获取imageview想要显示的宽和高
            ImageSizeUtil.ImageSize imageViewSize = ImageSizeUtil.getImageViewSize(imageview);
            opts.inSampleSize = ImageSizeUtil.caculateInSampleSize(opts,
                    imageViewSize.width, imageViewSize.height);

            opts.inJustDecodeBounds = false;
            is.reset();
            bitmap = BitmapFactory.decodeStream(is, null, opts);

//            conn.disconnect();
            return bitmap;

        } catch (Exception e)
        {
            e.printStackTrace();
        } finally
        {
            try
            {
                if (is != null)
                    is.close();
            } catch (IOException e)
            {
            }

            try
            {
                if (fos != null)
                    fos.close();
            } catch (IOException e)
            {
            }
        }

        return null;

    }



    // 获取FTP服务器列表
    public FTPFile[] getFtpFileList(String serverPath) throws Exception
    {
        if (ftpClient == null)
        {
            Log.info(TAG,"ftpClient is null");
            return null;
        }
        // 先判断服务器文件是否存在
       return ftpClient.listFiles(serverPath);
    }

    // 如果ftp上传打开，就关闭掉
    public void closeFTP() throws Exception
    {
        if (ftpClient.isConnected())
        {
            ftpClient.disconnect();
        }
    }
}
