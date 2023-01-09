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

import org.apache.spark.sql.types.{ArrayType, IntegerType, LongType, StringType, StructField, StructType}
import org.scalatest.funsuite.AnyFunSuite
import za.co.absa.commons.io.TempDirectory
import za.co.absa.spark.commons.test.SparkTestBase
import za.co.absa.spark.partition.sizing.sizer.{FromDataframeSampleSizer, FromDataframeSizer, FromSchemaSizer, FromSchemaWithSummariesSizer}
import za.co.absa.spark.partition.sizing.types.DataTypeSizes
import za.co.absa.spark.partition.sizing.types.DataTypeSizes.DefaultDataTypeSizes

class DataFramePartitionerTest extends AnyFunSuite with SparkTestBase with DummyDatasets {

  import DataFramePartitioner._

  private implicit val defaultSizes: DataTypeSizes = DefaultDataTypeSizes

  val fromDataframeRecordSizer = new FromDataframeSizer()
  val fromSchemaRecordSizer = new FromSchemaSizer()
  val fromDataframe2SampleSizer = new FromDataframeSampleSizer(2)
  val fromDataframe4SampleSizer = new FromDataframeSampleSizer(4)
  val fromDataframe6SampleSizer = new FromDataframeSampleSizer(6)
  val fromSchemaSummariesSizer = new FromSchemaWithSummariesSizer()

  test("Empty dataset") {
    val tempFolder = TempDirectory().asString
    val schema = new StructType()
      .add("not_important", StringType, nullable = true)
    val df = spark.read.schema(schema).parquet(tempFolder)
    assertResult(0)(df.rdd.getNumPartitions)
    val result1 = df.repartitionByPlanSize(None, Option(2))

    val result2 = df.repartitionByRecordCount(5)

    val result3 = df.repartitionByDesiredSize(fromDataframeRecordSizer)(None, Option(2))
    val result4 = df.repartitionByDesiredSize(fromSchemaRecordSizer)(None, Option(2))
    val result5 = df.repartitionByDesiredSize(fromDataframe2SampleSizer)(None, Option(2))
    val result6 = df.repartitionByDesiredSize(fromSchemaSummariesSizer)(None, Option(2))

    assertResult(df)(result1)
    assertResult(df)(result2)
    assertResult(df)(result3)
    assertResult(df)(result4)
    assertResult(df)(result5)
    assertResult(df)(result6)
  }

  test("Small nested dataset") {
    val df = readDfFromJsonWhenReady(nestedCaseSchema, nestedFilePath)

    val max2RecordsPerPart = df.repartitionByRecordCount(2)
    assertResult(4)(max2RecordsPerPart.rdd.partitions.length)
    val max4RecordsPerPArt = df.repartitionByRecordCount(4)
    assertResult(2)(max4RecordsPerPArt.rdd.partitions.length)

    val min = Some(200L)
    val max = Some(2000L)

    val result2 = df.repartitionByPlanSize(min, max)
    val result3 = df.repartitionByDesiredSize(fromDataframeRecordSizer)(min, max)
    val result4 = df.repartitionByDesiredSize(fromSchemaRecordSizer)(min, max)
    val result5 = df.repartitionByDesiredSize(fromDataframe2SampleSizer)(min, max)
    val result6 = df.repartitionByDesiredSize(fromDataframe4SampleSizer)(min, max)
    val result7 = df.repartitionByDesiredSize(fromDataframe6SampleSizer)(min, max)
    assertResult(2)(result2.rdd.partitions.length)
    assertResult(5)(result3.rdd.partitions.length)
    assertResult(2)(result4.rdd.partitions.length)
    assert(result5.rdd.partitions.length >= 1)
    assert(result6.rdd.partitions.length >= 1)
    assert(result7.rdd.partitions.length >= 1)

    assertThrows[IllegalArgumentException](df.repartitionByDesiredSize(fromSchemaSummariesSizer)(min, max))
  }


}
