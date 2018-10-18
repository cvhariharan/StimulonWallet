package com.hariharan.stimulonwallet.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hariharan.stimulonwallet.R;
import com.hariharan.stimulonwallet.ReceiveActivity;
import com.hariharan.stimulonwallet.ScanActivity;
import com.hariharan.stimulonwallet.Utils.TokenHandler;
import com.hariharan.stimulonwallet.contracts.Token;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.web3j.abi.datatypes.Address;
import org.web3j.utils.Numeric;

public class TransactionsFragment extends Fragment {

    private static final String TAG = "TransactionsFragment";
    private RecyclerView mRecycler;
    private View mView;
    private ArrayList<Transaction> transactionsList;
    private ProgressBar mProgress;
    private TextView mBalance;
    private Token token;
    private Button sendBtn, recvBtn;
    private EditText valueIp;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_transactions, container, false);
        mRecycler = (RecyclerView) mView.findViewById(R.id.transactions);
        mProgress = mView.findViewById(R.id.progress);
        mBalance = mView.findViewById(R.id.balance);
        valueIp = mView.findViewById(R.id.stm);

        sendBtn = (Button) mView.findViewById(R.id.send_btn);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String value = valueIp.getText().toString();
                if(!TextUtils.isEmpty(value)) {
                    Intent i = new Intent(getContext(), ScanActivity.class);
                    i.putExtra("value",value);
                    startActivity(i);
                } else {
                    Toast.makeText(getContext(), "STM value cannot be empty", Toast.LENGTH_LONG).show();
                }
            }
        });

        mRecycler.setVisibility(View.INVISIBLE);
        mRecycler.setHasFixedSize(true);
        mRecycler.setAdapter(new TransactionsAdapter(new ArrayList<Transaction>()));
        mRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        recvBtn = mView.findViewById(R.id.recv_btn);
        recvBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), ReceiveActivity.class);
                startActivity(i);
            }
        });

        transactionsList = new ArrayList<>();

        //Make the API call in a new thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                makeAPICall(getString(R.string.etherscan_url));
            }
        }).start();
        token = TokenHandler.loadToken();

        //Use the transactions to calculate the balance for an accurate balance
        //Make the token call in a new thread
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    final BigInteger balance = token.balanceOf(new Address(TokenHandler.credentials.getAddress())).sendAsync().get().getValue();
//                    Log.d(TAG, "run: address: "+TokenHandler.credentials.getAddress());
//                    mBalance.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            Log.d(TAG, "run: "+balance);
//                            mBalance.setText(balance.toString());
//                        }
//                    });
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                } catch (ExecutionException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
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
            //Set the balance to zero
            Transaction.balance = 0;

            JSONObject jsonResp = new JSONObject(resp);
            JSONArray results = jsonResp.getJSONArray("result");
            for(int i = 0; i < results.length(); i++) {
                JSONObject tx = results.getJSONObject(i);
                String toAddress = tx.getJSONArray("topics").getString(1);
                String fromAddress = tx.getJSONArray("topics").getString(2);
                toAddress = Numeric.prependHexPrefix(toAddress.substring(26));
                fromAddress = Numeric.prependHexPrefix(fromAddress.substring(26));
                if(toAddress.equals(TokenHandler.credentials.getAddress()) || fromAddress.equals(TokenHandler.credentials.getAddress())) {
                    Log.d(TAG, "parseJson: "+ Numeric.decodeQuantity(tx.getString("data")));
                    transactionsList.add(new Transaction(tx));
                }
            }

            //Calculate balance from previous transactions as it is updated on etherscan much faster
            Log.d(TAG, "parseJson: Balance "+Transaction.balance);
            mBalance.setText(String.valueOf(Transaction.balance));

            mRecycler.post(new Runnable() {
                @Override
                public void run() {
                    mProgress.setVisibility(View.INVISIBLE);
                    mRecycler.setVisibility(View.VISIBLE);
                    mRecycler.setAdapter(new TransactionsAdapter(transactionsList));
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
    private static final String TAG = "Transaction";
    public String otherAddress;
    public String value;
    private int index;
    public static int balance=0;
    public Transaction(JSONObject tx) {
        try {
            String sign = "-";
            String fromAddress = Numeric.prependHexPrefix(tx.getJSONArray("topics").getString(1).substring(26));
            index = fromAddress.equals(AuthFragment.credentials.getAddress()) ? 2 : 1;
            Log.d(TAG, "Transaction: JSON: "+fromAddress);
            Log.d(TAG, "Transaction: NJ: "+AuthFragment.credentials.getAddress());
            Log.d(TAG, "Transaction: Index: "+index);
            otherAddress = tx.getJSONArray("topics").getString(index).substring(26);
            if(index == 1)
                sign = "+";
            value = sign+Numeric.decodeQuantity(tx.getString("data")).toString();
            balance += Integer.valueOf(value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

