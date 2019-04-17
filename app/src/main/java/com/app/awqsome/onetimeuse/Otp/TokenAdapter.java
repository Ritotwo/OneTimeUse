package com.app.awqsome.onetimeuse.Otp;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.HashMap;
import java.util.Map;

public class TokenAdapter extends BaseAdapter {

    TokenPersistence tokenPersistence;
    LayoutInflater layoutInflater;
    Map<String, Token> tokenCodes;

    public TokenAdapter(Context context) {
        tokenPersistence = new TokenPersistence(context);
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        tokenCodes = new HashMap<>();
        registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                tokenCodes.clear();
            }

            @Override
            public void onInvalidated() {
                tokenCodes.clear();
            }
        });
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }
}
