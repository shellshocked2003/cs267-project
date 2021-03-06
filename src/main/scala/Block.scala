package edu.berkeley.ce.rockslicing

import breeze.linalg
import breeze.linalg.{DenseVector, DenseMatrix}
import scala.language.postfixOps

object Face {
  private val EPSILON = 1e-6
}

/** A simple data structure to represent the face of a rock block.
  *
  * @constructor Create a new rock face.
  * @param normalVec The normal vector to the face. The individual vector components can
  * be accessed as 'a', 'b', and 'c'.
  * @param distance The distance from the normal vector to the center of the rock block.
  * Accessed as 'd'.
  * @param phi The friction angle (phi) of the face.
  * @param cohesion The cohesion of the face.
  */
case class Face(normalVec: (Double,Double,Double), distance: Double,
                val phi: Double, val cohesion: Double) {
  val (a,b,c) = normalVec
  val d = distance

  /**
    * Checks if each of the parameters of a face is approximately 0.0 and,
    * if so, sets it to 0.0.
    */
  def applyTolerance: Face = {
    val newA = if (math.abs(a) > Face.EPSILON) a else 0.0
    val newB = if (math.abs(b) > Face.EPSILON) b else 0.0
    val newC = if (math.abs(c) > Face.EPSILON) c else 0.0
    val newD = if (math.abs(d) > Face.EPSILON) d else 0.0
    val newPhi = if (math.abs(phi) > Face.EPSILON) phi else 0.0
    val newCohesion = if (math.abs(cohesion) > Face.EPSILON) cohesion else 0.0
    Face((newA,newB,newC), newD, newPhi, newCohesion)
  }
}

object Block {
  private val EPSILON = 10e-12

  private def applyTolerance(d: Double): Double =
    if (math.abs(d) >= EPSILON) d else 0.0
}

/** A rock block.
  *
  * @constructor Create a new rock block
  * @param center Cartesian coordinates for the center of the rock block. The individual
  * components can be accessed as 'centerX', 'centerY', and 'centerZ'.
  * @param faces: The faces that define the boundaries of the rock block.
  */
