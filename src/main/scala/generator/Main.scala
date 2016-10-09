package generator

import java.io.{File, PrintWriter}
import java.sql.{Connection, DatabaseMetaData, DriverManager, ResultSet}

import org.fusesource.scalate._

case class Config(driver: String = "org.postgresql.Driver", url: String = "jdbc:postgresql://localhost:5432/mydb", database: String, login: String, password: String)

class Main {

  def main(args : Array[String]) = {

    val config = Config(database = "cdv", login = "cdv", password = "K7C3POSK8")

      val tables = loadTableDefinitions(config)

      render(tables)

  }

  def loadTableDefinitions(config : Config) = {

    Class.forName(config.driver)

    val connection = DriverManager.getConnection(config.url, config.login, config.password)

    try {

      val metadata = connection.getMetaData

      val columnMetas = metadata.getColumns(null, config.database, null, null)

      val tables = iterate(columnMetas).map{
        columnMeta =>
          Map(
              "table" -> columnMeta.getString("TABLE_NAME"),
              "column" -> columnMeta.getString("COLUMN_NAME"),
              "type" -> columnMeta.getString("TYPE_NAME"),
              "length" -> Option(columnMeta.getInt("COLUMN_SIZE")).map(_.toString).getOrElse(""),
              "precision" -> Option(columnMeta.getInt("DECIMAL_DIGITS")).map(_.toString).getOrElse(""),
              "nullable" -> columnMeta.getString("IS_NULLABLE"),
              "autoInc" -> columnMeta.getString("IS_AUTOINCREMENT")
           )
      }.toArray
        .groupBy(c => c("table"))
        .map{
          case(tableName, columns) => columns
        }

      Map("tables" -> tables)

    } finally {
      connection.close()
    }

  }

  def render(tables: Map[String, Array[Map[String, String]]]) = {
    val engine = new TemplateEngine
    engine.workingDirectory = new File("generated")
    tables.foreach{
      case (tableName, columns) => renderTable(engine, tableName, columns)
    }
  }

  def renderTable(engine: TemplateEngine, tableName: String, columns: Array[Map[String, String]]) = {

    val out = createPrintWriterForTable(tableName)

    try {

      engine.layout("scalaModel.mustache", out, columns)


    } finally {
      scalaFilePrintWriter.close()
    }

  }

  def createPrintWriterForTable(tableName: String) = new PrintWriter(tableName + ".scala")

  def iterate(resultSet: ResultSet): Iterator[ResultSet] = {
    new Iterator[ResultSet] {
      def hasNext = resultSet.next()
      def next() = resultSet
    }
  }


}

case class ColumnDef(columnName: String, typeName: String, columnSize: Int, decimalDigits: Int, nullable: Int, autoInc: Int)
