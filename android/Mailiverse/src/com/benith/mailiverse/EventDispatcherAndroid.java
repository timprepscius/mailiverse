package com.benith.mailiverse;

import java.util.Timer;
import java.util.TimerTask;

import android.os.Handler;
import android.os.Message;

import mail.client.EventDispatcher;

public class EventDispatcherAndroid extends EventDispatcher {

	Timer timer;
	
	public EventDispatcherAndroid ()
	{
		setupTimer();
	}

	public void setupTimer ()
	{
		final Handler h = new Handler(new Handler.Callback() {
	
			@Override
			public boolean handleMessage(Message msg) {
				dispatchEvents();
				return false;
			}
		});
	
		class Messager extends TimerTask {
			public void run() {
				h.sendEmptyMessage(0);
			}
		};
		
		timer = new Timer();
		timer.schedule(new Messager(), 0, 500);
	}
}
