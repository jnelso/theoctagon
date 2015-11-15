package com.meltmedia.dao;

import com.meltmedia.data.User;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;
import com.google.inject.persist.jpa.JpaPersistModule;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jheun
 * Date: 6/26/13
 */
public class UserDAOTest {

  private PersistService service;

  private UserDAO dao;

  @Before
  public void setupContext() throws IllegalArgumentException, NoSuchFieldException {

    Injector injector = Guice.createInjector( new JpaPersistModule("unitTest") );

    service = injector.getInstance( PersistService.class );
    service.start();

    dao = injector.getInstance( UserDAO.class );

  }

  @After
  public void tearDown() {

    service.stop();

  }

  @Test
  public void simplePersistTest() {

    User testUser = new User();
    testUser.setEmail( "testUser@meltdev.com" );
    testUser.setPassword( "vespa" );

    dao.create( testUser );

    Assert.assertNotNull( testUser.getId() );

    User getUser = dao.get( testUser.getId() );

    Assert.assertEquals( testUser.getEmail(), getUser.getEmail() );

  }

  @Test
  public void testEmptyList() {

    List<User> userList = dao.list();

    Assert.assertEquals( 0, userList.size() );

  }
  
  @Test
  public void testUpdateUser() {

    User testUser = new User();
    testUser.setEmail( "testUpdateUser@meltdev.com" );
    testUser.setPassword( "vespa" );

    User updatedUser = dao.update( testUser );

    Assert.assertEquals( testUser.getEmail(), updatedUser.getEmail() );

  }

  @Test
  public void testGetUserByEmail() {

    String email = "testGetUserByEmail@meltdev.com";

    User testUser = new User();
    testUser.setEmail( email );
    testUser.setPassword( "vespa" );

    User createdUser = dao.create( testUser );

    User fetchedUser = dao.getByEmail( email );

    Assert.assertEquals( createdUser.getId(), fetchedUser.getId() );

  }

}
