package com.expression.tool.rdb;

import android.content.Context;
import android.util.Log;

import com.expression.tool.rdb.model.StartInfo;
import com.expression.tool.rdb.utils.AppConfig;
import com.expression.tool.rdb.utils.CommonUtils;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * RDBWorker
 */

public class RDBWorker extends Thread {
    private static final int WORKER_PULL = 0x03010101;
    private static final int WORKER_INSTALL = 0x03010103;

    private static final int TAG_START = 0x01010101;
    private static final int TAG_DIR = 0x01010103;
    private static final int TAG_DATA = 0x01010113;
    private static final int TAG_END = 0x01011101;
    private static final int TAG_CLOSE = 0x01010301;

    private Context mContext;
    private boolean mRunning;
    private Socket mClientSocket;
    private int worker;
    private InputStream mInputStream;
    private OutputStream mOutputStream;
    private File mFile;
    private FileOutputStream mOutStream;

    public RDBWorker(Context context, Socket client) {
        super();
        mContext = context;
        mClientSocket = client;
        mRunning = true;

    }

    private byte[] toBytes(int tag) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) ((tag >> 24) & 0xff);
        bytes[1] = (byte) ((tag >> 16) & 0xff);
        bytes[2] = (byte) ((tag >> 8) & 0xff);
        bytes[3] = (byte) (tag & 0xff);

        return bytes;
    }

    private int getTag(byte[] info) {
        int tag = (info[0] & 0xff) << 24 | (info[1] & 0xff) << 16 |
                (info[2] & 0xff) << 8 | (info[3] & 0xff);

        return tag;
    }

    private int getSize(byte[] info) {
        int size = (info[4] & 0xff) << 24 | (info[5] & 0xff) << 16 |
                (info[6] & 0xff) << 8 | (info[7] & 0xff);

        return size;
    }

    private void processInstallStart(InputStream stream, byte[] info) {
        int size = getSize(info);

        try {
            byte[] data = read(size);
            String str = new String(data);

            Gson gson = new Gson();
            StartInfo startInfo = gson.fromJson(str, StartInfo.class);

            Log.e("tag_rdb", "send start");
            data_count = 0;
            // BusProvider.getBus().post(new DataEvent(TAG_START, str));

            mFile = new File(AppConfig.APP_PATH + File.separator + startInfo.packageName);
            if (mFile.exists()) {
                mFile.delete();
            }
            mOutStream = new FileOutputStream(mFile);

            // BusProvider.getBus().post(new DataEvent(DataEvent.TAG_TIP, "create outstream"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int data_count = 0;

    private void processData(InputStream stream, byte[] info) {
        int size = getSize(info);
        data_count++;
        // if (data_count == 1398) {
        //    String log = String.format("%02x%02x%02x%02x %02x%02x%02x%02x",
        //            info[0],info[1],info[2],info[3],
        //            info[4],info[5],info[6],info[7]);
        //    Log.e("tag_rdb", "info:" + log);
        // }
        // Log.e("tag_rdb", "data_count:" + data_count + ",size:" + size);
        try {
            byte[] data = read(size);
            mOutStream.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processEnd() {
        try {
            if (null != mOutStream) {
                mOutStream.close();
                mOutStream = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        mRunning = false;
        Log.e("tag_rdb", "send end");
        // BusProvider.getBus().post(new DataEvent(TAG_END, mFile.getAbsolutePath()));
    }

    private byte[] read(int len) {
        int count = 0;
        byte[] data = new byte[len];
        while (mRunning && count < len) {
            try {
                byte[] temp = new byte[len - count];
                int r = mInputStream.read(temp);
                if (r > 0) {
                    System.arraycopy(temp, 0, data, count, r);
                    count += r;
                } else if (0 == r) {
                    Log.e("tag_rdb", "read is 0, socket may be closed");
                    mRunning = false;
                    try {
                        if (null != mOutStream) {
                            mOutStream.close();
                            mOutStream = null;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return data;
    }

    @Override
    public void run() {
        try {
            worker = 0;
            mInputStream = mClientSocket.getInputStream();
            mOutputStream = mClientSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (mRunning) {
            try {
                int tag = 0;
                if (worker == 0) {
                    byte[] info = read(4);
                    Log.d("tag_rdb", "recv,worker,info len:" + info.length);
                    if (info.length == 4) {
                        tag = getTag(info);
                        Log.d("tag_rdb", "recv,worker,tag:" + tag);
                        switch (tag) {
                            case WORKER_INSTALL:
                            case WORKER_PULL:
                                worker = tag;
                                break;
                        }
                    }
                } else if (worker == WORKER_PULL) {
                    Log.d("tag_rdb", "WORKER_PULL");
                    worker_pull();
                } else if (worker == WORKER_INSTALL) {
                    Log.d("tag_rdb", "WORKER_INSTALL");
                    worker_install();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            mClientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            CommonUtils.installNormal(mContext, mFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void worker_install() {
        byte[] info = read(8);
//                String log = String.format("%02x%02x%02x%02x%02x%02x%02x%02x",
//                        info[0],info[1],info[2],info[3],
//                        info[4],info[5],info[6],info[7]);
//                Log.e("tag_rdb", log);
        if (info.length >= 4) {
            int tag = getTag(info);
            switch (tag) {
                case TAG_START:
                    processInstallStart(mInputStream, info);
                    break;
                case TAG_DATA:
                    processData(mInputStream, info);
                    break;
                case TAG_END:
                    processEnd();
                    break;
            }
        }
    }


    private void processPullStart(InputStream stream, byte[] info) {
        int size = getSize(info);
        byte[] data = read(size);
        String root = new String(data);
        Log.d("tag_rdb", "processPullStart,root:" + root);

        ArrayList<File> fileList = new ArrayList<>();

        File target = new File(root);
        fileList.add(target);

        while(fileList.size() > 0) {
            File file = fileList.get(0);
            fileList.remove(0);
            if (file.exists()) {
                if (file.isFile()) {
                    processPullFile(root, file);
                } else if (file.isDirectory()) {
                    processPullDir(root, file);
                    File[] files = file.listFiles();
                    Log.d("tag_rdb", "item size:" + files.length);
                    for (File item : files) {
                        Log.d("tag_rdb", "item:" + item.getAbsolutePath());
                        fileList.add(item);
                    }
                } else {
                    // error
                }
            } else {
                // error
            }
        }
        try {
            byte[] tag = toBytes(TAG_CLOSE);
            mOutputStream.write(tag);
            mOutputStream.write(toBytes(0));
            mOutputStream.flush();
            mOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mRunning = false;
    }

    private void processPullDir(String root, File file) {
        String fullPath = file.getAbsolutePath();
        String currPath = fullPath.replace(root, "");
        Log.d("tag_rdb", "processPullDir:" + fullPath + ",currPath:" + currPath + "currPath len:" + currPath.length());

        int pathLen = currPath.length();
        if (pathLen > 1) {
            currPath = currPath.substring(1);
            pathLen = currPath.length();
            byte[] tag = toBytes(TAG_DIR);
            byte[] len = toBytes(pathLen);
            try {
                mOutputStream.write(tag);
                mOutputStream.write(len);
                if (pathLen > 0) {
                    mOutputStream.write(currPath.getBytes());
                }
                mOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void processPullFile(String root, File file) {
        String fullPath = file.getAbsolutePath();
        String filePath = fullPath.replace(root+"/", "");
        Log.d("tag_rdb", "processPullFile:" + fullPath + ",filePath:" + filePath);

        byte[] tag = toBytes(TAG_START);
        byte[] len = toBytes(filePath.length());
        try {
            Log.d("tag_rdb", "try to send TAG_START,len:" + filePath.length());
            mOutputStream.write(tag);
            mOutputStream.write(len);
            mOutputStream.write(filePath.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            tag = toBytes(TAG_DATA);
            byte[] data = new byte[SEND_BLOCK];
            FileInputStream inputStream = new FileInputStream(fullPath);

            while (true) {
                int dataLen = inputStream.read(data);
                Log.d("tag_rdb", "send TAG_DATA,len:" + dataLen);
                if (dataLen <= 0) {
                    break;
                }
                mOutputStream.write(tag);
                mOutputStream.write(toBytes(dataLen));
                if (dataLen == SEND_BLOCK) {
                    mOutputStream.write(data);
                } else {
                    byte[] revData = new byte[dataLen];
                    System.arraycopy(data, 0, revData, 0, dataLen);
                    mOutputStream.write(revData);
                }
                mOutputStream.flush();
            }

            tag = toBytes(TAG_END);
            mOutputStream.write(tag);
            mOutputStream.write(toBytes(0));
            mOutputStream.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private static final int SEND_BLOCK = 16 * 1024;

    private void worker_pull() {
        byte[] info = read(8);
        if (info.length >= 4) {
            int tag = getTag(info);
            Log.d("tag_rdb", "worker_pull,tag:" + tag);
            switch (tag) {
                case TAG_START:
                    Log.d("tag_rdb", "worker_pull,TAG_START:" + tag);
                    processPullStart(mInputStream, info);
                    break;
            }
        }
    }
}
