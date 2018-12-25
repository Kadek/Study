import copy
import math
import time

import numpy as np
from sklearn import preprocessing

from cubes import Edge, Node, Side

class PerspectiveModel:

	def __init__(self):
		self.precision = 0.0001

	def get_globalized_data(self, cubes):
		nodes = []
		edges = []
		sides = []
		index = 0
		for cube in cubes:
			nodes += copy.deepcopy(cube.nodes)
			edges += [Edge(index*8 + edge.start, index*8 + edge.stop) for edge in cube.edges]
			sides += [
				Side(
					[index*8 + point for point in side.points]
					, side.colour
				) 
				for side in cube.sides
			]
			index += 1
		return (nodes,edges,sides)

	def compute_nodes(self, nodes, perspective_data):
		perspective_nodes = []
		for node in nodes:
			perspective_node = self.compute_node(node, perspective_data)
			perspective_nodes.append(perspective_node)
		return perspective_nodes

	def compute_node(self, node, perspective_data):

		if(node.z <= perspective_data.distance):
			return None

		x = (((node.x * perspective_data.distance / node.z) / perspective_data.viewport_width) + perspective_data.viewport_width ) * perspective_data.canvas_width
		y = (((node.y * perspective_data.distance / node.z) / perspective_data.viewport_height) + perspective_data.viewport_height/2 ) * perspective_data.canvas_height 
		return Node([x,y,node.z])

	def compute_edges(self, nodes, edges, perspective_data):
		perspective_edges = []
		for edge in edges:
			if(nodes[edge.start] is None or nodes[edge.stop] is None):
				continue
			perspective_edges.append(Edge(nodes[edge.start], nodes[edge.stop]))
		return perspective_edges

	def compute_sides(self, nodes, sides, perspective_data):
		perspective_sides = []
		for side in sides:
			flag = False
			for point in side.points:
				if(nodes[point] is None):
					flag = True
					continue
			if(flag):
				continue

			points = [point for point in side.points]
			perspective_sides.append(Side(points, side.colour))

		return perspective_sides

	def compute_sides_with_bsp(self, nodes, sides, perspective_data):
		if(len(sides) == 0):
			return None

		splitting_side = Polygon(sides[0].points, sides[0].colour)
		sides.remove(sides[0])
		normal = self.get_normal((
			nodes[splitting_side.points[0]], 
			nodes[splitting_side.points[1]], 
			nodes[splitting_side.points[2]]
		))
		normal = preprocessing.normalize([normal])[0]
	
		front = []
		back = []

		for side in sides:
			flag = False
			front_count = 0
			back_count = 0
			dots = []
			for point in side.points:
				vector = (nodes[point].x, nodes[point].y, nodes[point].z)
				pin_point = [
					nodes[splitting_side.points[0]].x, 
					nodes[splitting_side.points[0]].y, 
					nodes[splitting_side.points[0]].z
				]

				temp_normal = normal
				for i in range(3):
					if((pin_point[i] < 0 and normal[i] < 0) or 
						(pin_point[i] > 0 and normal[i] > 0)):
						temp_normal[i] = -1*normal[i]


				direction = preprocessing.normalize([np.subtract(vector, pin_point)])[0]			
				dot = np.dot(temp_normal, direction)
				dots.append(dot)
				#(front_count_inc, back_count_inc) = self.determine_face(dot)
				#front_count += front_count_inc
				#back_count += back_count_inc

			dot_avg = 0
			n = 0
			for dot in dots:
				if(dot > self.precision or dot < -1*self.precision):
					dot_avg += dot
					n += 1

			if(n != 0):
				dot_avg = dot_avg/n
			else:
				dot_avg = 0
			if(dot_avg < -1*self.precision):
				back.append(side)
			elif(dot_avg > self.precision):
				front.append(side)
			else:
				front.append(side)
			"""
			if(back_count > front_count):
				back.append(side)
			elif(front_count > back_count):
				front.append(side)
			else:
				back.append(side)
"""
		splitting_side.set_back(self.compute_sides_with_bsp(nodes, back, perspective_data))
		splitting_side.set_front(self.compute_sides_with_bsp(nodes, front, perspective_data))

		return splitting_side

	def determine_face(self, dot):
		front_count = 0
		back_count = 0

		print(dot)
		if(dot < -1*self.precision):
			back_count += 1
		elif(dot > self.precision):
			front_count += 1
		else:
			front_count += 1 
			back_count += 1

		return (front_count, back_count)

	def get_normal(self, vector):
		vector_a = self.get_vector(vector[1], vector[0])
		vector_b = self.get_vector(vector[2], vector[0])

		return np.cross(vector_a, vector_b)

	def get_vector(self, vector_a, vector_b):
		return (vector_a.x - vector_b.x, vector_a.y - vector_b.y, vector_a.z - vector_b.z)

	def zoom(self, perspective_data, amount):
		perspective_data.distance += amount

	def translate_z(self, cubes, amount):
		for cube in cubes:
			for node in cube.nodes:
				node.z = node.z + amount
		return cubes

	def translate(self, cubes, x_amount, y_amount):
		for cube in cubes:
			for node in cube.nodes:
				node.x = node.x + x_amount
				node.y = node.y + y_amount
		return cubes

	def rotate(self, cubes, y_amount, x_amount):
		x_amount = -1 * x_amount
		for cube in cubes:
			for node in cube.nodes:
				node.y = node.y*math.cos(x_amount) - node.z*math.sin(x_amount)
				node.z = node.y*math.sin(x_amount) + node.z*math.cos(x_amount)

				node.x = node.x*math.cos(y_amount) + node.z*math.sin(y_amount)
				node.z = -node.x*math.sin(y_amount) + node.z*math.cos(y_amount)
		return cubes

	def rotate_z(self, cubes, z_amount):
		for cube in cubes:
			for node in cube.nodes:
				node.x = node.x*math.cos(z_amount) - node.y*math.sin(z_amount)
				node.y = node.x*math.sin(z_amount) + node.y*math.cos(z_amount)
		return cubes

	def get_drawable_points(self, nodes, points):
		if(len(nodes) < points[3]):
			return None
		for i in range(4):
			if(nodes[points[i]]) is None:
				return None

		return [
			(nodes[points[0]].x, nodes[points[0]].y),
			(nodes[points[1]].x, nodes[points[1]].y),
			(nodes[points[2]].x, nodes[points[2]].y),
			(nodes[points[3]].x, nodes[points[3]].y)
		]

class PerspectiveData:

	def __init__(
		self, 
		viewport_width,
		viewport_height,
		canvas_width,
		canvas_height,
		distance,
	):
		self.viewport_width = viewport_width
		self.viewport_height = viewport_height
		self.canvas_width = canvas_width
		self.canvas_height = canvas_height
		self.distance = distance

class Polygon:

	def __init__(self, points, colour):
		self.points = points
		self.colour  = colour
		self.front = None
		self.back = None

	def get_back(self):
		return self.back

	def get_front(self):
		return self.front

	def set_back(self, back):
		self.back = back

	def set_front(self, front):
		self.front = front


	def __repr__(self):
		return ("%s, color: %s") % (str(self.points), self.colour)

if __name__ == "__main__":

	a = Node([1,0,0])
	b = Node([0,1,0])
	c = Node([1,1,0])

	perspectiveModel = PerspectiveModel()
	print(perspectiveModel.get_vector(b,a))
	print(perspectiveModel.get_vector(c,a))
	print(perspectiveModel.get_normal([a,b,c]))

	normal = perspectiveModel.get_vector(c,a)
	print(np.dot(normal, np.subtract([1,1,1], [1,0,0])))
	print(np.dot(normal, np.subtract([-1,-1,-1], [1,0,0])))