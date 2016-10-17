package generator

import java.io.{File, IOException}

/**
  * Created by leonardo on 16/10/16.
  */
object Directories {

  lazy val templateDir = directory("templates")
  lazy val generatedTemplateDir = directory(".generator"+File.separator+"templates")
  lazy val scalaClassDir = directory(".generator"+File.separator+"classes")

  def directory(path: String) = {
    val dir = new File(path)
    if (!dir.exists()) {
      if (!dir.mkdirs()) {
        throw new IOException(s"Couldn't create directory $path")
      }
    }
    dir
  }


}
