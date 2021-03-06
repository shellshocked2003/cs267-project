import org.scalatest._
import edu.berkeley.ce.rockslicing.{Face, Block, Joint, Delaunay}

class BlockSpec extends FunSuite {
  val boundingFaces = List(
    Face((-1.0, 0.0, 0.0), 0.0, phi=0, cohesion=0), // -x = 0
    Face((1.0, 0.0, 0.0), 1.0, phi=0, cohesion=0),  // x = 1
    Face((0.0, -1.0, 0.0), 0.0, phi=0, cohesion=0), // -y = 0
    Face((0.0, 1.0, 0.0), 1.0, phi=0, cohesion=0),  // y = 1
    Face((0.0, 0.0, -1.0), 0.0, phi=0, cohesion=0), // -z = 0
    Face((0.0, 0.0, 1.0), 1.0, phi=0, cohesion=0)   // z = 1
  )
  val unitCube = Block((0.0, 0.0, 0.0), boundingFaces)

  val boundingFaces2 = List(
    Face((-1.0, 0.0, 0.0), 0.0, phi=0, cohesion=0), // -x = 0
    Face((1.0, 0.0, 0.0), 2.0, phi=0, cohesion=0),  // x = 2
    Face((0.0, -1.0, 0.0), 0.0, phi=0, cohesion=0), // -y = 0
    Face((0.0, 1.0, 0.0), 2.0, phi=0, cohesion=0),  // y = 2
    Face((0.0, 0.0, -1.0), 0.0, phi=0, cohesion=0), // -z = 0
    Face((0.0, 0.0, 1.0), 2.0, phi=0, cohesion=0)   // z = 2
  )
  val twoCube = Block((0.0, 0.0, 0.0), boundingFaces2)

  test("The plane z = 0.5 should intersect the unit cube") {
    val joint = Joint((0.0, 0.0, 1.0), 1/2.0, center=(0.0, 0.0, 0.0), dipAngle=0,
                      dipDirection=0, phi=0, cohesion=0, shape=Nil)
    assert(unitCube.intersects(joint).isDefined)
  }

  test("The plane z = 0.75 with non-zero origin should intersect the unit cube") {
    val joint = Joint((0.0, 0.0, 1.0), 1/2.0, center=(0.0, 0.0, 1/4.0), dipAngle=0,
                      dipDirection=0, phi=0, cohesion=0, shape=Nil)
    assert(unitCube.intersects(joint).isDefined)
  }

  test("The plane -z = 0.5 should intersect the unit cube") {
    val joint = Joint((0.0, 0.0, -1.0), -1/2.0, center=(0.0, 0.0, 0.0), dipAngle=0,
                      dipDirection=0, phi=0, cohesion=0, shape=Nil)
    assert(unitCube.intersects(joint).isDefined)
  }

  test("The plane -z = 0.75 with non-zero origin should intersect the unit cube") {
    val joint = Joint((0.0, 0.0, -1.0), -1/2.0, center=(0.0, 0.0, 1/4.0), dipAngle=0,
                      dipDirection=0, phi=0, cohesion=0, shape=Nil)
    assert(unitCube.intersects(joint).isDefined)
  }

  test("The plane z = 2 should not intersect the unit cube") {
    val joint = Joint((0.0, 0.0, 1.0), 2.0, center=(0.0, 0.0, 0.0), dipAngle=0,
                      dipDirection=0, phi=0, cohesion=0, shape=Nil)
    assert(unitCube.intersects(joint).isEmpty)
  }

  test("The plane z = 2 with non-zero origin should not intersect the unit cube") {
    val joint = Joint((0.0, 0.0, 1.0), 1.0, center=(0.0, 0.0, 1.0), dipAngle=0,
                      dipDirection=0, phi=0, cohesion=0, shape=Nil)
    assert(unitCube.intersects(joint).isEmpty)
  }

