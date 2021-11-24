package niva.geoserver.security;


import org.geoserver.security.GeoServerAuthenticationProvider;
import org.geoserver.security.GeoServerSecurityProvider;
import org.geoserver.security.config.SecurityNamedServiceConfig;

/**
 * The Aquamonitor Security Provider provides authentication using the Aquamonitor API.
 * 
 * The roles provides are: AUTHENTICATED_ROLE, AQUAMONITOR_USER
 * 
 * 
 * @author Roar Brænden
 *
 */
public class AquamonitorSecurityProvider extends GeoServerSecurityProvider {
	
	
	@Override
	public Class<? extends GeoServerAuthenticationProvider> getAuthenticationProviderClass() {
		return AquamonitorAuthenticationProvider.class;
	}
	
	@Override
	public GeoServerAuthenticationProvider createAuthenticationProvider(SecurityNamedServiceConfig config) {
		return new AquamonitorAuthenticationProvider();
	}
	
}
