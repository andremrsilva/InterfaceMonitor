InterfaceMonitor
================


InterfaceMonitor is a small Java project based on SNMP to connect to a local or remote SNMP agent in order to obtain information about the network interfaces usage and status.

This project uses two main components:
 - snmp4j to allow the conection to the destination MIB
 - grizzly-webserver to make the gathered information available via a simple Webservice, with a graphical presentation.


Apart from the current feature of this project, it is also a great example of the simple usage of both libraries, and can be improved to monitor any other values available on MIB's.


Usage:

    $java -jar interfaceMonitor-1.0.jar -h

	    Interfaces monitor help

    ---Optional args:
    -f                  use this flag to find the interval the destination MIB is updated
    
    -agentIp=<ip>       (default:localhost) ip address where the snmp agent is listening
    -agentPort=<port>   (default:7000) port where the snmp agent is listening
    -httpPort=<port>    (default:9999) port where the web service will be available
    -t=<timeInMilisecs> (default:5000) html table with results pooling time in miliseconds
    -p=<timeInMilisecs> (default:15000) interval between each pool to the snmp agent

