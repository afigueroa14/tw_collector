package test

import utilities.Emoji

/**
  * Created by user on 6/13/17.
  */
object boot_emoji extends App {


  val code = 0x1f5378

  println (s"Emoji Name ${Emoji.get(code)}")

  val emname =  Emoji.get(code) match {
    case Some(emname) => emname
    case None => "NA"
  }

  println (s"Emoji Name ${emname} ")


  "\uD83D\uDE0B Get Emoji — List of all Emojis to ✂ Copy and \uD83D\uDCCB Paste \uD83D\uDC4C".foreach(item => {

    val emname =  Emoji.get(item.toString.codePointAt(0) ) match {
      case Some(emname) => "&" + emname.replace (" ", "-")
      case None => ""
    }

    if (! emname.isEmpty ) println (s"Emoji Name ${emname} ")

  })

  val s = "\uD83D\uDE0B Get Emoji — List of all Emojis to ✂ Copy and \uD83D\uDCCB Paste \uD83D\uDC4C"

  val sr = s.flatMap(c => ("." + c))

  sr.foreach(c => {print (s" ${c}")})





  val all = "\uD83D\uDE0B Get Emoji — List of all Emojis to ✂ Copy and \uD83D\uDCCB Paste \uD83D\uDC4C".foreach(item => {

    val emname =  Emoji.get(item.toString.codePointAt(0) ) match {
      case Some(emname) => "&" + emname.replace (" ", "-")
      case None => ""
    }

    if (! emname.isEmpty ) emname

  })

  println (s" All --->${all}")


  val s2 = "\uD83D\u2702 Get Emoji — List of all ✂  Emojis to ✂ Copy and \uD83D\uDCCB Paste \uD83D\uDC4C"

  val sr2 = s2.map (_.toString).filter(item => {

    true
  })

  println (s"\n\n Print --- ${sr2}")

  for (item <- s2) {

    println (s"Data ---> ${item} UnideCode ${item.toString.codePointAt(0)}--> ${Emoji.get(item)}")
  }

  val s4 = "\uD83D\uDE03 Smileys & People"

  println (s" Codes ${Emoji.emcodes(s2)}")


  // map((_, 1)).reduceByKeyAndWindow(_ + _, Seconds(60))
  val rst = Emoji.emcodes(s2).map((_, 1))


  println (s" rst ${rst} ")

  val rst2 = Emoji.emcodes(s2).groupBy(_.toString).map (x => (x._1,x._2.size))

  println (s" rst ${rst2} ")



  println (s" New Version 2 ${Emoji.encodesv2(s2)} ")



}
