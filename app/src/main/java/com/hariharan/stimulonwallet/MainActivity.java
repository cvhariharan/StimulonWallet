package com.hariharan.stimulonwallet;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.hariharan.stimulonwallet.contracts.Token;

import org.web3j.abi.datatypes.Utf8String;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.http.HttpService;

import java.io.File;
import java.math.BigInteger;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Button mSubmit;
    private EditText password;
    private Credentials credentials;
    private Web3j web3j;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSubmit = (Button) findViewById(R.id.submit);
        password = (EditText) findViewById(R.id.password);

        new Thread(new Runnable() {
            @Override
            public void run() {
                web3j = Web3jFactory.build(new HttpService("https://ropsten.infura.io/v3/08cacc7bdd5144dfaed0ce1893e99021"));
                try {
                    Log.d(TAG, "onCreate: " + web3j.web3ClientVersion().sendAsync().get().getWeb3ClientVersion());
                    Log.d(TAG, "run: ");
                }catch (Exception e) {
                    Log.e(TAG, "onCreate: "+e.getMessage());
                }
            }
        }).start();
        


        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
//                    File file = new File(getApplicationContext().getFilesDir(), "/wallet");
//                    if(!file.exists())
//                        file.createNewFile();
                    String address = "0x5d740d08744fa58ebcb1B071E96C10F05bDe6577";
                    preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    String filename = preferences.getString("File", "");
                    if(TextUtils.isEmpty(filename)) {
                        filename = WalletUtils.generateNewWalletFile(password.getText().toString(), getDir("wallets", MODE_PRIVATE), false);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("File", filename);
                        editor.apply();
                    }
                    Log.d(TAG, "onClick: Filename "+filename);
                    credentials = WalletUtils.loadCredentials(password.getText().toString(),
                            getDir("wallets", MODE_PRIVATE).toString()+"/"+filename);
                    Token token = Token.load(address, web3j, credentials, BigInteger.valueOf(2000000),BigInteger.valueOf(21));
                    Utf8String value = token.name().sendAsync().get();
                    Log.d(TAG, "onClick: "+credentials.getAddress());
                    Log.d(TAG, "onClick: "+ value.getValue());
                } catch (CipherException e) {
                    Toast.makeText(getApplicationContext(),"Invalid Password", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
