package niva.geoserver.security;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
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

import niva.aquamonitor.data.ws.LoginWebService;
import niva.aquamonitor.data.ws.UserCargo;



public class AquamonitorAuthenticationProvider extends GeoServerAuthenticationProvider {
	
	static final GeoServerRole AQUAMONITOR_USER = new GeoServerRole("AQUAMONITOR_USER");
	
	static final String NO_USER = "NoUser";
	
	private static final Logger LOGGER = Logging.getLogger(AquamonitorAuthenticationProvider.class);
	
	private final LoginWebService loginService = LoginWebService.createService();

	/**
	 * Supports either a Basic Username / Password or a Cookie aqua_key
	 */
	@Override
	public boolean supports(Class<? extends Object> authentication, HttpServletRequest request) {
		boolean supp;
		supp = UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
		
		Cookie[] cookie = request.getCookies();
		int i = 0;
		while (!supp && i < cookie.length) {
			supp = "aqua_key".equals(cookie[i].getName());
			i++;
		}
		return supp;
	}

	@Override
	public Authentication authenticate(Authentication auth, HttpServletRequest request) {
		
		UsernamePasswordAuthenticationToken result;
		UserCargo user = null;
		
		if (request != null && request.getCookies() != null) {
			
			Cookie[] cookie = request.getCookies();
			int i = 0;
			String token = null;
			
			while (i < cookie.length) {
				if( "aqua_key".equals(cookie[i].getName()))
					token = cookie[i].getValue();
				i++;
			}
			
			if (token != null) {
				LOGGER.fine("Found aqua_key. Check if it's correct.");
				
				try {
					user = loginService.authenticateToken(token);
				}
				catch (IOException ie) {
					LOGGER.severe("Exception calling Aquamonitor LoginWebService");
					throw new AuthenticationServiceException(ie.getMessage(), ie);
				}
			}
		}
		
	
		if ((user == null || NO_USER.equals(user.usertype)) && auth.getPrincipal()!=null && !auth.getPrincipal().toString().isEmpty()) {
			String username, password;
			
			username = auth.getPrincipal().toString();
			password = auth.getCredentials().toString();
			
			
			try {
				LOGGER.fine("Try username/password against Aquamonitor");
				user = loginService.authenticateUser(username, password);
			}
			catch (IOException ie) {
				LOGGER.severe("Exception calling Aquamonitor LoginWebService.");
				throw new AuthenticationServiceException(ie.getMessage(),ie);
	
			}
		}
		
		if (user != null && !NO_USER.equals(user.usertype)) {
			LOGGER.fine("Aquamonitor user found.");
	        Set<GrantedAuthority> roles = new HashSet<GrantedAuthority>();       
	        RoleCalculator calc = new RoleCalculator(getSecurityManager().getActiveRoleService());
	        try {
	            roles.addAll(calc.calculateRoles(new GeoServerUser(user.username)));
	        } catch (IOException e) {
	            throw new AuthenticationServiceException(e.getMessage(),e);
	        }
	        roles.add(GeoServerRole.AUTHENTICATED_ROLE);
	        roles.add(AQUAMONITOR_USER);
	        
	        result = new UsernamePasswordAuthenticationToken(user.username, null, roles);
	        
	        return result;
		}
		else {
			return null;
		}        
	}

}
