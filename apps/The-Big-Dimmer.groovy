/**
 *  Copyright 2015 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  The Big Switch
 *
 *  Author: SmartThings
 *
 *  Date: 2013-05-01
 */
definition(
	name: "The Big Dimmer",
	namespace: "petermajor",
	author: "Peter Major",
	description: "Based on 'The Big Switch'. Turns on, off and dim a collection of lights based on the state of a specific dimmer.",
	category: "Convenience",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet@2x.png"
)

preferences {
	section("When this switch is turned on, off or dimmed") {
		input "master", "capability.switch", title: "Where?"
	}
	section("Dim these switches") {
		input "dimSwitches", "capability.switchLevel", multiple: true, required: false
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
	subscribe(master, "switch.on", onHandler)
	subscribe(master, "switch.off", offHandler)
	subscribe(master, "level", dimHandler)   
}

def onHandler(evt) {
	log.debug "On"
	log.debug "Dim level: $master.currentLevel"
	dimSwitches?.on()
  	dimSwitches?.setLevel(master.currentLevel)
}

def offHandler(evt) {
	log.debug "Off"
	dimSwitches?.off()
}

def dimHandler(evt) {

	def newLevel = evt.value.toInteger()
	log.debug "Dim level: $newLevel"

	dimSwitches?.setLevel(newLevel)
}