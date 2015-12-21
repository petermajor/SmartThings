metadata {
        definition (name: "Pete Z-Wave Test Device", namespace: "petermajor", author: "Peter Major") {
                capability "Actuator"
                capability "Switch"
                capability "Polling"
                capability "Refresh"
                capability "Configuration"
                capability "Zw Multichannel"
        }

        simulator {
                // These show up in the IDE simulator "messages" drop-down to test
                // sending event messages to your device handler
                status "basic report on":
                       zwave.basicV1.basicReport(value:0xFF).incomingMessage()
                status "basic report off":
                        zwave.basicV1.basicReport(value:0).incomingMessage()
                status "basic set on":
                           zwave.basicV1.basicSet(value:0xFF).incomingMessage()
                status "multichannel switch":
                       zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1).encapsulate(zwave.switchBinaryV1.switchBinarySet(switchValue: 0xFF)).incomingMessage()

                // simulate turn on
                reply "2001FF,delay 5000,2002": "command: 2503, payload: FF"

                // simulate turn off
                reply "200100,delay 5000,2002": "command: 2503, payload: 00"
        }

        tiles {
                standardTile("switch", "device.switch", width: 2, height: 2,
                            canChangeIcon: true) {
                        state "on", label: '${name}', action: "switch.off",
                              icon: "st.unknown.zwave.device", backgroundColor: "#79b821"
                        state "off", label: '${name}', action: "switch.on",
                              icon: "st.unknown.zwave.device", backgroundColor: "#ffffff"
                }
                standardTile("refresh", "command.refresh", inactiveLabel: false,
                             decoration: "flat") {
                        state "default", label:'', action:"refresh.refresh",
                              icon:"st.secondary.refresh"
                }

                main (["switch", "temperature"])
                details (["switch", "refresh"])
        }
}

def parse(String description) {
        def result = null
        def cmd = zwave.parse(description, [0x60: 3])
        if (cmd) {
                result = zwaveEvent(cmd)
                log.debug "Parsed ${cmd} to ${result.inspect()}"
        } else {
                log.debug "Non-parsed event: ${description}"
        }
        result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd)
{
    log.debug "MCO2-zwaveEvent-BasicReport ${cmd}"
    createEvent(name:"switch", value: cmd.value ? "on" : "off")
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
    log.debug "MCO2-zwaveEvent-SwitchBinaryReport ${cmd}"
    createEvent(name:"switch", value: cmd.value ? "on" : "off")
}

// Many sensors send BasicSet commands to associated devices.
// This is so you can associate them with a switch-type device
// and they can directly turn it on/off when the sensor is triggered.
def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd)
{
    log.debug "MCO2-zwaveEvent-BasicSet ${cmd}"
    createEvent(name:"switch", value: cmd.value ? "on" : "off")
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
    log.debug "MCO2-zwaveEvent-MultiChannelCmdEncap ${cmd}"
    
    //def encapsulatedCommand = cmd.encapsulatedCommand([0x20: 1, 0x25: 1])

    // can specify command class versions here like in zwave.parse
    //log.debug ("Command from endpoint ${cmd.sourceEndPoint}: ${encapsulatedCommand}")

    //if (encapsulatedCommand) {
    //    return zwaveEvent(encapsulatedCommand)
    //}
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelEndPointReport cmd) {
    log.debug "MCO2-zwaveEvent-MultiChannelEndPointReport ${cmd}"
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
    log.debug "MCO2-zwaveEvent-Command ${cmd}"
    createEvent(descriptionText: "${device.displayName}: ${cmd}")
}

def on() {
    log.debug "MCO2-on"
    delayBetween([
        zwave.basicV1.basicSet(value: 0xFF).format(),
        zwave.basicV1.basicGet().format()
    ], 2000)}

def off() {
    log.debug "MCO2-off"
    delayBetween([
        zwave.basicV1.basicSet(value: 0xFF).format(),
        zwave.basicV1.basicGet().format()
    ], 2000)
}

def refresh() {
    log.debug "MCO2-refresh"
    zwave.basicV1.basicGet().format()
}

def poll() {
    log.debug "MCO2-poll"
    zwave.basicV1.basicGet().format()
}

def configure() {
    log.debug "MCO2-configure"
    def cmd = delayBetween([
        
        //zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint:2).encapsulate(
        //  zwave.basicV1.basicSet(value: 0x00)
        //).format()
        zwave.multiChannelV3.multiChannelEndPointGet().format()
    ], 800)
  log.debug "MCO2-configure: ${cmd}"
  cmd
}