  test("The plane y = 1 should not intersect the unit cube") {
    val joint = Joint((0.0, 1.0, 0.0), 1.0, center=(0.0, 0.0, 0.0), dipAngle=0,
                      dipDirection=0, phi=0, cohesion=0, shape=Nil)
    assert(unitCube.intersects(joint).isEmpty)
  }

  test("The plane y = 1 with non-zero origin should not intersect the unit cube") {
    val joint = Joint((0.0, 1.0, 0.0), 1/2.0, center=(0.0, 1/2.0, 0.0), dipAngle=0,
                      dipDirection=0, phi=0, cohesion=0, shape=Nil)
    assert(unitCube.intersects(joint).isEmpty)
  }

  test("The plane -y = 1 should not intersect the unit cube") {
    val joint = Joint((0.0, -1.0, 0.0), -1.0, center=(0.0, 0.0, 0.0), dipAngle=0,
                      dipDirection=0, phi=0, cohesion=0, shape=Nil)
    assert(unitCube.intersects(joint).isEmpty)
  }

  test("The plane y = 0.99 should intersect the unit cube") {
    val joint = Joint((0.0, 1.0, 0.0), 0.99, center=(0.0, 0.0, 0.0), dipAngle=0,
                      dipDirection=0, phi=0, cohesion=0, shape=Nil)
    assert(unitCube.intersects(joint).isDefined)
  }

  test("The plane y = 0.99 with non-zero origin should intersect the unit cube") {
    val joint = Joint((0.0, 1.0, 0.0), 0.49, center=(0.0, 0.5, 0.0), dipAngle=0,
                      dipDirection=0, phi=0, cohesion=0, shape=Nil)
    assert(unitCube.intersects(joint).isDefined)
  }

  test("The plane -y = 0.99 should intersect the unit cube") {
    val joint = Joint((0.0, -1.0, 0.0), -0.99, center=(0.0, 0.0, 0.0), dipAngle=0,
                      dipDirection=0, phi=0, cohesion=0, shape=Nil)
    assert(unitCube.intersects(joint).isDefined)
  }

  test("The plane -y = 0.99 with non-zero origin should intersect the unit cube") {
    val joint = Joint((0.0, -1.0, 0.0), -0.49, center=(0.0, 0.5, 0.0), dipAngle=0,
                      dipDirection=0, phi=0, cohesion=0, shape=Nil)
    assert(unitCube.intersects(joint).isDefined)
  }

  test("The plane -x + z = 1 at global origin should intersect the unit cube") {
    val joint = Joint((-1.0, 0.0, 1.0), 1.0, center=(0.0, 0.0, 0.0), dipAngle=0,
                      dipDirection=0, phi=0, cohesion=0, shape=Nil)
    assert(unitCube.intersects(joint).isDefined)
  }

  test("The plane -x + z = 1 at (1.0,0.0,1.0) should not intersect the unit cube") {
    val joint = Joint((-1.0, 0.0, 1.0), 1.0, center=(1.0, 0.0, 1.0), dipAngle=0,
                      dipDirection=0, phi=0, cohesion=0, shape=Nil)
    assert(unitCube.intersects(joint).isEmpty)
  }

  test("The plane -x + z = 1 at global origin should intersect the two cube") {
    val joint = Joint((-1.0, 0.0, 1.0), 1.0, center=(0.0, 0.0, 0.0), dipAngle=0,
                      dipDirection=0, phi=0, cohesion=0, shape=Nil)
    assert(twoCube.intersects(joint).isDefined)
  }

  test("The plane -x + z = 1 at (1.0,0.0,1.0) should intersect the two cube") {
    val joint = Joint((-1.0, 0.0, 1.0), 1.0, center=(1.0, 0.0, 1.0), dipAngle=0,
                      dipDirection=0, phi=0, cohesion=0, shape=Nil)
    assert(twoCube.intersects(joint).isDefined)
  }

  test("The unit cube should not contain any redundant faces") {
    assert(unitCube.nonRedundantFaces == boundingFaces)
  }

