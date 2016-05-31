/*
 *  * Copyright (C) 2015-2016 The Android Open Source Project
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 */

package com.laocuo.duoduophoto;

import java.io.File;
import java.util.ArrayList;

import com.laocuo.duoduophoto.util.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class DirSelectActivity extends Activity implements OnItemClickListener{
    private static final int REQUEST_OK = 1;
    private Context mContext;
    private String rootPath;
    private TextView title;
    private TextView empty;
    private ListView list;
    private DirSelectAdapter mDirSelectAdapter;
    private ArrayList<String> items;
    private ArrayList<String> paths;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dir_select);
        mContext = this;
        title = (TextView)findViewById(R.id.dir_select_title);
        list = (ListView)findViewById(R.id.dir_select_list);
        empty = (TextView)findViewById(R.id.dir_select_list_empty);
        empty.setText("Empty");
        list.setEmptyView(empty);
        mDirSelectAdapter = new DirSelectAdapter();
        rootPath = Utils.getSDPath();
        items=new ArrayList<String>();
        paths=new ArrayList<String>();
        list.setOnItemClickListener(this);
        list.setAdapter(mDirSelectAdapter);
        getFileDir(rootPath);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        getMenuInflater().inflate(R.menu.dir_select_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        int id = item.getItemId();
        if (id == R.id.reset_directory) {
            Intent i = new Intent();
            Bundle b = new Bundle();
            b.putString("savePath", title.getText().toString());
            i.putExtras(b);
            setResult(REQUEST_OK, i);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getFileDir(String filePath){
        File f=new File(filePath); 
        if(f.exists() && f.canWrite()){
            items.clear();
            paths.clear();
            title.setText(filePath);
            File[] files=f.listFiles();
            if(!filePath.equals(rootPath)){
                items.add("..");
                paths.add(f.getParent());
            }
            for(int i=0;i<files.length;i++){
                File file=files[i];
                if(file.isDirectory()){
                    items.add(file.getName());
                    paths.add(file.getPath());
                }
            }
            mDirSelectAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    private class DirSelectAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return items.size();
        }

        @Override
        public Object getItem(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int arg0) {
            // TODO Auto-generated method stub
            return arg0;
        }

        @Override
        public View getView(int arg0, View arg1, ViewGroup arg2) {
            // TODO Auto-generated method stub
            View v = null;
            TextView name = null;
            if (arg1 != null) {
                v = arg1;
                ViewHolder vh = (ViewHolder)v.getTag();
                name = vh.tv;
            } else {
                v = LayoutInflater.from(mContext).inflate(R.layout.dir_select_listitem, arg2, false);
                name = (TextView)v.findViewById(R.id.dir_select_list_name);
                ViewHolder vh = new ViewHolder();
                vh.tv = name;
                v.setTag(vh);
            }
            name.setText(items.get(arg0));
            return v;
        }

        private class ViewHolder {
            TextView tv;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        // TODO Auto-generated method stub
        getFileDir(paths.get(arg2));
    }
}
