/**
 *  Switch Activates Hello, Home Phrase
 *
 *  Copyright 2017 Peter Major
 *  Modified version of https://raw.githubusercontent.com/SmartThingsCommunity/SmartThingsPublic/master/smartapps/michaelstruck/switch-activates-home-phrase.src/switch-activates-home-phrase.groovy
 *  Version 1.01 3/8/15
 *  Changes:
 *  Allows either on or off phrase to be null
 *  Can set app name
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
 *  Ties a Hello, Home phrase to a switch's (virtual or real) on/off state. Perfect for use with IFTTT.
 *  Simple define a switch to be used, then tie the on/off state of the switch to a specific Hello, Home phrases.
 *  Connect the switch to an IFTTT action, and the Hello, Home phrase will fire with the switch state change.
 *
 *
 */
definition(
    name: "Switch Activates Home Phrase",
    namespace: "petermajor",
    author: "Peter Major",
    description: "Ties a Hello, Home phrase to a switch's state. Perfect for use with IFTTT.",
    category: "Convenience",
    iconUrl: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/IFTTT-SmartApps/App1.png",
    iconX2Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/IFTTT-SmartApps/App1@2x.png",
    iconX3Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/IFTTT-SmartApps/App1@2x.png")


preferences {
	page(name: "getPref")
}
	
def getPref() {    
    dynamicPage(name: "getPref", title: "Choose Switch and Phrases", install:true, uninstall: true) {
    section("Choose a switch to use...") {
		input "controlSwitch", "capability.switch", title: "Switch", multiple: false, required: true
    }
    def phrases = location.helloHome?.getPhrases()*.label
		if (phrases) {
        	phrases.sort()
			section("Perform the following phrase when...") {
				log.trace phrases
				input "phrase_on", "enum", title: "Switch is on", required: false, options: phrases
				input "phrase_off", "enum", title: "Switch is off", required: false, options: phrases
			}
			section([mobileOnly:true]) {
				label title: "Assign a name", required: false
			}			
		}
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	subscribe(controlSwitch, "switch", "switchHandler")
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	subscribe(controlSwitch, "switch", "switchHandler")
}

def switchHandler(evt) {
	if (evt.value == "on" && settings.phrase_on) {
    	location.helloHome.execute(settings.phrase_on)
    } else if (evt.value == "off" && settings.phrase_off) {
    	location.helloHome.execute(settings.phrase_off)
    }
}

