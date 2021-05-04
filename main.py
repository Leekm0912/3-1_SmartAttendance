# -*- coding:utf-8 -*-
from azure.cognitiveservices.vision.face.models import APIErrorException
import cv2

import FaceDetection
import UseFirebase as UF

if __name__ == "__main__":
    fd = FaceDetection.FaceDetection.instance()
    while True:
        try:
            fd.capture_faces()
            fd.init_source_images()
            student_id, student_json = fd.verify_face_to_face()
            # 일단 지금은 온도센서코드 달기 전이니 테스트 위해 직접 넣어줌.
            student_json[student_id]["temp"] = 0
            student_json[student_id]["result"] = 1
            # 역시 소켓통신 구현 전이니 직접 넣어줌
            ref_dir = "210321_1_K0125146"
            UF.UseFirebase.updateData(ref_dir, student_id, student_json)
            fd.init_face_data()

        # API 요청 한도 초과.
        except APIErrorException as ae:
            print(ae.message)

        except TypeError as te:
            print("TypeError")

        # Q 누를시 종료
        if cv2.waitKey(1) & 0xFF == ord('q'):
            cv2.destroyAllWindows()
            exit()
