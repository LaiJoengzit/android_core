package com.bot.android_usbtocan.USBtoCAN;

import android.app.Application;

import cn.wch.ch34xuartdriver.CH34xUARTDriver;

/**
 * Created by Dear yg on 2019/11/28 23:30.
 */
public class USB extends Application {
    public static CH34xUARTDriver driver;//帮助类的生命周期与整个应用程序的生命周期是相同的
}