/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

/*
 * edited from: android_tutorial_pubsub:@author damonkohler@google.com (Damon Kohler)
 */
package com.bot.android_etabotproj;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import org.ros.android.RosActivity;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import std_msgs.String;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity2 extends RosActivity {

    private DownloaderView<String> downloaderView;

    private Uploader uploader;

    public MainActivity2() {
        // The RosActivity constructor configures the notification title and ticker
        // messages.
        super("Pubsub Tutorial", "Pubsub Tutorial");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        downloaderView = findViewById(R.id.ROStext1);
        downloaderView.setTopicName("chatter");
        downloaderView.setMessageType(String._TYPE);
        downloaderView.setMessageToStringCallable(String::getData);
    }
    @Override
    public void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new java.lang.String[]{ACCESS_COARSE_LOCATION}, 11);
            ActivityCompat.requestPermissions(this, new java.lang.String[]{ACCESS_FINE_LOCATION}, 12);
            finish();
        }
    }

    @Override
    protected void init(NodeMainExecutor nodeMainExecutor) {
        uploader = new Uploader();
        // At this point, the user has already been prompted to either enter the URI
        // of a master to use or to start a master locally.

        // The user can easily use the selected ROS Hostname in the master chooser
        // activity.
        NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(getRosHostname());
        nodeConfiguration.setMasterUri(getMasterUri());
        nodeMainExecutor.execute(uploader, nodeConfiguration);
        // The RosTextView is also a NodeMain that must be executed in order to
        // start displaying incoming messages.
        nodeMainExecutor.execute(downloaderView, nodeConfiguration);
    }

    public void setDownloaderView(DownloaderView<String> downloaderView) {
        this.downloaderView = downloaderView;
    }
}
