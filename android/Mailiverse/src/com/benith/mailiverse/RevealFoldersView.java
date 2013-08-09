package com.benith.mailiverse;

import com.slidingmenu.lib.app.SlidingActivity;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class RevealFoldersView extends ViewContainer
{
	ListView list;
	SlidingActivity context;
	
	public RevealFoldersView(SlidingActivity context)
	{
		this.context = context;
		
		LayoutInflater inflater = LayoutInflater.from(context);
		view = inflater.inflate(R.layout.reveal_folders, null);
	
		list = (ListView) view.findViewById(R.id.folderList);
		list.setOnItemClickListener(listener);
		list.setAdapter(new FolderListAdapter(view.getContext()));
	}

	public View getView ()
	{
		return view;
	}
	
	OnItemClickListener listener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
			context.showContent();
		}
		
	};

}
