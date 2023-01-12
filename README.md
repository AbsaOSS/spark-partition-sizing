# spark-partition-sizing

Library for controlling the size of partitions when writing using Spark.

## Motivation
Sometimes partitions written by Spark are quite unequal(data skew), which makes further reading potentially problematic and processing inefficient.
`spark-partition-sizing` aims to reduce this problem by providing a number of utilities for achieving a more balanced partitioning.

## Usage

[![Build](https://github.com/AbsaOSS/spark-partition-sizing/workflows/Build/badge.svg)](https://github.com/AbsaOSS/spark-partition-sizing/actions)

sbt
```scala
libraryDependencies += "za.co.absa" %% "spark-partition-sizing" % "X.Y.Z"
```

### Scala 2.11 [![Maven Central](https://maven-badges.herokuapp.com/maven-central/za.co.absa/spark-partition-sizing_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/za.co.absa/spark-partition-sizing_2.11)

Maven
```xml
<dependency>
   <groupId>za.co.absa</groupId>
   <artifactId>spark-partition-sizing_2.11</artifactId>
   <version>${latest_version}</version>
</dependency>
```

### Scala 2.12 [![Maven Central](https://maven-badges.herokuapp.com/maven-central/za.co.absa/spark-partition-sizing_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/za.co.absa/spark-partition-sizing_2.12)

```xml
<dependency>
   <groupId>za.co.absa</groupId>
   <artifactId>spark-partition-sizing_2.12</artifactId>
   <version>${latest_version}</version>
</dependency>
```

### Building and testing
To build and test the package locally, run:
```
sbt clean test
```

### How to generate Code coverage report
```sbt
sbt jacoco
```
Code coverage will be generated on path:
```
{project-root}/target/scala-{scala_version}/jacoco/report/html
```

## Repartitioning

The goal of the `DataFramePartitioner` class is to offer new partitioning possibilities and other helping functions.

### repartitionByPlanSize

Repartitions the `DataFrame` so the partition size is between the provided _min_ and _max_ parameters, judged by the execution
plan, thus if the current partition size is not within the specified range, the dataframe will be repartitioned so that the 
block size will be within the range.

```scala
    import za.co.absa.spark.partition.sizing.DataFramePartitioner.DataFrameFunctions
    val minPartitionSizeInBytes = Some(1048576)//1mb
    val maxPartitionSizeInBytes = Some(2097152)//2mbs

    //specifying a range of values
    val repartitionedDfWithRange = df.repartitionByPlanSize(minPartitionSizeInBytes, maxPartitionSizeInBytes)
    //specifying a minimum partition size
    val repartitionedDfWithMax = df.repartitionByPlanSize(None, maxPartitionSizeInBytes)
    //specifying a maximum partition size
    val repartitionedDfWithMin = df.repartitionByPlanSize(minPartitionSizeInBytes, None)
```

### repartitionByRecordCount

Repartitions the `DataFrame` that each partition contains roughly the provided number of records

```scala
    import za.co.absa.spark.partition.sizing.DataFramePartitioner.DataFrameFunctions
    val targetNrOfRecordsPerPartition = 100
    val repartitionedDf = df.repartitionByRecordCount(targetNrOfRecordsPerPartition)
```

### repartitionByDesiredSize

Similarly to `repartitionByPlanSize`, it estimates the total size of the dataframe and checks if that estimation is within 
the specified _min_ and _max_ range. However, the difference is that other options for computing the estimated dataset size 
are available, as opposed the plan size which may not always give accurate results.
 Providing a way of estimating the total size is done through Sizers. These are the available sizers:

#### FromDataframeSizer

It estimates the size of each row and then sums the values, thus giving a better estimate of the data size
 and not needing an extra parameter. Its main drawback is that it can be slow, since all the data is being processed and
  may fail on deeply nested rows if the computing resources are limited.

```scala
    import za.co.absa.spark.partition.sizing.DataFramePartitioner.DataFrameFunctions
    import za.co.absa.spark.partition.sizing.sizer.FromDataframeSizer
    val minPartitionSizeInBytes = Some(1048576)//1mb
    val maxPartitionSizeInBytes = Some(2097152)//2mbs
    
    val sizer = new FromDataframeSizer()

    //specifying a range of values
    val repartitionedDfWithRange = df.repartitionByDesiredSize(sizer)(minPartitionSizeInBytes, maxPartitionSizeInBytes)
```

#### FromSchemaSizer

Estimate the row size based on a dataframe schema and the expected/typical field sizes. Its main advantage is that this approach is quite quick, since
 the data from the dataset will not be used and no action will run through the data. The accuracy of the estimation, however,
  is not likely to be high, since nullability or complex structures may not be so well estimated.
  It needs an implicit parameter for computing the data sizes, which can be a custom user provided one or the default DefaultDataTypeSizes.

```scala
    import za.co.absa.spark.partition.sizing.DataFramePartitioner.DataFrameFunctions
    import za.co.absa.spark.partition.sizing.sizer.FromSchemaSizer
    import za.co.absa.spark.partition.sizing.types.DataTypeSizes
    import za.co.absa.spark.partition.sizing.types.DataTypeSizes.DefaultDataTypeSizes
    val minPartitionSizeInBytes = Some(1048576)//1mb
    val maxPartitionSizeInBytes = Some(2097152)//2mbs
    
    implicit val defaultSizes: DataTypeSizes = DefaultDataTypeSizes
    
    val sizer = new FromSchemaSizer()

    //specifying a range of values
    val repartitionedDfWithRange = df.repartitionByDesiredSize(sizer)(minPartitionSizeInBytes, maxPartitionSizeInBytes)
```

#### FromSchemaWithSummariesSizer

Similarly to `FromSchemaSizer`, it uses the schema, but also takes the nullability into account by using the dataset summaries to 
compute what percentage of each column is null. Therefore, it will apply a weight(the percentage of non-null values) to each column's estimation.
This approach has the advantage of being quicker, but somewhat slower than `FromSchemaSizer`.
Its main limitation lies in the fact that it can only be used when all the columns of the dataset are primitive, non-nested values,
 the limitation being due to the Spark summary statistics.

```scala
    import za.co.absa.spark.partition.sizing.DataFramePartitioner.DataFrameFunctions
    import za.co.absa.spark.partition.sizing.sizer.FromSchemaWithSummariesSizer
    import za.co.absa.spark.partition.sizing.types.DataTypeSizes
    import za.co.absa.spark.partition.sizing.types.DataTypeSizes.DefaultDataTypeSizes
    val minPartitionSizeInBytes = Some(1048576)//1mb
    val maxPartitionSizeInBytes = Some(2097152)//2mbs
    
    implicit val defaultSizes: DataTypeSizes = DefaultDataTypeSizes
    
    val sizer = new FromSchemaWithSummariesSizer()

    //specifying a range of values
    val repartitionedDfWithRange = df.repartitionByDesiredSize(sizer)(minPartitionSizeInBytes, maxPartitionSizeInBytes)

```
#### FromDataframeSampleSizer

Estimate the data size based on a few random sample rows taken from the whole data. The number of samples can be specified by the user, otherwise the default would be 1.
This approach has the advantage of being quicker, since taking samples is not so costly as going through all the data,
 but the total estimated size will be dependent on the random samples, therefore a higher number of samples is likely to give a better estimate, but be costlier.

```scala
    import za.co.absa.spark.partition.sizing.DataFramePartitioner.DataFrameFunctions
    import za.co.absa.spark.partition.sizing.sizer.FromDataframeSampleSizer

    val numberOfSamples = 10 
    val minPartitionSizeInBytes = Some(1048576)//1mb
    val maxPartitionSizeInBytes = Some(2097152)//2mbs
    
    val sizer = new FromDataframeSampleSizer(numberOfSamples)

    //specifying a range of values
    val repartitionedDfWithRange = df.repartitionByDesiredSize(sizer)(minPartitionSizeInBytes, maxPartitionSizeInBytes)
```

## How to Release

Please see [this file](RELEASE.md) for more details.
