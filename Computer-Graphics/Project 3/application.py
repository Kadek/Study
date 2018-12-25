import json
import copy
import math
import time
import sys

import pygame
import trimesh
import numpy as np

from sklearn import preprocessing


class Application:
	def __init__(self, filename):
		self.filename = filename
		pygame.display.set_caption('Grafika Komputerowa')
		self.background = (0,0,0)
		self.colour_numbers = self.initialize_colours()

		self.config = self.load_config()
		self.screen = self.create_screen(self.config)

		self.objects = []
		self.sphere_config = self.load_sphere_config()
		self.objects.append(self.generate_sphere(self.sphere_config))

		(self.columns, self.rows) = self.get_pixels(self.config)
		self.directions = self.generate_directions(self.columns, self.rows, self.config)
		self.origins = np.zeros([len(self.columns) * len(self.rows), 3])

		self.light_source = self.load_light_source()


	def load_config(self):
		with open(self.filename) as f:
			data = json.load(f)
			scene = data["scene"]

			config = Config(
				scene["screen_width"],
				scene["screen_height"],
				scene["viewport_width"],
				scene["viewport_height"],
				scene["distance"],
				scene["granularity"],
				scene["quant"]
			)
		return config

	def load_sphere_config(self):
		with open(self.filename) as f:
			data = json.load(f)
			scene = data["sphere"]

			config = SphereConfig(
				scene["radius"],
				scene["center"],
				scene["complexity"]
			)
		return config

	def load_light_source(self):
		with open(self.filename) as f:
			data = json.load(f)
			scene = data["light"]

			light_source = LightSource(
				scene["coords"],
				scene["intensity"],
				scene["albedo"],
				scene["kd"],
				scene["n"],
				scene["ks"],
			)
		return light_source

	def create_screen(self, config):
		screen = pygame.display.set_mode((config.screen_width, config.screen_height))
		return screen

	def generate_sphere(self, sphere_config):
		mesh = trimesh.creation.icosphere(sphere_config.complexity, sphere_config.radius)
		translation = np.zeros([mesh.vertices.shape[0], 3])

		for i in range(3):
			translation[:, i] = sphere_config.center[i]

		mesh.vertices += translation
		return mesh

	def run(self):
		running = True
		while running:
			for event in pygame.event.get():
				mouse_pressed = pygame.mouse.get_pressed()
				if event.type == pygame.QUIT:
					running = False

				if event.type == pygame.KEYUP:
					print("event received")
					if event.key == pygame.K_q:
						self.translate(self.light_source, -1*self.config.quant, 0)
					if event.key == pygame.K_a:
						self.translate(self.light_source, self.config.quant, 0)
					if event.key == pygame.K_w:
						self.translate(self.light_source, -1*self.config.quant, 1)
					if event.key == pygame.K_s:
						self.translate(self.light_source, self.config.quant, 1)
					if event.key == pygame.K_e:
						self.translate(self.light_source, self.config.quant, 2)
					if event.key == pygame.K_d:
						self.translate(self.light_source, -1*self.config.quant, 2)

			self.display()
			pygame.display.flip()

	def translate(self, light_source, amount, column):
		light_source.coords[column] += amount
		print(light_source.coords)

	def display(self):
		self.screen.fill(self.background)

		intersections = self.trace(self.origins, self.directions, self.objects[0], self.config)
		intersections = np.unique(intersections)

		#intersections = self.eliminate_invisible(intersections, self.light_source, self.objects[0])

		for intersection in intersections:

			triangle_coords = []
			triangle = self.objects[0].faces[intersection]
			for index in triangle:
				mesh = self.objects[0].vertices[index]
				x = (((mesh[0] * self.config.distance / mesh[2]) / self.config.canvas_width) + self.config.canvas_width ) * self.config.screen_width
				y = (((mesh[1] * self.config.distance / mesh[2]) / self.config.canvas_height) + self.config.canvas_height/2 ) * self.config.screen_height 
				triangle_coords.append([x,y])
				
			colour = self.compute_colour(triangle)
			for i in range(3):
				if(colour[i] > 255):
					colour[i] = 255
			pygame.draw.polygon(self.screen, colour, triangle_coords)
		print("done rendering")

	def eliminate_invisible(self, intersections, light_source, mesh):
		triangle_centers = mesh.triangles_center.take(intersections, axis=0)
		directions = light_source.coords - triangle_centers
		hits = mesh.ray.intersects_any(
			ray_origins=triangle_centers,
			ray_directions=directions
		)
		hits = np.invert(hits)
		return np.extract(hits, intersections)


	def compute_colour(self, triangle):

		triangle_center = preprocessing.normalize(self.get_triangle_center(triangle).reshape(1,-1)).reshape(-1,1)
		triangle_normal = preprocessing.normalize(self.get_triangle_normal(triangle, triangle_center).reshape(1,-1))
		light_dir = preprocessing.normalize(self.get_light_dir(triangle_center, self.light_source))

		diffuse = np.array(self.light_source.albedo) * self.light_source.intensity * max(0, np.dot(triangle_normal, -1*light_dir))

		reflection = self.reflect(light_dir, triangle_normal);
		power = 0
		try:
			power = math.pow(np.maximum(0, np.dot(reflection, -1*triangle_center)[0]), self.light_source.n)
		except OverflowError:
			power = sys.maxsize
		specular = self.light_source.intensity * power 
		colour = diffuse * self.light_source.kd + specular * self.light_source.ks
		return colour.reshape(-1,1)

	def get_triangle_center(self, triangle):
		triangle_center = [0,0,0]
		for index in triangle:
			point = self.objects[0].vertices[index]
			for i in range(3):
				triangle_center[i] += point[i]

		for i in range(3):
				triangle_center[i] /= 3

		return np.array(triangle_center)

	def get_triangle_normal(self, triangle, triangle_center):
		points = []
		for index in triangle:
			points.append(self.objects[0].vertices[index])

		normal = self.get_normal(points)
		return normal

	def get_light_dir(self, triangle_center, light_source):
		return np.array(self.get_vector(triangle_center, light_source.coords))


	def get_normal(self, vector):
		vector_a = self.get_vector(vector[1], vector[0])
		vector_b = self.get_vector(vector[2], vector[0])

		return np.cross(vector_a, vector_b)

	def get_vector(self, vector_a, vector_b):
		return (vector_a[0] - vector_b[0], vector_a[1] - vector_b[1], vector_a[2] - vector_b[2])

	def reflect(self, normal, direction):
		return 2*np.dot(normal, direction)*normal - direction

	def get_pixels(self, config):
		screen_left_boundary = -1*config.canvas_width/2
		screen_right_boundary = config.canvas_width/2

		screen_top_boundary = -1*config.canvas_height/2
		screen_bottom_boundary = config.canvas_height/2

		columns = np.linspace(
			screen_left_boundary,
			screen_right_boundary,
			config.screen_width/config.granularity
		)

		rows = np.linspace(
			screen_top_boundary,
			screen_bottom_boundary,
			config.screen_height/config.granularity
		)

		return (columns, rows)

	def generate_directions(self, columns, rows, config):
		coords = np.transpose([np.tile(columns, len(rows)), np.repeat(rows, len(columns))])

		z = np.full([len(columns)*len(rows),1], config.distance)
		return np.column_stack((coords, z))

	def trace(self, origins, directions, mesh, config):
		locations, index_ray, index_tri = mesh.ray.intersects_location(
			ray_origins=origins,
			ray_directions=directions,
			multiple_hits=False
		)

		return index_tri


	def get_colour_number(self, colourName):
		return self.colour_numbers[colourName]

	def initialize_colours(self):
		return {
			"green": (0, 255, 0),
			"red": (255, 0, 0),
			"yellow": (255, 255, 0),
			"blue": (0, 0, 255),
			"orange": (255, 128, 0),
			"violet": (153, 0, 153),
			"purple": (255, 153, 153),
			"gray": (160, 160, 160),
			"black": (0, 0, 0),
		}

class Config:
	def __init__(
		self,
		screen_width,
		screen_height,
		canvas_width,
		canvas_height,
		distance,
		granularity,
		quant
	):
		self.screen_width = screen_width
		self.screen_height = screen_height
		self.canvas_width = canvas_width
		self.canvas_height = canvas_height
		self.distance = distance
		self.granularity = granularity
		self.quant = quant

class SphereConfig:
	def __init__(
		self,
		radius,
		center,
		complexity
	):
		self.radius = radius
		self.center = center
		self.complexity = complexity

class FloorConfig:
	def __init__(
		self,
		left_down,
		left_up,
		right_down,
		right_up,
		colour
	):
		self.left_down = left_down
		self.left_up = left_up
		self.right_down = right_down
		self.right_up = right_up
		self.colour = colour

class LightSource:
	def __init__(
		self,
		coords,
		intensity,
		albedo,
		kd,
		n,
		ks
	):
		self.coords = coords
		self.intensity = intensity
		self.albedo = albedo
		self.kd = kd
		self.n = n
		self.ks = ks

if __name__ == "__main__":

	pv = Application("config.json")
	pv.run()




