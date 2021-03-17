package niva.aquamonitor.data.ws;

public class TestAuthentication {
    
    private static final String ERROR_MESSAGE = "System environment variable AQUAMONITOR_TEST_USER must be set prior to these tests. <username>:<password>";
    
    private static String[] sysEnvironment;
    static {
        String env = System.getProperty("AQUAMONITOR_TEST_USER");
        sysEnvironment = (env == null || !env.contains(":") ? null : env.split(":"));
    }
    
    public static String getUsername() {
        if (sysEnvironment == null || sysEnvironment.length != 2) {
            throw new IllegalStateException(ERROR_MESSAGE);
        }
        return sysEnvironment[0];
    }
    
    public static String getPassword() {
        if (sysEnvironment == null || sysEnvironment.length != 2) {
            throw new IllegalStateException(ERROR_MESSAGE);
        }
        return sysEnvironment[1];
    }
}
