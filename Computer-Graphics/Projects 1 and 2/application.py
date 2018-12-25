import json
import copy

import pygame

from cubes import Cube
from perspectiveModel import PerspectiveModel, PerspectiveData


class ProjectionController:
	def __init__(self, width, height, filename):
		self.filename = filename
		self.screen = pygame.display.set_mode((width, height))
		pygame.display.set_caption('Grafika Komputerowa')
		self.background = (10,10,50)
		self.cubes = []
		self.edgeColour = (200,200,200)

		self.perspective_model = PerspectiveModel()

		self.perspective_data = self.load_perspective_data()
		self.width = self.perspective_data.canvas_width
		self.height = self.perspective_data.canvas_height

		self.load_cubes()
		self.load_quants()

		self.colors = False
		self.bsp = False

		self.colourNumbers = self.initialize_colours()


	def load_perspective_data(self):
		with open(self.filename) as f:
			data = json.load(f)
			scene = data["scene"]

			perspective_data = PerspectiveData(
				scene["viewport_width"],
				scene["viewport_height"],
				scene["canvas_width"],
				scene["canvas_height"],
				scene["distance"]
			)
		return perspective_data

	def load_cubes(self):
		with open(self.filename) as f:
			data = json.load(f)
			cubes = data["cubes"];
			for cube in cubes:
				nodes = cube["nodes"]
				edges = cube["edges"]
				sides = cube["sides"]

				cube = Cube()
				cube.addNodes(nodes)
				cube.addEdges(edges)
				cube.addSides(sides)

				self.addCube(cube)

	def load_quants(self):
		with open(self.filename) as f:
			data = json.load(f)
			quants = data["quants"];

			self.zoom_quant = quants["zoom_quant"]
			self.translate_z_quant = quants["translate_z_quant"]
			self.translate_quant = quants["translate_quant"]
			self.rotate_quant = quants["rotate_quant"]
			self.rotate_z_quant = quants["rotate_z_quant"]

	def addCube(self, cube):
		self.cubes.append(cube)

	def display(self):
		self.screen.fill(self.background)

		nodes = []
		edges = []
		sides = []
		(nodes, edges, sides) = self.perspective_model.get_globalized_data(self.cubes)

		perspective_nodes = self.perspective_model.compute_nodes(nodes, self.perspective_data)

		if(not self.colors):
			perspective_edges = self.perspective_model.compute_edges(perspective_nodes, edges, self.perspective_data)
			self.draw_edges(perspective_edges)

		if(self.colors and not self.bsp):
			perspective_sides = self.perspective_model.compute_sides(perspective_nodes, sides, self.perspective_data)
			self.draw_sides(perspective_nodes, perspective_sides)

		if(self.bsp):
			perspective_sides = self.perspective_model.compute_sides(nodes, sides, self.perspective_data)
			perspective_polygons = self.perspective_model.compute_sides_with_bsp(nodes, perspective_sides, self.perspective_data)
			self.draw_polygons(perspective_nodes, perspective_polygons)


	def draw_edges(self, perspective_edges):
		for edge in perspective_edges:
			pygame.draw.aaline(
				self.screen, 
				self.edgeColour, 
				(edge.start.x, edge.start.y), 
				(edge.stop.x, edge.stop.y), 
				1
			)

	def draw_sides(self, perspective_nodes, perspective_sides):
		for side in perspective_sides:
			pygame.draw.polygon(
				self.screen,
				self.get_colour_number(side.colour),
				[[perspective_nodes[point].x, perspective_nodes[point].y] for point in side.points]
			)

	def draw_polygons(self, perspective_nodes, root_polygon):
		if(root_polygon is None):
			return

		polygon = root_polygon.get_back()
		if(polygon is not None):
			self.draw_polygons(perspective_nodes, polygon)

		drawable_points = self.perspective_model.get_drawable_points(perspective_nodes, root_polygon.points)
		if(drawable_points is not None):
			pygame.draw.polygon(
				self.screen,
				self.get_colour_number(root_polygon.colour),
				drawable_points
			)

		polygon = root_polygon.get_front()
		if(polygon is not None):
			self.draw_polygons(perspective_nodes, polygon)

	def run(self):
		running = True
		while running:
			for event in pygame.event.get():
				mouse_pressed = pygame.mouse.get_pressed()
				if event.type == pygame.QUIT:
					running = False

				if event.type == pygame.KEYUP and event.key == pygame.K_SPACE:
					if(self.colors and self.bsp):
						self.bsp = False
						self.colors = False
					elif(self.colors):
						self.bsp = True
					else:
						self.colors = True


				if mouse_pressed[2] and event.type == pygame.MOUSEBUTTONDOWN:
					if event.button == 4:
						self.cubes = self.perspective_model.rotate_z(self.cubes, -self.rotate_z_quant)
					elif event.button == 5:
						self.cubes = self.perspective_model.rotate_z(self.cubes, self.rotate_z_quant)
				if mouse_pressed[0] and event.type == pygame.MOUSEBUTTONDOWN:
					if event.button == 4:
						self.perspective_model.zoom(self.perspective_data, self.zoom_quant)
					elif event.button == 5:
						self.perspective_model.zoom(self.perspective_data, -self.zoom_quant)
				elif event.type == pygame.MOUSEBUTTONDOWN:
					if event.button == 4:
						self.cubes = self.perspective_model.translate_z(self.cubes, -self.translate_z_quant)
					elif event.button == 5:
						self.cubes = self.perspective_model.translate_z(self.cubes, self.translate_z_quant)

				if mouse_pressed[0] and event.type == pygame.MOUSEMOTION:
					movement = event.rel
					self.cubes = self.perspective_model.translate(self.cubes, movement[0] * self.translate_quant, movement[1] * self.translate_quant)
				if mouse_pressed[2] and event.type == pygame.MOUSEMOTION:
					movement = event.rel
					self.cubes = self.perspective_model.rotate(self.cubes, movement[0] * self.rotate_quant, movement[1] * self.rotate_quant)
					
			self.display()
			pygame.display.flip()

	def get_colour_number(self, colourName):
		return self.colourNumbers[colourName]

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


if __name__ == "__main__":

	pv = ProjectionController(1300, 650, "config.json")
	pv.run()




