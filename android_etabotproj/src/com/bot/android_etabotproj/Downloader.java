package com.bot.android_etabotproj;

import android.content.Context;
import android.util.AttributeSet;

import org.ros.android.MessageCallable;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeMain;
import org.ros.node.topic.Subscriber;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class Downloader<T, S> implements NodeMain {
    private String topicName;
    private String GraphNameString ="android/DownloaderView";
    private String messageType;
    private MessageCallable<T, S> callable;
    private T output;

    public Downloader() {
        this.topicName = "chatter";
    }

    public Downloader(String topic) {
        this.topicName = topic;
    }

    public Downloader(String topic, String DefaultNodeName) {
        this.topicName = topic;
        this.GraphNameString = DefaultNodeName;
    }

    public Downloader(String topic, String DefaultNodeName, String messageType) {
        this.topicName = topic;
        this.GraphNameString = DefaultNodeName;
        this.messageType = messageType;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public void setMessageToStringCallable(MessageCallable<T, S> callable) {
        this.callable = callable;
    }

    //allow to set new a NodeName to avoid collapsing
    public void setDefaultNodeName(String graphNameString) {
        this.GraphNameString = graphNameString;
    }

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of(GraphNameString);
    }

    public T getOutput(){return this.output;}

    @Override
    public void onStart(ConnectedNode connectedNode) {
        Subscriber<S> subscriber = connectedNode.newSubscriber(topicName, messageType);
        subscriber.addMessageListener(message -> {
            if (callable != null) {
                output = callable.call(message);
                //Log.i("DownloaderView","Downloaded from " + topicName);
            } else {
                output = null;
            }
        });
    }

    @Override
    public void onShutdown(Node node) {
    }

    @Override
    public void onShutdownComplete(Node node) {
    }

    @Override
    public void onError(Node node, Throwable throwable) {
    }

    public MessageCallable<T, S> getCallable() {
        return callable;
    }

    public void setCallable(MessageCallable<T, S> callable) {
        this.callable = callable;
    }
}
