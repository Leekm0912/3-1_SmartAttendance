# -*- coding:utf-8 -*-
from azure.cognitiveservices.vision.face.models import APIErrorException
import cv2
import threading
from multiprocessing.pool import ThreadPool

from requests import get

import FaceDetection
import UseFirebase as UF
import ArduinoSerialProtocol

import requests


def work():
    global working
    global text
    if not working:
        working = True
        fd.init_source_images()
        thread = threading.Thread(target=fd.verify_face_to_face)
        thread.start()
        thread.join()
        student_id, student_json = fd.student_id, fd.json_data
        if student_json["result"]:
            # 일단 지금은 온도센서코드 달기 전이니 테스트 위해 직접 넣어줌.
            text = "temperature check"
            #thread = threading.Thread(target=ArduinoSerialProtocol.ArduinoSerialProtocol.start2)
            # asp.start("s")  # 온도측정 시작
            #thread.start()
            #thread.join()
            ArduinoSerialProtocol.ArduinoSerialProtocol.start2()
            temp = float(max(ArduinoSerialProtocol.ArduinoSerialProtocol.data))  # 5개의 값중 가장 높은값을 불러옴
            student_json[student_id]["temp"] = temp
            if 34 < temp < 37.5:
                student_json[student_id]["result"] = 1  # 정상
            elif 37.5 < temp < 38:
                student_json[student_id]["result"] = 2  # 미열
            elif 38 < temp < 41:
                student_json[student_id]["result"] = 3  # 고열
            else:
                student_json[student_id]["result"] = 0  # 오류
            print("온도 결과", student_json[student_id]["temp"], student_json[student_id]["result"])
            # 역시 소켓통신 구현 전이니 직접 넣어줌
            ref_dir = "210512_1_K0125146"
            UF.UseFirebase.updateData(ref_dir, student_id, student_json[student_id])

        else:
            print("인식결과 없음")
        fd.init_face_data()
        text = "detecting face"
        fd.count = 0
        working = False


if __name__ == "__main__":

    fd = FaceDetection.FaceDetection.instance()
    asp = ArduinoSerialProtocol.ArduinoSerialProtocol.instance()
    working = False
    tp = ThreadPool(processes=1)
    text = "detecting face"
    while True:
        try:
            fd.capture_faces(text)
            if fd.count == 3:
                text = "checking"
                print('Colleting Samples Complete!!!')
                if not working:
                    print("working")
                    threading.Thread(target=work).start()

        # API 요청 한도 초과.
        except APIErrorException as ae:
            print(ae.message)

        except TypeError as te:
            print("TypeError")

        # Q 누를시 종료
        if cv2.waitKey(1) & 0xFF == ord('q'):
            fd.cap.release()
            cv2.destroyAllWindows()
            exit()
