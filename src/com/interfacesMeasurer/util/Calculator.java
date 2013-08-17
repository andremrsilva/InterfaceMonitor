package com.interfacesMeasurer.util;

import com.interfacesMeasurer.interfaces.BandwidthMark;
import com.interfacesMeasurer.interfaces.Interface;

import java.util.Date;
import java.util.TreeMap;

/**
 * User: andre
 * Date: 10/28/11
 * Time: 1:58 AM
 */
public class Calculator {

    private static long COUNTERS_MAX_VALUE = 4294967295L;


    /**
     * this function removes the bandwidth marks that are older than one hour and calculates the usage percentage for the
     * last minute
     *
     * @param inter the interface to be used
     * @return the percentage of usage
     */
    public static int calculateLastMinuteUsage(Interface inter){

        TreeMap<Long,BandwidthMark> minuteMeasures = inter.getLastMinuteMeasures();

        long now = (new Date()).getTime();
        if (minuteMeasures.size() == 0){
            return 0;
        }
        // remove outdated values
        while (minuteMeasures.firstKey() < now - 60000){
            minuteMeasures.remove(minuteMeasures.firstKey());
        }
        BandwidthMark finalMark = minuteMeasures.lastEntry().getValue();
        BandwidthMark initialMark = minuteMeasures.firstEntry().getValue();

        // TODO find a way to identify the duplex mode for each interface and use the correct function using that
        // for now, just use half duplex to work correctly on my pc
        int actualPercentage = calculateHalfDuplexBandwith( initialMark, finalMark, inter.getMaxBandwidth());
        return actualPercentage;
    }

    /**
     * this function removes the bandwidth marks that are older than one hour and calculates the usage percentage for the
     * last hour
     *
     * @param inter the interface to be used
     * @return the percentage of usage
     */
    public static int calculateLastHourUsage(Interface inter){

        TreeMap<Long,BandwidthMark> hourMeasures = inter.getLastHourMeasures();

        long now = (new Date()).getTime();
        if (hourMeasures.size() == 0){
            return 0;
        }

        // remove outdated values
        while (hourMeasures.firstKey() < now - 3600000){
            hourMeasures.remove(hourMeasures.firstKey());
        }

        BandwidthMark finalMark = hourMeasures.lastEntry().getValue();
        BandwidthMark initialMark = hourMeasures.firstEntry().getValue();

        // TODO find a way to identify the duplex mode for each interface and use the correct function using that
        // for now, just use half duplex to work correctly on my pc
        int actualPercentage = calculateHalfDuplexBandwith( initialMark, finalMark, inter.getMaxBandwidth());
        return actualPercentage;
    }

    /**
     * calculate the percentage of usage between two bandwidth marks, using the full duplex expression documented by
     * <a href="http://www.cisco.com/en/US/tech/tk648/tk362/technologies_tech_note09186a008009496e.shtml">Cisco</a>
     *
     * @param initialMark the mark with the older timestamp
     * @param finalMark the mark with the most recent timestamp
     * @param maxBandwidth the maxbandwith defined for the interface being calculated
     * @return the percentage used in the given interval of time
     */
    public static int calculateFullDuplexBandwith(BandwidthMark initialMark, BandwidthMark finalMark,
                                                  Long   maxBandwidth){

        long deltaIn = getInDelta(initialMark, finalMark);
        long deltaOut = getOutDelta(initialMark, finalMark);
        long deltaTime = (finalMark.getTimestamp() - initialMark.getTimestamp())/1000;

        // deltaTime and maxBandWidth can't be 0
        // in the tested MIB's, it was impossible to get the Wireless interfaces max badwidth, so put it hardcoded
        if (maxBandwidth == 0) maxBandwidth = 54000000L;
        if (deltaTime == 0) deltaTime = 1;

        return (int )((Math.max(deltaIn,deltaOut) * 8 * 100 )/(deltaTime * maxBandwidth));
    }

    /**
     * calculate the percentage of usage between two bandwidth marks, using the half duplex expression documented by
     * <a href="http://www.cisco.com/en/US/tech/tk648/tk362/technologies_tech_note09186a008009496e.shtml">Cisco</a>
     *
     * @param initialMark the mark with the older timestamp
     * @param finalMark the mark with the most recent timestamp
     * @param maxBandwidth the maxbandwith defined for the interface being calculated
     * @return the percentage used in the given interval of time
     */
    public static int calculateHalfDuplexBandwith(BandwidthMark initialMark, BandwidthMark finalMark,
                                                  Long   maxBandwidth){

        long deltaIn = getInDelta(initialMark, finalMark);
        long deltaOut = getOutDelta(initialMark, finalMark);
        long deltaTime = (finalMark.getTimestamp() - initialMark.getTimestamp())/1000;

        // deltaTime and maxBandWidth can't be 0
        // in the tested MIB's, it was impossible to get the Wireless interfaces max badwidth, so put it hardcoded
        if (maxBandwidth == 0) maxBandwidth = 54000000L;
        if (deltaTime == 0) deltaTime = 1;

        return (int )(((deltaIn + deltaOut) * 8 * 100 )/(deltaTime * maxBandwidth));
    }

    /**
     * returns the delta between initial and final inOctetcs for two given bandwidth marks
     * This method calculates the delta using the number of times that the counter has restarted
     *
     * @param initialMark the mark with the older timestamp
     * @param finalMark the mark with the most recent timestamp
     * @return the calculated delta
     */
    private static long getInDelta(BandwidthMark initialMark, BandwidthMark finalMark){
        long numberOfRestarts = 0;
        if (finalMark.getInRestarts() > initialMark.getInRestarts()){
            numberOfRestarts = finalMark.getInRestarts() - initialMark.getInRestarts();
        }
        return (finalMark.getInOctets() + (COUNTERS_MAX_VALUE * numberOfRestarts)) - initialMark.getInOctets();
    }

    /**
     * returns the delta between initial and final outOctetcs for two given bandwidth marks
     * This method calculates the delta using the number of times that the counter has restarted
     *
     * @param initialMark the mark with the older timestamp
     * @param finalMark the mark with the most recent timestamp
     * @return the calculated delta
     */
    private static long getOutDelta(BandwidthMark initialMark, BandwidthMark finalMark){
        long numberOfRestarts = 0;
        if (finalMark.getOutRestarts() > initialMark.getOutRestarts()){
            numberOfRestarts = finalMark.getOutRestarts() - initialMark.getOutRestarts();
        }
        return (finalMark.getOutOctets() + (COUNTERS_MAX_VALUE * numberOfRestarts)) - initialMark.getOutOctets();
    }
}
