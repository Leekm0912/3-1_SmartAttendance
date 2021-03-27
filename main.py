# -*-coding:utf-8-*-
from azure.cognitiveservices.vision.face.models import APIErrorException
import cv2

import FaceDetection

if __name__ == "__main__":
    fd = FaceDetection.FaceDetection.instance()
    while True:
        try:
            fd.capture_faces()
            fd.init_source_images()
            fd.verify_face_to_face()
            fd.init_face_data()

        # API 요청 한도 초과.
        except APIErrorException as e:
            print(e.message)

        if cv2.waitKey(1) & 0xFF == ord('q'):
            cv2.destroyAllWindows()
            exit()
