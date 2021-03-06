package gov.usgs.cida.auth.webservice.authentication;

import gov.usgs.cida.auth.exception.NotAuthorizedException;
import gov.usgs.cida.auth.model.AuthToken;
import gov.usgs.cida.auth.service.ServicePaths;
import gov.usgs.cida.auth.service.authentication.CidaActiveDirectoryTokenService;
import gov.usgs.cida.auth.service.authentication.IAuthTokenService;
import gov.usgs.cida.auth.service.authentication.ManagedAuthTokenService;

import javax.naming.NamingException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/")
public class AuthenticationWebervice {
	private final static Logger LOG = LoggerFactory.getLogger(AuthenticationWebervice.class);

	private IAuthTokenService cidaActiveDirectoryAuthTokenService = new CidaActiveDirectoryTokenService();
	private IAuthTokenService managedAuthTokenService = new ManagedAuthTokenService();
	
	@POST
	@Path(ServicePaths.AD + "/" + ServicePaths.TOKEN)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response doAdAuth(
			@FormParam("username") String username,
			@FormParam("password")
			@DefaultValue("") String password) throws NamingException {
		LOG.trace("User {} is attempting to authenticate", username);
		return getADResponse(cidaActiveDirectoryAuthTokenService, username, password.toCharArray());
	}
	
	@POST
	@Path(ServicePaths.MANAGED + "/" + ServicePaths.TOKEN)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response doManagedAuth(
			@FormParam("username") String username,
			@FormParam("password")
			@DefaultValue("") String password) throws NamingException {
		LOG.trace("User {} is attempting to authenticate", username);
		return getADResponse(managedAuthTokenService, username, password.toCharArray());
	}

	/**
	 * Authenticates, creates token, generates proper Response
	 *
	 * @param username
	 * @param password
	 * @return
	 */
	protected Response getADResponse(IAuthTokenService authTokenService, String username, char[] password) {
		Response response;
		
		try {
			AuthToken token = authTokenService.authenticate(username, password);
			LOG.trace("Added token {} to database", token.getTokenId());
			response = Response.ok(token.toJSON(), MediaType.APPLICATION_JSON_TYPE).build();
		} catch (NotAuthorizedException e) {
			LOG.debug("User {} could not authenticate", username);
			response = Response.status(Response.Status.UNAUTHORIZED).build();
		}
		
		return response;
	}
}
