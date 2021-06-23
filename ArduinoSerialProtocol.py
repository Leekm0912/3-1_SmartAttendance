from time import sleep

import configparser

import serial
from serial import SerialException


class ArduinoSerialProtocol:
    # 클래스 로딩시 자동으로 연결되도록.
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

    # 싱글톤
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
                connect = True
            except serial.SerialException as se:
                print(se)

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

            except SerialException as se:
                print(se)
                print("SerialException")

            except ValueError as ve:
                print(ve)
            # 기타 예외 발생해도 프로그램 종료 안되도록.
            except Exception as e:
                print(e)

    # 객체 만들어서 생성하는방법은 불안정해서 static 메소드로 바로 사용.
    # 아두이노에게 온도 데이터를 달라는 신호를 보낸 후 측정한 온도중 가장 높은값을 리턴.
    @classmethod
    def start2(cls):
        # 온도 데이터 저장할 list
        cls.data = []
        # 아두이노에게 신호를 보내는 부분
        # ser.write는 전송한 byte를 리턴하므로 전달 여부를 위해 print를 사용.
        print("전송된 byte 길이 =", cls.ser.write("s".encode()))
        # 5회 측정함
        while len(cls.data) < 5:
            try:
                # 시리얼에서 byte data를 읽어온 후 디코딩
                res = cls.ser.readline().decode()
                # data가 있다면 공백과 줄바꿈(\n) 삭제 후 data에 추가.
                if res:
                    cls.data.append(res.strip())
                print(res)
            # Serial 관련 예외 처리
            except serial.SerialException as se:
                print(se)
                print("SerialException")
            # 통신 오류인지 가끔 이상한 값이 넘어와서 예외가 발생해 처리해줌.
            except ValueError as ve:
                print(ve)
        print("받은 데이터", cls.data)
        # 받은 데이터중 가장 높은값을 리턴해줌.
        return max(cls.data)


if __name__ == "__main__":
    print(ArduinoSerialProtocol.start2())
