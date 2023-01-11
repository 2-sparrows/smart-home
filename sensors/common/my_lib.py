import network
import machine
import time


SIGNAL_COUNT = {
    'wifi_initializing': 2,
    'wifi_start_connecting': 3,
    'wifi_reconnecting': 6,
    'wifi_failed': 7,
    'wifi_connected': 4,
    'sending_data': 1,
    'sent_data': 2,
}


class Logger():
    def log(self, text):
        f = open('log.txt', 'w')
        print(f.write(text))
        f.close()

def read_wifi_credentials():
    with open('wifi', 'r') as f:
        return f.readline()[:-1], f.readline()[:-1]


class WifiConnection():
    def __initialize(self):
        signaller = StatusSignaller()
        ssid, password = read_wifi_credentials()
        signaller.blink('wifi_initializing')

        self.wlan = network.WLAN(network.STA_IF)
        self.wlan.active(True)
        self.wlan.connect(ssid, password)
        signaller.blink('wifi_start_connecting')

        # Wait for connect or fail
        max_wait = 20
        while max_wait > 0:
            if self.wlan.status() < 0 or self.wlan.status() >= 3:
                break
            max_wait -= 1
            print('waiting for connection...')
            signaller.blink('wifi_reconnecting')
            time.sleep(1)

        # Handle connection error
        if self.wlan.status() != 3:
            signaller.blink('wifi_failed')
            raise RuntimeError('network connection failed')
        else:
            signaller.blink('wifi_connected')
            print('connected')
        status = self.wlan.ifconfig()
        print('ip = ' + status[0])
        Logger().log(status[0] + ' ' + str(self.wlan.status()))

    def __init__(self):
        self.__initialize()

    def reconnect_if_needed(self):
        if self.wlan == None or self.wlan.status() != 3 or not self.wlan.isconnected():
            self.__initialize()


class StatusSignaller():

    def __init__(self):
        self.led = machine.Pin('LED', machine.Pin.OUT)
        self.iter_count = 0
        self.blink_each = 5
        self.time_to_sleep = 0.1

    def iteration(self):
        self.iter_count += 1
        if self.iter_count >= self.blink_each:
            self.iter_count = 0
            self.led.on()
            time.sleep(.1)
            self.led.off()

    def on(self):
        self.led.on()

    def off(self):
        self.led.off()

    def blink(self, name):
        amount = SIGNAL_COUNT[name]
        for _ in range(amount):
            self.on()
            time.sleep(self.time_to_sleep)
            self.off()
            time.sleep(self.time_to_sleep)
