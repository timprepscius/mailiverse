package com.benith.mailiverse;

import java.util.ArrayList;
import java.util.List;

import core.callback.CallbackDefault;
import core.util.Arrays;
import core.util.Strings;

import mail.client.Events;
import mail.client.model.Conversation;
import mail.client.model.Folder;
import mail.client.model.FolderSet;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

public class ConversationListAdapter extends BaseAdapter
{
	Object NOTLOADED = new Object();
	
	Context context;
	FolderSet folder;
	List<Conversation> buffer;
	String filter;
	
	public ConversationListAdapter (Context context)
	{
		this.context = context;

		Application.getInstance().state.client.getMaster().getEventPropagator().add(
			Events.LoadConversation, this, 
			new CallbackDefault () {
				public void onSuccess(Object... arguments) throws Exception {
					updateData();
				}
			}
		);

		Application.getInstance().state.client.getMaster().getEventPropagator().add(
			Events.LoadFolderPart, this, 
			new CallbackDefault () {
				public void onSuccess(Object... arguments) throws Exception {
					updateData();
				}
			}
		);
	}
	
	public void updateData ()
	{
		notifyDataSetChanged();
	}
	
	public void setFolder(FolderSet folder) 
	{
		buffer = new ArrayList<Conversation>();
		this.folder = folder;
		notifyDataSetChanged();
	}
		
	@Override
	public int getCount() 
	{
		if (folder != null)
			return folder.getNumConversations();
		
		return 0;
	}

	@Override
	public Object getItem(int pos) 
	{
		if (pos >= buffer.size())
			buffer.addAll(folder.getConversations(buffer.size(), Math.min(pos+20, getCount()), filter));
			
		if (pos >= buffer.size())
			return NOTLOADED;
		
		return buffer.get(pos);
	}

	@Override
	public long getItemId(int pos) 
	{
		return pos;
	}
	
	@Override
	public boolean hasStableIds ()
	{
		return false;
	}

	@Override
	public View getView(int pos, View view, ViewGroup parent) 
	{
		Object item = getItem(pos);
		if (view == null)
		{
			LayoutInflater inflater = LayoutInflater.from(context);
			view = inflater.inflate(R.layout.conversation_item, null);
		}
		
		TextView name = (TextView) view.findViewById(R.id.conversationName);
		TextView date = (TextView) view.findViewById(R.id.conversationDate);
		TextView subject = (TextView) view.findViewById(R.id.conversationSubject);
		TextView brief = (TextView) view.findViewById(R.id.conversationBrief);

		if (item != NOTLOADED && ((Conversation)item).isLoaded())
		{
			Conversation conversation = (Conversation)item;
			name.setText(conversation.getHeader().getAuthorsShortList());
			date.setText(conversation.getHeader().getRelativeDate());
			subject.setText(conversation.getHeader().getSubjectExcludingReplyPrefix());
			brief.setText(conversation.getHeader().getBrief());
		}
		else
		{
			name.setText("Loading...");
			date.setText("");
			subject.setText("");
			brief.setText("");
		}
		
		return view;
	}

}
