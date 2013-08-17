package com.interfacesMeasurer;

import com.interfacesMeasurer.http.HttpProxy;
import com.interfacesMeasurer.interfaces.Interface;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.TableEvent;
import org.snmp4j.util.TableUtils;
import com.interfacesMeasurer.util.Oids;

import java.io.IOException;
import java.util.*;

public class EthSNMP {

    // MIB cache validity finder
    private long INITIAL_TIME = 600000;// initialize it with 10 minutes
    private long interfacesValuesUpdateInterval = INITIAL_TIME;// initialize it with 10 minutes
    public static Map<String,Long> lastIns = new HashMap<String, Long>();
    public static Map<String,Long> lastOuts = new HashMap<String, Long>();
    public static Map<String,Long> lastMeasures = new HashMap<String, Long>();
    public static Map<String,Boolean> firstChangeIsDone = new HashMap<String, Boolean>();

    // interfaces monitor variables
    private long destinationBootTimestamp;
    private Snmp snmp;
    private Target target;
    private Thread thread;
    public static Map<String,Interface> interfaces = new HashMap<String, Interface>();
    public static int tableRefreshTimeInMilis = 5000;

    public EthSNMP(String ip, String port, Integer htmlTableRefreshTime) throws Exception{
        tableRefreshTimeInMilis = htmlTableRefreshTime;
        initSnmp(ip, port);
    }

    public void stop() throws Exception{
        if (thread.isAlive()){
            thread.interrupt();
        }
    }

