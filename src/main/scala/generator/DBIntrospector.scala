package generator

import java.sql.{DriverManager, ResultSet, ResultSetMetaData}

/**
  * Created by leonardo on 12/10/16.
  */
class DBIntrospector(config : Config) {

  /**
    * Example:
    *   {
    *     "users" -> [
    *           {
    *             "TABLE_NAME" -> "users"
    *             "COLUMN_NAME" -> "id"
    *             "COLUMN_TYPE" -> "INT"
    *             "NOT_NULL" -> "1"
    *           },
    *           .
    *           .
    *           .
    *       ]
    *   }
    */
  lazy val tables : DBIntrospector.TableMap = loadTableDefinitions(config)

  def columnNames(meta : ResultSetMetaData) : Array[String] = {
    (for {
      i <- 1 to meta.getColumnCount
    } yield meta.getColumnName(i)).toArray
  }

  def rsToMap(columnNames : Array[String], resultSet: ResultSet): Map[String, String] = {
    columnNames.map(name => name -> resultSet.getString(name)).toMap
  }

  def loadTableDefinitions(config : Config) : DBIntrospector.TableMap = {

    Class.forName(config.driver)

    val connection = DriverManager.getConnection(config.url, config.login, config.password)

    try {

      val metadata = connection.getMetaData

      val columnMetas = metadata.getColumns(null, config.database, null, null)

      val metaColumnNames = columnNames(columnMetas.getMetaData)

      iterate(columnMetas).map(rsToMap(metaColumnNames, _)).toArray.groupBy(c => c("TABLE_NAME"))

    } finally {
      connection.close()
    }

  }

  def iterate(resultSet: ResultSet): Iterator[ResultSet] = {
    new Iterator[ResultSet] {
      def hasNext = resultSet.next()
      def next() = resultSet
    }
  }


}

object DBIntrospector {

  type TableMap = Map[String, Array[Map[String, String]]]

  def apply(config: Config): DBIntrospector = new DBIntrospector(config)

}

case class ColumnDef(columnName: String, typeName: String, columnSize: Int, decimalDigits: Int, nullable: Int, autoInc: Int)
