package rs.utils.app;

import android.annotation.SuppressLint;
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
import rs.utils.app.MessageQueue.MessageHandler;

/**
 * Класс-хелпер, позволяющий организовать работу с ФН
 * @author nick
 *
 */
@SuppressLint("NewApi")
@SuppressWarnings("deprecation")
public class AppCore extends Application implements ServiceConnection {

	private Intent START_INTENT = Const.FISCAL_STORAGE;
	public static final int NO_CONNECTION =  0;
	public static final int CONNECTING = 1;
	public static final int CONNECTED = 2;
	public static interface StorageTask {
		public void execute(FiscalStorage storage);
	}
	
	/**
	 * Интерфейс для реализации своей работы с ФН
	 * @author nick
	 *
	 */
	public static interface ProcessTask {
		/**
		 * Вызвается из потока, при исполнении FNOperationTask
		 * @param storage - ссылка на FiscalStorage
		 * @param task - экземпляр FNOperationTask который вызвал интерфейс
		 * @return - код возврата (константа из Const.Errors )
		 */
		public int execute(FiscalStorage storage, FNOperaionTask task,Object...args);
	}
	/**
	 * Интерфейс, вызываемый при завершении работы 	FNOperationTask
	 * @author nick
	 *
	 */
	public static interface ResultTask {
		/**
		 * Вызывается из UI-потока
		 * @param result -код, который вернул ProcessTask 
		 */
		public void onResult(int result);
	}
	/**
	 * 
	 */
	public static final int MSG_FISCAL_STORAGE_READY = 50000;
	/**
	 * Параллельная задача, отображающая диалог
	 * @author nick
	 *
	 * @param <I> - тип параметров задачи
	 * @param <R> - тип параметра результата
	 */
	public static abstract class TaskWithDialog<I,R> extends AsyncTask<I, String, R>  {
		private Context _context;
		private ProgressDialog _dialog;
		public TaskWithDialog(Context context) {
			_context = context;
		}
		@Override
		protected void onProgressUpdate(String... values) {
			if(_dialog == null) {
				_dialog = new ProgressDialog(_context,android.R.style.Theme_Holo_Light_Dialog);
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
			if(_dialog != null)
				_dialog.dismiss();
		}
	}
	/**
	 * 
	 * @author nick
	 *
	 */
	public abstract class FNOperaionTask extends TaskWithDialog<Object, Integer> {
		private int _message;
		public FNOperaionTask(Context context, int message) { 
			super(context);
			_message = message;
		}
		protected Object getResultData() { return null; }
		@Override
		protected void onPostExecute(Integer result) {
			if(_message != 0)
				_queue.sendMessage(_message, result.intValue(),getResultData());
			super.onPostExecute(result);
		}
		/**
		 * Показать (обновить) диалог с сообщением
		 * @param v
		 */
		public void showProgress(String v) {
			publishProgress(v);
		}
	}
	private MessageQueue _queue;
	private FiscalStorage _storage;

	public AppCore() {
	}
	public void onCreate() {
		super.onCreate();
		_queue = new MessageQueue(this);
	};
	/**
	 * Проинициализировать сервис фискального накопителя
	 * @return
	 */
	public int initialize() {
		if(_storage == null) {
			if(START_INTENT == Const.FISCAL_STORAGE) {
				ComponentName cn = null;
				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
					cn = startForegroundService(Const.FISCAL_STORAGE);
				else 
					cn = startService(Const.FISCAL_STORAGE);
				if(cn== null) return NO_CONNECTION;
			}
			return bindService(START_INTENT, this, BIND_AUTO_CREATE) == true ? CONNECTING: NO_CONNECTION;
		}
		return CONNECTED;
	}
	protected void setCoreIntent(Intent i) {
		START_INTENT = i;
	}
	public void runOnUI(Runnable r) {
		_queue.post(r);
	}
	/** 
	 * 
	 * @param h
	 */
	public void registerHandler(MessageHandler h) {
		_queue.registerHandler(h);
	}
	public void removeHandler(MessageHandler h) {
		_queue.removeHandler(h);
	}
	public void sendMessage(int msg, Object payload) {
		_queue.sendMessage(msg,payload);
	}
	protected void onConnected(int result) {} 
	@Override
	public void onServiceConnected(ComponentName arg0, IBinder binder) {
		_storage = FiscalStorage.Stub.asInterface(binder);
		_state = 1;
		new FNOperaionTask(this,MSG_FISCAL_STORAGE_READY) {
			private int R = Errors.SYSTEM_ERROR;
			@Override
			protected Integer doInBackground(Object... args) {
				try {
					R = _storage.init();
					onConnected(R);
				} catch(RemoteException re) {
					R =  Errors.SYSTEM_ERROR;
				}
				return R;
			}
			protected Object getResultData() {
				return R;
			}; 
		}.execute(); 
	}
	private int _state  = 0;
	@Override
	public void onServiceDisconnected(ComponentName arg0) {
		_storage = null;
	}
	protected FiscalStorage getStorage() {
		if(_storage == null && _state != 0)
			bindService(START_INTENT, this, BIND_AUTO_CREATE);
		return _storage; 
	}
	
	public void deinitialize() {
		if(_storage != null) try {
			unbindService(this);
		} catch(Exception e) { }
	}
	public FNOperaionTask newTask(Context context, final ProcessTask r, final ResultTask onResult ) {
		return new FNOperaionTask(context,0) {
			@Override
			protected Integer doInBackground(Object... args) {
				if(_storage == null) {
					Log.d("FNCORE2", "Service lost?");
					return Errors.SYSTEM_ERROR;
					
				}
				return r.execute(_storage, this,args);
			}
			@Override
			protected void onPostExecute(Integer result) {
				super.onPostExecute(result);
				if(onResult != null)
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
		Log.d("FNCORE2","Bind died");
		
	}
	@Override
	public void onNullBinding(ComponentName name) {
		// TODO Auto-generated method stub
		
	}

}
