package com.benith.mailiverse;

import core.callback.CallbackDefault;
import mail.client.Events;
import mail.client.model.Conversation;
import mail.client.model.Mail;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MailListAdapter extends BaseAdapter
{
	static final int TYPE_HEADER = 0, TYPE_MAIL = 1;
	Object HEADER = new Object();
	
	Conversation conversation;
	Context context;
	
	public MailListAdapter (Context context)
	{
		this.context = context;

		Application.getInstance().state.client.getMaster().getEventPropagator().add(
			Events.LoadMail, this, 
			new CallbackDefault () {
				public void onSuccess(Object... arguments) throws Exception {
					updateData();
				}
			}
		);
	}
	
	public void updateData()
	{
		notifyDataSetChanged();
	}
	
	public void setConversation(Conversation conversation) 
	{
		this.conversation = conversation;
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() 
	{
		if (conversation != null)
			return conversation.getNumItems()+1;
		
		return 0;
	}
	
	@Override
    public int getItemViewType(int position) {
        if (position == 0)
        	return TYPE_HEADER;
        return TYPE_MAIL;
    }

	@Override
	public Object getItem(int pos) 
	{
		if (pos == 0)
			return HEADER;
		
		return conversation.getItems().get(pos-1);
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

			switch (getItemViewType(pos))
			{
			
			case TYPE_HEADER:
				view = inflater.inflate(R.layout.mail_header_item, null);
				break;
				
			case TYPE_MAIL:
				view = inflater.inflate(R.layout.mail_item, null);
				break;
			}
		}
		
		switch (getItemViewType(pos))
		{
			case TYPE_HEADER:
				TextView subject = (TextView)view.findViewById(R.id.mailSubject);
				subject.setText(conversation.getHeader().getSubject());
				break;
				
			case TYPE_MAIL:
				Mail mail = (Mail)getItem(pos);
				TextView author = (TextView)view.findViewById(R.id.mailAuthor);
				TextView date = (TextView)view.findViewById(R.id.mailDate);
				TextView recipients = (TextView)view.findViewById(R.id.mailRecipients);
				TextView body = (TextView)view.findViewById(R.id.mailBody);
				
				if (mail.isLoaded())
				{
					author.setText("" + mail.getHeader().getAuthor());
					date.setText(mail.getHeader().getRelativeDate());
					recipients.setText(mail.getHeader().getRecipients().shortList());
					
					String html, mimeType;
					if (mail.getBody().hasHTML())
					{
						mimeType = "text/html";
						html = mail.getBody().getStrippedHTML();
					}
					else
					if (mail.getBody().hasText())
					{
						mimeType = "text/plain";
						html = mail.getBody().getStrippedText();
					}
					else
					{
						mimeType = "text/plain";
						html = "";
					}
					
					String encoding = "utf-8";
					body.setText(html);
//					body.loadData(html, mimeType, encoding);
				}
				else
				{
					author.setText("Loading...");
					date.setText("");
					recipients.setText("");
					body.setText("");
//					body.clearView();
				}
				
				break;
		}
		
		return view;
	}

}
