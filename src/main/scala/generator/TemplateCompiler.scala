package generator

import java.io.{File, FileFilter, FilenameFilter}
import play.twirl.compiler._

/**
  * Created by leonardo on 12/10/16.
  */
class TemplateCompiler(templateDir: File, compiledTemplateDir: File) {

  final val Template = """(.*)\.scala\.(.*)""".r

  def compile = {

    val templateFiles = templateDir.listFiles

    templateFiles.foreach{
      file =>
        file.getName match {
          case Template(basename, extension) => compileFileTo(file, compiledTemplateDir)
          case other : String => println(s"ignoring $other")
        }

    }

  }

  def compileFileTo(file: File, compiledTemplateDir: File) = {
    TwirlCompiler.compile(file, templateDir, compiledTemplateDir, "play.twirl.api.TxtFormat")
  }

}
