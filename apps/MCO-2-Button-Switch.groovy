definition(
	name: "MCO 2 Button Switch",
	namespace: "petermajor",
	author: "Peter Major",
	description: "App to sync the endpoints on the MCO 2 Button Switch with 2 switches",
	category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")
    
preferences {
	section("2 button switch") {
		input "master", "capability.zwMultichannel", title: "Switch?"
	}
	section("Controls these switchess") {
		input "switch1", "capability.switch", title: "Button 1", required: true
		input "switch2", "capability.switch", title: "Button 2", required: true
	}    
}

def installed()
{   
	initialize()
}

def updated()
{
	unsubscribe()
	initialize() 
}

def initialize()
{   
	subscribe(master, "epEvent", endpointEvent)
    
    subscribe(switch1, "switch.on", onHandler)
    subscribe(switch2, "switch.on", onHandler)
    subscribe(switch1, "switch.off", offHandler)
    subscribe(switch2, "switch.off", offHandler)    
}

def endpointEvent(evt) {
	def values = evt.value.split(":")
	def endpoint = values[0]
	def payload = values[1]

	def theswitch = (endpoint == "2") ? switch2 : switch1

	if (payload == "200300"){
    	theswitch.off();
    } else if (payload == "2003FF"){
    	theswitch.on();
    }
}

def onHandler(evt) {
	def endpoint = (evt.deviceId == switch2.id) ? 2 : 1
	master.on(endpoint)
}

def offHandler(evt) {
	def endpoint = (evt.deviceId == switch2.id) ? 2 : 1
	master.off(endpoint)
}