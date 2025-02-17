package org.aion.unity;

import avm.Address;

public interface StakerRegistryListener {

    // TODO: analyze listener failure model

    /**
     * When the signing address of a staker is changed.
     *
     * @param staker            the staker address
     * @param newSigningAddress the new signing address
     */
    void onSigningAddressChange(Address staker, Address newSigningAddress);

    /**
     * When the coinbase address of a staker is changed.
     *
     * @param staker             the staker address
     * @param newCoinbaseAddress the new coinbase address
     */
    void onCoinbaseAddressChange(Address staker, Address newCoinbaseAddress);

    /**
     * When this listener is added.
     *
     * @param staker the staker address
     */
    void onListenerAdded(Address staker);

    /**
     * When this listener is removed.
     *
     * @param staker the staker address
     */
    void onListenerRemoved(Address staker);

    /**
     * When a slashing is applied to a staker.
     *
     * @param staker the staker's address
     * @param stake the amount of stake being slashed
     */
    void onSlashing(Address staker, long stake);
}
