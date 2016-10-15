definition(
    name: "Turn on lights when Home",
    namespace: "petermajor",
    author: "Peter Major",
    description: "Turn on lights when mode changes from Away to Home, but only if it's dark.",
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
    subscribe(location, "mode", modeChanged)

    state.lastMode = location.mode

    log.debug "current mode is ${state.lastMode}"
}

def isDark() {
    def time = now()
    log.debug "now: " + time
    
    def times = getSunriseAndSunset()
    log.debug "times: ${times}"
    log.debug "sunrise: ${times.sunrise}, ${times.sunrise.time}"
    log.debug "sunset: ${times.sunset}, ${times.sunset.time}"
    
    def result = time < times.sunrise.time || time > times.sunset.time
    log.debug "isDark = $result"
    result
}

def modeChanged(evt) {
    log.debug "modeChanged: $evt"

    log.debug "previous mode: ${state.lastMode}"

    // value will be the name of the mode
    // e.g., "Home" or "Away"
    log.debug "new mode: ${evt.value}"
    
    if(evt.value == "Home" && state.lastMode == "Away") {
        if(isDark()) {
            log.debug "turning lights on"
            switches.on()
        }
    }
}