package niva.geoserver.security;

import org.geoserver.security.web.auth.AuthenticationProviderPanelInfo;


public class AquamonitorAuthenticationProviderPanelInfo 
			extends AuthenticationProviderPanelInfo<AquamonitorAuthenticationProviderConfig,
													AquamonitorAuthenticationProviderPanel> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2687055982606539297L;
	
	public AquamonitorAuthenticationProviderPanelInfo() {
	    
        setComponentClass(AquamonitorAuthenticationProviderPanel.class);
        setServiceClass(AquamonitorAuthenticationProvider.class);
        setServiceConfigClass(AquamonitorAuthenticationProviderConfig.class);
        setPriority(0);
	}

}