  test("Adding planes x,y,z = +- 2 to the unit cube should be considered redundant") {
    val redundantBoundingFaces = boundingFaces ++ List(
      Face((-1.0, 0.0, 0.0), 2.0, phi=0, cohesion=0), // -x = 2
      Face((1.0, 0.0, 0.0), 2.0, phi=0, cohesion=0),  // x = 2
      Face((0.0, -1.0, 0.0), 2.0, phi=0, cohesion=0), // -y = 2
      Face((0.0, 1.0, 0.0), 2.0, phi=0, cohesion=0),  // y = 2
      Face((0.0, 0.0, -1.0), 2.0, phi=0, cohesion=0), // -z = 2
      Face((0.0, 0.0, 1.0), 2.0, phi=0, cohesion=0)   // z = 2
    )
    val redundantUnitCube = Block((0.0, 0.0, 0.0), redundantBoundingFaces)

    assert(redundantUnitCube.nonRedundantFaces == boundingFaces)
  }

  test("The two cube should not contain any redundant faces") {
    assert(twoCube.nonRedundantFaces == boundingFaces2)
  }

  test("Adding the boundary -x + 2z = 0 to the two cube should render z = 2 redundant") {
    val newFace = Face((-1.0, 0.0, 2.0), 0.0, phi=0, cohesion=0)
    val redundantBoundingFaces = newFace::boundingFaces2
    val redundant2Cube = Block((1.0, 1.0, 1.0), redundantBoundingFaces)
    val expectedFaces = redundantBoundingFaces.slice(0, redundantBoundingFaces.length - 1)
    assert(redundant2Cube.nonRedundantFaces == expectedFaces)
  }

  test("Moving origin of two-cube to (1,0,0) should only affect x-distances") {
    val newFaces = twoCube.updateFaces((1.0, 0.0, 0.0))
    val newBlock = Block((1.0, 0.0, 0.0), newFaces)
    val testFaces = newBlock.nonRedundantFaces
    val expectedFaces = List(
      Face((-1.0, 0.0, 0.0), 1.0, phi=0, cohesion=0),
      Face((1.0, 0.0, 0.0), 1.0, phi=0, cohesion=0),
      Face((0.0, -1.0, 0.0), 0.0, phi=0, cohesion=0),
      Face((0.0, 1.0, 0.0), 2.0, phi=0, cohesion=0),
      Face((0.0, 0.0, -1.0), 0.0, phi=0, cohesion=0),
      Face((0.0, 0.0, 1.0), 2.0, phi=0, cohesion=0)
    )
    assert(testFaces == expectedFaces)
  }

  test("Moving origin of two-cube to (1,1,1) should affect all distances") {
    val newFaces = twoCube.updateFaces((1.0, 1.0, 1.0))
    val newBlock = Block((1.0, 1.0, 1.0), newFaces)
    val testFaces = newBlock.nonRedundantFaces
    val expectedFaces = List(
      Face((-1.0, 0.0, 0.0), 1.0, phi=0, cohesion=0),
      Face((1.0, 0.0, 0.0), 1.0, phi=0, cohesion=0),
      Face((0.0, -1.0, 0.0), 1.0, phi=0, cohesion=0),
      Face((0.0, 1.0, 0.0), 1.0, phi=0, cohesion=0),
      Face((0.0, 0.0, -1.0), 1.0, phi=0, cohesion=0),
      Face((0.0, 0.0, 1.0), 1.0, phi=0, cohesion=0)
    )
    assert(testFaces == expectedFaces)
  }

  test("Adding the face -x=1 to a two-cube centered at origin should be considered redundant") {
    val redundantFace = Face((-1.0, 0.0, 0.0), 1.0, phi=0, cohesion=0)
    val newTwoCube = Block((0.0, 0.0, 0.0), redundantFace::boundingFaces2)
    val testFaces = newTwoCube.nonRedundantFaces
    assert(testFaces == boundingFaces2)
  }

