# -*- coding:utf-8 -*-
from azure.cognitiveservices.vision.face.models import APIErrorException
import cv2
import threading
import playSound

import FaceDetection
import UseFirebase as UF
import ArduinoSerialProtocol
import SocetServer


def work():
    global working
    global text
    try:
        if not working:
            working = True
            sound_thread = threading.Thread(target=playSound.playSound.play, args=("sound/audio_0.wav",))
            sound_thread.daemon = True
            sound_thread.start()
            fd.init_source_images()
            thread = threading.Thread(target=fd.verify_face_to_face)
            thread.daemon = True
            thread.start()
            thread.join()
            student_id, student_json = fd.student_id, fd.json_data
            if student_json["result"]:
                text = "temperature check"
                if ArduinoSerialProtocol.ArduinoSerialProtocol.connected:
                    ArduinoSerialProtocol.ArduinoSerialProtocol.start2()
                    temp = float(max(ArduinoSerialProtocol.ArduinoSerialProtocol.data))  # 5개의 값중 가장 높은값을 불러옴
                    student_json[student_id]["temp"] = temp
                    sound_thread_args = ""
                    if 34 < temp < 37.5:
                        student_json[student_id]["result"] = 1  # 정상
                        sound_thread_args = ("sound/audio_1.wav",)
                    elif 37.5 < temp < 38:
                        student_json[student_id]["result"] = 2  # 미열
                        sound_thread_args = ("sound/audio_2.wav",)
                    elif 38 < temp < 41:
                        student_json[student_id]["result"] = 3  # 고열
                        sound_thread_args = ("sound/audio_3.wav",)
                    else:
                        student_json[student_id]["result"] = 0  # 오류
                        sound_thread_args = ("sound/audio_5.wav",)
                    sound_thread = threading.Thread(target=playSound.playSound.play, args=(sound_thread_args,))
                    sound_thread.daemon = True
                    sound_thread.start()
                    print("온도 결과", student_json[student_id]["temp"], student_json[student_id]["result"])
                else:
                    print("아두이노 미 연결")
                    student_json[student_id]["temp"] = 0
                    student_json[student_id]["result"] = 0
                # 소켓통신으로 받아온 데이터의 가장 최근항목을 테이블의 이름으로 사용.
                ref_dir = ss.data[-1]
                print("소켓통신 데이터 목록 :", ss.data)
                print("ref_dir =", ref_dir)
                UF.UseFirebase.updateData(ref_dir, student_id, student_json[student_id])

            else:
                print("인식결과 없음")
    except Exception as e:
        print(e)
    finally:
        fd.init_face_data()
        text = "detecting face"
        fd.count = 0
        working = False


if __name__ == "__main__":
    ss = SocetServer.SocetServer.instance()
    server_thread = threading.Thread(target=ss.start)
    server_thread.daemon = True
    server_thread.start()
    fd = FaceDetection.FaceDetection.instance()
    asp = ArduinoSerialProtocol.ArduinoSerialProtocol.instance()
    working = False
    text = "detecting face"
    # 데이터 들어올때까지 실행안함.
    while True:
        while not ss.data:
            pass
        while ss.server_state:
            try:
                fd.capture_faces(text)
                if fd.count == 3:
                    text = "checking"
                    print('Colleting Samples Complete!!!')
                    if not working:
                        print("working")
                        work_thread = threading.Thread(target=work)
                        work_thread.daemon = True
                        work_thread.start()
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
        ss.data = []
        print("대기")
        cv2.destroyAllWindows()
