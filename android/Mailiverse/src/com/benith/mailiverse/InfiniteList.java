package com.benith.mailiverse;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class InfiniteList extends ListActivity implements OnScrollListener 
{
    Aleph0 adapter = new Aleph0();

    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setListAdapter(adapter); 
        getListView().setOnScrollListener(this);
    }

    public void onScroll(AbsListView view, int firstVisible, int visibleCount, int totalCount) {
/*
        boolean loadMore =
            firstVisible + visibleCount >= totalCount;

        if(loadMore) {
            adapter.count += visibleCount; // or any other amount
            adapter.notifyDataSetChanged();
        }
        
*/
    }

    public void onScrollStateChanged(AbsListView v, int s) 
    { 
    	
    }    

    class Aleph0 extends BaseAdapter {
        int count = 40; /* starting amount */

        public int getCount() { return count; }
        public Object getItem(int pos) { return pos; }
        public long getItemId(int pos) { return pos; }

        public View getView(int pos, View v, ViewGroup p) {
                TextView view = new TextView(InfiniteList.this);
                view.setText("entry " + pos);
                return view;
        }
    }
}
