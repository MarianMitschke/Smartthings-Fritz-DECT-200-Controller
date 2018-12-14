/**
 *  Fritz DECT 200 (Connect Edition)
 *
 *  Copyright 2016 Marian Mitschke
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
 */
 
metadata {
	definition (name: "fritz-dect-200-connect", namespace: "marianmitschke", author: "Marian Mitschke") {
		capability "Switch"
        capability "Refresh"
        capability "Polling"
        capability "TemperatureMeasurement"
        capability "Power Meter"
		capability "Energy Meter"
	}

	simulator {
	}

	tiles {
    		valueTile("temperature", "device.temperature", width: 1, height: 1) {
			state("temperature", label:'${currentValue}°', unit:"C",
					backgroundColors:[
							// Celsius
							[value: 0, color: "#153591"],
							[value: 7, color: "#1e9cbb"],
							[value: 15, color: "#90d2a7"],
							[value: 23, color: "#44b621"],
							[value: 28, color: "#f1d801"],
							[value: 35, color: "#d04e00"],
							[value: 37, color: "#bc2323"]
					]
			)
		}
        
		valueTile("power", "device.power", decoration: "flat") {
			state "default", label:'${currentValue} W'
		}
		valueTile("energy", "device.energy", decoration: "flat") {
			state "default", label:'${currentValue} kWh'
		}
    
        standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
            state "off", label: 'Off', action: "switch.on",
                  icon: "st.switches.switch.off", backgroundColor: "#ffffff"
            state "on", label: 'On', action: "switch.off",
                  icon: "st.switches.switch.on", backgroundColor: "#79b821"
        }
        
    	standardTile("refresh", "capability.refresh", width: 1, height: 1,  decoration: "flat") {
      		state ("default", label:"Refresh", action:"refresh.refresh", icon:"st.secondary.refresh")
    	}    
        
        main("switch")
        details(["switch","refresh", "temperature","power","energy"])
	}
    
    command "on"
    command "off"
}

preferences {
  input("outletAIN", "text", title: "Outlet AIN", required: true, displayDuringSetup: true)
}

def parse(description) {
    def msg = parseLanMessage(description)
}

def ain()
{
	return outletAIN
}

def on() {
	log.debug("Executing 'on'")
        
	parent.on(this)
}

def refresh(){
	log.debug("Executing 'refresh'")
    
	parent.refresh(this)
}

def off() {
  	log.debug("Executing 'off'")
        
	parent.off(this)
}

def poll(){
  	log.debug("Executing 'poll'")
    
	parent.poll(this)
}

def generateStateSwitch(boolean on)
{
	log.debug("received state switch event.")
	if (on)
		sendEvent(name: "switch", value: "on", isStateChange: true)
	else
		sendEvent(name: "switch", value: "off", isStateChange: true)
}


def generateRefreshEvent(response){
	log.debug(response)
    
    def energyval = Double.parseDouble(response.energy) / 1000
    def powerval = Double.parseDouble(response.power) / 1000
    def tempval = Double.parseDouble(response.temperature) / 10
    
    def tempStateChange = false
    
    if (state.tempval)
    {
    	if (state.tempval != tempval){
    		state.tempval = tempval
        	tempStateChange = true
        }
    }
    else
    {
    	state.tempval = tempval
        tempStateChange = true
    }
    
    sendEvent(name: "energy", value: energyval, unit: "kW")
    sendEvent(name: "power", value: powerval, unit: "W")
    sendEvent(name: "temperature", value: tempval, unit: "C", isStateChange: tempStateChange, displayed: true)
}