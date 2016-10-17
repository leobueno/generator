package generator

import java.io.File
import play.twirl.api.Template3

case class Config(driver: String = "org.postgresql.Driver", url: String = "jdbc:postgresql://localhost:5432/", database: String, login: String, password: String)

object Main {

  def main(args : Array[String]) : Unit = {

    val config = Config(url = "jdbc:postgresql://localhost:5432/cdv", database = "cdv", login = "cdv", password = "K7C3POSK8")

    val tables = DBIntrospector(config).tables

    render(tables)

  }

  def render(tables: Map[String, Array[Map[String, String]]]) = {
    val compiler = new TemplateCompiler(Directories.templateDir,Directories.generatedTemplateDir)
    compiler.compile
    val scalaCompiler = new ScalaCompiler(Directories.generatedTemplateDir, Directories.scalaClassDir)
    val templates = scalaCompiler.compile()
    templates.foreach{
      template =>
        val result = template.render("teste", tables)
        println(result.body)
    }
  }

//  def renderTable(engine: TemplateEngine, tableName: String, columns: Array[Map[String, String]]) = {
//
//    val out = createPrintWriterForTable(tableName)
//
//    try {
//
//      engine.layout("scalaModel.mustache", out, columns)
//
//
//    } finally {
//      scalaFilePrintWriter.close()
//    }
//
//  }
//
//  def createPrintWriterForTable(tableName: String) = new PrintWriter(tableName + ".scala")

}
