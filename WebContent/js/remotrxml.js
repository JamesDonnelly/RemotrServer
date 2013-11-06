/*
 * Remotr XML Calls
 * These should probably be made on demand
 * @author Matt
 */

var 

SESSION_LOGIN = 
	'<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'+
	'<Request>'+
	    '<Subsystem>'+
	        '<SubsystemName>Session</SubsystemName>'+
	        '<Methods>'+
	            '<Method name="login">'+
	                '<Params>'+
	                    '<Param type="Device" name="device">'+
	                        '<Device>'+
	                            '<Name>RemotrWebUI</Name>'+
	                            '<Type>System</Type>'+
	                            '<Resources>'+
	                                '<Resource>'+
	                                    '<Name>Broadcaster</Name>'+
	                                    '<EventType>Broadcast</EventType>'+
	                                    '<DisplayName>Broadcaster Node</DisplayName>'+
	                                    '<Online>true</Online>'+
	                                '</Resource>'+
	                            '</Resources>'+
	                        '</Device>'+
	                    '</Param>'+
	                '</Params>'+
	            '</Method>'+
	        '</Methods>'+
	    '</Subsystem>'+
	'</Request>',

EVENT_SUBSCRIBE_BROADCAST = 
	'<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'+
	'<Request>'+
	    '<Subsystem>'+
	        '<SubsystemName>Event</SubsystemName>'+
	        '<Methods>'+
	            '<Method name="registerForEvents">'+
	                '<Params>'+
	                    '<Param type="Event" name="event">'+
	                        '<Event>'+
	                        	'<Type>Broadcast</Type>'+
	                        	'<Name>RegisterForEvents</Name>'+
	                        '</Event>'+
	                    '</Param>'+
	                '</Params>'+
	            '</Method>'+
	        '</Methods>'+
	    '</Subsystem>'+
	'</Request>'

DEVICE_GET_ALL = 
	'<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'+
	'<Request>'+
	    '<Subsystem>'+
	        '<SubsystemName>Device</SubsystemName>'+
	        '<Methods>'+
	            '<Method name="getAllRegisteredDevices">'+
	            '</Method>'+
	        '</Methods>'+
	    '</Subsystem>'+
	'</Request>'


	;