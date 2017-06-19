package utilities

import java.io.FileReader

import org.slf4j.LoggerFactory


/** Object {Emoji Class} use for processing all aspect of the emoji
  *
  *  @constructor create a new emjson for extracting the emoji data
  */

object Emoji {

  //--------------------------------------------------------------------------------------------------------------------
  // Log
  //--------------------------------------------------------------------------------------------------------------------
  val logger = LoggerFactory. getLogger("Emoji")

  // Emoji Store Collection
  // Int Contain the Emoji Code
  // String Contain the EmojiName
  private var emojiData = scala.collection.mutable.Map[Int, String]()

  // Constructor for the singleton object
  {
     // Load the Emoji File
     val cfgEmojiJson = getClass().getClassLoader().getResource("emoji.json")

     // request to load the emoji json file
     load(cfgEmojiJson.getFile)

  }


  /** Method  Get the Emoji Name from the Emoji Code (Unicode)
    *
    *  @param codePoint Codepoint for the emoji
    */
   def get(codePoint: Int): Option[String] = {
    if (emojiData.contains(codePoint)) emojiData.get(codePoint)
    else None
  }

  /** Method  Get the Emoji Name from the Emoji Char
    *
    *  @param item Codepoint for the emoji in char
    */
   def get (item : Char ) : String = {
    val rst = get(item.toString.codePointAt(0) ) match {
      case Some(name) => "&" + name.replace (" ", "-").trim
      case None => ""
    }

    rst.mkString

  }


  /** Method  For a String Create a List of all Emoji Names
    *
    *  @param str String with all Emoji Names
    */
  def emcodes (str : String) : Seq [String] = for {
        item <- str
        name = get(item)
        if (! name.isEmpty)
      } yield name



  /** Method  For a String Create a String of all Emoji Names
    *
    *  @param data String with all Emoji Names
    */
  def encodesv2 (data : String) : String = {
      var aemoji = ""

      try
      {
        if (data.length > 0) {
          for (item <- data) {
            val vitem = get(item)
            if (!vitem.isEmpty) aemoji +=  vitem + " "
          }
        }

      } catch {
        case exp : Exception  => logger.error (s"Module encodesv2 Error ${exp.getMessage}")
      }

      aemoji
  }

  /** Method  For Loading the EMOJI JSON and Create a MAP with the Code,Name
    *
    *  @param fileName File which contain all the Emoji in JSON
    */
  def load (fileName : String) : Unit = {

    import com.google.gson.Gson
    try {

      logger.info(s"Loading Emoji File ${fileName}")

      var ncode : String = ""
      val gson = new Gson
      val jsonElement =  gson.fromJson(new FileReader(fileName),classOf [ Array [emjson]])

      for (item <- jsonElement) {

        if (item.unified.contains("-")) item.unified = item.unified.substring(0,item.unified.indexOf("-"))

        // Add to the map
        emojiData += (Integer.parseInt(item.unified, 16) -> item.name)

        // display the emoji
        logger.info(s"Name ${item.unified} = ${item.name}")
      }

    } catch {
      case e: Exception => e.printStackTrace
    }

  }
}


