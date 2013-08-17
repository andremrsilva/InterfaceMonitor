package com.interfacesMeasurer.interfaces;

/**
 * User: andre
 * Date: 1/21/12
 * Time: 7:47 PM
 */
public class InterfaceTypeMapper {

    //Returns the interface type using values from rfc1213-mib2.asn1

    public static String getTypeName(int type){
        switch (type){
            case 1:return "other";
            case 2:return "regular1822";
            case 3:return "hdh1822";
            case 4:return "ddn-x25";
            case 5:return "rfc877-x25";
            case 6:return "ethernet-csmacd";
            case 7:return "iso88023-csmacd";
            case 8:return "iso88024-tokenBus";
            case 9:return "iso88025-tokenRing";
            case 10:return "iso88026-man";
            case 11:return "starLan";
            case 12:return "proteon-10Mbit";
            case 13:return "proteon-80Mbit";
            case 14:return "hyperchannel";
            case 15:return "fddi";
            case 16:return "lapb";
            case 17:return "sdlc";
            case 18:return "ds1";
            case 19:return "e1";
            case 20:return "basicISDN";
            case 21:return "primaryISDN";
            case 22:return "propPointToPointSerial";
            case 23:return "ppp";
            case 24:return "softwareLoopback";
            case 25:return "eon";
            case 26:return "ethernet-3Mbit";
            case 27:return "nsip";
            case 28:return "slip";
            case 29:return "ultra";
            case 30:return "ds3";
            case 31:return "sip";
            case 32:return "frame-relay";
            default:return "Unknown";
        }
    }
}
