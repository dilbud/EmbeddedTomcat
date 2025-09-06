package org.dbx.host.config;

import org.apache.catalina.Context;
import org.apache.catalina.Manager;
import org.apache.catalina.Server;
import org.apache.catalina.Wrapper;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.manager.HTMLManagerServlet;
import org.apache.catalina.manager.ManagerServlet;
import org.apache.catalina.mbeans.MBeanFactory;
import org.apache.catalina.startup.HostConfig;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.modeler.Registry;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.management.ObjectInstance;
import javax.management.ObjectName;
import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.LinkedHashSet;

@Configuration
public class TomcatConfig {
//    @Bean
//    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> customTomcatManager() {
//        return factory -> {
//            factory.setPort(8082);
//            factory.addContextCustomizers(context -> {
//                try {
//                    // Load Manager application path from classpath
//                    ClassLoader classLoader = getClass().getClassLoader();
//                    URL resource = classLoader.getResource("manager");
//                    if (resource == null) {
//                        throw new IllegalStateException("Manager application not found in classpath");
//                    }
//
//                    String managerAppPath = new File(resource.toURI()).getAbsolutePath();
//
//                    // Create a new Tomcat instance
//                    Tomcat tomcat = new Tomcat();
//
//                    // Add user and role for Manager GUI
//                    tomcat.addUser("admin", "password");
//                    tomcat.addRole("admin", "manager-gui");
//
//                    // Add Manager application
//                    Context managerContext = tomcat.addWebapp("/manager", managerAppPath);
//                    System.out.println("Manager app added at context path: " + managerContext.getPath());
//                } catch (Exception e) {
//                    throw new IllegalStateException("Failed to add Tomcat Manager app", e);
//                }
//            });
//        };
//    }

    private static class CustomTomcatServletWebServerFactory extends TomcatServletWebServerFactory {
        public CustomTomcatServletWebServerFactory() {
            super();
            this.setDisableMBeanRegistry(false);
        }

        public CustomTomcatServletWebServerFactory(int port) {
            super(port);
            this.setDisableMBeanRegistry(false);
        }

        public CustomTomcatServletWebServerFactory(String contextPath, int port) {
            super(contextPath, port);
            this.setDisableMBeanRegistry(false);
        }

        @Override
        public void setDisableMBeanRegistry(boolean disableMBeanRegistry) {
            super.setDisableMBeanRegistry(false);
        }

        @Override
        public WebServer getWebServer(ServletContextInitializer... initializers) {
            return super.getWebServer(initializers);
        }

        @Override
        protected TomcatWebServer getTomcatWebServer(Tomcat tomcat) {
            // This is the right place to add additional contexts to the
            // embedded Tomcat instance that Spring Boot will use.
            try {

                tomcat.addUser("admin", "admin");
                tomcat.addRole("admin", "manager-gui");
                ClassLoader classLoader = getClass().getClassLoader();
                URL resource = classLoader.getResource("manager");
                if (resource == null) {
                    throw new IllegalStateException("Manager application not found in classpath");
                }

                String managerAppPath = new File(resource.toURI()).getAbsolutePath();
//
//                    // Step 1: Get the Class object for SomeClass
//                    Class<?> clazz = Class.forName("org.apache.tomcat.util.modeler.Registry");
//
//                    // Step 2: Retrieve the private static field “SOME_STATIC_FIELD”
//                    Field field = clazz.getDeclaredField("registry");
//
//                    // Step 3: Make the field accessible
//                    field.setAccessible(true);
//
//                    // Step 4: Override the value of the static field
//                    // Note: For static fields, pass null as the object reference
//                    field.set(null, new Registry(){
//
//                    });
                Registry registry = Registry.getRegistry(null, null);
                // Passing null will load all available descriptors on the classpath
//                    registry.loadDescriptors("org.apache.catalina.ha.deploy", Thread.currentThread().getContextClassLoader());
                // Add the manager context to the running instance
                Context managerContext = tomcat.addWebapp("/manager", managerAppPath);
                StandardHost s = (StandardHost) tomcat.getHost();
                s.addLifecycleListener(new HostConfig());
//                managerContext.createWrapper();
//
//                HTMLManagerServlet htmlManagerServlet = new HTMLManagerServlet();
//                Wrapper wrapper = tomcat.addServlet(managerContext.getPath(), "yyy", htmlManagerServlet);
//                htmlManagerServlet.setWrapper(wrapper);
                // Note: if you need to add security settings (users, roles, realm configuration)
                // for this context, you’ll need to configure the Realm on the parent Engine or Host.
                System.out.println("Manager app added at context path: " + managerContext.getPath());
            } catch (Exception e) {
                throw new IllegalStateException("Failed to add Tomcat Manager app", e);
            }
            return super.getTomcatWebServer(tomcat);
        }

    }


    @Bean
    public TomcatServletWebServerFactory tomcatFactory() {
        TomcatServletWebServerFactory tomcatServletWebServerFactory = new CustomTomcatServletWebServerFactory(8080);
        tomcatServletWebServerFactory.setBaseDirectory(new File("./deploy"));
        return tomcatServletWebServerFactory;
    }
}