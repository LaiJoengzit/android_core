package com.bot.android_usbtocan;

import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.bot.android_usbtocan.USBtoCAN.USBtoCAN;

public class MainActivity extends AppCompatActivity {
    com.bot.android_usbtocan.USBtoCAN.USBtoCAN USBtoCAN;
    static TextView USBcomfirm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        USBcomfirm = (TextView) findViewById(R.id.USBCAN);
        USBcomfirm.setText("null");
        if(USBtoCAN.getDriver()!=null){
        USBtoCAN= new USBtoCAN();
        USBtoCAN.judgeRetval();
        }
    }
    public static void sendMessage(Message msg){
        USBcomfirm.setText(msg.toString());
    }
    public static void sendMessage(String msg){
        USBcomfirm.setText(msg);
    }
}