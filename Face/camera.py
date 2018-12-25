import os
import threading
import time

import cv2
import dlib
import numpy as np

class Camera:

	def __init__(self, path, app):
		self.path = path
		self.app = app
		self.working = False


	def resize(self, image, width=None, height=None, inter=cv2.INTER_AREA):
	    # initialize the Vdimensions of the image to be resized and
	    # grab the image size
	    dim = None
	    (h, w) = image.shape[:2]

	    # if both the width and height are None, then return the
	    # original image
	    if width is None and height is None:
	        return image

	    # check to see if the width is None
	    if width is None:
	        # calculate the ratio of the height and construct the
	        # dimensions
	        r = height / float(h)
	        dim = (int(w * r), height)

	    # otherwise, the height is None
	    else:
	        # calculate the ratio of the width and construct the
	        # dimensions
	        r = width / float(w)
	        dim = (width, int(h * r))

	    # resize the image
	    resized = cv2.resize(image, dim, interpolation=inter)

	    # return the resized image
	    return resized

	def start(self):

		self.delay = 10
		self.t1 = threading.Thread(target=self.recording)
		self.t1.start()

	def rect_to_bb(self, rect):
		x = rect.left()
		y = rect.top()
		w = rect.right() - x
		h = rect.bottom() - y
	 
		return (x, y, w, h)

	def shape_to_np(self, shape, dtype="int"):
		coords = np.zeros((68, 2), dtype=dtype)
	 
		for i in range(0, 68):
			coords[i] = (shape.part(i).x, shape.part(i).y)
	 
		return coords

	def find_face(self, face_descriptor):

		min_val = 1
		min_face = ""
		values = []

		faces = os.listdir("faces")
		for face in faces:
			with open("faces/"+face, "r") as file:
				real_descriptor = file.read() 

			real_descriptor = [float(x) for x in real_descriptor.split()]

			new_val = self.get_euclid(real_descriptor, face_descriptor)
			if(new_val < min_val):
				min_val = new_val
				min_face = face

			values.append((face, new_val))

		if(min_val > 0.2):
			min_face = "unknown"

		print(values)
		return (min_face, min_val)

	def draw_info(self, image):

		detector = dlib.get_frontal_face_detector()
		predictor = dlib.shape_predictor("face_shape")
		facerec = dlib.face_recognition_model_v1("face_name")

		rects = detector(image, 1)

		for (i, rect) in enumerate(rects):

			shape = predictor(image, rect)
			shape_np = self.shape_to_np(shape)

			(x, y, w, h) = self.rect_to_bb(rect)
			cv2.rectangle(image, (x, y), (x + w, y + h), (0, 255, 0), 2)
		 
			face_descriptor = facerec.compute_face_descriptor(image, shape)

			(name, val) = self.find_face(face_descriptor)

			cv2.putText(image, "Face #{} dist={}".format(name, val), (x - 10, y - 10),
				cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0, 255, 0), 2)

			for (x, y) in shape_np:
				cv2.circle(image, (x, y), 1, (0, 0, 255), -1)

		return image

	def recording(self):

		cap = cv2.VideoCapture(0)
		self.working = True

		while(self.working):
				ret, frame = cap.read()

				if(not ret):
					cap.release()
					cap = cv2.VideoCapture(0)
					continue

				image = self.resize(frame, width=500)

				if("debug" in self.app.title):
					image = self.draw_info(image)

				count = 0
				for widget in self.app.app.allWidgets():
					if(not widget.isVisible()):
						count += 1
				if(count == 3):
					self.working = False
	
				cv2.imshow('bezpieczna kamera', image)
				cv2.waitKey(1)

				if(self.app.title == "Face - manager"):
					break

		cap.release()
		cv2.destroyAllWindows()

	def compute_live_face(self):

		detector = dlib.get_frontal_face_detector()
		predictor = dlib.shape_predictor("face_shape")
		facerec = dlib.face_recognition_model_v1("face_name")

		cap = cv2.VideoCapture(0)		
		ret, frame = cap.read()
		cv2.waitKey(1)

		image = self.resize(frame, width=500)
		rects = detector(image, 1)

		if(len(rects) < 1):
			return False

		rect = rects[0]
		shape = predictor(image, rect)
		face_descriptor = facerec.compute_face_descriptor(image, shape)

		return face_descriptor

	def get_euclid(self, X, Y):

		sum = 0
		for i in range(len(X)):
			sum = (X[i] - Y[i])**2

		return sum**(1/2)

	def create_user_profile(self, user):
		
		if(os.path.isfile("faces/"+user+".txt")):
			return False

		face_descriptor = self.compute_live_face()

		with open("faces/"+user+".txt", "w") as file:
			file.write(str(face_descriptor)) 

		return True

	def verify_user_profile(self, user):
		
		if(not os.path.isfile("faces/"+user+".txt")):
			return False

		face_descriptor = self.compute_live_face()
		if(not face_descriptor):
			return False

		with open("faces/"+user+".txt", "r") as file:
			real_descriptor = file.read() 

		real_descriptor = [float(x) for x in real_descriptor.split()]

		print(self.get_euclid(face_descriptor, real_descriptor))
		if(self.get_euclid(face_descriptor, real_descriptor) > 0.025):
			return False

		return True
