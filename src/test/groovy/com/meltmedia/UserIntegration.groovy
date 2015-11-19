package com.meltmedia

import groovyx.net.http.RESTClient
import org.junit.Test

/**
 * Created with IntelliJ IDEA.
 * User: jheun
 * Date: 6/26/13
 */
class UserIntegration {

    private static final String JSON = "application/json"
    private static final String URL = "http://localhost:8080/api/user"
	
	static {
		def http = new RESTClient( URL )
		for (int i = 0; i < 20; i++) {
			http.post(body: [ email:"testUser" + i + ".list@meltdev.com", password:"vespa" ], requestContentType: JSON)
		}
	}
    /**
     * Test that we can create a good user and that we can get that user after it has been created
     */
    @Test
    public void testCreateGoodUser() {

        def http = new RESTClient( URL )
        def email = "testUser@meltdev.com"

        // Create the user
        def resp = http.post(body: [ email:email, password:"vespa" ], requestContentType: JSON)

        assert resp.status == 200
        assert resp.data.id != null
        assert resp.data.email == email

        // Get the user
        resp = http.get( path:"/api/user/" + resp.data.id, requestContentType: JSON )

        assert resp.status == 200
        assert resp.data.id != null
        assert resp.data.email == email

    }

    @Test
    public void testCreateUserWithBadEmail() {

        def http = new RESTClient( URL )

        try {

            // Attempt to create the user
            http.post(body: [ email:"testmeltdev.com", password:"vespa" ], requestContentType: JSON)
            assert false, "Expected exception"

        } catch( ex ) {

            assert ex.response.status == 400

        }

    }

    @Test
    public void testCreateUserWithNullEmail() {

        def http = new RESTClient( URL )

        try {

            // Attempt to create the user
            http.post(body: [ password:"vespa" ], requestContentType: JSON)
            assert false, "Expected exception"

        } catch( ex ) {

            assert ex.response.status == 400

        }

    }


    @Test
    public void testCreateUserWithBadPassword() {

        def http = new RESTClient( URL )

        try {

            // Attempt to create the user
            http.post(body: [ email:"test@meltdev.com", password:" " ], requestContentType: JSON)
            assert false, "Expected exception"

        } catch( ex ) {

            assert ex.response.status == 400

        }

    }

    @Test
    public void testCreateUserWithNullPassword() {

        def http = new RESTClient( URL )

        try {

            // Attempt to create the user
            http.post(body: [ email:"testmeltdev.com" ], requestContentType: JSON)
            assert false, "Expected exception"

        } catch( ex ) {

            assert ex.response.status == 400

        }
    }


    @Test
    public void testGetNonExistantUser() {

        def http = new RESTClient( URL )

        try {

            // Attempt to create the user
            http.get(path: "/api/user/-1", requestContentType: JSON)
            assert false, "Expected exception"

        } catch( ex ) {

            assert ex.response.status == 404

        }

    }

    @Test
    public void testListUser() {

        def http = new RESTClient( URL )

        // Create a user so we have at least 1
        http.post(body: [ email:"testUser.list@meltdev.com", password:"vespa" ], requestContentType: JSON)

        def resp = http.get(path: "/api/user", requestContentType: JSON)

        assert resp.status == 200
        assert resp.data.asList.size > 0

    }
	
	@Test
	public void testListUserPageLimitDefault() {
		
		def http = new RESTClient( URL )

		def resp = http.get(path: "/api/user", requestContentType: JSON)

		assert resp.status == 200
		assert resp.data.asList.size == 20;

	}
	
	@Test
	public void testListUserPageSizeSetting() {

		def http = new RESTClient( URL )

		def resp = http.get(path: "/api/user", requestContentType: JSON, query:["pageLimit": "15"])

		assert resp.status == 200
		assert resp.data.asList.size == 15;

	}
	
	@Test
	public void testListUserPageSizeLessThanLimit() {
		
		def http = new RESTClient( URL )

		def resp = http.get(path: "/api/user", requestContentType: JSON, query:["pageLimit": "25"])

		assert resp.status == 200
		assert resp.data.asList.size == 21;

	}
	
	
	@Test
	public void testListUserPageGreaterThanMax() {
		
		def http = new RESTClient( URL )

		try {
			def resp = http.get(path: "/api/user", requestContentType: JSON,
				query:["pageLimit": "30", "pageNumber" : "2"])
			assert false, "Expected exception"

		} catch( ex ) {

			assert ex.response.status == 400

		}

	}
	
	@Test
	public void testListUserSecondPage() {
		
		def http = new RESTClient( URL )

		def resp = http.get(path: "/api/user", requestContentType: JSON,
			query:["pageLimit": "10", "pageNumber" : "2"])

		assert resp.status == 200
		assert resp.data.asList.size == 10;

	}
	
	@Test
	public void testListUserThirdPage() {
		
		def http = new RESTClient( URL )

		def resp = http.get(path: "/api/user", requestContentType: JSON,
			query:["pageLimit": "10", "pageNumber" : "3"])

		assert resp.status == 200
		assert resp.data.asList.size == 1;

	}
	
	@Test
	public void testListUserInvalidPage() {
		
		def http = new RESTClient( URL )

		try {
			def resp = http.get(path: "/api/user", requestContentType: JSON,
				query:["pageLimit": "10", "pageNumber" : "-1"])
			assert false, "Expected exception"

		} catch( ex ) {

			assert ex.response.status == 400

		}

	}
	
	@Test
	public void testListUserInvalidLimit() {
		
		def http = new RESTClient( URL )

		try {
			def resp = http.get(path: "/api/user", requestContentType: JSON,
				query:["pageLimit": "-10", "pageNumber" : "1"])
			assert false, "Expected exception"

		} catch( ex ) {

			assert ex.response.status == 400

		}

	}
	

}
