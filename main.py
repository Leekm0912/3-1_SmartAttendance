# -*- coding:utf-8 -*-
from azure.cognitiveservices.vision.face.models import APIErrorException
import cv2
import threading
import playSound

import FaceDetection
import UseFirebase as UF
import ArduinoSerialProtocol
import SocetServer


# 스레드에서 동작할 부분.
# 사용자에게 화면을 계속 출력하기 위해 스레드에서 실행.
def work():
    # 현재 작업중이란걸 판단해줄 변수
    global working
    # 사용자에게 출력할 text를 담은 변수
    global text
    try:
        # 현재 작업 실행을 하고있지 않다면 실행. 중복실행 방지.
        if not working:
            # 현재 작업중이란걸 알려주기 위해. 중복실행 방지.
            working = True
            # Azure API를 이용한 얼굴 비교 실행
            fd.init_source_images()
            thread = threading.Thread(target=fd.verify_face_to_face)
            thread.daemon = True
            thread.start()
            # 얼굴 비교 작업이 끝날때까지 기다려줌.
            thread.join()
            # 얼굴 비교가 끝난 후 데이터를 불러옴.
            student_id, student_json = fd.student_id, fd.json_data
            # 작업 결과가 true라면 json 가공
            if student_json["result"]:
                # 사용자에게 온도 측정중이라고 알려줌.
                text = "temperature check"
                playSound.playSound.play("sound/audio_0.wav")
                # 아두이노 연결되어있다면 온도측정.
                if ArduinoSerialProtocol.ArduinoSerialProtocol.connected:
                    ArduinoSerialProtocol.ArduinoSerialProtocol.start2()
                    # 아두이노에 온도 측정을 요청하고 체온값중 가장 높은값을 불러온 후 저장.
                    temp = float(max(ArduinoSerialProtocol.ArduinoSerialProtocol.data))
                    student_json[student_id]["temp"] = temp
                    # 체온에 따라 결과값 대입.
                    if 34 < temp < 37.5:
                        student_json[student_id]["result"] = 1  # 정상
                        sound_thread_args = "sound/audio_1.wav"
                    elif 37.5 < temp < 38:
                        student_json[student_id]["result"] = 2  # 미열
                        sound_thread_args = "sound/audio_2.wav"
                    elif 38 < temp < 41:
                        student_json[student_id]["result"] = 3  # 고열
                        sound_thread_args = "sound/audio_3.wav"
                    else:
                        student_json[student_id]["result"] = 0  # 오류
                        sound_thread_args = "sound/audio_5.wav"
                    playSound.playSound.play(sound_thread_args)
                    print("온도 결과", student_json[student_id]["temp"], student_json[student_id]["result"])
                # 아두이노가 연결되지 않았을때 테스트를 위해서 온도에 0을 대입.
                else:
                    print("아두이노 미 연결")
                    student_json[student_id]["temp"] = 0
                    student_json[student_id]["result"] = 0
                # 소켓통신으로 받아온 데이터의 가장 최근항목을 테이블의 이름으로 사용.
                ref_dir = ss.data[-1]
                print("소켓통신 데이터 목록 :", ss.data)
                print("ref_dir =", ref_dir)
                print("조회 id", student_id)
                print("data", student_json[student_id])
                # Firebase에 데이터 업로드.
                UF.UseFirebase.updateData(ref_dir, student_id, student_json[student_id])
            # 인식한 얼굴이 없을때.
            else:
                print("인식결과 없음")
    # 작업이 멈추지 않도록 처리.
    except Exception as e:
        print(e)
    # 데이터 초기화 후 working을 false로 줘서 실행이 가능하도록 함.
    finally:
        fd.init_face_data()
        text = "detecting face"
        fd.count = 0
        working = False


# 메인 진입점.
if __name__ == "__main__":
    # 소켓 서버 실행. 백그라운드에서 계속 데이터를 받아올것임.
    ss = SocetServer.SocetServer.instance()
    server_thread = threading.Thread(target=ss.start)
    server_thread.daemon = True
    server_thread.start()
    # Azure API 관련 객체 생성
    fd = FaceDetection.FaceDetection.instance()
    # Arduino Serial통신 관련 객체 생성
    asp = ArduinoSerialProtocol.ArduinoSerialProtocol.instance()
    # 변수 초기화.
    working = False
    text = "detecting face"
    # 클라이언트가 작업 종료를 지시한 후에도 프로그램이 종료되지 않고 대기상태에 들어갈 수 있도록 하기 위함.
    while True:
        # 소켓 통신으로 데이터 들어올때까지 실행안함.
        while not ss.data:
            pass
        # 클라이언트가 서버 종료를 지시하기 전까지 계속 반복.
        while ss.server_state:
            try:
                # 사용자에게 카메라 화면을 출력해주고 인식한 얼굴이 있으면 캡쳐하고 저장해주는 메서드.
                fd.capture_faces(text)
                # 3장을 찍음.
                if fd.count == 3:
                    text = "checking"
                    print('Colleting Samples Complete!!!')
                    # 3장을 찍고 백그라운드 작업이 동작중이 아니라면
                    if not working:
                        print("working")
                        # 카메라 화면 정지 방지를 위해 스레드로 백그라운드에서 작업.
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
        # 클라이언트가 대기 명령을 보낸 후 종료 작업.
        ss.data = []
        print("대기")
        cv2.destroyAllWindows()