  test("The points of intersection between the four planes should (0.0, 0.0, 0.0) & (0.0, 5.0, 0.0)") {
    val testFace1 = Face((1.0, 0.0, 0.0), 0.0, phi=0, cohesion=0)
    val testFace2 = Face((0.0, 1.0, 0.0), 0.0, phi=0, cohesion=0)
    val testFace3 = Face((0.0, 0.0, 1.0), 0.0, phi=0, cohesion=0)
    val testFace4 = Face((0.0, 1.0, 0.0), 5.0, phi=0, cohesion=0)
    val testBlock = Block((1.0, 1.0, 1.0), List[Face](testFace1, testFace2, testFace3, testFace4))
    val face1 = List((0.0, 5.0, 0.0), (0.0, 0.0, 0.0))
    val face2 = List((0.0, 0.0, 0.0))
    val face3 = List((0.0, 5.0, 0.0), (0.0, 0.0, 0.0))
    val face4 = List((0.0, 5.0, 0.0))
    val expectedIntersection = List(face1, face2, face3, face4)
    val vertices = testBlock.findVertices
    assert(vertices == expectedIntersection)
  }

  test("There should be no intersection between the planes") {
    val testFace1 = Face((1.0, 0.0, 0.0), 0.0, phi=0, cohesion=0)
    val testFace2 = Face((1.0, 0.0, 0.0), 5.0, phi=0, cohesion=0)
    val testFace3 = Face((0.0, 0.0, 1.0), 0.0, phi=0, cohesion=0)
    val testBlock = Block((1.0, 1.0, 1.0), List[Face](testFace1, testFace2, testFace3))
    val expectedIntersection = List.empty[(Double, Double, Double)]
    val vertices = testBlock.findVertices
    assert((vertices(0) == expectedIntersection) && (vertices(1) == expectedIntersection) && (vertices(2) == expectedIntersection))
  }

  test("There should three entries in the triangulation list ordered in a clockwise fashion") {
    val testFace1 = Face((1.0, 0.0, 0.0), 1.0, phi=0, cohesion=0)
    val testFace2 = Face((0.0, 1.0, 0.0), 1.0, phi=0, cohesion=0)
    val testFace3 = Face((0.0, 0.0, 1.0), 1.0, phi=0, cohesion=0)
    val testFace4 = Face((1/math.sqrt(2.0), 1/math.sqrt(2.0), 0.0), 1/math.sqrt(2.0), phi=0, cohesion=0)
    val testBlock = Block((1.0, 1.0, 1.0), List[Face](testFace1, testFace2, testFace3, testFace4))
    val vertices = testBlock.findVertices
    val mesh = testBlock.meshFaces(vertices)
    val testVertices = List(Delaunay.Vector2(0.0, 1.0), Delaunay.Vector2(1.0,0.0), Delaunay.Vector2(1.0,1.0))
    val expectedTriangulation = (Delaunay.Triangulation(testVertices)).toList
    assert(mesh(2) == expectedTriangulation)
  }

  test("Centroid should be at (0.0, 0.0, 0.0)") {
    val testFace1 = Face((1.0, 0.0, 0.0), 1.0, phi=0, cohesion=0)
    val testFace2 = Face((0.0, 1.0, 0.0), 1.0, phi=0, cohesion=0)
    val testFace3 = Face((0.0, 0.0, 1.0), 1.0, phi=0, cohesion=0)
    val testFace4 = Face((-1.0, 0.0, 0.0), 1.0, phi=0, cohesion=0)
    val testFace5 = Face((0.0, -1.0, 0.0), 1.0, phi=0, cohesion=0)
    val testFace6 = Face((0.0, 0.0, -1.0), 1.0, phi=0, cohesion=0)
    val testBlock = Block((0.0, 0.0, 0.0), List[Face](testFace1, testFace2, testFace3, testFace4, testFace5, testFace6))
    val vertices = testBlock.findVertices
    val mesh = testBlock.meshFaces(vertices)
    val centroid = testBlock.centroid(vertices, mesh)
    val expectedCentroid = (0.0, 0.0, 0.0)
    assert(centroid == expectedCentroid)
  }

