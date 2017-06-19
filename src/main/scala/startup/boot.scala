package startup


import com.typesafe.config.ConfigFactory
import org.apache.ignite.{IgniteCache, Ignition}
import org.apache.ignite.configuration.IgniteConfiguration
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}
import org.slf4j.LoggerFactory
import sun.misc.{Signal, SignalHandler}
import twitter4j.auth.AuthorizationFactory
import twitter4j.conf.ConfigurationBuilder
import utilities.Emoji
import utilities.URLExtractor._


/** Singleton Application Object Twitter Collector Application. Entry Point for the applicaiton
  *
  *  @author angel Figueroa Cruz
  *  @version 0.0.1
  *  @constructor
  */
object boot extends App  with SignalHandler {


  //--------------------------------------------------------------------------------------------------------------------
  // Calculate the number of processors
  //--------------------------------------------------------------------------------------------------------------------
  val procs = Runtime.getRuntime.availableProcessors

  //--------------------------------------------------------------------------------------------------------------------
  // method use for processing the signal
  //--------------------------------------------------------------------------------------------------------------------
  val SIGING = "INT"
  val SIGTERM = "TERM"
  Signal.handle(new Signal(SIGING), this)
  Signal.handle(new Signal(SIGTERM), this)

  //--------------------------------------------------------------------------------------------------------------------
  // Log
  //--------------------------------------------------------------------------------------------------------------------
  val logger = LoggerFactory. getLogger("boot")
  logger.info(s"Twitter Collector Version 1.0.0 No. of Processor ${procs}")

  //--------------------------------------------------------------------------------------------------------------------
  // For load the Configuration
  //--------------------------------------------------------------------------------------------------------------------
  val props = ConfigFactory.load("tw.collector.conf")
  logger.info(s"Twitter Config Master ${props.getString("spark.master")} Application Name ${props.getString("spark.app.name")} ")


  //--------------------------------------------------------------------------------------------------------------------
  // Setup the Spark Configuration
  //--------------------------------------------------------------------------------------------------------------------
  val config = new SparkConf().setMaster(props.getString("spark.master")).setAppName(props.getString("spark.app.name"))

  //--------------------------------------------------------------------------------------------------------------------
  // Ignite configuration. It will use for store results
  //--------------------------------------------------------------------------------------------------------------------
  val igniteCfg = new IgniteConfiguration
  val ignite = Ignition.start(igniteCfg)

  //--------------------------------------------------------------------------------------------------------------------
  // Spark Contents. Allows to extract info from the Twitter Streeam
  //--------------------------------------------------------------------------------------------------------------------
  val sparkContext = new SparkContext(config)
  sparkContext.setLogLevel("OFF")

  //--------------------------------------------------------------------------------------------------------------------
  // Stream the Context Frequency
  //--------------------------------------------------------------------------------------------------------------------
  val SCCTFactor = 5
  val streamingContext = new StreamingContext(sparkContext, Seconds(SCCTFactor))

  //--------------------------------------------------------------------------------------------------------------------
  // Twitter Security Information
  //--------------------------------------------------------------------------------------------------------------------
  val consumerKey = props.getString("twitter4j.oauth.consumerKey")
  val consumerSecret = props.getString("twitter4j.oauth.consumerSecret")
  val accessToken = props.getString("twitter4j.oauth.accessToken")
  val accessTokenSecret = props.getString("twitter4j.oauth.accessTokenSecret")

  val url = props.getString("twitter4j.url")

  val tw_conf = new ConfigurationBuilder().setOAuthAccessToken(accessToken)
    .setOAuthAccessTokenSecret(accessTokenSecret)
    .setOAuthConsumerKey(consumerKey)
    .setOAuthConsumerSecret(consumerSecret)
    .setStreamBaseURL(url)
    .setSiteStreamBaseURL(url)

  val tw_auth = AuthorizationFactory.getInstance(tw_conf.build())

  //--------------------------------------------------------------------------------------------------------------------
  // Twitter Stream
  //--------------------------------------------------------------------------------------------------------------------
  val tweetsStream = TwitterUtils.createStream(streamingContext, Some(tw_auth))
  val englishTweets = tweetsStream.filter(_.getLang() == "en")

  //--------------------------------------------------------------------------------------------------------------------
  // Store all the Totals
  //--------------------------------------------------------------------------------------------------------------------
  val TotalIgnite   = ignite.getOrCreateCache [String,Long]   ("Total")


  //--------------------------------------------------------------------------------------------------------------------
  // Total Tweets
  //--------------------------------------------------------------------------------------------------------------------
  englishTweets.foreachRDD(rdd => {
      StoreTotal ("TW",rdd.count())   // Store the Total TWitter
      StoreTotal ("TI",SCCTFactor)
  })



