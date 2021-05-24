from time import sleep

import configparser

import serial
from serial import SerialException


class ArduinoSerialProtocol:
    config = configparser.ConfigParser()
    config.read("config.ini", encoding="utf-8")
    data = []
    __instance = None
    connected = False

    try:
        ser = serial.Serial(
            port=config["Arduino"]["port"],
            baudrate=config["Arduino"]["baudrate"],
            timeout=1
        )
        connected = True
    except serial.SerialException as se:
        print(se)
        connected = False

    @classmethod
    def __getInstance(cls):
        return cls.__instance

    @classmethod
    def instance(cls, *args, **kargs):
        cls.__instance = cls(*args, **kargs)
        cls.instance = cls.__getInstance
        return cls.__instance

    def __init__(self):
        self.config = configparser.ConfigParser()
        self.config.read("config.ini", encoding="utf-8")
        self.temp_result = []

        connect = True
        while not connect:
            try:
                self.ser = serial.Serial(
                    # port="/dev/ttyACM0",
                    port=self.config["Arduino"]["port"],
                    baudrate=self.config["Arduino"]["baudrate"],
                    timeout=1
                )
                print("?곌껐?꾨즺 ", self.ser.portstr)  # ?곌껐???ы듃 ?뺤씤.
                connect = True
            except serial.SerialException as se:
                print(se)
                print("?쒕━???ы듃 ?곌껐 ?ㅻ쪟")

    def start(self, command):
        count = 0
        while count < 5:
            try:
                if command == "s":
                    # command += "\n"
                    # print(command)
                    print(self.ser.readable())
                    print(self.ser.write(bytes("s", encoding='ascii')))
                    sleep(3)
                    # self.ser.write(b"s\n")
                    print("send message")
                    command = ""
                # check
                if self.ser.readable():
                    print(self.ser)
                    count += 1
                    res = self.ser.readline().decode()
                    # decode byte data and slice \n
                    print(res)
                    self.temp_result.append(res)

            except serial.SerialException as se:
                print(se)
                print("SerialException")

            except ValueError as ve:
                print(ve)

            except Exception as e:
                print(e)

    @classmethod
    def start2(cls):
        cls.data = []
        print("전송된 byte 길이 =", cls.ser.write("s".encode()))
        while len(cls.data) < 5:
            try:

                res = cls.ser.readline().decode()
                # decode byte data and slice \n
                if res:
                    cls.data.append(res.strip())
                print(res)

            except serial.SerialException as se:
                print(se)
                print("SerialException")

            except ValueError as ve:
                print(ve)
        print("받은 데이터", cls.data)
        return max(cls.data)


if __name__ == "__main__":
    print(ArduinoSerialProtocol.start2())
