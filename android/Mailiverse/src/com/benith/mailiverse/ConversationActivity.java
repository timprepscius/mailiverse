package com.benith.mailiverse;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ListView;
import android.support.v4.app.NavUtils;

public class ConversationActivity extends Activity {

	MailListAdapter mailListAdapter;
	ListView list;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_conversation);
		
		overridePendingTransition(R.anim.forward_enter, R.anim.forward_leave);
		
		mailListAdapter = new MailListAdapter(this);
		mailListAdapter.setConversation(Application.getInstance().state.conversation);
		
		list = (ListView) findViewById(R.id.mailList);
		list.setAdapter(mailListAdapter);
		
		Button backButton = (Button) findViewById(R.id.conversationBackButton);
		backButton.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				// i need to load the animation, add a listener that sets the view to gone.. or something
				ConversationActivity.this.finish();
				overridePendingTransition(R.anim.back_leave,R.anim.back_enter);		
			} 
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_conversation, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
