package sentiment.mllib
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.mllib.classification.NaiveBayesModel
import org.apache.spark.mllib.feature.HashingTF
import org.apache.spark.mllib.linalg.Vector

object MLlibSentimentAnalyzer {

  /**
   * Predicts sentiment of the tweet text with Naive Bayes model passed after removing the stop words.
   *
   * @param text          -- Complete text of a tweet.
   * @param stopWordsList -- Broadcast variable for list of stop words to be removed from the tweets.
   * @param model         -- Naive Bayes Model of the trained data.
   * @return Int Sentiment of the tweet.
   */
  def computeSentiment(text: String, stopWordsList: Broadcast[List[String]], model: NaiveBayesModel): Int = {
    val tweetInWords: Seq[String] = getBarebonesTweetText(text, stopWordsList.value)
    val polarity = model.predict(MLlibSentimentAnalyzer.transformFeatures(tweetInWords))
//    println(polarity)
    normalizeMLlibSentiment(polarity)
  }

  def normalizeMLlibSentiment(sentiment: Double) = {
    sentiment match {
      case x if x == 0 => -1 // negative
      case x if x == 2 => 0 // neutral
      case x if x == 4 => 1 // positive
      case _ => 0 // if cant figure the sentiment, term it as neutral
    }
  }

  def getBarebonesTweetText(tweetText: String, stopWordsList: List[String]): Seq[String] = {
    //Remove URLs, RT, MT and other redundant chars / strings from the tweets.
    tweetText.toLowerCase()
      .replaceAll("\n", "")
      .replaceAll("rt\\s+", "")
      .replaceAll("\\s+@\\w+", "")
      .replaceAll("@\\w+", "")
      .replaceAll("\\s+#\\w+", "")
      .replaceAll("#\\w+", "")
      .replaceAll("(?:https?|http?)://[\\w/%.-]+", "")
      .replaceAll("(?:https?|http?)://[\\w/%.-]+\\s+", "")
      .replaceAll("(?:https?|http?)//[\\w/%.-]+\\s+", "")
      .replaceAll("(?:https?|http?)//[\\w/%.-]+", "")
      .split("\\W+")
      .filter(_.matches("^[a-zA-Z]+$"))
      .filter(!stopWordsList.contains(_))
    //.fold("")((a,b) => a.trim + " " + b.trim).trim
  }

  val hashingTF = new HashingTF()
  def transformFeatures(tweetText: Seq[String]): Vector = {
    hashingTF.transform(tweetText)
  }
}
