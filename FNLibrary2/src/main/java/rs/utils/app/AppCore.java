package rs.utils.app;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import rs.fncore.Const;
import rs.fncore.Const.Errors;
import rs.fncore.FiscalStorage;
import rs.utils.Utils;
import rs.utils.app.MessageQueue.MessageHandler;

/**
 * Класс-хелпер, позволяющий организовать работу с ФН
 *
 * @author nick
 */
@SuppressLint("NewApi")
@SuppressWarnings("deprecation")
public class AppCore extends Application implements ServiceConnection {

    private final String TAG = this.getClass().getName();
    private Intent START_INTENT = Const.FISCAL_STORAGE;
    private Object _storageLockObject = new Object();
    private volatile boolean _dontWaitForFN = false;

    private MessageQueue _queue;
    volatile private FiscalStorage _storage;
    volatile boolean _killAll = false;

    public interface StorageTask {
        void execute(FiscalStorage storage);
    }

    /**
     * Интерфейс для реализации своей работы с ФН
     *
     * @author nick
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
     * @author nick
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
     * Параллельная задача, отображающая диалог
     *
     * @param <I> - тип параметров задачи
     * @param <R> - тип параметра результата
     * @author nick
     */
    public static abstract class TaskWithDialog<I, R> extends AsyncTask<I, String, R> {
        private Context _context;
        private ProgressDialog _dialog;

        public TaskWithDialog(Context context) {
            _context = context;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            if (_dialog == null) {
                _dialog = new ProgressDialog(_context, android.R.style.Theme_Holo_Light_Dialog);
                _dialog.setIndeterminate(true);
                _dialog.setCancelable(false);
                _dialog.setCanceledOnTouchOutside(false);
                _dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                _dialog.show();
            }
            _dialog.setMessage(values[0]);
        }

        @Override
        protected void onPostExecute(R result) {
            if (_dialog != null)
                _dialog.dismiss();
        }
    }

    /**
     * @author nick
     */
    public abstract class FNOperaionTask extends TaskWithDialog<Object, Integer> {
        private int _message;

        public FNOperaionTask(Context context, int message) {
            super(context);
            _message = message;
        }

        protected Object getResultData() {
            return null;
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (_message != 0)
                _queue.sendMessage(_message, result.intValue(), getResultData());
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
        _queue = new MessageQueue(this);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Проинициализировать сервис фискального накопителя
     * по умолчанию - ждать, когда появится ФН
     * @return
     */
    public boolean initialize() {
        return initialize(false);
    }

    /**
     * Проинициализировать сервис фискального накопителя
     * @param dontWaitForFN - true не ждать, когда появится ФН, проинициализировать ,как есть,
     *                        false - ждать, когда появится ФН
     * @return
     */
    public boolean initialize(boolean dontWaitForFN) {
        synchronized (_storageLockObject) {
            _killAll = false;
            _dontWaitForFN = dontWaitForFN;
            if (_storage == null) {
                if (START_INTENT == Const.FISCAL_STORAGE) {
                    if (!Utils.startService(this)) return false;
                }

                return bindService(START_INTENT, this, BIND_AUTO_CREATE);
            }
            return true;
        }
    }

    public void deinitialize() {
        _killAll = true;

        synchronized (_storageLockObject) {
            if (_storage != null) {
                try {
                    unbindService(this);
                } catch (Exception e) {
                    Log.e(TAG, "exception", e);
                }
                finally {
                    _storage = null;
                }
            }
        }
    }

    protected void setCoreIntent(Intent i) {
        START_INTENT = i;
    }

    public void runOnUI(Runnable r) {
        _queue.post(r);
    }

    /**
     * @param h
     */
    public void registerHandler(MessageHandler h) {
        _queue.registerHandler(h);
    }

    public void removeHandler(MessageHandler h) {
        _queue.removeHandler(h);
    }

    public void sendMessage(int msg, Object payload) {
        _queue.sendMessage(msg, payload);
    }

    protected void onConnected(int result) {
        synchronized (_storageLockObject) {
            _storageLockObject.notifyAll();
        }
    }

    @Override
    public void onServiceConnected(ComponentName arg0, IBinder binder) {
        synchronized (_storageLockObject) {
            Log.d(TAG, "Service onServiceConnected");
            _storage = FiscalStorage.Stub.asInterface(binder);
            new FNOperaionTask(this, MSG_FISCAL_STORAGE_READY) {
                private int R = Errors.SYSTEM_ERROR;

                @Override
                protected Integer doInBackground(Object... args) {
                    synchronized (_storageLockObject) {
                        try {
                            if (_dontWaitForFN) {
                                R = _storage.init();
                            }
                            else{
                                do{
                                    R = _storage.init();
                                    try{
                                        Thread.sleep(500);
                                    }
                                    catch (InterruptedException e){
                                    }
                                }while((R==Errors.DEVICE_ABSEND || !_storage.isReady()) && ! _killAll);
                            }
                            onConnected(R);
                        } catch (Exception e) {
                            Log.e(TAG, "exception", e);
                            R = Errors.SYSTEM_ERROR;
                        } finally {
                            return R;
                        }
                    }
                }

                protected Object getResultData() {
                    return R;
                }
            }.execute();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName arg0) {
        synchronized (_storageLockObject) {
            Log.d(TAG, "Service onServiceDisconnected");
            _storage = null;
            try {
                unbindService(this);
            } catch (Exception e) {
                Log.e(TAG, "exception", e);
            }
        }
    }

    protected FiscalStorage getStorage() {
        synchronized (_storageLockObject) {
            return _storage;
        }
    }

    public FNOperaionTask newTask(Context context, final ProcessTask r, final ResultTask onResult) {

        return new FNOperaionTask(context, 0) {
            @Override
            protected Integer doInBackground(Object... args) {
                synchronized (_storageLockObject) {
                    if (_storage == null) {
                        Log.d(TAG, "Service lost?, trying reconnect");
                        if (!initialize()) {
                            Log.d(TAG, "Service lost, reconnect failed, _storage=" + _storage);
                            return Errors.SYSTEM_ERROR;
                        }

                        try {
                            while (_storage == null || !_storage.isReady() && !_killAll) {
                                Log.d(TAG, "waiting for storage ... ");
                                _storageLockObject.wait(1000);
                            }
                        } catch (InterruptedException|RemoteException e) {
                            Log.e(TAG, "Service lost wait exception:", e);
                        }

                        if (_storage == null) {
                            Log.d(TAG, "Service lost, reconnect failed wait for _storage");
                            return Errors.SYSTEM_ERROR;
                        }
                    }
                    return r.execute(_storage, this, args);
                }
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
        Log.e(TAG, "Service onBindingDied");
    }

    @Override
    public void onNullBinding(ComponentName name) {
        Log.e(TAG, "Service onNullBinding");
    }

}
