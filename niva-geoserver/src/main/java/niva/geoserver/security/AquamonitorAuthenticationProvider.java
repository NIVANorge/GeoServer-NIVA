package niva.geoserver.security;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.logging.Logger;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;


import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import org.geoserver.security.GeoServerAuthenticationProvider;
import org.geoserver.security.impl.GeoServerRole;
import org.geoserver.security.impl.GeoServerUser;
import org.geoserver.security.impl.RoleCalculator;

import org.geotools.util.logging.Logging;

import niva.aquamonitor.data.ws.LoginController;
import niva.aquamonitor.data.ws.UserCargo;


/**
 * Checks a request for either cookie or basic authentication.
 * Using Aquamonitor API at the default site AquaServices.
 * 
 * Host could be altered by setting environment_variable: AQUAMONITOR_HOST_ADDRESS
 * 
 * @author Roar Brænden, NIVA
 *
 */
public class AquamonitorAuthenticationProvider extends GeoServerAuthenticationProvider {

	static final String NO_USER = "NoUser";
	
	private static final Logger LOGGER = Logging.getLogger(AquamonitorAuthenticationProvider.class);
	
	private final LoginController loginService = LoginController.createService();

	/**
	 * Supports either a Basic Username / Password or a Cookie aqua_key
	 */
	@Override
	public boolean supports(Class<? extends Object> authentication, HttpServletRequest request) {
		
	    if (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication)) {
	        return true;
	    }
		
	    for (Cookie cookie : request.getCookies()) {
	        if ("aqua_key".equals(cookie.getName())) {
	            return true;
	        }
	    }

		return false;
	}

	@Override
	public Authentication authenticate(Authentication auth, HttpServletRequest request) {
		
		UserCargo user = null;
		
		if (request != null && request.getCookies() != null) {
			
			String token = null;
			for (Cookie cookie : request.getCookies()) {
			    if("aqua_key".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
			    }
			}
			
			if (token != null) {
				LOGGER.fine("Found aqua_key. Check if it's correct.");
				
				try {
					user = loginService.authenticateToken(token);
				}
				catch (IOException ie) {
					LOGGER.severe("Exception calling Aquamonitor Login.");
					throw new AuthenticationServiceException(ie.getMessage(), ie);
				}
			}
		}
		
	
		if ((user == null || NO_USER.equals(user.usertype)) 
		        && auth.getPrincipal() != null 
		        && !auth.getPrincipal().toString().isEmpty()) {
			String username, password;
			
			username = auth.getPrincipal().toString();
			password = auth.getCredentials().toString();
			
			
			try {
				LOGGER.fine("Try username/password against Aquamonitor");
				user = loginService.authenticateUser(username, password);
			}
			catch (IOException ie) {
				LOGGER.severe("Exception calling Aquamonitor Login.");
				throw new AuthenticationServiceException(ie.getMessage(),ie);
	
			}
		}
		
		if (user == null || NO_USER.equals(user.usertype)) {
		    return null;
		}
		
		LOGGER.fine("Aquamonitor user found.");
        try {
            final Set<GrantedAuthority> roles = new HashSet<GrantedAuthority>();
            roles.addAll(calculateRoles(user.username));
            roles.add(GeoServerRole.AUTHENTICATED_ROLE);
            roles.add(new AquamonitorUserRole(user));
            
            return new UsernamePasswordAuthenticationToken(user.username, null, roles);

        } catch (IOException e) {
            throw new AuthenticationServiceException(e.getMessage(),e);
        }       
	}
	
	private SortedSet<GeoServerRole> calculateRoles(String username) throws IOException {
	    return new RoleCalculator(getSecurityManager().getActiveRoleService())
                .calculateRoles(new GeoServerUser(username));
	}
	
	static class AquamonitorUserRole extends GeoServerRole {
	    
	    /** serialVersionUID */
        private static final long serialVersionUID = 2853237326337565198L;

        AquamonitorUserRole(UserCargo user) {
	        super("AQUAMONITOR_USER");
	        this.setUserName(user.username);
	        this.getProperties().setProperty("AQUA_KEY", user.key);
	        this.getProperties().setProperty("AQUA_TOKEN", user.token);
	    }
	}

}
