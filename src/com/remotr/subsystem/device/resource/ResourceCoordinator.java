package com.remotr.subsystem.device.resource;

import java.util.ArrayList;

import com.remotr.subsystem.device.domain.Device;
import com.remotr.subsystem.device.resource.domain.Resource;

/**
 * Interface for classes providing {@link Resource} management in the system
 * @author matt
 *
 */
public interface ResourceCoordinator {
	
	/**
	 * Returns a {@link Resource} using the given Id
	 * @param id
	 * @return the {@link Resource}
	 */
	public Resource getResource(Long id);
	
	/**
	 * Returns the cached instance of the passed in resource
	 * @param resource
	 * @return
	 */
	public Resource getResource(Resource resource);
	
	/**
	 * Returns a list of {@link Resource} objects associated with the {@link Device}
	 * @param device
	 * @return
	 */
	public ArrayList<Resource> getResource(Device device);
	
	/**
	 * Adds the given {@link Resource} to the {@link Device}
	 * @param resource
	 * @param device
	 * @throws IllegalArgumentException
	 */
	public void addResource(Resource resource, Device device) throws IllegalArgumentException;
	
	/**
	 * Removes a {@link Resource} from the {@link Device} 
	 * @param id
	 */
	public void removeResource(Long id);

}
