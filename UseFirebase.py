import datetime
import configparser

import json
import firebase_admin
from firebase_admin import credentials
from firebase_admin import db


class UseFirebase:
    private_config = configparser.ConfigParser()
    private_config.read("private_config.ini", encoding="utf-8")
    cred = credentials.Certificate(private_config["Firebase"]["KEY"])
    # Initialize the app with a service account, granting admin privileges
    firebase_admin.initialize_app(cred, {
        'databaseURL': private_config["Firebase"]["databaseURL"]
    })

    # 테이블명 : 날짜_교시_과목코드
    @staticmethod
    def updateData(ref_dir, student_id, data):
        ref = db.reference(ref_dir)
        snapshot = db.reference(ref_dir + "/" + student_id)
        temp = json.loads(json.dumps(snapshot.get()))
        if temp and temp["result"] == 1:
            print("이미 출석한 사용자 입니다.")
            return
        now = datetime.datetime.now().strftime('%y%m%d')
        ref.update(data)
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
