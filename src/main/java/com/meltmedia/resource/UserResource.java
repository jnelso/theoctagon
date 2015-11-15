package com.meltmedia.resource;

import com.meltmedia.dao.UserDAO;
import com.meltmedia.data.User;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.meltmedia.representation.JsonMessageException;
import com.meltmedia.representation.UserRepresentation;
import com.meltmedia.service.ValidationService;
import com.meltmedia.util.BakedBeanUtils;
import com.meltmedia.util.UserUtil;
import com.praxissoftware.rest.core.Link;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * UserResource: jheun
 * Date: 6/26/13
 */

@Path("/user")
@Singleton
public class UserResource {

  @Context UriInfo uriInfo;

  private Logger log = LoggerFactory.getLogger( getClass() );

  protected UserRepresentation createRepresentation(User user) {

    UserRepresentation rep = new UserRepresentation(user);
    // Link to the full entity
    rep.getLinks().add( new Link( uriInfo.getBaseUriBuilder().path( UserResource.class ).path( user.getId().toString()).build(), "self", MediaType.APPLICATION_JSON ) );
    return rep;

  }

  @Inject ValidationService validationService;
  @Inject UserDAO dao;

  @GET
  @Produces("application/json")
  public List<UserRepresentation> getUsers() {
    List<User> users = dao.list();

    List<UserRepresentation> userReps = new ArrayList<UserRepresentation>();

    for (User user : users) {
      userReps.add( createRepresentation( user ) );
    }

    return userReps;
  }

  @GET
  @Produces("application/json")
  /*
   * @param minRangeIndex: optional, defaults to 0
   * @param maxRangeIndex: optional, defaults to 0. If both min and maxRangeIndex are 0, return full list
   * @param pageNumber: zero-indexed, optional, defaults to first page's worth of users
   * @param maxUsersPerPage: optional, defaults to 20
   * @return
   */
  public List<UserRepresentation> getUsersByPage(
		  @PathParam("minRangeIndex") long minRangeIndex,
		  @PathParam("maxRangeIndex") long maxRangeIndex,
		  @PathParam("pageNumber") long pageNumber,
		  @PathParam("maxUsersPerPage") long maxUsersPerPage) {
    List<User> users = dao.list();
    
    final long MAX_USERS_PER_PAGE_DEFAULT = 20;

    List<UserRepresentation> userReps = new ArrayList<UserRepresentation>();
    List<UserRepresentation> filteredUserReps = new ArrayList<UserRepresentation>();

    // Validate parameters for minRangeIndex, maxRangeIndex
    if (minRangeIndex < 0 || maxRangeIndex < 0 || minRangeIndex > maxRangeIndex) {
    	throw new WebApplicationException( 404 );
    }
    
    // Validate pageNumber parameter
    if (pageNumber < 0) {
    	throw new WebApplicationException( 404 );
    }
    
    // Validate maxUsersPerPage parameter
    if (maxUsersPerPage < 0) {
    	throw new WebApplicationException( 404 );
    }
    
    // Set maxUsersPerPage to default value if not specified
    if (maxUsersPerPage == 0) {
    	maxUsersPerPage = MAX_USERS_PER_PAGE_DEFAULT;
    }
    
    // Set maxRangeIndex to default value if not specified
    if (maxRangeIndex == 0) {
    	maxRangeIndex = users.size() - 1;
    }
    
    // Filter out only users in range if range parameters were set
    int userIndex = 0;
    for (User user : users) {
      if (userIndex >= minRangeIndex && userIndex <= maxRangeIndex) {
    	  userReps.add( createRepresentation( user ) );
      }
      if (userIndex > maxRangeIndex) {
    	  break;
      }
      userIndex++;
    }
    
    // Check that pageNumber is not out of valid range
    if (pageNumber > 0) {
    	if (userReps.size() / maxUsersPerPage < (pageNumber + 1)) {
    		throw new WebApplicationException( 404 );
    	}	
    }
    
    // Filter out only users on this page and return
    int startIndex = (int) (pageNumber * maxUsersPerPage);
	for (int i = startIndex; i < startIndex + maxUsersPerPage; i++) {
		filteredUserReps.add(userReps.get(i));
	}

    return filteredUserReps;
  }
  
  @GET
  @Path("/{userId}")
  @Produces("application/json")
  public UserRepresentation getUser(@PathParam("userId") long id) {
    User user = dao.get( id );

    if (user == null) {
      throw new WebApplicationException( 404 );
    }

    return createRepresentation( user );
  }

  @POST
  @Consumes("application/json")
  @Produces("application/json")
  public UserRepresentation addUser(UserRepresentation rep) {

    // Validate the new user
    validationService.runValidationForJaxWS( rep );

    User user = new User();

    try {

      // Copy the appropriate properties to the new User object
      BakedBeanUtils.safelyCopyProperties( rep, user );

    } catch ( BakedBeanUtils.HalfBakedBeanException ex ) {

      log.error( "There was an error processing the new user input.", ex );
      throw new JsonMessageException( Response.Status.INTERNAL_SERVER_ERROR, "There was an error processing the input." );

    }

    // Set the new password, salting and hashing and all that neat jazz
    UserUtil.setupNewPassword( user, rep.getPassword().toCharArray() );

    // Create the user in the system
    dao.create( user );

    // Return a representation of the user
    return createRepresentation( user );

  }

}
