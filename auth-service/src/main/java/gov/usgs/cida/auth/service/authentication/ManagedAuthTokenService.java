package gov.usgs.cida.auth.service.authentication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.usgs.cida.auth.dao.AuthTokenDAO;
import gov.usgs.cida.auth.dao.IAuthTokenDAO;
import gov.usgs.cida.auth.exception.NotAuthorizedException;
import gov.usgs.cida.auth.model.AuthToken;
import gov.usgs.cida.auth.model.User;

public class ManagedAuthTokenService implements IAuthTokenService{
	private final static Logger LOG = LoggerFactory.getLogger(ManagedAuthTokenService.class);
	
	private IAuthTokenDAO authTokenDao; 
	private IAuthService authService;
	
	public ManagedAuthTokenService() {
		authTokenDao = new AuthTokenDAO();
		authService = new ManagedAuthService();
	}
	
	//For testability
	public ManagedAuthTokenService(IAuthTokenDAO authTokenDao, IAuthService authService) {
		this.authTokenDao = authTokenDao;
		this.authService = authService;
	}
	
	//For testing, can replace later with IOC/DI
	protected void setAuthTokenDao(IAuthTokenDAO authTokenDao) {
		this.authTokenDao = authTokenDao;
	}
	protected void setAuthService(IAuthService authService) {
		this.authService = authService;
	}
	
	@Override
	public AuthToken authenticate(String username, char[] password) throws NotAuthorizedException {
		User user = authService.authenticate(username, password);
		user.setRoles(authTokenDao.getRoles(username));
		LOG.debug("User {} has authenticated", user.getUsername());
		
		if (user.isAuthenticated()) {
			AuthToken token = authTokenDao.create(user);
			return token;
		} else {
			throw new NotAuthorizedException();
		}
	}

}
