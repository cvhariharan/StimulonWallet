package com.hariharan.stimulonwallet.Utils;

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
import java.util.concurrent.ExecutionException;

/**
 * Created by hariharan on 10/11/18.
 */



public class TokenHandler {

    public static Web3j web3j;
    public static Credentials credentials;
    public static Token token;
    public static String tokenAddress;

    public static String sendTo(String address, String value) {
        Address to = new Address(address);
//        BigInteger val = BigInteger.valueOf(Long.valueOf(value));
        Uint256 val = new Uint256(Long.valueOf(value));
        if(token == null) {
            token = loadToken();
        }
        try {
            return token.transfer(to, val).sendAsync().get().getTransactionHash();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Token loadToken() {
        return Token.load(tokenAddress, web3j, credentials, BigInteger.valueOf(2000000), BigInteger.valueOf(21));
    }
}
