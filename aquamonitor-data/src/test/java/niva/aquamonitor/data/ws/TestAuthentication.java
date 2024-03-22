package niva.aquamonitor.data.ws;

public class TestAuthentication {
    
    private static final String ERROR_MESSAGE = "System environment variable AQUAMONITOR_TEST_USER must be set prior to these tests. <username>:<password>";
    private static final String TEST_USER = "AQUAMONITOR_TEST_USER";
    private static String[] sysEnvironment;
    static {
        String env = System.getProperty(TEST_USER);
        if (env == null) {
        	env = System.getenv(TEST_USER);
        }
        sysEnvironment = (env == null || !env.contains(":") ? null : env.split(":"));
    }
    
    /**
     * Username set as system environment variable AQUAMONITOR_TEST_USER
     */
    public static String getUsername() {
        if (sysEnvironment == null || sysEnvironment.length != 2) {
            throw new IllegalStateException(ERROR_MESSAGE);
        }
        return sysEnvironment[0];
    }
    
    /**
     * Password set as system environment variable AQUAMONITOR_TEST_USER
     */
    public static String getPassword() {
        if (sysEnvironment == null || sysEnvironment.length != 2) {
            throw new IllegalStateException(ERROR_MESSAGE);
        }
        return sysEnvironment[1];
    }
}
