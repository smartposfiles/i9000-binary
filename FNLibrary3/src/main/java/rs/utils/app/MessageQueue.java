package rs.utils.app;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class MessageQueue implements Handler.Callback {

    public static interface MessageHandler {
        public boolean onMessage(Message msg);
    }

    private Handler mHandler;
    private Set<MessageHandler> HANDLERS = Collections.synchronizedSet(new HashSet<MessageHandler>());

    public MessageQueue(Context context) {
        mHandler = new Handler(context.getMainLooper(), this);
    }

    @Override
    public boolean handleMessage(Message msg) {
        synchronized (HANDLERS) {
            for (MessageHandler h : HANDLERS)
                if (h.onMessage(msg)) break;
        }
        return true;
    }

    public void registerHandler(MessageHandler h) {
        HANDLERS.add(h);
    }

    public void removeHandler(MessageHandler h) {
        HANDLERS.remove(h);
    }

    public void sendMessage(int message, int arg1, int arg2, Object payload) {
        Message m = mHandler.obtainMessage(message);
        m.arg1 = arg1;
        m.arg2 = arg2;
        m.obj = payload;
        mHandler.sendMessage(m);

    }

    public void sendMessage(int message, int arg1, Object payload) {
        sendMessage(message, arg1, 0, payload);
    }

    public void sendMessage(int message, int arg1, int arg2) {
        sendMessage(message, arg1, arg2, null);
    }

    public void sendMessage(int message, Object payload) {
        sendMessage(message, 0, 0, payload);
    }

    public void sendMessage(int message) {
        sendMessage(message, 0, 0, null);
    }

    public void post(Runnable r) {
        mHandler.post(r);
    }
}