    public Boolean go(final Boolean findMode,final Long poolingTime){

        if (findMode){
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    if (interfacesValuesUpdateInterval == INITIAL_TIME){
                        System.out.println("\nIt was impossible to calculate the interval. (Finished too soon??)");
                    }else{
                        System.out.println("\nThe calculated time was " + interfacesValuesUpdateInterval + " miliseconds");
                    }
                }
            });
        }

        // Get destination timestamp

        PDU pdu = new PDU();
        pdu.add(new VariableBinding(SnmpConstants.sysUpTime));
        try {
            ResponseEvent response = snmp.get(pdu, target);
            TimeTicks t = (TimeTicks)response.getResponse().getVariable(SnmpConstants.sysUpTime);
            destinationBootTimestamp = (new Date()).getTime() - t.toMilliseconds();
        }catch (Exception e){
            System.out.println("impossible reach MIB");
            return false;
        }

        // Fill interfaces

        TableUtils tableUtils;
        tableUtils = new TableUtils(snmp,new DefaultPDUFactory());

        OID[] columnsToRequest = new OID[]{Oids.INTERFACE_DESCR_OID,Oids.INTERFACE_SPEED_OID,
                Oids.INTERFACE_TYPE_OID, Oids.INTERFACE_MAC_ADD_OID, Oids.INTERFACE_MTU_OID};
        List<TableEvent> tableEvents = tableUtils.getTable(target, columnsToRequest,null,null);

        String name = "";
        String mac = "";
        int type = 0;
        Long bandwidth = 0L;
        int mtu = 0;
        for (TableEvent event : tableEvents) {
            for(VariableBinding vb: event.getColumns()) {
                if (vb.getOid().startsWith(Oids.INTERFACE_DESCR_OID)){
                    name = vb.getVariable().toString();
                }else if (vb.getOid().startsWith(Oids.INTERFACE_SPEED_OID)){
                    bandwidth = vb.getVariable().toLong();
                    if (bandwidth == 0){
                        bandwidth = 54000000L;
                    }
                }else if (vb.getOid().startsWith(Oids.INTERFACE_TYPE_OID)){
                    type = vb.getVariable().toInt();
                }else if (vb.getOid().startsWith(Oids.INTERFACE_MTU_OID)){
                    mtu = vb.getVariable().toInt();
                }else if (vb.getOid().startsWith(Oids.INTERFACE_MAC_ADD_OID)){
                    mac = vb.getVariable().toString();
                }
            }
            Interface inter = new Interface(name,bandwidth, event.getIndex(), type, mac, mtu);
            interfaces.put(name,inter);
        }

        if (destinationBootTimestamp == 0 || interfaces.size() == 0){
            String errorMessage = "Impossible to fetch required params: ";
            errorMessage += destinationBootTimestamp == 0 ? "'boot time from destination machine' " : "";
            errorMessage += interfaces.size() == 0 ? "'interfaces list from destination machine'" : "";
            System.out.println(errorMessage);
            return false;
        }

        //Start thread to pool values from MIB

        Runnable r1 = new Runnable() {
            public void run() {
                try {
                    while (true) {

                        for (Interface interf : interfaces.values()){

                            PDU pdu = new PDU();

                            pdu.add(new VariableBinding(SnmpConstants.sysUpTime));
                            pdu.add(new VariableBinding(((OID)Oids.INTERFACE_STATE_OID.clone()).append(interf.getIndex())));
                            pdu.add(new VariableBinding(((OID)Oids.INTERFACE_DESCR_OID.clone()).append(interf.getIndex())));
                            pdu.add(new VariableBinding(((OID)Oids.INTERFACE_OUTOCT_OID.clone()).append(interf.getIndex())));
                            pdu.add(new VariableBinding(((OID)Oids.INTERFACE_INOCT_OID.clone()).append(interf.getIndex())));

                            pdu.setType(PDU.GET);

                            ResponseListener listener = new ResponseListener() {
                                public void onResponse(ResponseEvent event) {

                                    int state = -1;
                                    String name = "";
                                    long outOctets = 0;
                                    long inOctets = 0;
                                    long timeInMilliseconds = 0;

                                    PDU strResponse;
                                    ((Snmp)event.getSource()).cancel(event.getRequest(), this);
                                    strResponse = event.getResponse();
                                    if (strResponse!= null) {
                                        for(VariableBinding vb: strResponse.getVariableBindings()) {
                                            if (vb.getOid().toString().contains(Oids.INTERFACE_STATE)){
                                                state = vb.getVariable().toInt();
                                            }else if (vb.getOid().toString().contains(Oids.INTERFACE_DESCR)){
                                                name = vb.getVariable().toString();
                                            }else if (vb.getOid().toString().contains(SnmpConstants.sysUpTime.toString())){
                                                TimeTicks t = new TimeTicks((TimeTicks)vb.getVariable());
                                                timeInMilliseconds = t.toMilliseconds();
                                            }else if (vb.getOid().toString().contains(Oids.INTERFACE_OUTOCT)){
                                                outOctets = vb.getVariable().toLong();
                                            }else if (vb.getOid().toString().contains(Oids.INTERFACE_INOCT)){
                                                inOctets = vb.getVariable().toLong();
                                            }
                                        }
                                        if (!interfaces.containsKey(name)){
                                            System.out.println("A new interface was found: " + name);
                                        }else{
                                            Interface inter = interfaces.get(name);
                                            inter.setState(state);


                                            if (findMode){

                                                // If in find Mode, the values will only be compared with the last one
                                                // to find the smaller interval where the MIB was updated

                                                long lastIn;
                                                long lastOut;

                                                if (!lastIns.containsKey(name)){
                                                    lastIns.put(name,inOctets);
                                                }
                                                if (!lastOuts.containsKey(name)){
                                                    lastOuts.put(name,outOctets);
                                                }
                                                if (!lastMeasures.containsKey(name)){
                                                    lastMeasures.put(name,timeInMilliseconds);
                                                }
                                                if (!firstChangeIsDone.containsKey(name)){
                                                    firstChangeIsDone.put(name,false);
                                                }

                                                lastIn = lastIns.get(name);
                                                lastOut = lastOuts.get(name);

                                                lastIns.put(name,inOctets);
                                                lastOuts.put(name,outOctets);
                                                if (lastIn != inOctets || lastOut != outOctets){
                                                    Long lastMeasure = lastMeasures.get(name);
                                                    if (firstChangeIsDone.get(name)){
                                                        if (timeInMilliseconds - lastMeasure < interfacesValuesUpdateInterval){
                                                            interfacesValuesUpdateInterval = timeInMilliseconds - lastMeasure;
                                                            System.out.println("The minimum update time found till" +
                                                                    " now was " + interfacesValuesUpdateInterval + " milliseconds.");
                                                        }
                                                    }else{
                                                        firstChangeIsDone.put(name,true);
                                                    }
                                                    lastMeasures.put(name,timeInMilliseconds);
                                                }
                                            }else{
                                                inter.addMeasure(destinationBootTimestamp + timeInMilliseconds,inOctets,outOctets);
                                            }
                                        }
                                    }
                                }};
                            try{
                                snmp.send(pdu, target, null, listener);
                            } catch (Exception e) {
                                System.out.println("Error while sending snmp request: " + e);
                            }
                        }

                        if (findMode){
                            // the pooling time in find mode is 100 milliseconds
                            Thread.sleep(100L);
                        }else{
                            Thread.sleep(poolingTime);
                        }
                    }
                } catch (InterruptedException iex) {
                    System.out.println("Application stopped. press ctrl+c");
                }
            }
        };

        thread = new Thread(r1);
        thread.start();
        return true;
    }

    public void initSnmp(String ip, String port) throws Exception{
        OctetString community = new OctetString( "private");
        Address address = new UdpAddress( ip + "/" + port);
        TransportMapping transport;
        try {
            transport = new DefaultUdpTransportMapping();
            snmp = new Snmp( transport );
        }
        catch ( IOException e ) {
            System.out.println("It was impossible to start SNMP agent: " + e);
            throw e;
        }

        CommunityTarget targetX = new CommunityTarget();
        targetX.setCommunity( community );
        target = targetX;

        target.setVersion( SnmpConstants.version2c );
        target.setAddress( address );
        target.setRetries( 1 );
        target.setTimeout( 1000 );

        try {
            snmp.listen();
        }catch ( IOException e ){
            System.out.println("It was impossible to start SNMP agent: " + e);
            throw e;
        }
    }

    protected static void printHelp(){
        System.out.println("Interfaces monitor help\n\n" +
                "---Optional args:\n" +
                "-f                  use this flag to find the interval the destination MIB is updated\n\n" +
                "-agentIp=<ip>       (default:localhost) ip address where the snmp agent is listening\n" +
                "-agentPort=<port>   (default:7000) port where the snmp agent is listening\n" +
                "-httpPort=<port>    (default:9999) port where the web service will be available\n" +
                "-t=<timeInMilisecs> (default:5000) html table with results pooling time in miliseconds\n" +
                "-p=<timeInMilisecs> (default:15000) interval between each pool to the snmp agent\n");

    }

    public static void main( String args[] ) throws Exception{

        String portHTTP = "9999";
        String agentIp = "localhost";
        String agentPort = "7000";
        Boolean findMode = false;
        Integer htmlTableRefreshTime = 5000;
        Long poollingTime = 15000L;

        for (String actualArg : args){
            if (actualArg.startsWith("-httpPort=")){
                portHTTP = actualArg.split("=")[1];
            }else if (actualArg.equals("-h") || actualArg.startsWith("--help")){
                printHelp();
                return;
            }else if (actualArg.startsWith("-agentIp=")){
                agentIp = actualArg.split("=")[1];
            }else if (actualArg.startsWith("-agentPort=")){
                agentPort = actualArg.split("=")[1];
            }else if (actualArg.startsWith("-f")){
                findMode = true;
            }else if (actualArg.startsWith("-t=")){
                htmlTableRefreshTime = Integer.parseInt(actualArg.split("=")[1]);
            }else if (actualArg.startsWith("-p=")){
                poollingTime = Long.parseLong(actualArg.split("=")[1]);
            }else{
                System.out.println("WARNING: Unkown argument (-h or --help):" + actualArg);
            }
        }

        EthSNMP app = new EthSNMP(agentIp, agentPort, htmlTableRefreshTime);

        if (!app.go(findMode, poollingTime)){
            return;
        }
        if (!findMode){
            HttpProxy proxy = new HttpProxy(portHTTP);
            if (!proxy.init()){
                app.stop();
            }
        }else{
            System.out.println("The aplication is going to fetch some values from the snmp agent in order to find the " +
                    "interval between each MIB update. The result should be used to start the application (argument -p=<value>)" +
                    " so that the measures taken from the agent are more accurate. To stop press ctrl+C");
        }
    }
}