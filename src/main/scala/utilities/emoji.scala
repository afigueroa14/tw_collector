package utilities

import java.io.FileReader

/**
  * Created by user on 6/13/17.
  */


object Emoji {

  // Emoji Store Collection
  private var emojiData = scala.collection.mutable.Map[Int, String]()

  // Startup the Collection
  {
     // Load the Emoji File
     val cfgEmojiJson = getClass().getClassLoader().getResource("emoji.json")

     // request to load the emoji json file
     load(cfgEmojiJson.getFile)

  }

   def get(codePoint: Int): Option[String] = {
    if (emojiData.contains(codePoint)) emojiData.get(codePoint)
    else None
  }

   def get (item : Char ) : String = {
    val rst = get(item.toString.codePointAt(0) ) match {
      case Some(name) => "&" + name.replace (" ", "-").trim
      case None => ""
    }

    rst.mkString

  }


  def emcodes (str : String) : Seq [String] = for {
    item <- str
    name = get(item)
    if (! name.isEmpty)
  } yield name


  def encodesv2 (data : String) : String = {
      var aemoji = ""
      for (item <- data) {

        val vitem = get(item)
        if (!vitem.isEmpty) aemoji +=  vitem + " "

      }
      aemoji
  }

  //-------------------------------------------------------------------------------------------------------------------
  // Load the Emoji Json File
  //-------------------------------------------------------------------------------------------------------------------
  def load (fileName : String) : Unit = {

    import com.google.gson.Gson
    try {

      var ncode : String = ""
      val gson = new Gson
      val jsonElement =  gson.fromJson(new FileReader(fileName),classOf [ Array [emjson]])

      for (item <- jsonElement) {

        if (item.unified.contains("-")) item.unified = item.unified.substring(0,item.unified.indexOf("-"))

        // Add to the map
        emojiData += (Integer.parseInt(item.unified, 16) -> item.name)
      }


    } catch {
      case e: Exception => e.printStackTrace
    }

  }
}


