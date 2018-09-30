package com.hariharan.stimulonwallet;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.http.HttpService;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Button mSubmit;
    private EditText password;
    private Credentials credentials;
    private Web3j web3j;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSubmit = (Button) findViewById(R.id.submit);
        password = (EditText) findViewById(R.id.password);


        web3j = Web3jFactory.build(new HttpService("https://ropsten.infura.io/v3/08cacc7bdd5144dfaed0ce1893e99021"));
        try {
            Log.d(TAG, "onCreate: " + web3j.web3ClientVersion().sendAsync().get().getWeb3ClientVersion());
        }catch (Exception e) {
            Log.e(TAG, "onCreate: "+e.getMessage());
        }

        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
//                    File file = new File(getApplicationContext().getFilesDir(), "/wallet");
//                    if(!file.exists())
//                        file.createNewFile();
                    String filename = WalletUtils.generateNewWalletFile(password.getText().toString(), getDir("wallets", MODE_PRIVATE), false);
                    Log.d(TAG, "onClick: Filename "+filename);
                    credentials = WalletUtils.loadCredentials(password.getText().toString(),
                            getDir("wallets", MODE_PRIVATE).toString()+"/"+filename);
                    Log.d(TAG, "onClick: "+credentials.getAddress());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
