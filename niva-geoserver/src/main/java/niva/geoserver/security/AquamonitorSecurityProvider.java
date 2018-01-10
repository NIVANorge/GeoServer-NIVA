package niva.geoserver.security;


import org.geoserver.security.GeoServerAuthenticationProvider;
import org.geoserver.security.GeoServerSecurityProvider;
import org.geoserver.security.config.SecurityNamedServiceConfig;

/**
 * Vi tilbyr muligheten til å autentisere brukere mot Aquamonitors påloggingsløsning.
 * Dette innebærer at gitt brukernavn/passord testes mot www.aquamonitor.no/portal.
 * Dersom vi får et positivt resultat vil vi registreres med rollene:
 * AUTHENTICATED_ROLE, AQUAMONITOR_USER
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
