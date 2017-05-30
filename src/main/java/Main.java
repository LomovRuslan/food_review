import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.sql.SparkSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.List;


public class Main {
    private static Review review;
    private static final String PATH_TO_REVIEWS = "src/main/resources/Reviews.csv";
    private static final int DELIMITER = 1000;
    private static final int REPARTITION_NUMBER = 100;

    static {
        System.setProperty("log4j.configurationFile", "props/log4j.xml");
    }

    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        boolean isTranslate = Boolean.parseBoolean(args[0]);
        String apiKey = args[1];
        SparkSession spark = SparkSession
                .builder().master("local[*]")
                .appName("food_review")
                .getOrCreate();
        JavaSparkContext jsc = new JavaSparkContext(spark.sparkContext());
        JavaRDD<Review> allReviews = jsc.textFile(PATH_TO_REVIEWS).map((Function<String, Review>) s -> {
            String[] fields = s.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
            String Id = fields[0];
            String ProductId = fields[1];
            String UserId = fields[2];
            String ProfileName = fields[3];
            String HelpfulnessNumerator = fields[4];
            String HelpfulnessDenominator = fields[5];
            String Score = fields[6];
            String Time = fields[7];
            String Summary = fields[8];
            String Text = fields[9];
            if (!HelpfulnessNumerator.isEmpty() && !HelpfulnessDenominator.isEmpty() && !Score.isEmpty() && !Time.isEmpty())
                if (StringUtils.isNumeric(HelpfulnessNumerator) && StringUtils.isNumeric(HelpfulnessDenominator) && StringUtils.isNumeric(Score) && StringUtils.isNumeric(Time))
                    review = new Review(Long.parseLong(Id), ProductId, UserId, ProfileName, Integer.parseInt(HelpfulnessNumerator), Integer.parseInt(HelpfulnessDenominator), Integer.parseInt(Score), Long.parseLong(Time), Summary, Text);
            return review;
        });

//        ***************************************************************************************
        JavaRDD<String> activeUsers = allReviews.map((Function<Review, String>) Review::getProfileName);
        JavaPairRDD<String, Integer> mostActiveUsers = activeUsers
                .mapToPair(word -> new Tuple2<>(word, 1))
                .reduceByKey((a, b) -> a + b);
        JavaPairRDD<Integer, String> swappedMostActiveUsers = mostActiveUsers.mapToPair((PairFunction<Tuple2<String, Integer>, Integer, String>) Tuple2::swap).sortByKey(false);

        List<Tuple2<Integer, String>> listOfMostActiveUsers = swappedMostActiveUsers.take(DELIMITER);
        logger.log(Level.getLevel("VERIFY"), "**************************************************************************************");
        logger.log(Level.getLevel("VERIFY"), "**********************************Most active users:**********************************");
        for (Tuple2<Integer, String> tuple : listOfMostActiveUsers)
            logger.log(Level.getLevel("VERIFY"), "Number of occurrences: " + tuple._1() + ",  user name: " + tuple._2());

//        ***************************************************************************************
        JavaRDD<String> productIds = allReviews.map((Function<Review, String>) Review::getProductId);
        JavaPairRDD<String, Integer> mostCommentedFoodItems = productIds
                .mapToPair(word -> new Tuple2<>(word, 1))
                .reduceByKey((a, b) -> a + b);
        JavaPairRDD<Integer, String> swappedMostCommentedFoodItems = mostCommentedFoodItems.mapToPair((PairFunction<Tuple2<String, Integer>, Integer, String>) Tuple2::swap).sortByKey(false);

        List<Tuple2<Integer, String>> listOfMostCommentedFoodItems = swappedMostCommentedFoodItems.take(DELIMITER);
        logger.log(Level.getLevel("VERIFY"), "**************************************************************************************");
        logger.log(Level.getLevel("VERIFY"), "******************************Most commented food items:******************************");
        for (Tuple2<Integer, String> tuple : listOfMostCommentedFoodItems)
            logger.log(Level.getLevel("VERIFY"), "Number of occurrences: " + tuple._1() + ", item id: " + tuple._2());

//        ***************************************************************************************
        JavaRDD<String> allComments = allReviews.map((Function<Review, String>) Review::getText);
        JavaPairRDD<String, Integer> mostUsedWords = allComments
                .flatMap(s -> {
                    List<String> list = new ArrayList<>();
                    String alphaOnly = Jsoup.parse(s).text().replaceAll("[^a-zA-Z ]+", "");
                    for (String str : alphaOnly.split(" ")) {
                        String toLowerCase = str.toLowerCase();
                        if (str.toLowerCase().matches("\\b" + toLowerCase + "\\b"))
                            list.add(toLowerCase);
                    }
                    return list.iterator();
                })
                .mapToPair(word -> new Tuple2<>(word, 1))
                .reduceByKey((a, b) -> a + b);
        JavaPairRDD<Integer, String> swappedMostUsedWords = mostUsedWords.mapToPair((PairFunction<Tuple2<String, Integer>, Integer, String>) Tuple2::swap).sortByKey(false);

        List<Tuple2<Integer, String>> listOfMostUsedWords = swappedMostUsedWords.take(DELIMITER);
        logger.log(Level.getLevel("VERIFY"), "**************************************************************************************");
        logger.log(Level.getLevel("VERIFY"), "***************************Most used words in the comments:***************************");
        for (Tuple2<Integer, String> tuple : listOfMostUsedWords)
            logger.log(Level.getLevel("VERIFY"), "Number of occurrences: " + tuple._1() + ", word: " + tuple._2());

//        ***************************************************************************************
        Translation.startToTranslate(isTranslate, allReviews, DELIMITER, REPARTITION_NUMBER,apiKey);
    }
}