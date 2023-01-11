import time
from machine import UART
import machine
import network
import urequests
import ujson
import socket
import time
import os
import my_lib


class DistanceReader:
    dataLength = 14
    dataToWait = 16
    dataPrefix = bytearray(b'\x57\x00')
    timeToWait = 0.05
    INF = 10000
    def __init__(self):
        self.uart = UART(0,921600)
        self.uart.flush()

    def __find_start(self, data):
        for i in range(self.dataLength):
            if data[i] == self.dataPrefix[0] and data[i + 1] == self.dataPrefix[1]:
                return i
        return None

    def __out_of_range(self, data):
        return (data[12] | (data[13] << 8)) == 0

    def __wait_for_start(self):
        # self.uart.flush()
        currentPrefix = bytearray(b'\x00\x00')
        while True:
            if self.uart.any() == 0:
                time.sleep(self.timeToWait)
                continue
            currentPrefix[0] = currentPrefix[1]
            currentPrefix[1] = self.uart.read(1)[0]
            if currentPrefix == self.dataPrefix:
                return


    def read(self):
        self.__wait_for_start()
        data = self.uart.read(self.dataLength)
        if data is None:
            raise RuntimeError("no data from uart")
        data = self.dataPrefix + data
        if self.__out_of_range(data):
            return self.INF, '0', 0
        distance = (data[8]) | (data[9]<<8) | (data[10]<<16);
        status = str(data[11]);
        signal = data[12] | data[13]<<8;
        return distance, status, signal


class LaserSender():
    def __init__(self):
        self.wifi_connection = my_lib.WifiConnection()
        self.values = []
        self.values_to_send = 20
        self.signaller = my_lib.StatusSignaller()

    def __send_readings(self):
        start = time.ticks_ms()
        value_json = ujson.dumps({"laserValue": self.values[0], "values": self.values})
        signaller.blink('sending_data')
        response = urequests.post('http://192.168.1.107:8080/v1/laser/values/submit', headers = {'content-type': 'application/json'}, data = value_json)
        end = time.ticks_ms()
        print(f'got response in {time.ticks_diff(end, start)}ms')
        signaller.blink('sent_data')
        if response.status_code != 200:
            print(response.status_code)
        response.close()
        signaller.blink('sent_data')


    def submit_reading(self, value):
        self.values.append(value)
        if len(self.values) >= self.values_to_send:
            self.__send_readings()
            self.values = []


reader = DistanceReader()
sender = LaserSender()
signaller = my_lib.StatusSignaller()
while True:
    try:
        sender.wifi_connection.reconnect_if_needed()
        data = reader.read()
        print(data)
        sender.submit_reading(data[0])
    except:
        pass
