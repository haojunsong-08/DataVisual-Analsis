// Databricks notebook source
// STARTER CODE - DO NOT EDIT THIS CELL
import org.apache.spark.sql.functions.desc
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import spark.implicits._
import org.apache.spark.sql.expressions.Window

// COMMAND ----------

// STARTER CODE - DO NOT EDIT THIS CELL
val customSchema = StructType(Array(StructField("lpep_pickup_datetime", StringType, true), StructField("lpep_dropoff_datetime", StringType, true), StructField("PULocationID", IntegerType, true), StructField("DOLocationID", IntegerType, true), StructField("passenger_count", IntegerType, true), StructField("trip_distance", FloatType, true), StructField("fare_amount", FloatType, true), StructField("payment_type", IntegerType, true)))

// COMMAND ----------

// STARTER CODE - YOU CAN LOAD ANY FILE WITH A SIMILAR SYNTAX.
val df = spark.read
   .format("com.databricks.spark.csv")
   .option("header", "true") // Use first line of all files as header
   .option("nullValue", "null")
   .schema(customSchema)
   .load("/FileStore/tables/nyc_tripdata.csv") // the csv file which you want to work with
   .withColumn("pickup_datetime", from_unixtime(unix_timestamp(col("lpep_pickup_datetime"), "MM/dd/yyyy HH:mm")))
   .withColumn("dropoff_datetime", from_unixtime(unix_timestamp(col("lpep_dropoff_datetime"), "MM/dd/yyyy HH:mm")))
   .drop($"lpep_pickup_datetime")
   .drop($"lpep_dropoff_datetime")

// COMMAND ----------

// LOAD THE "taxi_zone_lookup.csv" FILE SIMILARLY AS ABOVE. CAST ANY COLUMN TO APPROPRIATE DATA TYPE IF NECESSARY.

// ENTER THE CODE BELOW
val df_2 = spark.read
   .option("header", "true") // Use first line of all files as header
   .csv("/FileStore/tables/taxi_zone_lookup.csv") // the csv file which you want to work with
   .select(col("LocationID").cast("int").alias("LocationID"),col("Borough"),col("Zone"),col("service_zone"))

// COMMAND ----------

// STARTER CODE - DO NOT EDIT THIS CELL
// Some commands that you can use to see your dataframes and results of the operations. You can comment the df.show(5) and uncomment display(df) to see the data differently. You will find these two functions useful in reporting your results.
// display(df)
df.show(5) // view the first 5 rows of the dataframe

// COMMAND ----------

// STARTER CODE - DO NOT EDIT THIS CELL
// Filter the data to only keep the rows where "PULocationID" and the "DOLocationID" are different and the "trip_distance" is strictly greater than 2.0 (>2.0).

// VERY VERY IMPORTANT: ALL THE SUBSEQUENT OPERATIONS MUST BE PERFORMED ON THIS FILTERED DATA

val df_filter = df.filter($"PULocationID" =!= $"DOLocationID" && $"trip_distance" > 2.0)
df_filter.show(5)

// COMMAND ----------

// PART 1a: The top-5 most popular drop locations - "DOLocationID", sorted in descending order - if there is a tie, then one with lower "DOLocationID" gets listed first
// Output Schema: DOLocationID int, number_of_dropoffs int 

// Hint: Checkout the groupBy(), orderBy() and count() functions.
var q1a = df_filter.groupBy("DOLocationID").count().orderBy(col("count").desc, col("DOLocationID").asc).withColumnRenamed("count","number_of_dropoffs").limit(5)
// ENTER THE CODE BELOW
//q1a.show()

// COMMAND ----------

// PART 1b: The top-5 most popular pickup locations - "PULocationID", sorted in descending order - if there is a tie, then one with lower "PULocationID" gets listed first 
// Output Schema: PULocationID int, number_of_pickups int

// Hint: Code is very similar to part 1a above.
var q1b = df_filter.groupBy("PULocationID").count().orderBy(col("count").desc, col("PULocationID").asc).withColumnRenamed("count","number_of_pickups").limit(5)


// ENTER THE CODE BELOW


// COMMAND ----------

// PART 2: List the top-3 locations with the maximum overall activity, i.e. sum of all pickups and all dropoffs at that LocationID. In case of a tie, the lower LocationID gets listed first.
// Output Schema: LocationID int, number_activities int

// Hint: In order to get the result, you may need to perform a join operation between the two dataframes that you created in earlier parts (to come up with the sum of the number of pickups and dropoffs on each location). 

// ENTER THE CODE BELOW
var l_table = df_filter.groupBy("DOLocationID").count().orderBy(col("count").desc, col("DOLocationID").asc)
var r_table = df_filter.groupBy("PULocationID").count().orderBy(col("count").desc, col("PULocationID").asc).withColumnRenamed( "count" , "count2" )
var q2 = l_table.join(r_table , l_table("DOLocationID") === r_table("PULocationID"), "inner")
q2 = q2.withColumn("sum", l_table("count") + r_table("count2")).orderBy(col("sum").desc, col("PULocationID").asc).drop("count", "count2","PULocationID").withColumnRenamed( "DOLocationID" , "locationID" ).withColumnRenamed("sum","number_activities")
var q2_f = q2.limit(3)
//q2.show(5)

