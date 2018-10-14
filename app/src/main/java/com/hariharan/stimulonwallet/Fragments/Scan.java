package com.hariharan.stimulonwallet.Fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.zxing.Result;
import com.hariharan.stimulonwallet.AccountActivity;
import com.hariharan.stimulonwallet.R;
import com.hariharan.stimulonwallet.Utils.TokenHandler;
import com.hariharan.stimulonwallet.contracts.Token;

import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.generated.Uint256;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Scan extends Fragment {

    private static final String TAG = "ScanFragment";
    private View mView;
    private CodeScanner mCodeScanner;
    private String stm;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_scan, container, false);

        stm = getActivity().getIntent().getExtras().getString("value");

        CodeScannerView scannerView = mView.findViewById(R.id.scanner_view);
        if(checkCameraPermission()) {
            mCodeScanner = new CodeScanner(getContext(), scannerView);
            mCodeScanner.setDecodeCallback(new DecodeCallback() {
                @Override
                public void onDecoded(@NonNull final Result result) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String address = result.getText();
                            Log.d(TAG, "run: To: "+address+" send: "+stm);
                            SendToken sendToken = new SendToken();
                            sendToken.execute(address, stm);
                            Intent i = new Intent(getContext(), AccountActivity.class);
                            startActivity(i);
//                            Future<String> hash = TokenHandler.sendTo(address, stm);
//                            try {
//                                if(hash.get() != null) {
//                                    Log.d(TAG, "run: Hash: " + hash);
//                                    Toast.makeText(getContext(), "Sent", Toast.LENGTH_LONG).show();
//                                } else {
//                                    Toast.makeText(getContext(), "Could not send", Toast.LENGTH_LONG).show();
//                                }
//                                Intent i = new Intent(getContext(), AccountActivity.class);
//                                startActivity(i);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            } catch (ExecutionException e) {
//                                e.printStackTrace();
//                            }
                        }
                        });
                    }
            });
            scannerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mCodeScanner.startPreview();
                }
            });
        }
        else {
            requestPermission();
            Toast.makeText(getContext(), "Requires Camera Permission", Toast.LENGTH_LONG).show();
        }
        return mView;
    }

    private boolean checkCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getContext().checkSelfPermission(Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.requestPermissions(
                    new String[]{Manifest.permission.CAMERA},
                    10);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mCodeScanner != null)
            mCodeScanner.startPreview();
    }

    @Override
    public void onPause() {
        if(mCodeScanner != null)
            mCodeScanner.releaseResources();
        super.onPause();
    }

    class SendToken extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            Token token = TokenHandler.loadToken();
            try {
                Log.d(TAG, "doInBackground: "+strings[0]);
                return token.transfer(new Address(strings[0]), new Uint256(Long.valueOf(strings[1])))
                .sendAsync().get().getTransactionHash();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String res) {
            super.onPostExecute(res);
            Log.d(TAG, "onPostExecute: Done");
            if(res != null) {
                Toast.makeText(getContext(), res, Toast.LENGTH_LONG).show();
                Log.d(TAG, "onPostExecute: "+res);
            }
        }
    }
}


