package com.benith.mailiverse;

import java.util.ArrayList;
import java.util.List;

import core.callback.CallbackDefault;

import mail.client.Events;
import mail.client.model.Folder;
import mail.client.model.FolderSet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

public class FolderListAdapter extends BaseAdapter
{
	Application application;
	Context context;
	List<Folder> folders = new ArrayList<Folder>();
	
	public FolderListAdapter (Context context)
	{
		application = Application.getInstance();
		this.context = context;

		application.state.client.getMaster().getEventPropagator().add(
			Events.LoadFolder, this, 
			new CallbackDefault () {
				public void onSuccess(Object... arguments) throws Exception {
					updateData();
				}
			}
		);
	}
	
	public void updateData ()
	{
		folders = application.state.client.getMaster().getIndexer().getSystemFolders();
		FolderListAdapter.this.notifyDataSetChanged();
	}
	
	@Override
	public int getCount() 
	{
		return folders.size();
	}

	@Override
	public Object getItem(int pos) 
	{
		return folders.get(pos);
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
		if (view == null)
		{
			LayoutInflater inflater = LayoutInflater.from(context);
			view = inflater.inflate(R.layout.folder_item, null);
		}
		
		TextView name = (TextView) view.findViewById(R.id.folderName);
		TextView quantity = (TextView) view.findViewById(R.id.folderQuantity);
		
		FolderSet folder = (FolderSet)folders.get(pos);
		name.setText(folder.getName());
		quantity.setText("" + folder.getNumConversations());
		
		return view;
	}
	
}
