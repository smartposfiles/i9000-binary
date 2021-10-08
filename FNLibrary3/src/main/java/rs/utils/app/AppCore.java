package rs.utils.app;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import rs.fncore.Const;
import rs.fncore.Errors;
import rs.fncore.FiscalStorage;
import rs.utils.Utils;
import rs.utils.app.MessageQueue.MessageHandler;

/**
 * Класс-хелпер, позволяющий организовать работу с ФН
 *
 * @author amv
 */
public class AppCore extends Application implements ServiceConnection {

    static final Logger mLogger = Logger.getLogger("AppCore");
    private Intent START_INTENT = Const.FISCAL_STORAGE;
    private volatile long mWaitFNTimeoutMs = 0;
    private static final int WAIT_FN_TIMOUT_AFTER_SUSPEND=30*1000;
    private final Object mStorageLockObject = new Object();

    private MessageQueue mQueue;
    volatile private FiscalStorage mStorage;
    volatile boolean mKillAll = false;

    public interface StorageTask {
        void execute(FiscalStorage storage);
    }

    /**
     * Интерфейс для реализации своей работы с ФН
     *
     * @author amv
     */
    public interface ProcessTask {
        /**
         * Вызвается из потока, при исполнении FNOperationTask
         *
         * @param storage - ссылка на FiscalStorage
         * @param task    - экземпляр FNOperationTask который вызвал интерфейс
         * @return - код возврата (константа из Const.Errors )
         */
        int execute(FiscalStorage storage, FNOperaionTask task, Object... args);
    }

    /**
     * Интерфейс, вызываемый при завершении работы 	FNOperationTask
     *
     * @author amv
     */
    public interface ResultTask {
        /**
         * Вызывается из UI-потока
         *
         * @param result -код, который вернул ProcessTask
         */
        void onResult(int result);
    }

    /**
     *
     */
    public static final int MSG_FISCAL_STORAGE_READY = 50000;

    /**
     * Результат выполнения
     *
     * @param <T> - тип параметров результата
     * @author amv
     */
    public static class AsyncResult<T>{
        protected volatile T result=null;
        private Object syncRes = new Object();

        public void setResult(T data){
            synchronized (syncRes){
                result=data;
                syncRes.notifyAll();
            }
        }

        public T get() throws InterruptedException {
            synchronized (syncRes){
                while (result==null) {
                    syncRes.wait();
                }
                return result;
            }
        }
    }

    /**
     * Параллельная задача, отображающая диалог
     *
     * @param <I> - тип параметров задачи
     * @param <R> - тип параметра результата
     * @author amv
     */
    public static abstract class TaskWithDialog<I, R>  {
        private Context mContext;
        private volatile ProgressDialog mDialog;
        private String mDialogTitle;
        private ExecutorService mExecutor;
        private Handler mHandler;
        private AsyncResult<R> mResult;

        public TaskWithDialog(Context context) {
            this(context, null);
        }

        public TaskWithDialog(Context context, String dialogTitle) {
            mContext = context;
            mExecutor = Executors.newSingleThreadExecutor();
            mHandler = new Handler(context.getMainLooper());
            mResult = new AsyncResult<R>();
            mDialogTitle =dialogTitle;
        }

        abstract R doInBackground(I... args);

        public TaskWithDialog<I, R> execute(I... args) {
            mExecutor.execute(() -> {

                //Background work here
                final R res=doInBackground(args);
                mResult.setResult(res);

                //UI Thread work here
                mHandler.post(() -> onPostExecute(res));
            });
            return this;
        }

        public R get() throws InterruptedException {
            return mResult.get();
        }

        protected void onProgressUpdate(String... values) {
            if (mDialog==null){
                mDialog = new ProgressDialog(mContext, android.R.style.Theme_Holo_Light_Dialog);
                mDialog.setIndeterminate(true);
                mDialog.setCancelable(false);
                if (mDialogTitle !=null) mDialog.setTitle(mDialogTitle);
                mDialog.setCanceledOnTouchOutside(false);
                mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                mDialog.show();
            }
            mDialog.setMessage(values[0]);
        }

        protected final void publishProgress(String... values) {
            mHandler.post(() -> onProgressUpdate(values));
        }

        protected void onPostExecute(R result) {
            if (mDialog != null) {
                mDialog.dismiss();
            }
        }
    }

    /**
     * @author amv
     */
    public abstract class FNOperaionTask extends TaskWithDialog<Object, Integer> {
        private int mMessage;

        public FNOperaionTask(Context context, int message) {
            super(context);
            mMessage = message;
        }

        protected Object getResultData() {
            return null;
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (mMessage != 0)
                mQueue.sendMessage(mMessage, result.intValue(), getResultData());
            super.onPostExecute(result);
        }

        /**
         * Показать (обновить) диалог с сообщением
         *
         * @param v
         */
        public void showProgress(String v) {
            publishProgress(v);
        }
    }

