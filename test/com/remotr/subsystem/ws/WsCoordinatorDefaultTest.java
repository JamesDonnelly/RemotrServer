package com.remotr.subsystem.ws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.remotr.subsystem.session.SessionCoordinator;
import com.remotr.subsystem.ws.request.domain.WsRequest;
import com.remotr.subsystem.ws.response.WsResponseForwarder;
import com.remotr.subsystem.ws.response.domain.WsMethodHolder;
import com.remotr.subsystem.ws.response.domain.WsParamHolder;
import com.remotr.subsystem.ws.response.domain.WsSubsystemHolder;
import com.remotr.subsystem.ws.response.domain.WsSubsystemResponse;

public class WsCoordinatorDefaultTest extends WsTestBase {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testWsCoordinatorDefault() {
		WsCoordinator wsCoordinator = new WsCoordinatorDefault();
		assertNotNull(wsCoordinator);
	}

	@Test
	public void testSetResponseForwarder() {
		WsResponseForwarder forwarder = mock(WsResponseForwarder.class);
		
		WsCoordinatorDefault wsCoordinator = new WsCoordinatorDefault();
		
		assertNotNull(wsCoordinator);
		
		wsCoordinator.setResponseForwarder(forwarder);

		WsRequest request = buildRequest(wsCoordinator.getSubSystemName(), WSGET_SUBSYSTEMS, "TEST_REFF");
		assertNotNull(request);
		
		// This shouldn't cause an NPE
		wsCoordinator.runRequest(request, getTestDevice());
		
	}

	@Test
	public void testSetSessionCoordinator() {
		SessionCoordinator sessionCoordinator = mock(SessionCoordinator.class);
		assertNotNull(sessionCoordinator);
		
		WsCoordinatorDefault wsCoordinator = new WsCoordinatorDefault();
		assertNotNull(wsCoordinator);
		
		wsCoordinator.setSessionCoordinator(sessionCoordinator);
	}

	@Test
	public void testRegister() {
		WsCoordinatorDefault wsCoordinator = new WsCoordinatorDefault();	
		assertNotNull(wsCoordinator);
		
		TestRunnerClass runnerClass = new TestRunnerClass(wsCoordinator);
		assertNotNull(runnerClass);
		runnerClass.register();
		
		WsRequest request = buildRequest(wsCoordinator.getSubSystemName(), WSGET_SUBSYSTEMS, "TEST_REFF");
		assertNotNull(request);
		WsSubsystemResponse response = (WsSubsystemResponse) wsCoordinator.runRequest(request);
		assertNotNull(response);
		assertEquals(2, response.getSubsystemList().size());
		
		// See if the subsystem we just registered has made it in to the get response
		boolean found = false;
		for(WsSubsystemHolder sHolder : response.getSubsystemList()){
			if(sHolder.getSubsystemName().equals("TestRunnerClass")){
				found = true;
			}
		}
		assertTrue(found);
		
		found = false;
		for(WsSubsystemHolder sHolder : response.getSubsystemList()){
			if(sHolder.getSubsystemName().equals("TestRunnerClass")){
				assertEquals("This is a test class", sHolder.getSubsystemDescription());
				for(WsMethodHolder mHolder : sHolder.getWsMethodList()){
					if(mHolder.getMethodName().equals("testWsMethod")){
						assertEquals("This is a test method", mHolder.getDescription());
						assertFalse(mHolder.isPublic());
						assertFalse(mHolder.isAsync());
						for(WsParamHolder pHolder : mHolder.getWsParamList()){
							if(pHolder.getName().equals("string")){
								assertNotNull(pHolder);
								assertEquals(String.class.getSimpleName(), pHolder.getType());
								found = true;
							}
						}
					}
				}
			}
		}
		
		assertTrue(found);
	}

	@Test
	public void testUnregister() {
		WsCoordinatorDefault wsCoordinator = new WsCoordinatorDefault();	
		assertNotNull(wsCoordinator);
		
		TestRunnerClass runnerClass = new TestRunnerClass(wsCoordinator);
		assertNotNull(runnerClass);
		
		assertFalse(wsCoordinator.unregister(runnerClass));
	}

	@Test
	public void testRunRequestWsRequest() {
		// Starting a new WsCoordinator causes it to register with itself
		WsCoordinator wsCoordinator = new WsCoordinatorDefault();
		
		assertNotNull(wsCoordinator);
		assertNotNull(wsCoordinator.getSubSystemName());
		
		String reff = "TEST_REFF";
		WsRequest request = buildRequest(wsCoordinator.getSubSystemName(), WSGET_SUBSYSTEMS, reff);
		assertNotNull(request);
		
		WsSubsystemResponse response = (WsSubsystemResponse) wsCoordinator.runRequest(request);
		testResponse(request, response, true, true);
		
		assertEquals(1, response.getSubsystemList().size());
		
		WsSubsystemHolder sHolder = response.getSubsystemList().get(0);
		assertNotNull(sHolder);
		assertEquals(wsCoordinator.getSubSystemName(), sHolder.getSubsystemName());
		
		WsMethodHolder mHolder = sHolder.getWsMethodList().get(0);
		assertNotNull(mHolder);
		assertEquals(WSGET_SUBSYSTEMS, mHolder.getMethodName());
	}

	@Test
	public void testRunRequestWsRequestDevice() {
		WsResponseForwarder forwarder = mock(WsResponseForwarder.class);
		
		WsCoordinatorDefault wsCoordinator = new WsCoordinatorDefault();
		
		assertNotNull(wsCoordinator);
		
		wsCoordinator.setResponseForwarder(forwarder);

		WsRequest request = buildRequest(wsCoordinator.getSubSystemName(), WSGET_SUBSYSTEMS, "TEST_REFF");
		assertNotNull(request);
		
		// This shouldn't cause an NPE
		wsCoordinator.runRequest(request, getTestDevice());
		// TODO: Make this test better
	}

	@Test
	public void testGetSubSystemName() {
	WsCoordinator wsCoordinator = new WsCoordinatorDefault();
		
		assertNotNull(wsCoordinator);
		assertNotNull(wsCoordinator.getSubSystemName());
		assertEquals("WsCoordinator", wsCoordinator.getSubSystemName());
	}
	
	@WsClass(description="This is a test class")
	class TestRunnerClass implements WsRunner {
		private WsCoordinator wsCoordinator;
		
		public TestRunnerClass(WsCoordinator wsCoordinator){
			this.wsCoordinator = wsCoordinator;
		}

		@Override
		public String getSubSystemName() {
			return "TestRunnerClass";
		}
		 
		public void register(){
			this.wsCoordinator.register(this);
		}
		
		@WsMethod(
				isPublic=false,
				isAsync=false,
				description="This is a test method",
				exclude=false,
				wsParams={
						@WsParam(name="string", type=String.class)
					}
				)
		public void testWsMethod(String string){
			// Nothing here
		}
		
	}

}
