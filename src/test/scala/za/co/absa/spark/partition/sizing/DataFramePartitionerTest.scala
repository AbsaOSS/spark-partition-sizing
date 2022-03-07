/*
 * Copyright 2021 ABSA Group Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package za.co.absa.spark.partition.sizing

import org.apache.spark.sql.types
import org.apache.spark.sql.types.{ArrayType, IntegerType, LongType, StringType, StructField, StructType}
import org.scalatest.funsuite.AnyFunSuite
import za.co.absa.spark.commons.test.SparkTestBase

class DataFramePartitionerTest extends AnyFunSuite with SparkTestBase {

  import DataFramePartitioner._

  private val testCaseSchema = StructType(
    Array(
      StructField("id", LongType),
      StructField("key1", LongType),
      StructField("key2", LongType),
      StructField("struct1", StructType(Array(
        StructField("key3", IntegerType),
        StructField("key4", IntegerType)
      ))),
      StructField("struct2", StructType(Array(
        StructField("inner1", StructType(Array(
          StructField("key5", LongType),
          StructField("key6", LongType),
          StructField("skey1", StringType)
        )))
      ))),
      StructField("array1", ArrayType(StructType(Array(
        StructField("key7", LongType),
        StructField("key8", LongType),
        StructField("skey2", StringType)
      )))),
      StructField("array2", ArrayType(StructType(Array(
        StructField("key2", LongType),
        StructField("inner2", ArrayType(StructType(Array(
          StructField("key9", LongType),
          StructField("key10", LongType),
          StructField("struct3", StructType(Array(
            StructField("k1", IntegerType),
            StructField("k2", IntegerType)
          )))
        ))))
      ))))
    ))

  test("Empty dataset") {
    val schema = new StructType()
      .add("not_important", StringType, nullable = true)
    val df = spark.read.schema(schema).parquet("src/test/resources/data/empty")
    assertResult(0)(df.rdd.getNumPartitions)
    val result1 = df.repartitionByPlanSize(None, Option(2))

    val result2 = df.repartitionByRecordCount(5)
    assertResult(df)(result1)
    assertResult(df)(result2)
  }

  test("Small dataset") {
    val df = spark.read.schema(testCaseSchema).json("src/test/resources/nested_data")

    val max2RecordsPerPart = df.repartitionByRecordCount(2)
    assertResult(4)(max2RecordsPerPart.rdd.partitions.length)
    val max4RecordsPerPArt = df.repartitionByRecordCount(4)
    assertResult(2)(max4RecordsPerPArt.rdd.partitions.length)
    val max4PlanSizePart = df.repartitionByPlanSize(None, Option(200))
    assertResult(1)(max4PlanSizePart.rdd.partitions.length)
  }

}
