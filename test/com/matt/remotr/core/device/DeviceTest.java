package com.matt.remotr.core.device;

import static org.junit.Assert.*;

import org.junit.Test;

public class DeviceTest {
	private Device device;

	public DeviceTest() {
		device = new Device();
	}

	@Test
	public void testSetId() {
		assertNotNull(device);
		device.setId(1L);
	}

	@Test
	public void testGetId() {
		assertNotNull(device);
		device.setId(1L);
		assertEquals((Long)1L, (Long)device.getId());
	}

	@Test
	public void testSetType() {
		assertNotNull(device);
		device.setType(DeviceType.SYSTEM);
		assertEquals(DeviceType.SYSTEM, device.getType());
	}

	@Test
	public void testGetType() {
		assertNotNull(device);
		device.setType(DeviceType.ANDROID);
		assertNotSame(DeviceType.SYSTEM, device.getType());
	}

	@Test
	public void testSetName() {
		assertNotNull(device);
		device.setName("Test Name");
		assertEquals((String)"Test Name", device.getName());
	}

	@Test
	public void testGetName() {
		assertNotNull(device);
	}

	@Test
	public void testSetLastHeatbeatTime() {
		assertTrue(true);
	}

	@Test
	public void testGetLastHeartbeatTime() {
		assertTrue(true);
	}

	@Test
	public void testSetHasHeartbeat() {
		assertTrue(true);
	}

	@Test
	public void testIsHadHeartbeat() {
		assertTrue(true);
	}

	@Test
	public void testGetCommands() {
		assertTrue(true);
	}

	@Test
	public void testSetCommands() {
		assertTrue(true);
	}

}