    public AppCore() {
    }

    public void onCreate() {
        super.onCreate();
        mQueue = new MessageQueue(this);
    }

    /**
     * Проинициализировать сервис фискального накопителя
     * по умолчанию - ждать, когда появится ФН
     *
     * @return
     */
    public boolean initialize() {
        return initialize(0);
    }

    /**
     * Проинициализировать сервис фискального накопителя c таймаутом ожидания ФН
     *
     * @param waitFNTimeoutMs - время ожидания FN в мс, если 0 - то ждать бесконечно
     * @return
     */
    public boolean initialize(long waitFNTimeoutMs) {
        mKillAll = false;
        mWaitFNTimeoutMs = waitFNTimeoutMs;
        if (mStorage == null) {
            if (START_INTENT == Const.FISCAL_STORAGE) {
                if (!Utils.startService(this)) return false;
            }

            return bindService(START_INTENT, this, BIND_AUTO_CREATE);
        }
        return true;
    }

    public void deinitialize() {
        mKillAll = true;

        if (mStorage != null) {
            try {
                unbindService(this);
            } catch (Exception e) {
                mLogger.log(Level.SEVERE, "exception", e);
            } finally {
                mStorage = null;
            }
        }

    }

    protected void setCoreIntent(Intent i) {
        START_INTENT = i;
    }

    public void runOnUI(Runnable r) {
        mQueue.post(r);
    }

    /**
     * @param h
     */
    public void registerHandler(MessageHandler h) {
        mQueue.registerHandler(h);
    }

    public void removeHandler(MessageHandler h) {
        mQueue.removeHandler(h);
    }

    public void sendMessage(int msg, Object payload) {
        mQueue.sendMessage(msg, payload);
    }

    @Override
    public void onServiceConnected(ComponentName arg0, IBinder binder) {
            mLogger.log(Level.FINE,"Service onServiceConnected");
            mStorage = FiscalStorage.Stub.asInterface(binder);
            new FNOperaionTask(this, MSG_FISCAL_STORAGE_READY) {
                private int R = Errors.SYSTEM_ERROR;

                @Override
                protected Integer doInBackground(Object... args) {
                    try {
                        R = mStorage.checkFN(mWaitFNTimeoutMs);

                        synchronized (mStorageLockObject) {
                            mStorageLockObject.notifyAll();
                        }

                    } catch (Exception e) {
                        mLogger.log(Level.SEVERE, "exception", e);
                        R = Errors.SYSTEM_ERROR;
                    } finally {
                        return R;
                    }
                }

                protected Object getResultData() {
                    return R;
                }
            }.execute();
    }

    @Override
    public void onServiceDisconnected(ComponentName arg0) {

        mLogger.log(Level.FINE, "Service onServiceDisconnected");
        mStorage = null;
        try {
            unbindService(this);
        } catch (Exception e) {
            mLogger.log(Level.SEVERE, "exception", e);
        }
    }

    protected FiscalStorage getStorage() {
        return mStorage;
    }

    public FNOperaionTask newTask(Context context, final ProcessTask r, final ResultTask onResult) {

        return new FNOperaionTask(context, 0) {
            @Override
            protected Integer doInBackground(Object... args) {
                if(mStorage == null) {
                    mLogger.log(Level.INFO, "Service lost?, trying reconnect");
                    if (!initialize()) {
                        mLogger.log(Level.SEVERE, "Service lost, reconnect failed, storage=" + mStorage);
                        return Errors.SYSTEM_ERROR;
                    }

                    try {
                        synchronized (mStorageLockObject) {
                            while (mStorage == null || !mStorage.isReady() && !mKillAll) {
                                mLogger.log(Level.INFO, "waiting for storage reappear ... ");
                                mStorageLockObject.wait(1000);
                            }
                        }
                    } catch (InterruptedException | RemoteException e) {
                        mLogger.log(Level.SEVERE, "Service lost wait exception:", e);
                    }

                    if (mStorage == null) {
                        mLogger.log(Level.FINE,"Service lost, reconnect failed wait for storage");
                        return Errors.SYSTEM_ERROR;
                    }
                }

                return r.execute(mStorage, this, args);
            }

            @Override
            protected void onPostExecute(Integer result) {
                super.onPostExecute(result);
                if (onResult != null)
                    onResult.onResult(result.intValue());
            }
        };
    }

    public void invokeOnStorage(final StorageTask r) {
        new Thread() {
            @Override
            public void run() {
                r.execute(getStorage());
            }
        }.start();
    }

    @Override
    public void onBindingDied(ComponentName name) {
        mLogger.log(Level.SEVERE, "Service onBindingDied");
    }

    @Override
    public void onNullBinding(ComponentName name) {
        mLogger.log(Level.SEVERE, "Service onNullBinding");
    }

}
