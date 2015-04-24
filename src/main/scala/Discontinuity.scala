package edu.berkeley.ce.rockslicing

import breeze.linalg._

/** A simple data structure to represent a discontinuity.
  *
  * @constructor Create a new discontinuity.
  * @param normalVec The normal vector to the discontinuity. The individual vector components
  * can be accessed as 'a', 'b', and 'c'.
  * @param center Cartesian coordinates for the center of the discontinuity. The individual
           components can be accessed as 'centerX', 'centerY', and 'centerZ'.
  * @param distance The distance of the discontinuity from the local center, accessed as 'd'.
  * @param phi The discontinuity's friction angle (phi).
  * @param cohesion The cohesion along the discontinuity
  * @param shape A list of lines specifying the shape of the discontinuity. Each item is a
  * 3-tuple. The first two items specify the line, while the last gives the distance
  * of the line from the discontinuity's center in the local coordinate system.
 */
case class Discontinuity(normalVec: (Double, Double, Double), center: (Double, Double, Double),
                         val dipAngle: Double, val dipDirection: Double, distance: Double,
                         val phi: Double, val cohesion: Double,
                         val shape: List[(Double, Double, Double)]) {
  val (a, b, c) = normalVec
  val (centerX, centerY, centerZ) = center
  val d = distance

  /** Converts lines defining shape of discontinuity from local to global coordinates
    * @return A list of pairs, each representing a plane that specifies a boundary of the
    * discontinuity in the global coordinate space. The first item of each pair is a normal
    * vector for the plane, and the second item is the distance of the plane from the origin.
    */
  def globalCoordinates: List[((Double, Double, Double), Double)] = {
    val Nplane = DenseVector[Double](a, b, c)
    val strike = (dipDirection + math.Pi / 2) % (2 * math.Pi) // TODO Is this correct?
    val Nstrike = DenseVector[Double](math.cos(strike), math.sin(strike), 0.0)
    val Ndip = cross(Nplane, Nstrike)

    // Q defines the linear transformation to convert to global coordinates
    val Q = DenseMatrix.zeros[Double](3,3)
    Q(::, 0) := Nstrike
    Q(::, 1) := Ndip
    Q(::, 2) := Nplane

    val shapeVectors = shape.map {case (a, b, _) => DenseVector[Double](a, b, 0)}
    val globalShapeVecs = shapeVectors.map {x => Q*x}

    val centerVec = DenseVector[Double](centerX, centerY, centerZ)
    val localDistances = shape.map {x => x._2}
    val globalDistances = (globalShapeVecs.zip(localDistances)).map
                              {case (shapeVec, d) => d + shapeVec.dot(centerVec)}

    // Convert back to triples to hide underlying Breeze implementation
    val globalShapeTuples = globalShapeVecs.map {x => (x(0), x(1), x(2))}
    globalShapeTuples.zip(globalDistances)
  }
}
