package com.app.awqsome.onetimeuse.Otp;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class TokenPersistence {

    private static final String TAG = "TokenPersistence";

    private static final String NAME  = "tokens";
    private static final String ORDER = "tokenOrder";
    private final SharedPreferences prefs;
    private final Gson gson;

    private List<String> getTokenOrder() {
        Type type = new TypeToken<List<String>>(){}.getType();
        String str = prefs.getString(ORDER, "[]");
        //Log.d(TAG, str);
        List<String> order = gson.fromJson(str, type);
        return order == null ? new LinkedList<String>() : order;
    }

    private SharedPreferences.Editor setTokenOrder(List<String> order) {
        return prefs.edit().putString(ORDER, gson.toJson(order));
    }

    public TokenPersistence(Context ctx) {
        prefs = ctx.getApplicationContext().getSharedPreferences(NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public int length() {
        return getTokenOrder().size();
    }

    public boolean tokenExists(Token token) {
        return prefs.contains(token.getID());
    }

    public Token get(int position) {
        String key = getTokenOrder().get(position);
        String str = prefs.getString(key, null);

        try {
            return gson.fromJson(str, Token.class);
        } catch (JsonSyntaxException jse) {
            // Backwards compatibility for URL-based persistence.
            try {
                return new Token(str);
            } catch (Token.TokenUriInvalidException tuie) {
                tuie.printStackTrace();
            }
        }

        return null;
    }

    public void save(Token token) {
        String key = token.getID();
        //HashMap<String, byte[]> map = token.getMap();

        //if token exists, just update it
        if (prefs.contains(key)) {
            prefs.edit().putString(token.getID(), gson.toJson(token)).apply();
            return;
        }

        List<String> order = getTokenOrder();
        order.add(0, key);
        setTokenOrder(order).putString(key, gson.toJson(token)).apply();
    }

    public void delete(int position) {
        List<String> order = getTokenOrder();
        String key = order.remove(position);
        setTokenOrder(order).remove(key).apply();
    }
}
