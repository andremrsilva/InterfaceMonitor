package com.interfacesMeasurer.http;

import com.sun.grizzly.http.embed.GrizzlyWebServer;
import com.sun.grizzly.http.servlet.ServletAdapter;
import com.sun.jersey.spi.container.servlet.ServletContainer;

/**
 * User: andre
 * Date: 12/5/11
 * Time: 2:32 AM
 */
public class HttpProxy {

    private Integer port;

    public HttpProxy(String port) {
        this.port = Integer.parseInt(port);
    }

    public Boolean init() throws Exception{
        try{

            ServletAdapter adapter=new ServletAdapter();
            adapter.addInitParameter("com.sun.jersey.config.property.packages","com.interfacesMeasurer.http");
            adapter.setContextPath("/");
            adapter.setServletInstance(new ServletContainer());

            //Build the web server.
            GrizzlyWebServer webServer=new GrizzlyWebServer(port,".",false);

            //Add the servlet.
            webServer.addGrizzlyAdapter(adapter, new String[]{"/"});


            //Start it up.
            System.out.println(String.format("Jersey app started with WADL available at "
                    + "%sapplication.wadl\n",
                    "http://localhost:"+port+"/"));
            webServer.start();
            System.out.println(String.format("The calculated values can be seen at %smeasures\n",
                    "http://localhost:"+port+"/"));
        }catch(Exception e){
            System.out.println(String.format("Failed to start HTTP server: " + e));
            return false;
        }
        return true;
    }
}
