//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//
//

package com.bot.android_etabotproj;


import android.util.Log;

import org.ros.concurrent.CancellableLoop;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;

public class Uploader2 extends AbstractNodeMain {
    private String topic_name;

    public Uploader2() {
        this.topic_name = "chatter";
    }

    public Uploader2(String topic) {
        this.topic_name = topic;
    }

    public GraphName getDefaultNodeName() {
        return GraphName.of("rosjava_tutorial_pubsub/talker");
    }

    public void onStart(ConnectedNode connectedNode) {
        final Publisher<std_msgs.String> publisher = connectedNode.newPublisher(this.topic_name, "std_msgs/String");
        connectedNode.executeCancellableLoop(new CancellableLoop() {
            private int sequenceNumber;
            protected void setup() {
                this.sequenceNumber = 0;
            }
            protected void loop() throws InterruptedException {
                std_msgs.String str = (std_msgs.String)publisher.newMessage();
                str.setData("Hello world! " + this.sequenceNumber);
                publisher.publish(str);
                Log.i("Uploader","Uploaded to " + topic_name);
                ++this.sequenceNumber;
                Thread.sleep(1000L);
            }
        });
    }
}