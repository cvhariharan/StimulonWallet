package com.hariharan.stimulonwallet.Fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.hariharan.stimulonwallet.AccountActivity;
import com.hariharan.stimulonwallet.R;
import com.hariharan.stimulonwallet.Utils.TokenHandler;
import com.hariharan.stimulonwallet.contracts.Token;

import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.http.HttpService;

import java.math.BigInteger;
import java.util.concurrent.Future;

import rx.Subscription;

import static android.content.Context.MODE_PRIVATE;

public class AuthFragment extends Fragment {

    private static final String TAG = "AuthFragment";
    private View mView;
    private Button mSubmit;
    private EditText password;
    public static Credentials credentials;
    public static Web3j web3j;
    private SharedPreferences preferences;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_auth, container, false);
        mSubmit = (Button) mView.findViewById(R.id.submit);
        password = (EditText) mView.findViewById(R.id.password);

        TokenHandler.tokenAddress = getString(R.string.token_address);

        mSubmit.setVisibility(View.GONE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String infuraAPI = getString(R.string.infura_api);
                    web3j = Web3jFactory.build(new HttpService("https://ropsten.infura.io/v3/"+infuraAPI));
                    TokenHandler.web3j = web3j;
                    Log.d(TAG, "onCreate: " + web3j.web3ClientVersion().sendAsync().get().getWeb3ClientVersion());
                    Log.d(TAG, "run: ");
                    mSubmit.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "run: Enabled");
                            mSubmit.setVisibility(View.VISIBLE);
                            initializeButton();
                            Log.d(TAG, "run: Initialized");
                        }
                    });
                }catch (Exception e) {
                    Log.e(TAG, "onCreate: "+e.getMessage());
                }
            }
        }).start();


        return mView;
    }

    private void initializeButton() {
        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
//                    File file = new File(getApplicationContext().getFilesDir(), "/wallet");
//                    if(!file.exists())
//                        file.createNewFile();
                    String tokenAddress = getString(R.string.token_address);
                    preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                    String filename = preferences.getString("File", "");
                    if(TextUtils.isEmpty(filename)) {
                        filename = WalletUtils.generateNewWalletFile(password.getText().toString(), getContext().getDir("wallets", MODE_PRIVATE), false);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("File", filename);
                        editor.apply();
                    }
                    Log.d(TAG, "onClick: Filename "+filename);
                    credentials = WalletUtils.loadCredentials(password.getText().toString(),
                            getContext().getDir("wallets", MODE_PRIVATE).toString()+"/"+filename);
                    TokenHandler.credentials = credentials;
                    Token token = TokenHandler.loadToken();//Token.load(tokenAddress, web3j, credentials, BigInteger.valueOf(2000000), BigInteger.valueOf(21));
                    Future<Uint256> futureValue = token.balanceOf(new Address(credentials.getAddress())).sendAsync();
                    Log.d(TAG, "onClick: "+credentials.getAddress());
                    Log.d(TAG, "onClick: Balance - "+ futureValue.get().getValue());
                    if(!TextUtils.isEmpty(credentials.getAddress())) {
                        Log.d(TAG, "onClick: Intent");
                        Intent i = new Intent(getContext(), AccountActivity.class);
                        startActivity(i);
                    }

                } catch (CipherException e) {
                    Toast.makeText(getContext(),"Invalid Password", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
