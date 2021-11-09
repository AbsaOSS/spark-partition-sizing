# spark-partition-sizing

Sizing partitions in Spark.

## Repartitioning

The goal of the `DataFrameParitioner` class is to offer new partitioning possibilities and other helping functions.

### cacheIfNot

Cache `DataFrame` if it hasn't been yet cached. Get's rid of the annoying _Warning_ if trying to cache already cached
data.

### partitionsRecordCount

List of partitions each with their record count. 


### recordCount

Number of records in the `DataFrame`.

### repartitionByPlanSize

Repartitions the `DataFrame` so the partition size is between the provided _min_ and _max_, judged by the execution
plan (in average?).

### repartitionByRecordCount

Repartitionns the `DataFrame` that each partiong does not contain more the provided number of records (in average?). 

## Record sizing

The goal of the class `RecordSize` is to estimate the average size of a row/record in a data set.

### fromSchema

Estimate the row size based on a provided schema and and the expected/typical field sizes.

### fromDataFrame and fromDataFrameSample

Take the data in the provided `DataFrame` and get the row size based on its record - either based on a sample of data or
the whole set.

### fromDirectorSize

Compute the estimate row size from taking the directory size where the data are stored and the record count there.