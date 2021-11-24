package niva.geoserver.security;

import org.geoserver.security.config.BaseSecurityNamedServiceConfig;
import org.geoserver.security.config.SecurityAuthProviderConfig;


/**
 * Display options for AquamonitorAuthenticationProvider within GeoServer -> Security -> Authentication
 * 
 * @author Roar Brænden, NIVA
 *
 */
public class AquamonitorAuthenticationProviderConfig extends BaseSecurityNamedServiceConfig 
													 implements SecurityAuthProviderConfig{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3129440459632428117L;
	

	@Override
	public String getUserGroupServiceName() {
		return null;
	}

	@Override
	public void setUserGroupServiceName(String userGroupServiceName) {
	}


}
