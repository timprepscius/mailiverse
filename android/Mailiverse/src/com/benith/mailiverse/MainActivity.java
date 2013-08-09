package com.benith.mailiverse;

import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.app.SlidingActivity;

import core.util.HttpDelegateJava;
import core.util.LogOut;

import android.os.Bundle;
import android.view.Menu;
import mail.client.Client;
import mail.client.EventDispatcher;

public class MainActivity extends SlidingActivity {

	static LogOut log = new LogOut(MainActivity.class);
	
	RevealFoldersView revealFoldersView;
	FolderView folderView;
	
	public Client client;
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		Application application = Application.getInstance();

		try
		{
			application.state.client = Client.start(
				application.state.environment, 
				application.state.name, 
				new HttpDelegateAndroid(), 
				new EventDispatcherAndroid()
			);
		}
		catch (Exception e)
		{
			log.exception(e);
		}
		
		folderView = new FolderView(this);
		revealFoldersView = new RevealFoldersView(this);
		setContentView(folderView.getView());
		setBehindContentView(revealFoldersView.getView());
		getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
	}		

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

}
