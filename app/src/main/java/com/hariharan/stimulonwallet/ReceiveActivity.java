package com.hariharan.stimulonwallet;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.hariharan.stimulonwallet.Utils.TokenHandler;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.web3j.utils.Numeric;

public class ReceiveActivity extends AppCompatActivity {

    private static final String TAG = "ReceiveActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);
        ImageView qrcode = (ImageView) findViewById(R.id.qr_code);

        String text = TokenHandler.credentials.getAddress();
        TextView addressTxt = findViewById(R.id.address);
        addressTxt.setText(text);
        Log.d(TAG, "onCreate: "+text);
        Log.d(TAG, "onCreate: "+TokenHandler.credentials.getAddress());
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(text, BarcodeFormat.QR_CODE,250,250);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            qrcode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }
}
