import time
from machine import UART
import network
import urequests
import ujson
import socket
import time
import my_lib


class LightsReader():
    def __init__(self):
        self.wifi_connection = my_lib.WifiConnection()
        self.signaller = my_lib.StatusSignaller()

    def read_lights_value(self):
        try:
            response = urequests.get('http://192.168.1.107:8080/v1/lights/state/expected', headers = {'content-type': 'application/json'})
        except e:
            print(e)
            self.signaller.blink('receiving_error')
            return None
        if response.status_code != 200:
            print('got wrong response code: ' + str(response.status_code))
            return None

        response_json = response.json()
        response_value = response_json.get('laserValue', response_json.get('lightState'))
        response_value_bool = response_value == "On"
        if response_value_bool:
            self.signaller.blink('light_on')
        else:
            self.signaller.blink('light_off')
        return response_value_bool


class LightsSetter():
    def __init__(self):
        self.signal = machine.Pin(16, machine.Pin.OUT)

    def set_value(self, value):
        self.signal(value)


reader = LightsReader()
setter = LightsSetter()
while True:
    try:
        reader.wifi_connection.reconnect_if_needed()
        new_value = reader.read_lights_value()
        if new_value != None:
            print(f'setting {new_value}')
            setter.set_value(new_value)
        time.sleep(0.5)
    except Exception as e:
        print('Exception ' + str(e))
        pass


