package com.hariharan.stimulonwallet.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hariharan.stimulonwallet.AccountActivity;
import com.hariharan.stimulonwallet.R;
import com.hariharan.stimulonwallet.contracts.Token;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Uint;
import org.web3j.utils.Numeric;

public class TransactionsFragment extends Fragment {

    private static final String TAG = "TransactionsFragment";
    private RecyclerView mRecycler;
    private View mView;
    private ArrayList<Transaction> transactionsList;
    private ProgressBar mProgress;
    private TextView mBalance;
    private Token token;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_transactions, container, false);
        mRecycler = (RecyclerView) mView.findViewById(R.id.transactions);
        mProgress = mView.findViewById(R.id.progress);
        mBalance = mView.findViewById(R.id.balance);
        transactionsList = new ArrayList<>();

        //Make the API call in a new thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                makeAPICall(getString(R.string.etherscan_url));
            }
        }).start();
        token = Token.load(getString(R.string.token_address), AuthFragment.web3j,
                AuthFragment.credentials, BigInteger.valueOf(200000), BigInteger.valueOf(21));

        //Make the token call in a new thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final BigInteger balance = token.balanceOf(new Address(AuthFragment.credentials.getAddress())).sendAsync().get().getValue();
                    mBalance.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "run: "+balance);
                            mBalance.setText(balance.toString());
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }).start();
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
                String resp = response.body().string();
                parseJson(resp);
            }
        });
    }

    public void parseJson(String resp) {
        try {
            Log.d(TAG, "parseJson: "+resp);
            JSONObject jsonResp = new JSONObject(resp);
            JSONArray results = jsonResp.getJSONArray("result");
            for(int i = 0; i < results.length(); i++) {
                JSONObject tx = results.getJSONObject(i);
                String toAddress = tx.getJSONArray("topics").getString(1);
                String fromAdress = tx.getJSONArray("topics").getString(2);
                Log.d(TAG, "parseJson: To: "+toAddress+" From: "+fromAdress);
                if(toAddress.equals("0x0000000000000000000000002a5f493594ef5e7d81448c237dfb87003485fce5") || fromAdress.equals("0x0000000000000000000000002a5f493594ef5e7d81448c237dfb87003485fce5")) {
                    Log.d(TAG, "parseJson: "+ Numeric.decodeQuantity(tx.getString("data")));
                    transactionsList.add(new Transaction(tx));
                }
            }
            mRecycler.post(new Runnable() {
                @Override
                public void run() {
                    mProgress.setVisibility(View.INVISIBLE);
                    mRecycler.setVisibility(View.VISIBLE);
                    mRecycler.setHasFixedSize(true);
                    mRecycler.setAdapter(new TransactionsAdapter(transactionsList));
                    mRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    class TransactionsAdapter extends RecyclerView.Adapter<TransactionsAdapter.TransactionsHolder>{

        private ArrayList<Transaction> transactionsList;

        public TransactionsAdapter(ArrayList<Transaction> list) {
            this.transactionsList = list;
        }

        @Override
        public TransactionsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.transaction_view, parent, false);
            return new TransactionsHolder(view);
        }

        @Override
        public void onBindViewHolder(TransactionsHolder holder, int position) {
            Transaction tx = transactionsList.get(position);
            holder.transactionId.setText(tx.otherAddress);
            holder.value.setText(tx.value);
        }

        @Override
        public int getItemCount() {
            return transactionsList.size();
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

class Transaction {
    public String otherAddress;
    public String value;
    private int index;
    public Transaction(JSONObject tx) {
        try {
            index = tx.getJSONArray("topics").getString(1).equals(AuthFragment.credentials.getAddress()) ? 2 : 1;
            otherAddress = tx.getJSONArray("topics").getString(index).substring(26);
            value = Numeric.decodeQuantity(tx.getString("data")).toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

