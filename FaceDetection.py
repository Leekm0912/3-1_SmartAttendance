# -*- coding:utf-8 -*-
import configparser
import datetime
import os
from azure.cognitiveservices.vision.face import FaceClient
from azure.cognitiveservices.vision.face.models import APIErrorException
from msrest.authentication import CognitiveServicesCredentials
import cv2
import json
from collections import OrderedDict

import UseFirebase as UF


class FaceDetection:
    __instance = None

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

        private_config = configparser.ConfigParser()
        private_config.read("private_config.ini", encoding="utf-8")

        # 얼굴 인식용 xml 파일
        self.face_classifier = cv2.CascadeClassifier('haarcascade_frontalface_default.xml')

        # This key will serve all examples in this document.
        self.KEY = private_config["AZURE"]["KEY"]

        # This endpoint will be used in all examples in this quickstart.
        self.ENDPOINT = private_config["AZURE"]["END_POINT"]

        # Azure 객체 생성
        self.face_client = FaceClient(self.ENDPOINT, CognitiveServicesCredentials(self.KEY))

        # Base url for the Verify and Facelist/Large Facelist operations
        self.IMAGE_BASE_URL = private_config["AZURE"]["IMAGE_BASE_URL"]
        self.IMAGE_BASE_LOCAL = self.config["FaceDetection"]["image_base_local"]

        # List for the target face IDs (uuids)
        self.detected_faces_ids = []

        # Create a list to hold the target photos of the same person
        self.target_image_file_names = self.readTargetImageFileNames()

        self.detected_faces = self.init_target_faces()

        self.source_image_file_names = None

        self.source_image_id = list()
        self.json_data = OrderedDict()

        self.camera_number = self.config["FaceDetection"]["camera_number"]
        self.count = 0
        self.frame_count = 0
        self.cap = cv2.VideoCapture(int(self.camera_number))
        self.student_id = None

        # 폴더 없을시 만들어줌.
        if not os.path.isdir(self.IMAGE_BASE_LOCAL):
            os.mkdir(self.IMAGE_BASE_LOCAL)

    # 얼굴 인식 함수
    def face_extractor(self, img):
        gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
        faces = self.face_classifier.detectMultiScale(gray, 1.1, 5)

        # 찾은 얼굴이 없으면 None 리턴
        if faces is ():
            return None

        # 찾은 얼굴이 있으면 얼굴을 잘라서 cropped_face에 넣어서 리턴
        for (x, y, w, h) in faces:
            cropped_face = img[y:y + h, x:x + w]
        return cropped_face

    # Create a list to hold the target photos of the same person
    def readTargetImageFileNames(self):
        read_data = list()
        with open("TargetImageURLs.txt", "r", encoding="utf-8") as r:
            temp = r.readlines()
            for c in temp:
                read_data.append(c.strip())
            return read_data

    def init_target_faces(self):
        # Detect faces from target image url list, returns a list[DetectedFaces]
        for image_file_name in self.target_image_file_names:
            # We use detection model 3 to get better performance.
            detected_faces = self.face_client.face.detect_with_url(self.IMAGE_BASE_URL + image_file_name,
                                                                   detection_model='detection_03')
            # Add the returned face's face ID
            for detected in detected_faces:
                self.detected_faces_ids.append(detected.face_id)
                print('{} face(s) detected from image {}.'.format(len(self.detected_faces_ids), image_file_name))
        return detected_faces

    def capture_faces(self, text):
        self.faces = ()

        ret, frame = self.cap.read()
        self.frame_count += 1
        cv2.putText(frame, text, (0, 25), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0, 0, 0))
        img = cv2.resize(frame, (int(self.config["FaceDetection"]["screen_width"]),
                                 int(self.config["FaceDetection"]["screen_height"])))

        gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
        faces = self.face_classifier.detectMultiScale(gray, 1.1, 5)

        # 찾은 얼굴 표시
        for (x, y, w, h) in faces:
            cv2.rectangle(img, (x, y), (x + w, y + h), (255, 0, 0), 2)

        if faces is not ():
            if self.count <= 3:
                self.count += 1
                # ex > faces/user0.jpg   faces/user1.jpg ....
                file_name_path = self.IMAGE_BASE_LOCAL + '/user' + str(self.count) + '.jpg'
                cv2.imwrite(file_name_path, frame)
                # 캡쳐한 갯수 표시부분인데 일단 주석해놓음. 지저분해.
                # cv2.putText(frame, str(count), (50, 50), cv2.FONT_HERSHEY_COMPLEX, 1, (0, 255, 0), 2)

        else:
            if self.frame_count % 10 == 0:
                print("Face not Found")

        if self.config["FaceDetection"]["show_screen"]:
            cv2.imshow('Face Cropper', img)



    def init_source_images(self):
        # The source photos contain this person
        self.source_image_file_names = os.listdir(self.IMAGE_BASE_LOCAL)

        # Detect face(s) from source image 1, returns a list[DetectedFaces]
        # We use detection model 3 to get better performance.

        for source_image_file_name in self.source_image_file_names:
            temp = self.face_client.face.detect_with_stream(open("./faces/" + source_image_file_name, "rb"),
                                                            detection_model='detection_03')
            if temp:
                self.detected_faces.append(temp)
            else:
                continue
            # Add the returned face's face ID
            # 이부분 확인 필요.
            self.source_image_id.append((self.detected_faces[-1][0].face_id, source_image_file_name))
            print('{} face(s) detected from image {}.'.format(len(self.detected_faces), source_image_file_name))

    # Verification example for faces of the same person.
    # The higher the confidence, the more identical the faces in the images are.
    def verify_face_to_face(self):
        # 설정 불러오기. console, txt, json, None이 있음.
        way_of_print_data = self.config["FaceDetection"]["way_of_print_data"]

        for i in range(len(self.source_image_id)):
            for j in range(len(self.detected_faces_ids)):
                verify_result = self.face_client.face.verify_face_to_face(self.source_image_id[i][0],
                                                                          self.detected_faces_ids[j])

                if verify_result.is_identical:
                    print('Faces from {} & {} are of the same person, with confidence: {}'
                          .format(self.source_image_id[i][1], self.target_image_file_names[j],
                                  verify_result.confidence * 100))

                    # Download the image from the url
                    # response = requests.get(IMAGE_BASE_URL + source_image_file_names[i])
                    # img = Image.open("./faces/"+source_image_file_names[i])
                    # img.show()

                    # json data 가공
                    # 21660072.jpg면 21660072를 id로
                    self.json_data["result"] = True
                    self.student_id = self.target_image_file_names[j].split(".")[0]
                    self.json_data[self.student_id] = {}
                    self.json_data[self.student_id]["id"] = self.student_id
                    now = datetime.datetime.now()
                    now_date_time = now.strftime('%Y-%m-%d %H:%M:%S')
                    self.json_data[self.student_id]["time"] = now_date_time

                    print(json.dumps(self.json_data, ensure_ascii=False, indent="\t"))
                    return self.student_id, self.json_data

                else:
                    self.json_data["result"] = False
                    print('Faces from {} & {} are of a different person, with confidence: {}')

        return False

    def init_face_data(self):
        self.detected_faces = []
        self.source_image_id = []
        self.json_data = OrderedDict()
        self.json_data["result"] = False


if __name__ == "__main__":
    fd = FaceDetection.instance()
    while True:
        try:
            fd.capture_faces()
            fd.init_source_images()
            fd.verify_face_to_face()
            fd.init_face_data()

        # API 요청 한도 초과.
        except APIErrorException as e:
            print(e.message)

        # Q 누를시 종료
        if cv2.waitKey(1) & 0xFF == ord('q'):
            cv2.destroyAllWindows()
            exit()
