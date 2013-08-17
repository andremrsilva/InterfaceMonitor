package com.interfacesMeasurer.interfaces;

import com.interfacesMeasurer.util.Calculator;
import org.snmp4j.smi.OID;

import java.util.TreeMap;

/**
 * User: andre
 * Date: 10/27/11
 * Time: 8:13 PM
 */
public class Interface {

    private String name;
    private String mac;
    private int state;
    private Long maxBandwidth;
    private int mtu;
    private OID index;
    private String type;

    private long inRestartsCounter = 0;
    private long outRestartsCounter = 0;

    private TreeMap<Long,BandwidthMark> lastMinuteMeasures;
    private TreeMap<Long,BandwidthMark> lastHourMeasures;

    public Interface(String name, Long maxBandwidth, OID index, int type, String mac, int mtu) {
        this.name = name;
        this.mac = mac;
        this.maxBandwidth = maxBandwidth;
        this.mtu = mtu;
        this.index = index;
        this.type = InterfaceTypeMapper.getTypeName(type);
        lastMinuteMeasures = new TreeMap<Long,BandwidthMark>();
        lastHourMeasures = new TreeMap<Long,BandwidthMark>();
    }


    // public methods

    public void addMeasure(long timestamp, long inOctets, long outOctets){
        // check if counters were restarted
        if (lastHourMeasures.size() > 0 && lastHourMeasures.lastKey() < timestamp){
            if (lastHourMeasures.lastEntry().getValue().getInOctets() > inOctets){
                inRestartsCounter++;
            }
            if (lastHourMeasures.lastEntry().getValue().getOutOctets() > outOctets){
                outRestartsCounter++;
            }
        }
        BandwidthMark bandwidthMark =
                new BandwidthMark(timestamp,inOctets,outOctets,inRestartsCounter,outRestartsCounter);
        lastMinuteMeasures.put(bandwidthMark.getTimestamp(), bandwidthMark);
        lastHourMeasures.put(bandwidthMark.getTimestamp(), bandwidthMark);
    }

    public int getLastHourUsage(){
        return Calculator.calculateLastHourUsage(this);
    }

    public int getLastMinuteUsage(){
        return Calculator.calculateLastMinuteUsage(this);
    }

    // getters and setters

    public String getName() {
        return name;
    }

    public String getMac() {
        return mac;
    }

    public int getState() {
        return state;
    }

    public Long getMaxBandwidth() {
        return maxBandwidth;
    }

    public int getMtu() {
        return mtu;
    }

    public OID getIndex() {
        return index;
    }

    public String getType() {
        return type;
    }

    public void setState(int state) {
        this.state = state;
    }

    public TreeMap<Long, BandwidthMark> getLastMinuteMeasures() {
        return lastMinuteMeasures;
    }

    public TreeMap<Long, BandwidthMark> getLastHourMeasures() {
        return lastHourMeasures;
    }
}
