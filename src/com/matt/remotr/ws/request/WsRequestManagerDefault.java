package com.matt.remotr.ws.request;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jws.WebMethod;
import javax.xml.bind.JAXBElement;

import org.apache.log4j.Logger;
import org.springframework.core.annotation.AnnotationUtils;

import com.matt.remotr.core.device.domain.Device;
import com.matt.remotr.ws.request.domain.WsRequest;
import com.matt.remotr.ws.request.domain.WsRequestParameter;
import com.matt.remotr.ws.response.WsResponseForwarder;
import com.matt.remotr.ws.response.domain.WsDeviceResponse;
import com.matt.remotr.ws.response.domain.WsResponse;

public class WsRequestManagerDefault implements WsRequestManager, WsRequestRunner{
	private Logger log;
	private WsResponseForwarder responseForwarder;
	
	private Map<String, WsRequestRunner> runnerMap; // Maps a subsystem name to it's runner
	private Map<String, Map<Method, ArrayList<Class<?>>>> methodMap; // List of subsystems with all methods and params needed
	
	public WsRequestManagerDefault(){
		log = Logger.getLogger(this.getClass());
		runnerMap = new HashMap<String, WsRequestRunner>();
		methodMap = new HashMap<String, Map<Method, ArrayList<Class<?>>>>();
		
		//Register with yourself...
		register(this);
	}

	public void setResponseForwarder(WsResponseForwarder responseForwarder) {
		this.responseForwarder = responseForwarder;
	}

	@Override
	public boolean register(WsRequestRunner requestRunner) {
		if(!runnerMap.containsKey(requestRunner.getSubSystemName())){
			// We haven't seen this runner before - Add it to the map then get it's published methods
			runnerMap.put(requestRunner.getSubSystemName(), requestRunner);
			
			Map<Method, ArrayList<Class<?>>> methodParamMap = new HashMap<Method, ArrayList<Class<?>>>();
			Method[] methods = requestRunner.getClass().getMethods();
			for(Method m : methods){
				if(AnnotationUtils.findAnnotation(m, WsRequestMethod.class) != null || 
						AnnotationUtils.findAnnotation(m, WebMethod.class) != null){
					// it's a WebMethod - add it to the list and see if it needs any params
					ArrayList<Class<?>> paramList = new ArrayList<Class<?>>();
					Class<?>[] paramTypes = m.getParameterTypes();
					for(Class<?> paramType : paramTypes){
						paramList.add(paramType);
					}
					methodParamMap.put(m, paramList);
				}
			}
			methodMap.put(requestRunner.getSubSystemName(), methodParamMap);
			
			log.debug("WsRequestRunner for subsystem ["+requestRunner.getSubSystemName()+"] registered");
			return true;
		}else{
			return false;
		}
	}

	@Override
	public boolean unregister(WsRequestRunner requestRunner) {
		if(runnerMap.containsKey(requestRunner.getSubSystemName())){
			runnerMap.remove(requestRunner.getSubSystemName());
			return true;
		}else{
			return false;
		}
	}

	@Override
	public void runRequest(WsRequest wsRequest, Device device) {
		WsResponse response = getWsResponse();
		response.setReference(wsRequest.getReference());
		if(runnerMap.containsKey(wsRequest.getSubSystem())){
			WsRequestRunner requestRunner = runnerMap.get(wsRequest.getSubSystem());
			Object obj = runRequestOnRunner(wsRequest, requestRunner);
			if(obj instanceof WsDeviceResponse){
				response = (WsDeviceResponse) obj;
 			}else if(obj instanceof WsResponse){
				response = (WsResponse) obj;
 			}else{
 				log.error("Unknown response when running WsRequest ["+wsRequest.getReference()+"]");
 				response.setErrorMessage("Unknown response when running WsRequest");
 			}
		}else{
			log.error("No subsystem of name ["+wsRequest.getSubSystem()+"] found to handle this request");
			response.setErrorMessage("Subsystem name ["+wsRequest.getSubSystem()+"] not found");
		}
		responseForwarder.forwardWsResponse(device, response);
	}
	
