package utilities

import java.net.URL


/** Object URLExtractor Extracting from the String all URL
  *
  */
object URLExtractor {


    /** Method  Return a List with all the URL extract from the Text
    *
    *  @param text Contain the Text String which will extract all the URL
    */
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

  /** Method  Return a String List with all the URL extract from the Text
    *
    *  @param text Contain the Text String which will extract all the URL
    */
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


  /** Method  Return a String List with all the Domain extract from the Text - URL
    *
    *  @param text Contain the Text String which will extract all the URL
    */
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
