package com.remotr.subsystem.ws;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;

import org.apache.log4j.Logger;
import org.springframework.core.annotation.AnnotationUtils;

import com.remotr.core.Main;
import com.remotr.subsystem.device.domain.Device;
import com.remotr.subsystem.ws.request.domain.WsRequest;
import com.remotr.subsystem.ws.response.WsResponseForwarder;
import com.remotr.subsystem.ws.response.domain.WsMethodHolder;
import com.remotr.subsystem.ws.response.domain.WsParamHolder;
import com.remotr.subsystem.ws.response.domain.WsResponse;
import com.remotr.subsystem.ws.response.domain.WsSubsystemHolder;
import com.remotr.subsystem.ws.response.domain.WsSubsystemResponse;

// TODO: Service classes should not have to return a WsResponse, but should just return the object to be wrapped -
// 		 Maybe add the wrapped object type to the response object
// TODO: Add a message queue to stop this class from becoming overloaded
public class WsCoordinatorDefault implements WsCoordinator, WsRunner{
	private Logger log;
	private WsResponseForwarder responseForwarder;
	
	private Map<String, WsRunner> subsystemMap; // Maps the SubSystem name to it's runner
	private Map<String, Map<String, Map<String, Class<?> > > > methodMap; // Maps the subsystem name to a map of methods mapping to a map of param names and their class type
	private ArrayList<String> publicMethodList; // Simple list of public methods
	
	//TODO: Move away from the maps...
	private ArrayList<WsSubsystemHolder> systemHolders;
	
	public WsCoordinatorDefault(){
		log = Logger.getLogger(this.getClass());
		
		subsystemMap = new HashMap<String, WsRunner>();
		methodMap = new HashMap<String, Map<String, Map<String, Class<?>>>>();
		publicMethodList = new ArrayList<String>();
		systemHolders = new ArrayList<WsSubsystemHolder>();
		
		log.info("Starting WsManager");
		
		this.register(this);
	}

	public void setResponseForwarder(WsResponseForwarder responseForwarder) {
		this.responseForwarder = responseForwarder;
	}

	@Override
	public boolean register(WsRunner runner) {
		log.debug("Incoming registration request from ["+runner.getSubSystemName()+"]");
		if(!subsystemMap.containsKey(runner.getSubSystemName())){
			WsSubsystemHolder systemHolder = new WsSubsystemHolder();
			// We haven't seen this runner before - Add it to the map then get it's published methods
			subsystemMap.put(runner.getSubSystemName(), runner);
			systemHolder.setSubsystemName(runner.getSubSystemName());
			
			Map<String, Map<String, Class<?>>> methodParamMap = new HashMap<String, Map<String, Class<?>>>();
			Method[] methods = runner.getClass().getMethods();
			
			for(Method m : methods){
				WsMethod a = AnnotationUtils.findAnnotation(m, WsMethod.class);
				if(a != null && a.exclude() == false){
					log.debug("Found method ["+m.getName()+"]");
					WsMethodHolder methodHolder = new WsMethodHolder();
					methodHolder.setMethodName(m.getName());

					// it's a WebMethod - see if it's public, if it is add it a public list for faster access
					// then check to see if it needs any params
					if(a.isPublic() == true){
						log.debug("Method ["+m.getName()+"] is public - adding to list");
						publicMethodList.add(m.getName());
						methodHolder.setPublic(true);
					}
					if(!a.description().equals("")){
						methodHolder.setDescription(a.description());
					}
					
					Map<String, Class<?>> paramMap = new HashMap<String, Class<?>>();
					if(a.wsParams().length != 0){
						for(int i=0; i < a.wsParams().length; i++){
							log.debug("Method ["+m.getName()+"] takes param ["+a.wsParams()[i].name()+"] of type ["+a.wsParams()[i].type().getSimpleName()+"]");
							paramMap.put(a.wsParams()[i].name(), a.wsParams()[i].type());
							methodHolder.addWsRequestParam(a.wsParams()[i].name(), a.wsParams()[i].type().getSimpleName());;
						}
					}
					methodParamMap.put(m.getName(), paramMap);
					systemHolder.addWsServiceMethodHolder(methodHolder);
				}
			}
			systemHolders.add(systemHolder);
			methodMap.put(runner.getSubSystemName(), methodParamMap);
			
			log.info("WsRunner for subsystem ["+runner.getSubSystemName()+"] registered");
			return true;
		}else{
			return false;
		}
	}