	private Object runRequestOnRunner(WsRequest request, WsRequestRunner runner){
		try{
			ArrayList<WsRequestParameter> params = request.getParams();
			if(params != null){
				ArrayList<Object> l = new ArrayList<Object>();
				List<Class<?>> c = new ArrayList<Class<?>>();
				for(WsRequestParameter p : params){
					Class<?> clazz = Class.forName(p.getClassType());
					JAXBElement<?> element = (JAXBElement<?>) p.getValue();
					Object o = element.getValue();
					l.add(clazz.cast(o));	
					c.add(clazz);
				}
				Method method = runner.getClass().getMethod(request.getMethod(), c.toArray(new Class[c.size()]));
				return method.invoke(runner, l.toArray());
			}else{
				Method method = runner.getClass().getMethod(request.getMethod());
				return method.invoke(runner);
			}
		}catch(Exception e){
			log.error("Error invoking method via WsRequest", e);
		}
		return null;
	}
	
	@WsRequestMethod
	public WsResponse getSubSystems(){
		WsResponse response = getWsResponse();
		ArrayList<String> subsystems = new ArrayList<String>(runnerMap.keySet());
		response.setListResponse(subsystems);
		
		response.setSuccess(true);
		return response;
	}
	
	@WsRequestMethod
	public WsResponse getMethods(String subsystem){
		WsResponse response = getWsResponse();
		if(methodMap.containsKey(subsystem)){			
			Map<Method, ArrayList<Class<?>>> map = methodMap.get(subsystem);
			ArrayList<Method> methods = new ArrayList<Method>(map.keySet());
			
			ArrayList<String> methodNames = new ArrayList<String>();
			for(Method m : methods){
				methodNames.add(m.getName());
			}
			
			response.setListResponse(methodNames);
		}
		
		response.setSuccess(true);
		return response;		
	}
	
	@WsRequestMethod
	public WsResponse getParams(String subsystem, String methodName){
		WsResponse response = getWsResponse();
		ArrayList<WsRequestParameter> requestParameters = new ArrayList<WsRequestParameter>();
		Map<Method, ArrayList<Class<?>>> map = methodMap.get(subsystem);
		ArrayList<Method> methods = new ArrayList<Method>(map.keySet());
		
		for(Method m : methods){
			if(m.getName().equals(methodName)){
				ArrayList<Class<?>> paramList = map.get(m);
				for(Class<?> c : paramList){
					WsRequestParameter parameter = new WsRequestParameter();
					parameter.setClassType(c.getName());
					requestParameters.add(parameter);
				}
			}
		}
		
		response.setListResponse(requestParameters);
		response.setSuccess(true);
		return response;		
	}
	
	@WsRequestMethod
	public WsResponse getAll(){
		WsResponse response = getWsResponse();
		ArrayList<Map<String, Map<String, ArrayList<String>>>> list = new ArrayList<Map<String, Map<String, ArrayList<String>>>>();
		ArrayList<String> subsystems = new ArrayList<String>(runnerMap.keySet());
		
		for(String s : subsystems){
			Map<String, Map<String, ArrayList<String>>> sMap = new HashMap<String, Map<String, ArrayList<String>>>();
			Map<String, ArrayList<String>> mMap = new HashMap<String, ArrayList<String>>();

			Map<Method, ArrayList<Class<?>>> map = methodMap.get(s);
			ArrayList<Method> methods = new ArrayList<Method>(map.keySet());
			
			for(Method m : methods){
				ArrayList<Class<?>> paramList = map.get(m);
				ArrayList<String> pList =  new ArrayList<String>();
				for(Class<?> p : paramList){
					pList.add(p.getName());
				}
				mMap.put(m.getName(), pList);
			}
			
			sMap.put(s, mMap);
			list.add(sMap);
		}	
		
		response.setListResponse(list);
		response.setSuccess(true);
		return response;
	}
	
	private WsResponse getWsResponse(){
		WsResponse response = new WsResponse();
		response.setSubSystem(getSubSystemName());
		
		return response;
	}
	
	@Override
	public String getSubSystemName() {
		return "WsRequestManager";
	}

}
