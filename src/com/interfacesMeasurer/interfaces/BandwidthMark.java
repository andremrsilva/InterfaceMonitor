package com.interfacesMeasurer.interfaces;

/**
 * User: andre
 * Date: 10/28/11
 * Time: 2:13 AM
 */
public class BandwidthMark {

    private long timestamp;
    private long inOctets;
    private long outOctets;

    private long inRestarts = 0;
    private long outRestarts = 0;

    public BandwidthMark(long timestamp, long inOctects, long outOctets, long inRestarts, long outRestarts) {
        this.timestamp = timestamp;
        this.inOctets = inOctects;
        this.outOctets = outOctets;
        this.inRestarts = inRestarts;
        this.outRestarts = outRestarts;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getInOctets() {
        return inOctets;
    }

    public long getOutOctets() {
        return outOctets;
    }

    public long getInRestarts() {
        return inRestarts;
    }

    public long getOutRestarts() {
        return outRestarts;
    }
}
