package com.bot.android_etabotproj;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import org.ros.android.MessageCallable;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeMain;
import org.ros.node.topic.Subscriber;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class Downloader2<T> extends android.support.v7.widget.AppCompatTextView implements NodeMain {
    private String topicName;
    private String messageType;
    private MessageCallable<String, T> callable;

    public Downloader2(Context context) {
        super(context);
    }

    public Downloader2(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Downloader2(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public void setMessageToStringCallable(MessageCallable<String, T> callable) {
        this.callable = callable;
    }

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("android_gingerbread/DownloaderView");
    }

    @Override
    public void onStart(ConnectedNode connectedNode) {
        Subscriber<T> subscriber = connectedNode.newSubscriber(topicName, messageType);
        subscriber.addMessageListener(message -> {
            if (callable != null) {
                post(() -> setText(callable.call(message)));
                Log.i("DownloaderView","Downloaded");
            } else {
                post(() -> setText(message.toString()));
            }
            postInvalidate();
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

    public MessageCallable<String, T> getCallable() {
        return callable;
    }

    public void setCallable(MessageCallable<String, T> callable) {
        this.callable = callable;
    }
}
