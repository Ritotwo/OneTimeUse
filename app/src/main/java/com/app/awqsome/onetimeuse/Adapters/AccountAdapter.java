package com.app.awqsome.onetimeuse.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.app.awqsome.onetimeuse.Models.Account;
import com.app.awqsome.onetimeuse.Otp.Token;
import com.app.awqsome.onetimeuse.Otp.TokenCode;
import com.app.awqsome.onetimeuse.Otp.TokenPersistence;
import com.app.awqsome.onetimeuse.R;

import java.util.ArrayList;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.AccountsViewHolder> {

    private OnItemClickListener clickListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        clickListener = listener;
    }

    public ArrayList<Account> list;
    public TokenPersistence tokenPersistence;
    private static final String TAG = "AccountAdapter";

    public AccountAdapter(Context context, ArrayList<Account> list) {
        this.list = list;
        tokenPersistence = new TokenPersistence(context);
    }

    @NonNull
    @Override
    public AccountsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.customized_account_item, viewGroup,  false);
        return new AccountsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountsViewHolder accountsViewHolder, int i) {
        Account account = list.get(i);
        Token token = tokenPersistence.get(i);
        TokenCode codes = token.generateCodes();
        String tokenCode = codes.getCurrentCode();

        accountsViewHolder.tvLabel.setText(account.getLabel() + " (" + account.getUser() + ")");
        accountsViewHolder.tvOtp.setText(tokenCode);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class AccountsViewHolder extends RecyclerView.ViewHolder {

        TextView tvLabel = itemView.findViewById(R.id.tv_label);
        TextView tvOtp = itemView.findViewById(R.id.tv_otp);

        public AccountsViewHolder(@NonNull View itemView) {
            super(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(clickListener != null) {
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION) {
                            clickListener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }
}
