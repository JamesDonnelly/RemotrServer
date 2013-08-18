package com.matt.remotr.core.device;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.matt.remotr.ws.response.WsDeviceResponse;

public class DeviceCoordinatorWsImplTest {
	private DeviceCoordinatorWsImpl impl;
	private DeviceCoordinatorDefault deviceCoordinator;

	public DeviceCoordinatorWsImplTest() {
		impl = new DeviceCoordinatorWsImpl();
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testRegister() {
		Device device = new Device();
		device.setName("Test Device");
		device.setType(DeviceType.ANDROID);
		WsDeviceResponse wds = impl.register(device);
		
		assertTrue(wds.isSuccess());
	}

	@Test
	public void testDeregister() {
		assertTrue(true);
	}

	@Test
	public void testCheckRegistered() {
		assertTrue(true);
	}

	@Test
	public void testGetDeviceById() {
		assertTrue(true);
	}

	@Test
	public void testGetAllRegisteredDevices() {
		assertTrue(true);
	}

}