// COMMAND ----------

// PART 3: List all the boroughs in the order of having the highest to lowest number of activities (i.e. sum of all pickups and all dropoffs at that LocationID), along with the total number of activity counts for each borough in NYC during that entire period of time.
// Output Schema: Borough string, total_number_activities int

// Hint: You can use the dataframe obtained from the previous part, and will need to do the join with the 'taxi_zone_lookup' dataframe. Also, checkout the "agg" function applied to a grouped dataframe.

// ENTER THE CODE BELOW
var l_table3 = q2.withColumnRenamed("locationID","locationID2")
var combined_table = l_table3.join(df_2, l_table3("locationID2") === df_2("locationID"), "inner")
combined_table = combined_table.groupBy("Borough").sum("overall_activity").orderBy(col("sum(overall_activity)").desc).withColumnRenamed("sum(overall_activity)","total_number_activities")
//combined_table.show()

// COMMAND ----------

// PART 4: List the top 2 days of week with the largest number of (daily) average pickups, along with the values of average number of pickups on each of the two days. The day of week should be a string with its full name, for example, "Monday" - not a number 1 or "Mon" instead.
// Output Schema: day_of_week string, avg_count float

// Hint: You may need to group by the "date" (without time stamp - time in the day) first. Checkout "to_date" function.
var df_w_count = df_filter.withColumn("pickup_datetime", date_format(col("pickup_datetime"), "yyyy-MM-dd")).groupBy("pickup_datetime").count()
var df_w_weekday = df_w_count.withColumn("week_day", date_format(col("pickup_datetime"), "EEEE"))
var df_w_weekday_group = df_w_weekday.groupBy("week_day").avg("count").withColumnRenamed("avg(count)","avg_count").withColumnRenamed("week_day", "day_of_week").orderBy(col("avg_count").desc).limit(2)
// ENTER THE CODE BELOW
//df_w_weekday_group.show()

// COMMAND ----------

// PART 5: For each particular hour of a day (0 to 23, 0 being midnight) - in their order from 0 to 23, find the zone in Brooklyn borough with the LARGEST number of pickups. 
// Output Schema: hour_of_day int, zone string, max_count int

// Hint: You may need to use "Window" over hour of day, along with "group by" to find the MAXIMUM count of pickups

// ENTER THE CODE BELOW
var df_w_hr = df_filter.withColumn("pickup_datetime", date_format(col("pickup_datetime"),"H"))
var df_w_zone = df_w_hr.join(df_2, df_w_hr("PULocationID") === df_2("locationID"), "inner")
var df_w_group = df_w_zone.filter(col("Borough") === "Brooklyn")
var df_w_zone_group = df_w_group.groupBy("pickup_datetime","Zone").count()
var df_window = Window.partitionBy("pickup_datetime").orderBy(desc("count"))
df_w_zone_group = df_w_zone_group.withColumn("rank", rank().over(df_window))
df_w_zone_group = df_w_zone_group.select(col("pickup_datetime").cast("int").alias("pickup_datetime"), col("Zone"), col("count"),col("rank"))
df_w_zone_group = df_w_zone_group.filter(col("rank") === 1).orderBy(asc("pickup_datetime")).withColumnRenamed("pickup_datetime","hour_of_day").withColumnRenamed("count","max_count").drop("rank")
//df_w_zone_group.show()

// COMMAND ----------

// PART 6 - Find which 3 different days of the January, in Manhattan, saw the largest percentage increment in pickups compared to previous day, in the order from largest increment % to smallest increment %. 
// Print the day of month along with the percent CHANGE (can be negative), rounded to 2 decimal places, in number of pickups compared to previous day.
// Output Schema: day int, percent_change float


// Hint: You might need to use lag function, over a window ordered by day of month.

// ENTER THE CODE BELOW
var df_w_month = df_filter.withColumn("month", date_format(col("pickup_datetime"),"M"))
var df_w_man = df_w_month.join(df_2, df_w_month("PULocationID") === df_2("locationID"), "inner").filter(col("Borough") === "Manhattan").where(col("month") === "1").withColumn("day", date_format(col("pickup_datetime"), "d")).groupBy(col("day")).count()

df_w_man = df_w_man.select(col("day").cast("int").alias("day"), col("count"))
df_w_man = df_w_man.orderBy(asc("day"))

var df_w_window = Window.partitionBy().orderBy("day")

var df_w_lag = df_w_man.withColumn("value", lag("count",1).over(df_w_window))

df_w_lag =  df_w_lag.withColumn("percent_change", round(((col("count")-col("value"))/col("value")*100),2)).orderBy(desc("percent_change")).drop("count", "value").limit(3)


//df_w_lag.show()

// COMMAND ----------


