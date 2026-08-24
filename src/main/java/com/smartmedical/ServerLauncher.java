package com.smartmedical;

import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

import java.io.File;

public class ServerLauncher {

    public static void main(String[] args) throws Exception {
        int port = 8080;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {}
        }

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);
        tomcat.getConnector(); // Force HTTP connector creation

        File webappDir = new File("src/main/webapp");
        StandardContext ctx = (StandardContext) tomcat.addWebapp("/SmartMedicalERP", webappDir.getAbsolutePath());
        
        // Pass parent classloader so Tomcat digester can find container classes
        ctx.setParentClassLoader(ServerLauncher.class.getClassLoader());

        File additionWebInfClasses = new File("target/classes");
        if (additionWebInfClasses.exists()) {
            WebResourceRoot resources = new StandardRoot(ctx);
            resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes",
                    additionWebInfClasses.getAbsolutePath(), "/"));
            ctx.setResources(resources);
        }

        System.out.println("\n=================================================================");
        System.out.println("  Smart Medical ERP System v2.0 Started Successfully!");
        System.out.println("  Access URL:   http://localhost:" + port + "/SmartMedicalERP/login");
        System.out.println("  Admin Login:  admin / Admin@123");
        System.out.println("=================================================================\n");

        tomcat.start();
        tomcat.getServer().await();
    }
}
