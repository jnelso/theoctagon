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
  // @param pageNumber: optional, defaults to first page's worth of users
  // @param pageLimit: optional, defaults to 20
  // @return
  
 public List<UserRepresentation> getUsers(
		  @QueryParam("pageNumber") @DefaultValue("1") int pageNumber,
		  @QueryParam("pageLimit") @DefaultValue("20") int pageLimit) {
	  
	List<User> users = dao.list();

	List<UserRepresentation> userReps = new ArrayList<UserRepresentation>();
   
   // Validate pageNumber parameter
   if (pageNumber < 1) {
   	log.error( "pageNumber must be greater than or equal to 1." );
       throw new JsonMessageException( Response.Status.BAD_REQUEST, "pageNumber must be greater than or equal to 1." );

   }
   
   // Validate pageLimit parameter
   if (pageLimit < 1) {
   	log.error( "pageLimit must be greater than or equal to 1." );
       throw new JsonMessageException( Response.Status.BAD_REQUEST, "pageLimit must be greater than or equal to 1." );
   }
   
   // Check that pageNumber is not out of valid range
   if ((pageNumber - 1) > users.size() / pageLimit) {
   	log.error( "pageNumber must be in valid range for number of users." );
       throw new JsonMessageException( Response.Status.BAD_REQUEST, "pageNumber must be in valid range for number of users." );
   }
   
   // Filter out only users on this page and return

   users = users.subList((pageNumber-1)*pageLimit, Math.min((pageNumber-1)*pageLimit+pageLimit,  users.size()));
	
	for (User user : users) {
	  	userReps.add( createRepresentation( user ) );	     
	}
	
   return userReps;
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
