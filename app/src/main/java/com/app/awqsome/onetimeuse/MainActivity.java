package com.app.awqsome.onetimeuse;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.app.awqsome.onetimeuse.Adapters.AccountAdapter;
import com.app.awqsome.onetimeuse.Database.DatabaseHelperClass;
import com.app.awqsome.onetimeuse.Models.Account;
import com.app.awqsome.onetimeuse.Otp.Token;
import com.app.awqsome.onetimeuse.Otp.TokenPersistence;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements AccountAdapter.OnItemClickListener {

    private static final String TAG = "MainActivity";
    private DatabaseHelperClass db;
    FloatingActionButton btnBarcode;
    RecyclerView rvMain;
    AccountAdapter adapter;
    ArrayList<Account> accounts;
    TextView tvTest;
    Thread updateFunc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        db = new DatabaseHelperClass(this);
        tvTest = findViewById(R.id.tv_test);
        btnBarcode = findViewById(R.id.btn_barcode);

        if(db.isDatabaseEmpty()) {
            //donothing
        } else {
            tvTest.setVisibility(View.GONE);
            accounts = db.getAllAccounts();
            adapter = new AccountAdapter(this, accounts);
            adapter.setOnItemClickListener(this);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
            rvMain = findViewById(R.id.rv_main);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            rvMain.setLayoutManager(layoutManager);
            rvMain.setAdapter(adapter);

            updateFunc = new Thread(new Runnable() {
                @Override
                public void run() {
                    SimpleDateFormat sdf = new SimpleDateFormat("ss");
                    while (true) {
                        long dt = new Date().getTime();
                        int ct = Integer.valueOf(sdf.format(dt));
                        if(ct%30 == 0) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    refreshList();
                                }
                            });
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
            updateFunc.start();
        }

        btnBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BarcodeScanner.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.btn_refresh) {
            //refresh codes
            refreshList();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void refreshList() {
        adapter = new AccountAdapter(this, accounts);
        adapter.notifyDataSetChanged();
        rvMain.setAdapter(adapter);
    }

    @Override
    public void onItemClick(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete?")
                .setMessage("Do you want to delete?")
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new TokenPersistence(MainActivity.this).delete(position);
                        Account acc = accounts.get(position);
                        db.removeAccount(acc.getMyId());
                        accounts.remove(position);
                        Log.d(TAG, "onClick: 1 done");
                        Log.d(TAG, "onClick: 2 done");
                        Log.d(TAG, "onClick: 3 done");
                        if (!db.isDatabaseEmpty()) {
                            refreshList();
                            Log.d(TAG, "onClick: 4 done");
                        } else {
                            updateFunc.interrupt();
                            tvTest.setVisibility(View.VISIBLE);
                            Log.d(TAG, "onClick: 5 done");
                        }
                        Log.d(TAG, "onClick: 6 done");
                    }
                })
                .show();
        Log.d(TAG, "onClick: 7 done");

        //open dialog
        //delete note
    }
}
