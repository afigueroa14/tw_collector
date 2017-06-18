# tw_collector
Twitter Collector

The TWCR System is designed base on distributed architecture (Figure 1). There are two applications that compose the TWCR system. The first application (Twitter Collector) collects the Twitter Data, and second application (Twitter Rest) allows the user to query for the information. Both applications are connected using the Apache Ignite, which allows data to pass between different nodes on the Application Cluster. The Twitter Collector uses the SPARK Big Data Engine to manage the high volume of Twitter Stream.
<p align="center">
  <img src="https://github.com/afigueroa14/tw_collector/blob/master/src/main/resources/hw_tw.gif" height="500" width="650"/>
</p>