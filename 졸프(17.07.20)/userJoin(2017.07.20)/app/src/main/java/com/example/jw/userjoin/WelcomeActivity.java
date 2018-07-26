package com.example.jw.userjoin;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * Created by JW on 2017. 7. 5..
 */

public class WelcomeActivity extends MainActivity {

    private TextView Welcome;
    private SharedPreferences setting;
    private SharedPreferences.Editor editor;
    private Switch LogoutB;
    private TextView Num;


    //아두이노 서버와 연결
    private ImageView mLockbt;
    private ImageView mUnLockbt;
    private TextView mConnectionStatus;

    private static final String TAG = "TcpClient";
    private boolean isConnected = false;

    private String mServerIP = null;
    private Socket mSocket = null;
    private PrintWriter mOut;
    private BufferedReader mIn;
    private Thread mReceiverThread = null;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Welcome = (TextView)findViewById(R.id.TextWelcome);

        setting = getSharedPreferences("setting",0);
        editor = setting.edit();

        Num = (TextView)findViewById(R.id.numbetText);
        Num.setText("Number: "+ setting.getString("ID",""));

        editor.putBoolean("Auto_Login_enabled",true);
        editor.commit();

        Switch LogoutB = (Switch) findViewById(R.id.switchLog);
        LogoutB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked != true){
                    editor.clear();
                    editor.putBoolean("Auto_Login_enabled",false);
                    editor.commit();

                    Intent intent = new Intent(WelcomeActivity.this,MainActivity.class);
                    startActivity(intent);
                    finish();

                }
            }
        });

        //아두이노
        mLockbt = (ImageView)findViewById(R.id.Lock);
        mUnLockbt = (ImageView)findViewById(R.id.UnLock);
        mConnectionStatus = (TextView)findViewById(R.id.connection_status_textview);

        mLockbt.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){

                    if (!isConnected) showErrorDialog("서버로 접속된후 다시 해보세요.");
                    else {
                        new Thread(new SenderThread("open")).start();
                    }
                }
            });

        mUnLockbt.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){

                if (!isConnected) showErrorDialog("서버로 접속된후 다시 해보세요.");
                else {
                    new Thread(new SenderThread("close")).start();
                }
            }
        });



        new Thread(new ConnectThread("192.168.0.5", 80)).start();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        isConnected = false;
    }

    private class ConnectThread implements Runnable {

        private String serverIP;
        private int serverPort;

        ConnectThread(String ip, int port) {
            serverIP = ip;
            serverPort = port;

        }

        @Override
        public void run() {

            try {

                mSocket = new Socket(serverIP, serverPort);
                //ReceiverThread: java.net.SocketTimeoutException: Read timed out 때문에 주석처리
                //mSocket.setSoTimeout(3000);

                mServerIP = mSocket.getRemoteSocketAddress().toString();

            } catch( UnknownHostException e )
            {
                Log.d(TAG,  "ConnectThread: can't find host");
            }
            catch( SocketTimeoutException e )
            {
                Log.d(TAG, "ConnectThread: timeout");
            }
            catch (Exception e) {

                Log.e(TAG, ("ConnectThread:" + e.getMessage()));
            }


            if (mSocket != null) {

                try {

                    mOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream(), "UTF-8")), true);
                    mIn = new BufferedReader(new InputStreamReader(mSocket.getInputStream(), "UTF-8"));

                    isConnected = true;
                } catch (IOException e) {

                    Log.e(TAG, ("ConnectThread:" + e.getMessage()));
                }
            }


            runOnUiThread(new Runnable() {

                @Override
                public void run() {

                    if (isConnected) {

                        Log.d(TAG, "connected to " + serverIP);
                        mConnectionStatus.setText("connected to " + serverIP);

                        mReceiverThread = new Thread(new ReceiverThread());
                        mReceiverThread.start();
                    }else{

                        Log.d(TAG, "failed to connect to server " + serverIP);
                        mConnectionStatus.setText("failed to connect to server "  + serverIP);
                    }

                }
            });
        }
    }


    private class SenderThread implements Runnable {

        private String msg;

        SenderThread(String msg) {
            this.msg = msg;
        }

        @Override
        public void run() {

            mOut.println(this.msg);
            mOut.flush();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "send message: " + msg);
                }
            });
        }
    }


    private class ReceiverThread implements Runnable {

        @Override
        public void run() {

            try {

                while (isConnected) {

                    if ( mIn ==  null ) {

                        Log.d(TAG, "ReceiverThread: mIn is null");
                        break;
                    }

                    final String recvMessage =  mIn.readLine();

                    if (recvMessage != null) {

                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {

                                Log.d(TAG, "recv message: "+recvMessage);
                            }
                        });
                    }
                }

                Log.d(TAG, "ReceiverThread: thread has exited");
                if (mOut != null) {
                    mOut.flush();
                    mOut.close();
                }

                mIn = null;
                mOut = null;

                if (mSocket != null) {
                    try {
                        mSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            catch (IOException e) {

                Log.e(TAG, "ReceiverThread: "+ e);
            }
        }

    }


    public void showErrorDialog(String message)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error");
        builder.setCancelable(false);
        builder.setMessage(message);
        builder.setPositiveButton("OK",  new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        builder.create().show();
    }





}