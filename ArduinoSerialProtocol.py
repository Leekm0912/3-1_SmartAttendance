from time import sleep

import configparser

import serial
from serial import SerialException


class ArduinoSerialProtocol:
    data = []
    __instance = None
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
                print("연결완료 ", self.ser.portstr)  # 연결된 포트 확인.
                connect = True
            except serial.SerialException as se:
                print(se)
                print("시리얼 포트 연결 오류")

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
        try:
            ser = serial.Serial(
                port="COM5",
                baudrate=9600,
                timeout=1
            )
            cls.connected = True
        except serial.SerialException as se:
            print(se)
            cls.connected = False
        while len(cls.data) < 5:
            try:
                print(ser.write("s".encode()))
                res = ser.readline().decode()
                # decode byte data and slice \n
                if res:
                    cls.data.append(res.strip())
                print(res)

            except serial.SerialException as se:
                print(se)
                print("SerialException")

            except ValueError as ve:
                print(ve)
        print(cls.data)
        return max(cls.data)


if __name__ == "__main__":
    asp = ArduinoSerialProtocol.instance()
    print(ArduinoSerialProtocol.start2())
    print(asp.ser.readable())
    print(asp.ser.write(bytes("s", encoding='ascii')))
    sleep(3)
    while asp.ser.readable():
        # print(asp.ser)
        res = asp.ser.readline().decode()
        # decode byte data and slice \n
        print(res)
        asp.temp_result.append(res)
    asp.ser.close()
    # print(asp.temp_result)
