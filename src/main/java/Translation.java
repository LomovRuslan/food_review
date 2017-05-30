import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.VoidFunction;
import org.jsoup.Jsoup;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.Iterator;

public class Translation implements Serializable {
    private static String QUERY = "https://translation.googleapis.com/language/translate/v2?key=";
    private static String TARGET_LANGUAGE = "fr";

    private static final HttpClient client = new HttpClient();

    static {
        System.setProperty("log4j.configurationFile", "props/log4j.xml");
    }

    private static final Logger logger = LogManager.getLogger(Translation.class);

    static void startToTranslate(boolean start, JavaRDD<Review> allReviews, int delimiter, int repartitionNumber, String apiKey) {
        if (start)
            startToTranslate(allReviews, delimiter, repartitionNumber, apiKey);
    }

    private static void startToTranslate(JavaRDD<Review> textFile, int delimiter, int repartitionNumber, String apiKey) {
        logger.log(Level.getLevel("VERIFY"), "**************************************************************************************");
        logger.log(Level.getLevel("VERIFY"), "**********************************Start to translate**********************************");
        JavaRDD<String> allCommentsToTranslate = textFile.map((Function<Review, String>) review -> Jsoup.parse(review.getText()).text().replaceAll("[*^-]", "").trim().replaceAll(" +", " "));
        //Defining the repartition number and call translation method asynchronously for each part
        allCommentsToTranslate.repartition(repartitionNumber).foreachPartitionAsync((VoidFunction<Iterator<String>>) stringIterator -> {
            while (stringIterator.hasNext()) {
                String str = stringIterator.next();
                if (str.length() <= delimiter) {
                    translate(str, TARGET_LANGUAGE, apiKey);
                } else {
                    for (String innerString : SubList.splitWithDelimiter(str, delimiter)) {
                        translate(innerString, TARGET_LANGUAGE, apiKey);
                    }
                }
            }
        });
        allCommentsToTranslate.collect();
    }

    private static void translate(String textToTranslate, String targetLanguage, String apiKey) {
        PostMethod method = new PostMethod(QUERY + apiKey);
        BufferedReader br;
        method.addParameter("q", textToTranslate);
        method.addParameter("target", targetLanguage);
        try {
            int returnCode = client.executeMethod(method);
            if (returnCode == HttpStatus.SC_NOT_IMPLEMENTED) {
                logger.log(Level.getLevel("Error"), "The Post method is not implemented by this URI");
                // still consume the response body
                method.getResponseBodyAsString();
            } else {
                logger.log(Level.getLevel("VERIFY"), "String to translate: " + textToTranslate);
                br = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));
                String readLine;
                while (((readLine = br.readLine()) != null)) {
                    logger.log(Level.getLevel("VERIFY"), "Translated string: " + readLine);
                }
            }
        } catch (Exception e) {
            logger.log(Level.getLevel("Error"), e);
        } finally {
            method.releaseConnection();
        }
    }
}
