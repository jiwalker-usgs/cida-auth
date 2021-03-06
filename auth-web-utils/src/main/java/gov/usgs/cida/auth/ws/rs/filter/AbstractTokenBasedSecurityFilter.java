package gov.usgs.cida.auth.ws.rs.filter;

import gov.usgs.cida.auth.client.IAuthClient;
import gov.usgs.cida.auth.ws.rs.service.SecurityContextUtils;

import java.io.IOException;
import java.util.List;

import javax.annotation.Priority;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
/**
 * This jersey filter automatically handles restricting access if a user does not have roles. This is
 * used if your webservice layer does not have the functionality to restrict by endpoint.
 * 
 * This filter will restrict all access, but anonymous access to certain URLs by overriding getUnsecuredUris to return
 * a list of URIs
 */
@PreMatching
@Priority(Priorities.AUTHENTICATION)
public abstract class AbstractTokenBasedSecurityFilter implements ContainerRequestFilter {
	private static final Logger LOG = LoggerFactory.getLogger(AbstractTokenBasedSecurityFilter.class);
	
	
	@Context private HttpServletRequest httpRequest;
	
	/**
	 * You must implement this method to provide the type of IAuthClient you want.
	 * {@link gov.usgs.cida.auth.client.IAuthClient}
	 * @return an implementation of IAuthClient
	 */
	public abstract IAuthClient getAuthClient();
	
	/**
	 * You must implement this method to provide additional roles you wish to grant to
	 * the authenticated user in addition to the roles associated with the token from the 
	 * authorization services
	 * 
	 * @return list
	 */
	public abstract List<String> getAdditionalRoles();
	
	/**
	 * Implementing classes must specify which roles are required for the token to be valid.
	 */
	public abstract List<String> getAuthorizedRoles();
	

	/**
	 * Implementing classes must specify which URIs do not require authentication
	 */
	public abstract List<String> getUnsecuredUris();
	
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
    	//if root documentation or authenticate url, continue on without authorization
    	String requestedUri = requestContext.getUriInfo().getPath();
    	List<String> allowedUris = getUnsecuredUris();
    	if(allowedUris == null || !allowedUris.contains(requestedUri)) {
	        if (!SecurityContextUtils.isSessionOrSecurityContextAuthorizedForRoles(requestContext, httpRequest, getAuthorizedRoles())
	        		&& !SecurityContextUtils.isTokenAuthorized(requestContext, httpRequest, getAuthClient(), getAdditionalRoles())) {
	        	blockUnauthorizedRequest(requestContext);
	        }
    	}
    }
    
    private void blockUnauthorizedRequest(ContainerRequestContext requestContext) {
    	LOG.debug("blocking unauthorized request");
    	requestContext.abortWith(Response
	            .status(Response.Status.UNAUTHORIZED)
	            .entity("User cannot access the resource.")
	            .build());
    }
}