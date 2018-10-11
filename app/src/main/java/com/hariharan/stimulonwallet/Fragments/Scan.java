package com.hariharan.stimulonwallet.Fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
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
import com.hariharan.stimulonwallet.R;

public class Scan extends Fragment {

    private static final String TAG = "Scan";
    private View mView;
    private CodeScanner mCodeScanner;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_scan, container, false);
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
                            Log.d(TAG, "run: "+address);
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
}
