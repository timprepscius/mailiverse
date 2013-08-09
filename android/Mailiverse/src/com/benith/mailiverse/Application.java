package com.benith.mailiverse;

import java.util.Timer;
import java.util.TimerTask;

import android.os.Handler;
import android.os.Message;

import mail.client.Client;
import mail.client.model.Conversation;
import core.util.Environment;

public class Application 
{
	protected static Application instance = null;

	public static Application getInstance ()
	{
		if (instance == null)
			instance = new Application();

		return instance;
	}

	protected Application()
	{
		System.loadLibrary("Mailiverse");
	}

	public static class RunState 
	{
		public String name;
		public Environment environment;
		public Client client;
		
		public Conversation conversation;
	};

	RunState state = new RunState();

}
