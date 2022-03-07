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

import org.apache.spark.sql.types.{StringType, StructType}
import org.scalatest.funsuite.AnyFunSuite
import za.co.absa.spark.commons.test.SparkTestBase

class DataFramePartitionerTest extends AnyFunSuite with SparkTestBase {

  import DataFramePartitioner._

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
    val schema = new StructType()
      .add("not_important", StringType, nullable = true)
    val df = spark.read.schema(schema).json("src/test/resources/nested_data")

    val max2RecordsPerPart = df.repartitionByRecordCount(2)
    assertResult(4)(max2RecordsPerPart.rdd.partitions.length)
    val max4RecordsPerPArt = df.repartitionByRecordCount(4)
    assertResult(2)(max4RecordsPerPArt.rdd.partitions.length)
    val max4PlanSizePart = df.repartitionByPlanSize(None, Option(200))
    assertResult(1)(max4PlanSizePart.rdd.partitions.length)
  }

}
