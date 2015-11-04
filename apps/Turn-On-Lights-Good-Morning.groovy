definition(
    name: "Turn on lights on Good Morning!",
    namespace: "petermajor",
    author: "Peter Major",
    description: "Turn on lights when Good Morning! routine is activated, but only if it's before sunrise.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
    section("Lights") {
        input "switches", "capability.switch", title: "Which lights to turn on?", required: true, multiple: true
        input "offset", "number", title: "Sunrise offset"
    }
}

def installed() {
    initialize()
}
    
def updated() {
    unsubscribe()
    initialize()
}

def initialize() {
    subscribe(location, "routineExecuted", routineChanged)
    subscribe(location, "sunriseTime", sunriseTimeChanged)
	subscribe(location, "sunsetTime", sunsetTimeChanged)
    
	setSunriseTimeWithOffset(location.currentValue("sunriseTime"))
	setSunsetTimeWithOffset(location.currentValue("sunsetTime"))
}

def sunriseTimeChanged(evt) {
	setSunriseTimeWithOffset(evt.value)
}

def setSunriseTimeWithOffset(timeString) {
    log.debug "sunriseTime: $timeString"
	def time = Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timeString)
	state.sunriseTimeWithOffset = time.time + (offset * 60 * 1000)
}

def sunsetTimeChanged(evt) {
	setSunsetTimeWithOffset(evt.value)
}

def setSunsetTimeWithOffset(timeString) {
    log.debug "sunsetTime: $timeString"
	def time = Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timeString)
	state.sunsetTimeWithOffset = time.time - (offset * 60 * 1000)
}

def isDark() {
	def time = now()
	def result = time < state.sunriseTimeWithOffset && state.sunriseTimeWithOffset < state.sunsetTimeWithOffset
	log.debug "isDark = $result"
	result
}

def routineChanged(evt) {
    log.debug "routineChanged: $evt"

    // name will be "routineExecuted"
    log.debug "evt name: ${evt.name}"

    // value will be the ID of the SmartApp that created this event
    log.debug "evt value: ${evt.value}"

    // descriptionText will be the name of the routine
    // e.g., "I'm Back!" or "Goodbye!"
    log.debug "evt descriptionText: ${evt.descriptionText}"
    
    if(evt.descriptionText == "Good Morning! was executed") {
        if(isDark()) {
            log.debug "turning lights on"
            switches.on()
        }
    }
}