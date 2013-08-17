package com.interfacesMeasurer.http;

/**
 * User: andre
 * Date: 12/6/11
 * Time: 4:10 PM
 */
import com.interfacesMeasurer.EthSNMP;

import javax.servlet.http.HttpServletRequest;
import com.interfacesMeasurer.interfaces.Interface;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.util.Collection;
import java.util.List;
import java.util.Map;


@Path("/")
public class HttpRequestsHandler {

    @Context
    HttpServletRequest request;


    @GET @Path("/measures")
    public String measures() {
        StringBuffer s = new StringBuffer();
        s.append("<html>\n" +
                "\n" +
                "<style type=\"text/css\">\n" +
                "\n" +
                "body {text-align:center;}" +
                "h1 {text-align:center;}" +
                "table, td, th\n" +
                "{\n" +
                "border:1px solid green;\n" +
                "text-align:center;" +
                "}\n" +
                "th\n" +
                "{\n" +
                "background-color:green;\n" +
                "color:white;\n" +
                "}\n" +
                "</style>\n" +
                "</head>"+
                "<body>\n" +
                "\n" +
                "<script language=\"javascript\" type=\"text/javascript\">\n" +
                "<!-- \n" +
                "function updateInterfaces(){\n" +
                "var ajaxRequest;\n" +
                "\n" +
                "try{\n" +
                "ajaxRequest = new XMLHttpRequest();\n" +
                "} catch (e){\n" +
                "try{\n" +
                "ajaxRequest = new ActiveXObject(\"Msxml2.XMLHTTP\");\n" +
                "} catch (e) {\n" +
                "try{\n" +
                "ajaxRequest = new ActiveXObject(\"Microsoft.XMLHTTP\");\n" +
                "} catch (e){\n" +
                "return false;\n" +
                "}\n" +
                "}\n" +
                "}\n" +
                "ajaxRequest.onreadystatechange = function(){\n" +
                "if(ajaxRequest.readyState == 4){\n" +
                "var interfDivs = document.getElementById('interfDivs');\n" +
                "interfDivs.innerHTML = ajaxRequest.responseText;\n" +
                "}\n" +
                "}\n" +
                "ajaxRequest.open(\"GET\", document.URL + \"/table\", true);\n" +
                "ajaxRequest.send(null); \n" +
                "}\n" +
                "\n" +
                "//-->\n" +
                "setInterval( \"updateInterfaces()\", " +EthSNMP.tableRefreshTimeInMilis+ " );" +
                "</script>"+
                "\n" +
                "<h1>Interfaces Monitor</h1>\n" +
                "\n");
        s.append("<div id='interfDivs'>" + values() + "</div>");
        s.append("</body>\n" +
                "</html>");


        return  s.toString();
    }
    @GET @Path("/measures/table")
    public String values() {
        StringBuffer s = new StringBuffer();

        s.append("<table border=\"1\" align=\"center\">\n" +
                "<tr>\n" +
                "<th>Interface<br/>Type(Name)</th>\n" +
                "<th>Nominal Bandwidth</th>\n" +
                "<th>MTU</th>\n" +
                "<th>State</th>\n" +
                "<th>Mac Address</th>\n" +
                "<th>%Bandwidth <br/>" +
                "(last minute)</th>\n" +
                "<th>%Bandwidth <br/>" +
                "(last hour)</th>\n" +
                "</tr>\n");

        Map<String,Interface> stringInterfaceMap =  EthSNMP.interfaces;

        for (Interface inter : stringInterfaceMap.values()){

            s.append("<tr>\n<td>" + inter.getType()+ " ("+inter.getName()+")</td>\n");
            if (inter.getState() == 1){
                s.append("<td>"+getMeasureAsString(inter.getMaxBandwidth())+"</td>\n" +
                        "<td>"+inter.getMtu()+"</td>\n" +
                        "<td>UP</td>\n" +
                        "<td>"+inter.getMac()+"</td>\n" +
                        "<td>"+inter.getLastMinuteUsage()+" % </td>\n" +
                        "<td>"+inter.getLastHourUsage()+" % </td>\n" );
            }else{
                s.append("<td bgcolor='grey' >"+getMeasureAsString(inter.getMaxBandwidth())+"</td>\n" +
                        "<td bgcolor='grey' >"+inter.getMtu()+"</td>\n" +
                        "<td bgcolor='grey' >DOWN</td>\n" +
                        "<td bgcolor='grey' >"+inter.getMac()+"</td>\n" +
                        "<td bgcolor='grey' >-</td>\n" +
                        "<td bgcolor='grey' >-</td>\n" +
                        "</tr>\n");
            }
        }
        s.append("</table>");


        return  s.toString();
    }

    private String getMeasureAsString(Long bw){
        if (bw < 1000){
            return bw + " bits/s";
        }else if(bw < 1000){
            return (bw / 1000) + " Kbits/s";
        }else{
            return (bw / 1000000) + " Mbits/s";
        }
    }


}

