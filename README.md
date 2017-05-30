# food_review
analyze and transform the > 500.000 reviews from amazon

Specification:<br>
There is a distinct() method on spark rdd that returns all unique records.Can be applied if we want to make sure there is no duplicates.

Command to start a job:<br>
./bin/spark-submit \ <br>
  --class food_review.Main \ <br>
  --master spark://192.168.192.22:7077 \ <br>
  --executor-memory 512M \ <br>
  --driver-memory 512M \ <br>
  --deploy-mode cluster \ <br>
  --total-executor-cores 4 \ <br>
  /path/to/examples.jar \ <br>
  true  \ <br>
  yourGoogleAPIKEY
  
Specify the execution and driver memory, total executor cores, path to jar file, parameter for translation and google API_KEY.<br>
If 'true' than translate.


Steps:<br>
1.Add the Review.csv file to resource folder.<br>
2.mvn clean in the terminal.<br>
3.mvn install in the terminal.<br>
4.Specify the correct parameters for the command to start a job.<br>
5.Execute command<br>