	@Override
	public boolean unregister(WsRunner requestRunner) {
		return false;
	}
	
	@Override
	public WsResponse runRequest(WsRequest wsRequest) {
		return runRequestInternal(wsRequest, null);
	}

	@Override
	public void runRequest(WsRequest wsRequest, Device device) {
		runRequestInternal(wsRequest, device);
	}
	
	private WsResponse runRequestInternal(WsRequest wsRequest, Device device){
		WsResponse response = getWsResponse();
		
		if(subsystemMap.containsKey(wsRequest.getSubSystem().getSubsystemName())){
			WsRunner runner = subsystemMap.get(wsRequest.getSubSystem().getSubsystemName());
			
			try{
				Object obj = runRequestOnRunner(wsRequest, runner);
				
				if(obj instanceof WsResponse){
					response = (WsResponse) obj;
	 			}else if(obj instanceof WsSubsystemResponse){
					response = (WsSubsystemResponse) obj;
	 			}else{
	 				log.error("Unknown response when running WsRequest ["+wsRequest.getReference()+"]");
	 				response.setErrorMessage("Unknown response when running WsRequest");
	 			}
			}catch(Exception e){
				response.setException(e);
			}
			
		}else{
			log.error("No subsystem of name ["+wsRequest.getSubSystem().getSubsystemName()+"] found to handle this request");
			response.setErrorMessage("Subsystem name ["+wsRequest.getSubSystem().getSubsystemName()+"] not found");
		}
		
		response.setReference(wsRequest.getReference());
		if(device != null){
			responseForwarder.forwardWsResponse(device, response);
			return null;
		}else{
			return response;
		}
	}
	
	private Object runRequestOnRunner(WsRequest request, WsRunner runner) throws Exception {
		if(request.getMethod().getWsRequestParamList() != null){
			Map<String, Class<?>> paramMap = methodMap.get(runner.getSubSystemName()).get(request.getMethod().getMethodName());
			
			ArrayList<Object> paramObjects = new ArrayList<Object>();
			List<Class<?>> classList = new ArrayList<Class<?>>();
			for(WsParamHolder p : request.getMethod().getWsRequestParamList()){
				if(paramMap.containsKey(p.getName())){
					Class<?> clazz = paramMap.get(p.getName());
					JAXBElement<?> element = (JAXBElement<?>) p.getValue();
					Object o = element.getValue();
					paramObjects.add(clazz.cast(o));
					classList.add(clazz);
				}
			}
			
			Method method = runner.getClass().getMethod(request.getMethod().getMethodName(), classList.toArray(new Class[classList.size()]));
			return method.invoke(runner, paramObjects.toArray());
		}else{
			Method method = runner.getClass().getMethod(request.getMethod().getMethodName());
			return method.invoke(runner);
		}
	}
	
	@WsMethod(
			isPublic=true,
			description="Returns a list of subsystems and all methods"
			)
	public WsSubsystemResponse getSubSystems(){
		WsSubsystemResponse response = getWsSubsystemResponse();
		
		response.setSubsystemList(systemHolders);
		response.setSuccess(true);
		return response;
	}

	@Override
	public String getSubSystemName() {
		return "WsCoordinator";
	}
	
	private WsResponse getWsResponse(){
		WsResponse response = new WsResponse();
		response.setSubSystem(getSubSystemName());
		
		response.setVersionName(Main.getVersionName());
		response.setVersionNum(Main.getVersionNumber());
		
		return response;
	}
	
	private WsSubsystemResponse getWsSubsystemResponse() {
		WsSubsystemResponse response = new WsSubsystemResponse();
		response.setSubSystem(getSubSystemName());
		
		response.setVersionName(Main.getVersionName());
		response.setVersionNum(Main.getVersionNumber());
		
		return response;
	}

}