  //--------------------------------------------------------------------------------------------------------------------
  // Get the HashTags
  //--------------------------------------------------------------------------------------------------------------------
  val HashTagIgnite      = ignite.getOrCreateCache [String,Long] ("hashTag")
  val hashTagStream      = englishTweets.map(status => status.getText()).flatMap(status => status.split(" ")).filter(word => word.startsWith("#"))
  val topCounts60        = hashTagStream.map((_, 1)).reduceByKeyAndWindow(_ + _, Seconds(SCCTFactor)).map{case (topic, count) => (count, topic)}.transform(_.sortByKey(false))



  //--------------------------------------------------------------------------------------------------------------------
  // If one to Store on File
  //--------------------------------------------------------------------------------------------------------------------
  if (props.getBoolean("app.file.hashTag.save")) {
    topCounts60.saveAsTextFiles(props.getString("app.file.hashTag.filename"),props.getString("app.file.hashTag.fileext"))
  }

  //--------------------------------------------------------------------------------------------------------------------
  // Generate the HashTag into the Igine in order the REST Service can obtain the information to the customer
  //--------------------------------------------------------------------------------------------------------------------
  topCounts60.foreachRDD(rdd => {

    val topList = rdd.take (1000) // Ordered(1000)( Ordering[(Int,String)])
    var total : Long = 0l
    topList.foreach (item => {total = Store (HashTagIgnite,item)})

    //------------------------------------------------------------------------------------------------------------------
    // Push the information to the cache.
    //------------------------------------------------------------------------------------------------------------------
    StoreTotal ("HTAG", total)


  })


  //--------------------------------------------------------------------------------------------------------------------
  // Extract all te Emoji from the Tweeter Text
  //--------------------------------------------------------------------------------------------------------------------
  val EmojiTopIgnite            = ignite.getOrCreateCache [String,Long] ("emojiTop")
  val emojiStream               = englishTweets.map (str => Emoji.encodesv2(str.getText)).flatMap(status => status.split(" "))
  val emojiCounts60             = emojiStream.map((_, 1)).reduceByKeyAndWindow(_ + _, Seconds(SCCTFactor)).map{case (topic, count) => (count, topic)}.transform(_.sortByKey(false))

  //--------------------------------------------------------------------------------------------------------------------
  // If one to Store on File
  //--------------------------------------------------------------------------------------------------------------------
  if (props.getBoolean("app.file.emoji.save")) {
    emojiCounts60.saveAsTextFiles(props.getString("app.file.emoji.filename"),props.getString("app.file.emoji.fileext"))
  }

  //--------------------------------------------------------------------------------------------------------------------
  // Generate the HashTag into the Igine in order the REST Service can obtain the information to the customer
  //--------------------------------------------------------------------------------------------------------------------
  emojiCounts60.foreachRDD(rdd => {
    val topList = rdd.take (1000) // rdd.takeOrdered(100)( Ordering[(Int,String)])
    var total : Long = 0l

    topList.foreach (item => {total = Store (EmojiTopIgnite,item)})

    //------------------------------------------------------------------------------------------------------------------
    // Push the information to the cache.
    //------------------------------------------------------------------------------------------------------------------
    StoreTotal ("EMOJI", total)

  })



  //--------------------------------------------------------------------------------------------------------------------
  // Get the HashTags
  //--------------------------------------------------------------------------------------------------------------------
  val URLTopIgnite                 = ignite.getOrCreateCache [String,Long] ("URLTop")
  val URLinTW                      = englishTweets.map(status  => extractUrls2 (status.getText)).flatMap(status => status.split(" ")).filter(word => word.startsWith("https"))
  val URLinTWCounts60              = URLinTW.map((_, 1)).reduceByKeyAndWindow(_ + _, Seconds(SCCTFactor)).map{case (topic, count) => (count, topic)}.transform(_.sortByKey(false))

  //--------------------------------------------------------------------------------------------------------------------
  // If one to Store on File
  //--------------------------------------------------------------------------------------------------------------------
  if (props.getBoolean("app.file.url.save")) {
    URLinTWCounts60.saveAsTextFiles(props.getString("app.file.url.filename"),props.getString("app.file.url.fileext"))
  }

  //--------------------------------------------------------------------------------------------------------------------
  // Generate the HashTag into the Igine in order the REST Service can obtain the information to the customer
  //--------------------------------------------------------------------------------------------------------------------
  URLinTWCounts60.foreachRDD(rdd => {
    val topList = rdd.take (1000) // rdd.takeOrdered(100)( Ordering[(Int,String)])

    var total : Long = 0l
    topList.foreach (item => {total = Store (URLTopIgnite,item)})

    //------------------------------------------------------------------------------------------------------------------
    // Push the information to the cache.
    //------------------------------------------------------------------------------------------------------------------
    StoreTotal ("URL", total)

  })