case class Block(center: (Double,Double,Double), val faces: List[Face]) {
  val (centerX, centerY, centerZ) = center

  /**
    * Determine whether or not a joint intersects this rock block.
    * @param joint The joint to check for intersection.
    * @return An option indicating the point of intersection. If there is no
    * intersection, None is returned, otherwise Some((x,y,z)) is returned where
    * (x,y,z) is the point of intersection.
    */
  def intersects(joint: Joint): Option[(Double,Double,Double)] = {
    val linProg = new LinearProgram(4)
    // Minimize s
    linProg.setObjFun(Array[Double](0.0, 0.0, 0.0, 1.0), LinearProgram.MIN)

    // Restrict our attention to plane of joint
    val translatedJoint = joint.updateJoint(centerX, centerY, centerZ)
    val coeffs = Array[Double](translatedJoint.a, translatedJoint.b, translatedJoint.c, 0.0).map(Block.applyTolerance)
    val rhs = Block.applyTolerance(translatedJoint.d)
    linProg.addConstraint(coeffs, LinearProgram.EQ, rhs)

    // Require s to be within planes defined by faces of block
    faces.foreach { face =>
      val faceCoeffs = Array[Double](face.a, face.b, face.c, -1.0).map(Block.applyTolerance)
      val rhs = Block.applyTolerance(face.d)
      linProg.addConstraint(faceCoeffs, LinearProgram.LE, rhs)
    }

    // Require s to be within planes defining shape of joint
    translatedJoint.globalCoordinates.foreach { case ((a,b,c),d) =>
      val jointCoeffs = Array[Double](a, b, c, -1.0).map(Block.applyTolerance)
      val rhs = Block.applyTolerance(d)
      linProg.addConstraint(jointCoeffs, LinearProgram.LE, rhs)
    }

    linProg.solve() match {
      case None => None
      case Some((_, opt)) if (opt >= -Block.EPSILON) => None
      case Some((vars, _)) => Some((vars(0), vars(1), vars(2)))
    }
  }

  /**
    * Divide this block into two child blocks if a joint intersects this block.
    * @param joint A joint that may or may not divide this block.
    * @return A List of Block objects, containing the two child blocks divided by
    * the joint if it intersects this block. Otherwise, returns a one-item list
    * containing only this block.
    */
  def cut(joint: Joint): List[Block] =
    this.intersects(joint) match {
      case None => List(this)
      case Some((x,y,z)) => {
        val newX = centerX + x
        val newY = centerY + y
        val newZ = centerZ + z
        val updatedFaces = updateFaces(newX, newY, newZ)
        List(
          // New origin is guaranteed to lie within joint, so initial d = 0
          Block((newX,newY,newZ), Face((joint.a, joint.b, joint.c), 0.0, joint.phi, joint.cohesion)::updatedFaces),
          Block((newX,newY,newZ), Face((-joint.a,-joint.b,-joint.c), 0.0, joint.phi, joint.cohesion)::updatedFaces)
        )
      }
    }

  /**
    * Compute the faces of the rock block that are not geometrically redundant.
    * @return A list of faces that uniquely determine this rock block and are not
    * geometrically redundant.
    */
  def nonRedundantFaces: List[Face] =
    faces.distinct.filter { face =>
      val linProg = new LinearProgram(3)
      val objCoeffs = Array[Double](face.a, face.b, face.c).map(Block.applyTolerance)
      linProg.setObjFun(objCoeffs, LinearProgram.MAX)
      faces.foreach { f =>
        val faceCoeffs = Array[Double](f.a, f.b, f.c).map(Block.applyTolerance)
        val rhs = Block.applyTolerance(f.d)
        linProg.addConstraint(faceCoeffs, LinearProgram.LE, rhs)
      }
      val s = linProg.solve().get._2
      math.abs(s - face.d) <= Block.EPSILON
    }

  /**
    * Calculate the vertices of the block
    * @return List of of lists of vertices based on the current list of Faces. Order of list
    * follows the same order as input list of faces.
    * This function should only be called once all redundant faces have been removed.
    */
  def findVertices: List[List[(Double, Double, Double)]] = {
    // Iterate through list to check for intersection
    val tolerance = 1e-12
    var vertex_list = List.empty[List[(Double, Double, Double)]]
    for (i <- 0 until faces.length) {
      var temp_vertices = List.empty[(Double, Double, Double)]
      val ni = DenseVector[Double](faces(i).a, faces(i).b, faces(i).c)
      val di = faces(i).d
      for (j <- 0 until faces.length) {
        val nj = DenseVector[Double](faces(j).a, faces(j).b, faces(j).c)
        val dj = faces(j).d
        for (k <- 0 until faces.length) {
          val nk = DenseVector[Double](faces(k).a, faces(k).b, faces(k).c)
          val dk = faces(k).d
          // check if normals of faces are coplanar, if not find intersection
          if (tolerance < math.abs((ni dot linalg.cross(nj, nk)))) {
            val A_matx = DenseMatrix.zeros[Double](3,3)
            val b_vect = DenseVector[Double](di, dj, dk)
            A_matx(0, ::) := ni.t
            A_matx(1, ::) := nj.t
            A_matx(2, ::) := nk.t
            val p_vect = A_matx \ b_vect
            temp_vertices :::= List((p_vect(0), p_vect(1), p_vect(2)))
          }
        }
      }
      temp_vertices = temp_vertices.distinct // Remove any duplicates from list
      vertex_list :::= List(temp_vertices)
    }
    vertex_list.reverse
  }

  /**
    * Calculates the rotation matrix to rotate the input plane (specified by it's normal)
    * to the desired orientation (specified by the desired normal)
    * @param n_current: Current normal of plane
    * @param n_desired: Desired new normal
    * @return rmat: 3*3 rotation matrix
    */
  def rotationMatrix(n_current: (Double, Double, Double), n_desired: (Double, Double, Double)) :
                    DenseMatrix[Double] = {
    val tolerance = 1e-12
    val n_c = DenseVector[Double](n_current._1, n_current._2, n_current._3)
    val n_d = DenseVector[Double](n_desired._1, n_desired._2, n_desired._3)
    if (math.abs(linalg.norm(linalg.cross(n_c,n_d))) > tolerance) {
      val Txz = DenseMatrix.zeros[Double](3,3)
      val Tz = DenseMatrix.zeros[Double](3,3)
      val u = n_c(0)
      val v = n_c(1)
      val w = n_c(2)
      // Rotation matrix to rotate into x-z plane
      Txz(0,0) = u/math.sqrt(u*u + v*v)
      Txz(1,0) = -v/math.sqrt(u*u + v*v)
      Txz(0,1) = v/math.sqrt(u*u + v*v)
      Txz(1,1) = u/math.sqrt(u*u + v*v)
      Txz(2,2) = 1.0

      // Rotation matrix to rotate from x-z plane on z-axis
      Tz(0,0) = w/math.sqrt(u*u + v*v + w*w)
      Tz(2,0) = math.sqrt(u*u + v*v)/math.sqrt(u*u + v*v + w*w)
      Tz(0,2) = -math.sqrt(u*u + v*v)/math.sqrt(u*u + v*v + w*w)
      Tz(2,2) = w/math.sqrt(u*u + v*v + w*w)
      Tz(1,1) = 1.0
      val rmat = Tz * Txz
      return rmat
    } else {
      val rmat = DenseMatrix.eye[Double](3)
      return rmat
    }
  }

  /**
    * Mesh the faces using Delaunay triangulation. This meshing is done
    * in order to calculate the volume and centroid of the block
    * @return A list of lists that give the local to global mapping for the
    * triangulation of each of the faces.
    */
  def meshFaces(vertices: List[List[(Double, Double, Double)]]): List[List[(Int, Int, Int)]] = {
    // Determine rotation matrix to rotate faces perpendicular to z-axis. This way all vertices
    // are only in the x-y plane which makes triangulation easier.
    val ek = (0.0, 0.0, 1.0)
    var mesh = List.empty[List[(Int, Int, Int)]]
    for (i <- 0 until faces.length) {
      val nPlane = (faces(i).a, faces(i).b, faces(i).c)
      val R = rotationMatrix(nPlane, ek)
      var temp_mapping = List.empty[(Int, Int, Int)]
      var temp_vertices = List.empty[Delaunay.Vector2]
      for (j <- 0 until vertices(i).length) { // Iterate through vertices and rotate
        val temp_vector = R * DenseVector(vertices(i)(j)._1, vertices(i)(j)._2, vertices(i)(j)._3)
        temp_vertices :::= List(Delaunay.Vector2(temp_vector(0), temp_vector(1)))
      }
      temp_vertices = temp_vertices.reverse
      // Vertices are returned in CLOCKWISE order
      temp_mapping = (Delaunay.Triangulation(temp_vertices)).toList
      // If z-component of normal is negative, order needs to be reversed to maintain clock-wise
      // orientation
      val tolerance = 1e-12
      if (math.abs(faces(i).c + 1.0) < tolerance) {
        var temp_triangles = List.empty[(Int, Int, Int)]
        for (k <- 0 until temp_mapping.length) {
          temp_triangles :::= List((temp_mapping(k)._3, temp_mapping(k)._2, temp_mapping(k)._1))
        }
        temp_mapping = temp_triangles.reverse
      }
      mesh :::= List(temp_mapping)
    }
    mesh.reverse
  }

  /**
    * Calculates the centroid of the block.
    * @param vertices: A list of lists that contain the vertices of the block faces
    * @param mesh: A list of lists that contain the local to global mapping of the triangulation
    * @return The centroid of the block, (centerX, centerY, centerZ).
    */
  def centroid(vertices: List[List[(Double, Double, Double)]],
               mesh: List[List[(Int, Int, Int)]]): (Double, Double, Double) = {
    // Calculate volume, this is necessary before centroid calcs can be done
    var volume = 0.0
    for (i <- 0 until faces.length) {
      for (j <- 0 until mesh(i).length) {
        // Access entries of triangulation in reverse so they are in ANTI-CLOCKWISE order
        val a_vals = vertices(i)(mesh(i)(j)._3)
        val b_vals = vertices(i)(mesh(i)(j)._2)
        val c_vals = vertices(i)(mesh(i)(j)._1)
        val a = DenseVector[Double](a_vals._1, a_vals._2, a_vals._3)
        val b = DenseVector[Double](b_vals._1, b_vals._2, b_vals._3)
        val c = DenseVector[Double](c_vals._1, c_vals._2, c_vals._3)
        val ni = linalg.cross(b-a,c-a)
        volume += a dot ni
      }
    }
    volume = volume/6.0
    // Calculate centroid
    var centroid = DenseVector.zeros[Double](3)
    for (i <- 0 until faces.length) {
      for (j <- 0 until mesh(i).length) {
        val a_vals = vertices(i)(mesh(i)(j)._3)
        val b_vals = vertices(i)(mesh(i)(j)._2)
        val c_vals = vertices(i)(mesh(i)(j)._1)
        val a = DenseVector[Double](a_vals._1, a_vals._2, a_vals._3)
        val b = DenseVector[Double](b_vals._1, b_vals._2, b_vals._3)
        val c = DenseVector[Double](c_vals._1, c_vals._2, c_vals._3)
        val ni = linalg.cross(b-a,c-a)
        // Unit normals for each axis
        val ei = DenseVector[Double](1.0, 0.0, 0.0)
        val ej = DenseVector[Double](0.0, 1.0, 0.0)
        val ek = DenseVector[Double](0.0, 0.0, 1.0)
        val normal_list = List(ei, ej, ek)
        // Loop over each Cartesian axis
        for (k <- 0 until 3) {
          centroid(k) += 1/24.0 * (ni dot normal_list(k)
                                     * (math.pow((a+b) dot normal_list(k), 2)
                                      + math.pow((b+c) dot normal_list(k), 2)
                                      + math.pow((c+a) dot normal_list(k), 2)))
        }
      }
    }
    centroid :*= 1/(2.0*volume)
    (centroid(0) + centerX, centroid(1) + centerY, centroid(2) + centerZ)
  }

  /**
    * Calculates the distances of the joints relative to a new origin
    * @param localOrigin: new local origin
    * @return List of faces with updated distances
    */
  def updateFaces(localOrigin: (Double, Double,Double)): List[Face] = {
    faces.map { case Face((a,b,c), d, phi, cohesion) =>
      val tolerance = 1e-12
      var w = DenseVector.zeros[Double](3)
      if (math.abs(c) >= tolerance) {
        w(0) = localOrigin._1
        w(1) = localOrigin._2
        w(2) = localOrigin._3 - (d/c + centerZ)
      } else if (math.abs(b) >= tolerance) {
        w(0) = localOrigin._1
        w(1) = localOrigin._2 - (d/b + centerY)
        w(2) = localOrigin._3
      } else if (math.abs(a) >= tolerance) {
        w(0) = localOrigin._1 - (d/a + centerX)
        w(1) = localOrigin._2
        w(2) = localOrigin._3
      }
      val n = DenseVector[Double](a, b, c)
      val new_d = -(n dot w)/linalg.norm(n)
      Face((a, b, c), new_d, phi, cohesion)
    }
  }
}
