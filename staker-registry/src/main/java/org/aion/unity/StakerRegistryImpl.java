package org.aion.unity;

import avm.Address;
import avm.Blockchain;
import avm.Result;

import org.aion.avm.tooling.abi.Callable;
import org.aion.avm.userlib.AionMap;

import java.math.BigInteger;
import java.util.Map;


public class StakerRegistryImpl {
    
    static {
        stakers = new AionMap<>();
    }
    
    private static class Staker {
        private Address signingAddress;
        private Address coinbaseAddress;
        private BigInteger totalVote;
        
        // maps addresses to the votes those addresses have sent to this staker
        // the sum of votes.values() should always equal totalVote
        private Map<Address, BigInteger> votes;
        
        public Staker(Address signingAddress, Address coinbaseAddress) {
            this.signingAddress = signingAddress;
            this.coinbaseAddress = coinbaseAddress;
            this.totalVote = BigInteger.ZERO;
            this.votes = new AionMap<>();
        }
    }
    
    private static Map<Address, Staker> stakers;

    @Callable
    public static boolean register(Address signingAddress, Address coinbaseAddress) {
        Address caller = Blockchain.getCaller();
        Staker staker = new Staker(signingAddress, coinbaseAddress);

        if (!stakers.containsKey(caller)) {
            stakers.put(caller, staker);
            return true;
        } else {
            return false;
        }
    }

    // TODO: add return value for vote/unvote

    @Callable
    public static void vote(Address stakerAddress) {
        BigInteger value = Blockchain.getValue();
        Address senderAddress = Blockchain.getCaller();
        if (null != stakerAddress && stakers.containsKey(stakerAddress) && value.compareTo(BigInteger.ZERO) > 0) {
            Staker staker = stakers.get(stakerAddress);
            staker.totalVote = staker.totalVote.add(value);
            
            BigInteger vote = staker.votes.get(senderAddress);
            if (null == vote) {
                // This is the first time the sender has voted for this staker
                staker.votes.put(senderAddress, value);
            } else {
                staker.votes.replace(senderAddress, vote.add(value));
            }
        }
    }

    @Callable
    public static void unvote(Address stakerAddress, long amount) {
        Address senderAddress = Blockchain.getCaller();
        Blockchain.require(amount >= 0);
        BigInteger amountBI = BigInteger.valueOf(amount);
        if (null != stakerAddress && stakers.containsKey(stakerAddress)) {
            Staker staker = stakers.get(stakerAddress);
            if (staker != null && staker.votes.containsKey(senderAddress)) {
                Result result;
                BigInteger vote = staker.votes.get(senderAddress);
                if (vote.compareTo(amountBI) > 0) {
                    staker.votes.replace(senderAddress, vote.subtract(amountBI));
                    staker.totalVote = staker.totalVote.subtract(amountBI);
                    result = Blockchain.call(senderAddress, amountBI, new byte[0], Blockchain.getRemainingEnergy());
                } else {
                    staker.totalVote = staker.totalVote.subtract(vote);
                    result = Blockchain.call(senderAddress, vote, new byte[0], Blockchain.getRemainingEnergy());
                    staker.votes.remove(senderAddress);
                }
                // TODO: Determine what we want to do with "result".
                assert (null != result);
            }
        }
    }

    @Callable
    public static long getVote(Address stakingAddress) {
        Staker staker = stakers.get(stakingAddress);
        if (staker != null) {
            return staker.totalVote.longValue();
        } else {
            return 0;
        }
    }

    @Callable
    public static Address getSigningAddress(Address staker) {
        return stakers.containsKey(staker) ? stakers.get(staker).signingAddress : null;
    }

    @Callable
    public static Address getCoinbaseAddress(Address staker) {
        return stakers.containsKey(staker) ? stakers.get(staker).coinbaseAddress : null;
    }

    @Callable
    public static boolean setSigningAddress(Address newSigningAddress) {
        Address caller = Blockchain.getCaller();
        if (stakers.containsKey(caller)) {
            stakers.get(caller).signingAddress = newSigningAddress;
            return true;
        }
        return false;
    }

    @Callable
    public static boolean setCoinbaseAddress(Address newCoinbaseAddress) {
        Address caller = Blockchain.getCaller();
        if (stakers.containsKey(caller)) {
            stakers.get(caller).coinbaseAddress = newCoinbaseAddress;
            return true;
        }
        return false;
    }
}
