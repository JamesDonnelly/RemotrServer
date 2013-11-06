package com.remotr.subsystem.ws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.remotr.subsystem.session.SessionCoordinator;
import com.remotr.subsystem.ws.annotations.WsClass;
import com.remotr.subsystem.ws.annotations.WsMethod;
import com.remotr.subsystem.ws.annotations.WsParam;
import com.remotr.subsystem.ws.request.domain.WsRequest;
import com.remotr.subsystem.ws.response.WsResponseForwarder;
import com.remotr.subsystem.ws.response.domain.WsMethodHolder;
import com.remotr.subsystem.ws.response.domain.WsParamHolder;
import com.remotr.subsystem.ws.response.domain.WsSubsystemHolder;
import com.remotr.subsystem.ws.response.domain.WsSubsystemResponse;

public class WsCoordinatorDefaultTest extends WsTestBase {
	private String subsystemName;

	@Before
	public void setUp() throws Exception {
		WsCoordinatorService coordinatorService = new WsCoordinatorService();
		assertNotNull(coordinatorService);
		
		subsystemName = coordinatorService.getSubSystemName();
		assertNotNull(subsystemName);
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

		WsRequest request = buildRequest(subsystemName, WSGET_SUBSYSTEMS, "TEST_REFF");
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
		WsCoordinator wsCoordinator = getWsCoordinator();
		assertNotNull(wsCoordinator);
		
		TestRunnerClass runnerClass = new TestRunnerClass(wsCoordinator);
		assertNotNull(runnerClass);
		runnerClass.register();
		
		ArrayList<WsSubsystemHolder> subsystems = wsCoordinator.getSubSystemList();
		assertNotNull(subsystems);
		assertEquals(1, subsystems.size());
		
		// See if the subsystem we just registered has made it in to the get response
		boolean found = false;
		for(WsSubsystemHolder sHolder : subsystems){
			if(sHolder.getSubsystemName().equals("TestRunnerClass")){
				found = true;
			}
		}
		assertTrue(found);
		
		found = false;
		for(WsSubsystemHolder sHolder : subsystems){
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
		WsCoordinator wsCoordinator = new WsCoordinatorDefault();
		WsCoordinatorService coordinatorService = new WsCoordinatorService();
		
		coordinatorService.setWsCoordinator(wsCoordinator);
		coordinatorService.init();
		
		assertNotNull(wsCoordinator);
		assertNotNull(subsystemName);
		
		String reff = "TEST_REFF";
		WsRequest request = buildRequest(subsystemName, WSGET_SUBSYSTEMS, reff);
		assertNotNull(request);
		
		WsSubsystemResponse response = (WsSubsystemResponse) wsCoordinator.runRequest(request);
		testResponse(request, response, true, true);
		
		assertEquals(1, response.getSubsystemList().size());
	}

	@Test
	public void testRunRequestWsRequestDevice() {
		WsResponseForwarder forwarder = mock(WsResponseForwarder.class);
		
		WsCoordinatorDefault wsCoordinator = new WsCoordinatorDefault();
		
		assertNotNull(wsCoordinator);
		
		wsCoordinator.setResponseForwarder(forwarder);

		WsRequest request = buildRequest(subsystemName, WSGET_SUBSYSTEMS, "TEST_REFF");
		assertNotNull(request);
		
		// This shouldn't cause an NPE
		wsCoordinator.runRequest(request, getTestDevice());
		// TODO: Make this test better
	}
	
	@WsClass(description="This is a test class")
	static class TestRunnerClass implements WsRunner {
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
