package com.bot.android_usbtocan.USBtoCAN;

import android.app.Application;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Message;
import android.widget.Toast;

import com.bot.android_usbtocan.MainActivity;

import cn.wch.ch34xuartdriver.CH34xUARTDriver;

import static com.bot.android_usbtocan.USBtoCAN.USB.driver;

/**
 * Created by Dear yg on 2019/11/28 23:30.
 */
public class USBtoCAN extends Application {
    int retval;
    public USBtoCAN(){
        retval = driver.ResumeUsbList();
    }
    public static CH34xUARTDriver getDriver() {
        return driver;
    }
    public int judgeRetval() {
        if (retval == -1) {
            //System.out.println("串口错误1");
            MainActivity.sendMessage("串口错误1");
            return  1; }
        else if (retval == 0) {
            if (!USB.driver.UartInit()){
                //System.out.println("串口错误2");
                MainActivity.sendMessage("串口错误2");
                return  2;}
            else if (USB.driver.SetConfig(9600, (byte) 1, (byte) 8, (byte) 0, (byte) 0))//波特率 停止位 数据位 parity flowControl
            {
                //System.out.println("串口成功打开");
                Message msg = new Message();
                msg.obj = "串口成功打开";
                MainActivity.sendMessage(msg);
                //flag = 1;
                return  0;
            }
            else {
                //System.out.println("串口错误3");
                MainActivity.sendMessage("串口错误3");
                return  3;}
        }
        return -1;
    }
}
