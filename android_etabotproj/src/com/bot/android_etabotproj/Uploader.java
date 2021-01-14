//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//
//

package com.bot.android_etabotproj;


import android.util.Log;

import org.ros.android.MessageCallable;
import org.ros.concurrent.CancellableLoop;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;
import org.ros.time.WallTimeProvider;

import geometry_msgs.Vector3Stamped;

public class Uploader<T,S> extends AbstractNodeMain {
    private String topicName;
    private String GraphNameString ="android/Uploader";
    T vecS;
    private int loopPeriod;
    private int Seq;
    private S injector;
    private String messageType;
    private MessageCallable<T, S> callable;
    private boolean Switch = false;
    private boolean vecOn = false;

    public Uploader() {
        this.topicName = "chatter";
    }

    public Uploader(String topic) {
        this.topicName = topic;
    }

    public Uploader(String topic, String DefaultNodeName) {
        this.topicName = topic;
        this.GraphNameString = DefaultNodeName;
    }

    public Uploader(String topic, String DefaultNodeName, String messageType) {
        this.topicName = topic;
        this.GraphNameString = DefaultNodeName;
        this.messageType = messageType;
    }

    public GraphName getDefaultNodeName() {
        return GraphName.of(GraphNameString);
    }

    public void on(){this.Switch = true;}

    public boolean getvecOn(){return vecOn;}

    public T getVecS() {
        return vecS;
    }

    public int getSeq() {
        return Seq;
    }

    public int addSeq() {
        return Seq++;
    }

    public void setLoopPeriod(int time){this.loopPeriod=time;}

    //allow to set a new NodeName to avoid collapsing
    public void setDefaultNodeName(String graphNameString) {
        this.GraphNameString = graphNameString;
    }

    public void setMessageType(String messageType){this.messageType = messageType;}

    public void setMessageToStringCallable(MessageCallable<T, S> callable) {
        this.callable = callable;
    }

    public void setInjector(S injector) {
        this.injector = injector;
    }

    public void setTopicName(String topicName) { this.topicName = topicName; }

    public void onStart(ConnectedNode connectedNode) {
        final Publisher<T> publisher = connectedNode.newPublisher(this.topicName, messageType);
        vecS= connectedNode.getTopicMessageFactory().newFromType(messageType);
        Log.i("Publisher","vecS online");
        vecOn=true;
        connectedNode.executeCancellableLoop(new CancellableLoop() {
            protected void setup() {
                Seq = 0;
            }
            protected void loop() throws InterruptedException {
                if (injector != null && callable != null && Switch) {
                    vecS = callable.call(injector);
                    publisher.publish(vecS);
                    //Log.i("Uploader", "Uploaded to " + topicName);
                }
                Thread.sleep(loopPeriod);
            }
        });
    }
}