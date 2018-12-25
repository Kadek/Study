class Node:
	def __init__(self, coordinates):
		self.x = coordinates[0]
		self.y = coordinates[1]
		self.z = coordinates[2]

	def __repr__(self):
		return ("(x: %f, y: %f, z: %f)") % (self.x, self.y, self.z)
		
class Edge:
	def __init__(self, start, stop):
		self.start = start
		self.stop  = stop

	def __repr__(self):
		return ("(start: %s, end: %s)") % (self.start, self.stop)

class Side:
	def __init__(self, points, colour):
		self.points = points
		self.colour  = colour

	def __repr__(self):
		return ("%s, color: %s") % (str(self.points), self.colour)

class Cube:
	def __init__(self):
		self.nodes = []
		self.edges = []
		self.sides = []

	def addNodes(self, nodeList):
		for node in nodeList:
			self.nodes.append(Node(node))

	def addEdges(self, edgeList):
		for (start, stop) in edgeList:
			self.edges.append(Edge(start, stop))

	def addSides(self, sideList):
		for side in sideList:
			self.sides.append(Side(side["points"], side["colour"]))
