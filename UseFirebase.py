import datetime
import configparser

from requests import get
import json
import firebase_admin
from firebase_admin import credentials
from firebase_admin import db
from pyfcm import FCMNotification
import playSound


class UseFirebase:
    # 설정 불러오기.
    private_config = configparser.ConfigParser()
    config = configparser.ConfigParser()
    private_config.read("private_config.ini", encoding="utf-8")
    config.read("config.ini", encoding="utf-8")
    # 클라이언트 토큰을 NAS에서 다운로드 후 저장.
    with open("client_token.txt", "wb") as file:  # open in binary mode
        response = get(private_config["firebase_cloudmessaging"]["TOKEN_URL"])  # get request
        print("download")
        file.write(response.content)  # write to file

    cred = credentials.Certificate(private_config["Firebase"]["KEY"])
    # Initialize the app with a service account, granting admin privileges
    firebase_admin.initialize_app(cred, {
        'databaseURL': private_config["Firebase"]["databaseURL"]
    })

    token_list = []
    with open("client_token.txt", "r") as r:
        token_list = r.readlines()
    CLOUDMESSAGING_APIKEY = private_config["firebase_cloudmessaging"]["APIKEY"]

    @classmethod
    def cloudMessaging(cls, student_id, data, result):
        # 파이어베이스 콘솔에서 얻어 온 서버 키를 넣어 줌
        push_service = FCMNotification(cls.CLOUDMESSAGING_APIKEY)

        def sendMessage(body, title):
            # 메시지 (data 타입)
            data_message = {
                "body": body,
                "title": title
            }
            # 토큰값을 이용해 등록한 사용자에게 푸시알림을 전송함
            for token in cls.token_list:
                token = token.split("\n")[0]
                result = push_service.notify_single_device(registration_id=token, message_title=title,
                                                           message_body=body)

            # 전송 결과 출력
            print("클라우드 메시징 전송 결과 : ", result)

        if result == 1:
            sendMessage(data[student_id]["id"], "출석 완료")
        elif result == 2:
            sendMessage(data[student_id]["id"], "[주의] 미열")
        elif result == 3:
            sendMessage(data[student_id]["id"], "[주의] 고열")
        elif result == 0:
            sendMessage(data[student_id]["id"], "출석 오류")

    # 테이블명 : 날짜_교시_과목코드
    @classmethod
    def updateData(cls, ref_dir, student_id, data):
        # json 데이터를 저장하기 위해 id를 key로 사용.
        data = {student_id: data}
        ref = db.reference(ref_dir)
        # id 조회를 위해 student_id 데이터가 있는지 확인.
        snapshot = db.reference(ref_dir + "/" + student_id)
        temp = json.loads(json.dumps(snapshot.get()))
        print("id 조회결과 :", temp)
        # 정상처리된 사용자는 데이터 입력안함.
        if temp and temp["result"] == 1:
            print("이미 출석한 사용자 입니다.")
            playSound.playSound.play("sound/audio_4.wav")
            return
        # 데이터 입력.
        ref.update(data)
        cls.cloudMessaging(student_id, data, data[student_id]["result"])
        print(id, "학생 추가완료")

    # 학생 정보가 있는지 확인해주는 메소드.
    @staticmethod
    def isChecked(ref_dir, student_id):
        student = db.reference(ref_dir + "/" + student_id).get()
        if student:
            return student["result"]
        else:
            return 0


if __name__ == "__main__":
    stu_id = "21660074"
    test_data = {
        "id": stu_id,
        "time": "1234",
        "result": 1,
        "temp": "36.5"
    }
    dir1 = "210321_2_K0125146"
    UseFirebase.updateData(dir1, stu_id, test_data)
    print("isChecked:", UseFirebase.isChecked(dir1, stu_id))
