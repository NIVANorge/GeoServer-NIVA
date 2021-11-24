package niva.geoserver.security;

import org.apache.wicket.model.IModel;

import org.geoserver.security.web.auth.AuthenticationProviderPanel;


/**
 * 
 * @author Roar Brænden, NIVA
 *
 */
public class AquamonitorAuthenticationProviderPanel extends AuthenticationProviderPanel<AquamonitorAuthenticationProviderConfig> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4950905030709261995L;

	public AquamonitorAuthenticationProviderPanel(String id,
            IModel<AquamonitorAuthenticationProviderConfig> model) {
        super(id, model);
	}

}