  test("Centroid should be at (0.5, 0.5, 1.0)") {
    val testFace1 = Face((1.0, 0.0, 0.0), 1.0, phi=0, cohesion=0)
    val testFace2 = Face((0.0, 1.0, 0.0), 1.0, phi=0, cohesion=0)
    val testFace3 = Face((0.0, 0.0, 1.0), 2.0, phi=0, cohesion=0)
    val testFace4 = Face((-1.0, 0.0, 0.0), 1.0, phi=0, cohesion=0)
    val testFace5 = Face((0.0, -1.0, 0.0), 1.0, phi=0, cohesion=0)
    val testFace6 = Face((0.0, 0.0, -1.0), 1.0, phi=0, cohesion=0)
    val testBlock = Block((0.5, 0.5, 0.5), List[Face](testFace1, testFace2, testFace3, testFace4, testFace5, testFace6))
    val vertices = testBlock.findVertices
    val mesh = testBlock.meshFaces(vertices)
    val centroid = testBlock.centroid(vertices, mesh)
    val expectedCentroid = (0.5, 0.5, 1.0)
    assert(centroid == expectedCentroid)
  }

  test("Centroid should be at (-1.0, -1.0, -1.0)") {
    val testFace1 = Face((1.0, 0.0, 0.0), 0.0, phi=0, cohesion=0)
    val testFace2 = Face((0.0, 1.0, 0.0), 0.0, phi=0, cohesion=0)
    val testFace3 = Face((0.0, 0.0, 1.0), 0.0, phi=0, cohesion=0)
    val testFace4 = Face((-1.0, 0.0, 0.0), 2.0, phi=0, cohesion=0)
    val testFace5 = Face((0.0, -1.0, 0.0), 2.0, phi=0, cohesion=0)
    val testFace6 = Face((0.0, 0.0, -1.0), 2.0, phi=0, cohesion=0)
    val testBlock = Block((0.0, 0.0, 0.0), List[Face](testFace1, testFace2, testFace3, testFace4, testFace5, testFace6))
    val vertices = testBlock.findVertices
    val mesh = testBlock.meshFaces(vertices)
    val centroid = testBlock.centroid(vertices, mesh)
    val expectedCentroid = (-1.0, -1.0, -1.0)
    assert(centroid == expectedCentroid)
  }

  test("New distances should be shifted based on input local origin") {
    val testFace1 = Face((1.0, 0.0, 0.0), 2.0, phi=0, cohesion=0)
    val testFace2 = Face((0.0, 1.0, 0.0), 2.0, phi=0, cohesion=0)
    val testFace3 = Face((0.0, 0.0, 1.0), 2.0, phi=0, cohesion=0)
    val testFace4 = Face((-1.0, 0.0, 0.0), 0.0, phi=0, cohesion=0)
    val testFace5 = Face((0.0, -1.0, 0.0), 0.0, phi=0, cohesion=0)
    val testFace6 = Face((0.0, 0.0, -1.0), 0.0, phi=0, cohesion=0)
    val testBlock = Block((0.0, 0.0, 0.0), List(testFace1, testFace2, testFace3, testFace4, testFace5, testFace6))
    val updatedFaces = testBlock.updateFaces((2.0, 0.0, 0.0))
    val expectedFace1 = Face((1.0, 0.0, 0.0), 0.0, phi=0, cohesion=0)
    val expectedFace2 = Face((0.0, 1.0, 0.0), 2.0, phi=0, cohesion=0)
    val expectedFace3 = Face((0.0, 0.0, 1.0), 2.0, phi=0, cohesion=0)
    val expectedFace4 = Face((-1.0, 0.0, 0.0), 2.0, phi=0, cohesion=0)
    val expectedFace5 = Face((0.0, -1.0, 0.0), 0.0, phi=0, cohesion=0)
    val expectedFace6 = Face((0.0, 0.0, -1.0), 0.0, phi=0, cohesion=0)
    val expectedFaces = List(expectedFace1, expectedFace2, expectedFace3, expectedFace4, expectedFace5, expectedFace6)
    assert(updatedFaces == expectedFaces)
  }

