package com.interfacesMeasurer.util;

import org.snmp4j.smi.OID;

/**
 * User: andre
 * Date: 10/27/11
 * Time: 8:22 PM
 */
public class Oids {

    // use table with 64bits counters?? ->
    // http://tools.cisco.com/Support/SNMP/do/BrowseOID.do?local=en&translate=Translate&objectInput=1.3.6.1.2.1.31.1.1#oidContent

    public static final String INTERFACE_INDEX = "1.3.6.1.2.1.2.2.1.1";
    public static final String INTERFACE_DESCR = "1.3.6.1.2.1.2.2.1.2";
    public static final String INTERFACE_TYPE = "1.3.6.1.2.1.2.2.1.3";
    public static final String INTERFACE_MTU = "1.3.6.1.2.1.2.2.1.4";
    public static final String INTERFACE_SPEED = "1.3.6.1.2.1.2.2.1.5";
    public static final String INTERFACE_MAC_ADD = "1.3.6.1.2.1.2.2.1.6";
    public static final String INTERFACE_STATE = "1.3.6.1.2.1.2.2.1.8";
    public static final String INTERFACE_INOCT = "1.3.6.1.2.1.2.2.1.10";
    public static final String INTERFACE_OUTOCT = "1.3.6.1.2.1.2.2.1.16";

    public static final OID INTERFACE_INDEX_OID = new OID(INTERFACE_INDEX);
    public static final OID INTERFACE_DESCR_OID = new OID(INTERFACE_DESCR);
    public static final OID INTERFACE_TYPE_OID = new OID(INTERFACE_TYPE);
    public static final OID INTERFACE_MTU_OID = new OID(INTERFACE_MTU);
    public static final OID INTERFACE_SPEED_OID = new OID(INTERFACE_SPEED);
    public static final OID INTERFACE_MAC_ADD_OID = new OID(INTERFACE_MAC_ADD);
    public static final OID INTERFACE_STATE_OID = new OID(INTERFACE_STATE);
    public static final OID INTERFACE_INOCT_OID = new OID(INTERFACE_INOCT);
    public static final OID INTERFACE_OUTOCT_OID = new OID(INTERFACE_OUTOCT);

}
