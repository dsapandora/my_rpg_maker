package myrpgmaker.model

import myrpgmaker.lib._
import myrpgmaker.lib.FileHelper._

import org.json4s.native.Serialization

import scala.collection.JavaConversions._
import java.io._

case class Project(dir: File, data: ProjectData) {
  def writeMetadata(): Boolean = data.write(dir)

  def rcDir = dir
}

object Project {

  def startingProject(title: String, dir: File) = Project(dir, ProjectData())

  def readFromDisk(projDir: File): Option[Project] =
    ProjectData.read(projDir).map(Project(projDir, _))
}

