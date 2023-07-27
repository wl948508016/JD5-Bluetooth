package com.bdtd.jd4.portable.util;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * 手机和多参数便携仪通过TCP消息交互
 */
public class TCPClient {
    private static final String TAG = TCPClient.class.getSimpleName();
    private String mIp;
    private int mPort;
    private Socket mSocket;
    private SocketAddress mSocketAddress;
    private OutputStream mOutputStream;
    private InputStream mInputStream;
    private ReadThread mReadThread;
    private boolean isConnected = false;
    private ReceiverMsgListener mListener;

    public TCPClient(String ip, int port, ReceiverMsgListener listener) {
        this.mIp = ip;
        this.mPort = port;
        this.mListener = listener;
    }

    public void connect() {
        mReadThread = new ReadThread();
        mReadThread.start();
    }

    private class ReadThread extends Thread {
        @Override
        public void run() {
            try {
                mSocket = new Socket();
                mSocket.setKeepAlive(true);
                mSocketAddress = new InetSocketAddress(mIp, mPort);
                mSocket.connect(mSocketAddress, 10000);// 设置连接超时时间为10秒
                mOutputStream = mSocket.getOutputStream();
                mInputStream = mSocket.getInputStream();

                isConnected = true;
                if (mListener != null) {
                    mListener.connectSuccess();
                }
                Log.d(TAG, "connect success");
            } catch (IOException e) {
                mListener.connectFail(e.toString());
                Log.e(TAG, "connect fail: " + e);
            }

            while (isConnected) {
                try {
                    if (mInputStream == null) {
                        return;
                    }
                    byte[] buffer = new byte[1024];
                    while (mInputStream.read(buffer) != -1) {
                        String data = DataUtils.byte2Hex(buffer);
                        Message message = new Message();
                        message.what = 100;
                        Bundle bundle = new Bundle();
                        bundle.putString("data", data);
                        message.setData(bundle);
                        mHandler.sendMessage(message);
                    }
                } catch (Throwable e) {
                    Log.e(TAG, "receiver message fail: " + e.getMessage());
                }
            }
        }
    }

    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 100) {
                if (mListener != null) {
                    mListener.receiverMsg(msg.getData().getString("data"));
                }
            }
        }
    };

    public void send(String message) {
        if (mOutputStream != null) {
            new Thread(() -> {
                try {
                    mOutputStream.write(DataUtils.hex2Bytes(message));
                    Log.d(TAG, "send success");
                } catch (Exception e) {
                    Log.e(TAG, "send fail: " + e.getMessage());
                }
            }).start();
        }
    }

    public void close() {
        if (this.mReadThread != null) {
            this.mReadThread.interrupt();
        }

        try {
            if (mOutputStream != null) {
                mOutputStream.close();
            }
            if (mInputStream != null) {
                mInputStream.close();
            }
            if (this.mSocket != null) {
                this.mSocket.close();
                this.mSocket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        isConnected = false;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public interface ReceiverMsgListener {
        void receiverMsg(String msg);

        void connectSuccess();

        void connectFail(String errorMsg);
    }
}