  test("Cutting the two-cube with faces x=1 and z=1 should produce four blocks") {
    val xPlane = Joint((1.0,0.0,0.0), 0.0, (1.0,1.0,1.0), dipAngle=0, dipDirection=0,
                       phi=0, cohesion=0, shape=Nil)
    val zPlane = Joint((0.0,0.0,1.0), 0.0, (1.0,1.0,1.0), dipAngle=0, dipDirection=0,
                       phi=0, cohesion=0, shape=Nil)
    val xBlocks = twoCube.cut(xPlane)
    val xzBlocks = xBlocks.flatMap(_.cut(zPlane))

    val nonRedundantBlocks = xzBlocks.map { case block @ Block(center, faces) =>
      val nonRedundantFaces = block.nonRedundantFaces
      Block(center, nonRedundantFaces)
    }

    val centroidBlocks = nonRedundantBlocks.map { case block @ Block(center, _) =>
      val vertices = block.findVertices
      val mesh = block.meshFaces(vertices)
      val centroid = block.centroid(vertices, mesh)
      val updatedFaces = block.updateFaces(centroid)
      val tester = Block(centroid, updatedFaces)
      Block(centroid, updatedFaces)
    }

    val cleanedBlocks = centroidBlocks.map { case Block(center, faces) =>
      Block(center, faces.map(_.applyTolerance))
    }

    // Names describe block positions when locking from fourth octant to first octant
    val bottomLeft = Block((0.5,1.0,0.5), List(
      Face((0.0,0.0,1.0), 0.5, 0.0, 0.0),
      Face((1.0,0.0,0.0), 0.5, 0.0, 0.0),
      Face((-1.0,0.0,0.0), 0.5, 0.0, 0.0),
      Face((0.0,-1.0,0.0), 1.0, 0.0, 0.0),
      Face((0.0,1.0,0.0), 1.0, 0.0, 0.0),
      Face((0.0,0.0,-1.0), 0.5, 0.0, 0.0)
    ))

    val topLeft = Block((0.5,1.0,1.5), List(
      Face((0.0,0.0,-1.0), 0.5, 0.0, 0.0),
      Face((1.0,0.0,0.0), 0.5, 0.0, 0.0),
      Face((-1.0,0.0,0.0), 0.5, 0.0, 0.0),
      Face((0.0,-1.0,0.0), 1.0, 0.0, 0.0),
      Face((0.0,1.0,0.0), 1.0, 0.0, 0.0),
      Face((0.0,0.0,1.0), 0.5, 0.0, 0.0)
    ))

    val bottomRight = Block((1.5,1.0,0.5), List(
      Face((0.0,0.0,1.0), 0.5, 0.0, 0.0),
      Face((-1.0,0.0,0.0), 0.5, 0.0, 0.0),
      Face((1.0,0.0,0.0), 0.5, 0.0, 0.0),
      Face((0.0,-1.0,0.0), 1.0, 0.0, 0.0),
      Face((0.0,1.0,0.0), 1.0, 0.0, 0.0),
      Face((0.0,0.0,-1.0), 0.5, 0.0, 0.0)
    ))

    val topRight = Block((1.5,1.0,1.5), List(
      Face((0.0,0.0,-1.0), 0.5, 0.0, 0.0),
      Face((-1.0,0.0,0.0), 0.5, 0.0, 0.0),
      Face((1.0,0.0,0.0), 0.5, 0.0, 0.0),
      Face((0.0,-1.0,0.0), 1.0, 0.0, 0.0),
      Face((0.0,1.0,0.0), 1.0, 0.0, 0.0),
      Face((0.0,0.0,1.0), 0.5, 0.0, 0.0)
    ))

    val expectedBlocks = List(bottomLeft, topLeft, bottomRight, topRight)
    assert(cleanedBlocks == expectedBlocks)
  }
}
