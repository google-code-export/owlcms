package launch;

import java.io.File;

import org.apache.catalina.startup.Tomcat;

/**
 * Launch a web application as a user process.
 * @author jflamy
 *
 */
public class Main {

    /**
     * The directory that contains the web application files and the context
     * are named the same (as if they came from a .war)
     */
    private static final String APPLICATION_NAME = "owlcms";

	public static void main(String[] args) throws Exception {

        // The installation directory can be set through an environment variable
    	// Normally the launching script sets the current directory to be the location
    	// of this program.
    	String installDir = System.getenv("OWLCMS_DIR");
    	if (installDir == null || installDir.isEmpty()) {
    		installDir = System.getProperty("user.dir");
    	}
        // The web application is one level below.
        String webappDir = installDir+"/"+APPLICATION_NAME;

        //The port that we should run on can be set into an environment variable
        String webPort = System.getenv("OWLCMS_PORT");
        if(webPort == null || webPort.isEmpty()) {
            webPort = "80";
        }

        // configure the application and its URL
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(Integer.valueOf(webPort));
        tomcat.addWebapp("/"+APPLICATION_NAME, new File(webappDir).getCanonicalPath());
        System.setProperty("net.sf.ehcache.skipUpdateCheck","true");

        // start the server and wait
        tomcat.start();
        tomcat.getServer().await();  
    }
}