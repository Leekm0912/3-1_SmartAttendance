# -*- coding:utf-8 -*-
from azure.cognitiveservices.vision.face.models import APIErrorException
import cv2
import threading
from multiprocessing.pool import ThreadPool

import FaceDetection
import UseFirebase as UF


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
            student_json[student_id]["temp"] = 0
            student_json[student_id]["result"] = 1
            # 역시 소켓통신 구현 전이니 직접 넣어줌
            ref_dir = "210504_1_K0125146"
            UF.UseFirebase.updateData(ref_dir, student_id, student_json[student_id])

        else:
            print("인식결과 없음")
        fd.init_face_data()
        text = "detecting face"
        fd.count = 0
        working = False


if __name__ == "__main__":
    fd = FaceDetection.FaceDetection.instance()
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
