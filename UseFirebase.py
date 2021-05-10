import datetime
import configparser


import json
import firebase_admin
from firebase_admin import credentials
from firebase_admin import db
from pyfcm import FCMNotification


class UseFirebase:
    private_config = configparser.ConfigParser()
    private_config.read("private_config.ini", encoding="utf-8")
    cred = credentials.Certificate(private_config["Firebase"]["KEY"])
    # Initialize the app with a service account, granting admin privileges
    firebase_admin.initialize_app(cred, {
        'databaseURL': private_config["Firebase"]["databaseURL"]
    })

    CLOUDMESSAGING_TOKEN = private_config["firebase_cloudmessaging"]["TOKEN"]
    CLOUDMESSAGING_APIKEY = private_config["firebase_cloudmessaging"]["APIKEY"]

    @classmethod
    def cloudMessaging(cls, student_id,data):


        # 파이어베이스 콘솔에서 얻어 온 서버 키를 넣어 줌
        push_service = FCMNotification(cls.CLOUDMESSAGING_APIKEY)

        def sendMessage(body, title):
            # 메시지 (data 타입)
            data_message = {
                "body": body,
                "title": title
            }
            # 토큰값을 이용해 1명에게 푸시알림을 전송함
            result = push_service.notify_single_device(registration_id=cls.CLOUDMESSAGING_TOKEN, message_title=title, message_body=body)

            # 전송 결과 출력
            print(result)

        sendMessage(data[student_id]["id"],"출석 완료")



    # 테이블명 : 날짜_교시_과목코드
    @classmethod
    def updateData(cls, ref_dir, student_id, data):
        data = {student_id: data}
        ref = db.reference(ref_dir)
        snapshot = db.reference(ref_dir + "/" + student_id)
        temp = json.loads(json.dumps(snapshot.get()))
        if temp and temp["result"] == 1:
            print("이미 출석한 사용자 입니다.")
            return
        now = datetime.datetime.now().strftime('%y%m%d')
        ref.update(data)
        cls.cloudMessaging(student_id, data)
        print(id, "학생 추가완료")

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
        stu_id: {
            "time": "1234",
            "result": 1,
            "temp": "36.5"
        }
    }
    dir1 = "210321_2_K0125146"
    UseFirebase.updateData(dir1, stu_id, test_data)
    print("isChecked:", UseFirebase.isChecked(dir1, stu_id))
