package com.parag.remoteclient;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Intent serviceIntent;
    private boolean isServiceBound;
    private Messenger requestMessenger, receiveMessenger;
    private TextView textView;
    Button btnBind,btnUnbind,btnFetch;

    class MyRandomNumberReceiveHandler extends Handler
    {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what)
            {
                case 0: textView.setText(""+msg.arg1);break;
                default:break;
            }
            super.handleMessage(msg);
        }
    }
    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            requestMessenger = new Messenger(iBinder);
            receiveMessenger = new Messenger(new MyRandomNumberReceiveHandler());
            isServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            requestMessenger = null;
            receiveMessenger = null;
            isServiceBound = false;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView)findViewById(R.id.textview);

        btnBind = (Button)findViewById(R.id.btn_bind);
        btnUnbind = (Button)findViewById(R.id.btn_unbind);
        btnFetch = (Button)findViewById(R.id.btn_get_random_number);

        btnBind.setOnClickListener(this);
        btnUnbind.setOnClickListener(this);
        btnFetch.setOnClickListener(this);

        serviceIntent = new Intent();
        serviceIntent.setComponent(new ComponentName("com.parag.remoteservice","com.parag.remoteservice.RandomNumberService"));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.btn_bind: bind(); break;
            case R.id.btn_unbind: unbind(); break;
            case R.id.btn_get_random_number: fetchRandomNumber();break;
            default: Toast.makeText(this,"something bad happened",Toast.LENGTH_SHORT).show();
        }
    }

    private void bind() {
        bindService(serviceIntent,serviceConnection, Context.BIND_AUTO_CREATE);
        isServiceBound = true;
    }

    private void unbind()
    {
        unbindService(serviceConnection);
        isServiceBound = false;
    }

    private void fetchRandomNumber()
    {
        if(isServiceBound)
        {
            Message message = Message.obtain(null, 0);
            message.replyTo = receiveMessenger;
            try {
                requestMessenger.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}
