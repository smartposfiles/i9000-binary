package com.example.fn;

import com.example.fncoresample.R;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import rs.utils.app.AppCore;

public class Core extends AppCore implements Application.ActivityLifecycleCallbacks {

	private static Core _instance;
	private long APP_START;
	public static Core getInstance() { return _instance; }
	private ArrayAdapter<String> LOGGER;
	
	public Core() {
	}
	@Override
	public void onCreate() {
		super.onCreate();
		APP_START  = System.currentTimeMillis();
		registerActivityLifecycleCallbacks(this);
		_instance = this;
	}
	
	/**
	 * Добавить запись в журнал
	 * @param s
	 */
	public void addLogRecord(final String s) {
		runOnUI(new Runnable() {
			@Override
			public void run() {
				LOGGER.add(String.format("%.3f\t%s", (System.currentTimeMillis()-APP_START)/1000f,s));
			}
		});
	}
	
	public ListAdapter getLogger() { return LOGGER; }
	@Override
	public void onActivityCreated(Activity activity, Bundle arg1) {
		if(LOGGER == null)
			LOGGER = new ArrayAdapter<String>(activity, R.layout.log_row);
	}
	@Override
	public void onActivityDestroyed(Activity arg0) {
	}
	@Override
	public void onActivityPaused(Activity arg0) {
	}
	@Override
	public void onActivityResumed(Activity arg0) {
	}
	@Override
	public void onActivitySaveInstanceState(Activity arg0, Bundle arg1) {
	}
	@Override
	public void onActivityStarted(Activity arg0) {
	}
	@Override
	public void onActivityStopped(Activity arg0) {
	}
	@Override
	public void onBindingDied(ComponentName name) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onNullBinding(ComponentName name) {
		// TODO Auto-generated method stub
		
	}
}
