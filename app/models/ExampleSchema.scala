package models

//import models.LocalCluster
import scala.collection._
import org.joda.time.DateTime
import CustomTypes._
import com.gravity.hbase.schema._



/*             )\._.,--....,'``.
.b--.        /;   _.. \   _\  (`._ ,.
`=,-,-'~~~   `----(,_..'--(,_..'`-.;.'  */

/**
 * This test is intended to simultaneously test the library and show how you put together your own schema.
 */

/**
 * CUSTOM TYPES
 */
case class Kitten(name:String, age:Int, height:Double)

case class PageUrl(url:String)

/**
 * CUSTOM SERIALIZERS
 * These are serializers for custom types.  When you create your own serializers, which is common, it's useful to put them
 * in their own object definition.  Then, when you need the serializers in client code, make sure you import the object.  For
 * the below, you'd do
 * import com.gravity.hbase.schema.CustomTypes._
 */
object CustomTypes {

  implicit object PageUrlConverter extends ComplexByteConverter[PageUrl] {
    override def write(url:PageUrl, output:PrimitiveOutputStream) {
      output.writeUTF(url.url)
    }
    override def read(input:PrimitiveInputStream) = {
      PageUrl(input.readUTF())
    }
  }

  implicit object KittenConverter extends ComplexByteConverter[Kitten] {
    override def write(kitten:Kitten, output:PrimitiveOutputStream)  {
      output.writeUTF(kitten.name)
      output.writeInt(kitten.age)
      output.writeDouble(kitten.height)
    }

    override def read(input:PrimitiveInputStream) = {
      Kitten(input.readUTF(), input.readInt(), input.readDouble())
    }
  }

  implicit object KittenSeqConverter extends SeqConverter[Kitten]
}


object ExampleSchema extends Schema {


  //There should only be one HBaseConfiguration object per process.  You'll probably want to manage that
  //instance yourself, so this library expects a reference to that instance.  It's implicitly injected into
  //the code, so the most convenient place to put it is right after you declare your Schema.
  implicit val conf = LocalCluster.getTestConfiguration

  //A table definition, where the row keys are Strings
  class ExampleTable extends HbaseTable[ExampleTable,String, ExampleTableRow](tableName = "schema_example",rowKeyClass=classOf[String], tableConfig = HbaseTableConfig(maxFileSizeInBytes=1073741824))
  {
    def rowBuilder(result:DeserializedResult) = new ExampleTableRow(this,result)

    val meta = family[String, String, Any]("meta")
    //Column family definition
    //Inside meta, assume a column called title whose value is a string
    val title = column(meta, "title", classOf[String])
    //Inside meta, assume a column called url whose value is a string
    val url = column(meta, "url", classOf[String])
    //Inside meta, assume a column called views whose value is a string
    val views = column(meta, "views", classOf[Long])
    //A column called date whose value is a Joda DateTime
    val creationDate = column(meta, "date", classOf[DateTime])

    //A column called viewsArr whose value is a sequence of strings
    val viewsArr = column(meta,"viewsArr", classOf[Seq[String]])
    //A column called viewsMap whose value is a map of String to Long
    val viewsMap = column(meta,"viewsMap", classOf[Map[String,Long]])

    //A column family called views whose column names are Strings and values are Longs.  Can be treated as a Map
    val viewCounts = family[String, String, Long]("views")

    //A column family called views whose column names are YearDay instances and whose values are Longs
    val viewCountsByDay = family[String, YearDay, Long]("viewsByDay")

    //A column family called kittens whose column values are the custom Kitten type
    val kittens = family[String,String,Kitten]("kittens")

    val misc = family[String, String, Any]("misc")

    val misc1 = column(misc, "misc1", classOf[String])
    val misc2 = column(misc, "misc2", classOf[String])
    val misc3 = column(misc, "misc3", classOf[String])
  }

  class ExampleTableRow(table:ExampleTable,result:DeserializedResult) extends HRow[ExampleTable,String](result,table)

  //Register the table (DON'T FORGET TO DO THIS :) )
  val ExampleTable = table(new ExampleTable)

}
