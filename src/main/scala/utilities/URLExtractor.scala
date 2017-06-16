package utilities

import java.net.URL

/**
  * Created by user on 6/14/17.
  */
object URLExtractor {


    def extractUrls (text : String ) : scala.collection.mutable.Map [String,Int] = {

      var containedUrls =  scala.collection.mutable.Map [String,Int] ()

      val urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)"

      import java.util.regex.Pattern
      val pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE)

      val urlMatcher = pattern.matcher(text)

      while (urlMatcher.find) {
        containedUrls += (text.substring(urlMatcher.start(0), urlMatcher.end(0)) -> 1)
      }


      containedUrls
    }

  def extractUrls2 (text : String ) : String = {
    val urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)"

    var containedUrls : String = ""
    import java.util.regex.Pattern
    val pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE)

    val urlMatcher = pattern.matcher(text)

    while (urlMatcher.find) {
      containedUrls += text.substring(urlMatcher.start(0), urlMatcher.end(0)) + " "
    }

    containedUrls
  }

  def extractDomain (text : String ) : String = {

    val urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)"

    var containedUrls : String = ""
    import java.util.regex.Pattern
    val pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE)

    val urlMatcher = pattern.matcher(text)

    while (urlMatcher.find) {

      val gurl = new URL (text.substring(urlMatcher.start(0), urlMatcher.end(0)))

      containedUrls += gurl.getHost + " "
    }

    containedUrls

  }
}
