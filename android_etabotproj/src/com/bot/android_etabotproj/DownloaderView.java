package com.bot.android_etabotproj;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import org.ros.android.MessageCallable;
import org.ros.internal.message.MessageGenerationTemplate;
import org.ros.internal.message.MessageInterfaceClassProvider;
import org.ros.message.MessageDefinitionProvider;
import org.ros.message.MessageFactory;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeMain;
import org.ros.node.topic.Subscriber;
/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class DownloaderView<T> extends android.support.v7.widget.AppCompatTextView implements NodeMain {
    private String topicName;
    private String GraphNameString ="android/DownloaderView";
    private String messageType;
    private MessageCallable<String, T> callable;

    public DownloaderView(Context context) {
        super(context);
    }

    public DownloaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DownloaderView(Context context, AttributeSet attrs, int defStyle) {
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
        return GraphName.of(GraphNameString);
    }

    //allow to set new a NodeName to avoid collapsing
    public void setDefaultNodeName(String graphNameString) {
        this.GraphNameString = graphNameString;
    }

    @Override
    public void onStart(ConnectedNode connectedNode) {
        Subscriber<T> subscriber = connectedNode.newSubscriber(topicName, messageType);
        subscriber.addMessageListener(message -> {
            if (callable != null) {
                post(() -> setText(callable.call(message)));
                //Log.i("DownloaderView","Downloaded from " + topicName);
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
