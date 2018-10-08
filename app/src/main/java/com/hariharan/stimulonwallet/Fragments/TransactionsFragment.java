package com.hariharan.stimulonwallet.Fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.hariharan.stimulonwallet.R;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TransactionsFragment extends Fragment {

    private static final String TAG = "TransactionsFragment";
    private RecyclerView mRecycler;
    private View mView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_transactions, container, false);
        mRecycler = (RecyclerView) mView.findViewById(R.id.transactions);
        makeAPICall(getString(R.string.etherscan_url));
        return mView;
    }

    private void makeAPICall(String url) {
        //Use okhttp to make API call to etherscan and return the result to the recycler view
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                            .url(url+R.string.etherscan_api).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG, "onResponse: "+response.body().string());
            }
        });
    }

    class TransactionsAdapter extends RecyclerView.Adapter<TransactionsAdapter.TransactionsHolder>{

        private HashMap<String, String> tx;

        public TransactionsAdapter(HashMap<String, String> tx) {
            this.tx = tx;
        }

        @Override
        public TransactionsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.transaction_view, parent, false);
            return new TransactionsHolder(view);
        }

        @Override
        public void onBindViewHolder(TransactionsHolder holder, int position) {
        }

        @Override
        public int getItemCount() {
            return 0;
        }

        class TransactionsHolder extends RecyclerView.ViewHolder {

            public TextView transactionId;
            public TextView value;

            public TransactionsHolder(View itemView) {
                super(itemView);
                transactionId = (TextView) itemView.findViewById(R.id.transactionId);
                value = (TextView) itemView.findViewById(R.id.value);
            }
        }
    }
}

