package com.benith.mailiverse;

import core.callback.CallbackDefault;
import mail.client.Events;
import mail.client.model.Conversation;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

public class FolderView extends ViewContainer
{
	ListView list;
	MainActivity context;
	ConversationListAdapter conversationListAdapter;
	
	public FolderView(MainActivity context)
	{
		this.context = context;
		
		LayoutInflater inflater = LayoutInflater.from(context);
		view = inflater.inflate(R.layout.folder, null);

		conversationListAdapter = new ConversationListAdapter(view.getContext());
		list = (ListView) view.findViewById(R.id.conversationList);
		list.setOnItemClickListener(listener);
		list.setAdapter(conversationListAdapter);
		
		Button backButton = (Button) view.findViewById(R.id.folderRevealButton);
		backButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				FolderView.this.context.getSlidingMenu().showMenu(true);
			} 
		});
		
		setupEvents();
	}
	
	public void setupEvents ()
	{
		Application.getInstance().state.client.getMaster().getEventPropagator().add(
			Events.Initialized, this, new CallbackDefault() {
				public void onSuccess(Object... arguments) throws Exception {
					chooseInitialFolder();
				}
			}
		);
		
	}

	public void chooseInitialFolder ()
	{
		conversationListAdapter.setFolder(
			Application.getInstance().state.client.getMaster().getIndexer().getInbox()
		);
	}
	

	public View getView ()
	{
		return view;
	}
	
	OnItemClickListener listener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View v, int position, long id) 
		{
			Application.getInstance().state.conversation = 
				(Conversation)conversationListAdapter.getItem(position);
			
			Intent intent = new Intent(context, ConversationActivity.class);
			context.startActivity(intent);
		}
		
	};
}
