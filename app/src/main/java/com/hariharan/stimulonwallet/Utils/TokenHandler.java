package com.hariharan.stimulonwallet.Utils;

import android.os.AsyncTask;
import android.util.Log;

import com.hariharan.stimulonwallet.contracts.Token;

import org.json.JSONObject;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;

import org.web3j.tx.Contract;
import org.web3j.tx.ManagedTransaction;

import java.math.BigInteger;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by hariharan on 10/11/18.
 */


public class TokenHandler {

    public static Web3j web3j;
    public static Credentials credentials;
    public static Token token;
    public static String tokenAddress;

    public static Future<String> sendTo(String address, String value) {

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        final Address to = new Address(address);
//        BigInteger val = BigInteger.valueOf(Long.valueOf(value));
        final Uint256 val = new Uint256(Long.valueOf(value));
        if(token == null) {
            token = loadToken();
        }

        Future<String> future = executorService.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                try {
                    return token.transfer(to, val).send().getTransactionHash();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                return null;
            }
        });

        return future;
    }

    public static Token loadToken() {
        if(token == null)
            token = Token.load(tokenAddress, web3j, credentials, BigInteger.valueOf(60000000000L), BigInteger.valueOf(503570L));
        return token;
    }
}