  //--------------------------------------------------------------------------------------------------------------------
  // Domains
  //--------------------------------------------------------------------------------------------------------------------
  val DomainTopIgnite       = ignite.getOrCreateCache [String,Long] ("DomainTop")
  val DomainTW              = englishTweets.map(status  => extractDomain (status.getText)).flatMap(status => status.split(" "))
  val DomainTWCounts60      = DomainTW.map((_, 1)).reduceByKeyAndWindow(_ + _, Seconds(SCCTFactor)).map{case (topic, count) => (count, topic)}.transform(_.sortByKey(false))

  //--------------------------------------------------------------------------------------------------------------------
  // If one to Store on File
  //--------------------------------------------------------------------------------------------------------------------
  if (props.getBoolean("app.file.domain.save")) {
    DomainTWCounts60.saveAsTextFiles(props.getString("app.file.domain.filename"), props.getString("app.file.domain.fileext"))
  }

  //--------------------------------------------------------------------------------------------------------------------
  // Generate the HashTag into the Igine in order the REST Service can obtain the information to the customer
  //--------------------------------------------------------------------------------------------------------------------
    DomainTWCounts60.foreachRDD(rdd => {

      val topList = rdd.take (1000) // rdd.takeOrdered(100)( Ordering[(Int,String)])

      var total : Long = 0l
      topList.foreach (item => {total = Store (DomainTopIgnite,item)})

      //------------------------------------------------------------------------------------------------------------------
      // Push the information to the cache.
      //------------------------------------------------------------------------------------------------------------------
      StoreTotal ("DOMAIN", total)


  })


  //--------------------------------------------------------------------------------------------------------------------
  // Picture
  //--------------------------------------------------------------------------------------------------------------------
  val PhotoIgnite       = ignite.getOrCreateCache [String,Long] ("PhotoTag")
  val PhotoTW           = englishTweets.map(status  => extractDomain (status.getText)).flatMap(status => status.split(" ")).filter(word => (word.contains("instagram") || word.contains("pic.twitter.com")))
  val PhotoTWCounts60  = PhotoTW.map((_, 1)).reduceByKeyAndWindow(_ + _, Seconds(SCCTFactor)).map{case (topic, count) => (count, topic)}.transform(_.sortByKey(false))

  //--------------------------------------------------------------------------------------------------------------------
  // If one to Store on File
  //--------------------------------------------------------------------------------------------------------------------
  if (props.getBoolean("app.file.photo.save")) {
    PhotoTWCounts60.saveAsTextFiles(props.getString("app.file.photo.filename"), props.getString("app.file.photo.fileext"))
  }

  //--------------------------------------------------------------------------------------------------------------------
  // Generate the HashTag into the Igine in order the REST Service can obtain the information to the customer
  //--------------------------------------------------------------------------------------------------------------------
  PhotoTWCounts60.foreachRDD(rdd => {
    val topList = rdd.take (1000) // rdd.takeOrdered(100)( Ordering[(Int,String)])

    var total : Long = 0l
    topList.foreach (item => {total = Store (PhotoIgnite,item)})

    //------------------------------------------------------------------------------------------------------------------
    // Push the information to the cache.
    //------------------------------------------------------------------------------------------------------------------
    StoreTotal ("PHOTO", total)

  })


  //--------------------------------------------------------------------------------------------------------------------
  // Start the Engine
  //--------------------------------------------------------------------------------------------------------------------
  streamingContext.checkpoint("twCollectorCheckPoint")
  streamingContext.start() // Start the computation
  streamingContext.awaitTermination() // Wait for the computation to terminate


  /**  Method for Handling the Operating Signal - shutdown
    *
    *  @param signal the Signal which will recives when rquest to shutdown the system
    */
  override def handle(signal: Signal): Unit = {

    logger.info(s"Shutdown Twitter Collector...")
    // Wait for the Close of the Cluster
    streamingContext.stop(true,true)
  }


  /** Method  allows to Store the information on the Ignite Total Map
    *
    *  @param key the Ignite Data Key
    *  @param total the value , which contain the total
    */
  def StoreTotal (key : String , total  : Long ) : Unit = {

    try
      {
        // Check if Exits
        if (TotalIgnite.containsKey(key)) {
          val ctotal = TotalIgnite.get(key)
          TotalIgnite.replace(key, ctotal + total)
        } else TotalIgnite.put (key,total)

      } catch {
          case exp : Exception  => logger.error (s"Module StoreTotal Error ${exp.getMessage}")
      }
  }



  /** Method  allows to Store the information on the Ignite Map
    *
    *  @param igine the Ignite Data Set
    *  @param item the Item from the map that contain the information
    */
  def Store (igine : IgniteCache [String,Long] , item : (Int,String) ) : Long = {

    var total : Long = 0l

    try
    {

          if (! item._2.isEmpty ) {
            if (igine.containsKey(item._2)) {
              val old = igine.get(item._2)
              igine.replace(item._2,old + item._1 )
              total += old + item._1
            } else {
              igine.put (item._2,item._1)
              total +=  item._1
            }
          }

    } catch {
      case exp : Exception  => logger.error (s"Module Store Error ${exp.getMessage}")
    }

    // Return Value
    total
  }

}
