/* Remotr Base JS
 * WIP
 * @author Matt
 */

$(document).ready(function() {
	var
		sessionKey
	;


	if ('WebSocket' in window) {
        connect("ws://sancho.local:8084/remotr/wsocket");
    }

    $("#show-debug").click(function(e) {        
    	$('#debug-console').toggle();
 	});

 	$('label label-primary').mouseover(function(event) {
 		$('event-popup').fadeIn('slow', function() {
 			
 		});
 	}).mouseout(function(event) {
 		$('event-popup').fadeOut('slow', function() {
 			
 		});
 	});
	
	function connect(host) {
        ws = new WebSocket(host);
        ws.onopen = function () {
            console.log('Connected');

            send(SESSION_LOGIN);

            setTimeout(function(){
				send(EVENT_SUBSCRIBE_BROADCAST); 
				send(DEVICE_GET_ALL)
			}, 2000);

			$('#info-panel').remove('#info-disconnected');
			$('#info-panel').append('<span class="label label-primary" id="info-connected">Info</span> Connected');
        };
 
        ws.onmessage = function (evt) {
        	var txt=$("<p></p>").text(evt.data); 
        	var xml = parseXML(evt.data);
        	$('#debug-console').append(txt);
        	console.log(evt.data);

        	if(xml.find("SessionKey") != null){
        		sessionKey = xml.find("SessionKey");
        	}

        	console.log(xml.find("Event"));
        	var eventXml = xml.find("Event");
        	if(xml.find("Event") != null){
        		$('#event-panel').append('<span class="label label-primary">'+eventXml.find("EventType").text()+'</span> '+
        			xml.find("Response >Event > Name").text());
        	}

        	if(xml.find("Devices") != null){
        		xml.find("Device").each(function(index) {
        			 $('#device-dropdown').append('<li><a href="#">'+$(this).find("> Name").text()+'</a></li>');
        		});
        	}
        	
        };
 
        ws.onclose = function () {
        	console.log('Disconnected')
           	
			$('#info-panel').remove('#info-connected');
			$('#info-panel').append('<span class="label label-danger" id="info-disconnected">Info</span> Disconnected');

        };

        function send(msg){
        	try{
        		var xml = parseXML(msg);
        		if(sessionKey !=null){
        			sessionKey.appendTo(xml.find("Request"));
        		}
        		
				var str = xmlToString(xml);
				console.log(str)
            	ws.send(str);  
          	}catch(exception){  
          		console.log("Something went wrong")
          		console.log(exception)
         	}  
        }

        function parseXML(xml){
        	xmlDoc = $.parseXML(xml)
			$xml = $(xmlDoc)
			return $xml
	    }

	    function xmlToString(xmlData) { 
	        var xmlString;
	        //IE
	        if (window.ActiveXObject){
	            xmlString = xmlData[0].xml;
	        }
	        // code for Mozilla, Firefox, Opera, etc.
	        else{
	            xmlString = (new XMLSerializer()).serializeToString(xmlData[0]);
	        }
	        return xmlString;
	    }   

    };
});