package generator

import java.io.{File, FilenameFilter}

import play.twirl.api.{Template2, Template3}

import scala.tools.nsc.io.PlainFile
import scala.tools.nsc.settings.MutableSettings
import scala.tools.nsc.{GenericRunnerSettings, Global, Settings}

/**
  * Created by leonardo on 15/10/16.
  */

trait Probe

class ScalaCompiler(inputDir : File, outputDir : File) {

  def settings() : Settings = {
    val ms = new GenericRunnerSettings(println _)
    ms.embeddedDefaults[Probe]
    ms.outdir.value = outputDir.getAbsolutePath
    ms
  }

  val g = new Global(settings())

  val run = new g.Run

  def listTemplates(inputDir: File): List[String] = {
    inputDir.listFiles().flatMap{
      case f : File if f.getName.endsWith(".scala") => List(f.getAbsolutePath)
      case d : File if d.isDirectory => listTemplates(d)
    }.toList
  }

  def toClassName(baseDir: File, classFile: File) = {
    baseDir.toURI().relativize(classFile.toURI()).getPath().replace(File.separatorChar, '.').replace(".class","")
  }

  def listTemplateObjectClasses(rootDir: File): List[String] = {
    listTemplateObjectClasses(rootDir, rootDir)
  }

  def listTemplateObjectClasses(rootDir: File, currentDir: File): List[String] = {
    currentDir.listFiles().flatMap{
      case f : File if !f.isDirectory && f.getName.endsWith("$.class") => List(toClassName(rootDir, f))
      case d : File if d.isDirectory => listTemplateObjectClasses(rootDir, d)
      case _ => None
    }.toList
  }

  def getTemplate[T](classLoader: ClassLoader, name : String)(implicit man: Manifest[T]) : T =
    classLoader.loadClass(name).getField("MODULE$").get(man.runtimeClass).asInstanceOf[T]

  type TemplateType = Template2[String, DBIntrospector.TableMap, play.twirl.api.TxtFormat.Appendable]

  def compile(): List[TemplateType] = {

    run.compile(listTemplates(inputDir))

    val classLoader = new java.net.URLClassLoader(
       Array(outputDir.toURI.toURL),  // Using current directory.
        this.getClass.getClassLoader)

    listTemplateObjectClasses(outputDir).map{
      className =>
        getTemplate[TemplateType](classLoader, className)
    }

  }

}
