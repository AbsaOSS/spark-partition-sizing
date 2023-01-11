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

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.types.{ArrayType, IntegerType, LongType, StringType, StructField, StructType}
import org.scalatest.concurrent.Eventually
import za.co.absa.spark.commons.test.SparkTestBase

import scala.concurrent.duration.DurationInt
import scala.collection.immutable

trait DummyDatasets extends SparkTestBase {
  import spark.implicits._

  val simpleDfNames: immutable.Seq[String] = List("a", "b")
  val nestedDfNames: immutable.Seq[String] = simpleDfNames :+ "c"

  val simpleMultiDfNames: immutable.Seq[String] = simpleDfNames ++ List("c", "d")

  val simpleDf: DataFrame = List((1,"sds"), (5, "asfdbfnfgnfgg")).toDF(simpleDfNames: _*)
  val simpleMultiDf: DataFrame = List(
    (1, "", 1L, 5.0),
    (5, "asfdbfnfgnfgg", 60L, 0.0),
    (0, "bgfbfgbg", 4L, 0.0),
    (9, "c", 0L, 8.0)).toDF(simpleMultiDfNames: _*)
  val arrayDf: DataFrame = List((1,"sds", List()), (5, "asfdbfnfgnfgg", List(4,5,6,7,8))).toDF(nestedDfNames: _*)
  val structDf: DataFrame = List((1,"sds", (12,"zzzz")), (5, "asfdbfnfgnfgg", (55,""))).toDF(nestedDfNames: _*)

  val nestedFilePath = "/nested_data/nestedDf.json"

  protected val testCaseSchema: StructType = StructType(
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

  val nestedFilePath: String = "/nested_data/nestedDf.json"

  private def relativeToResourcePath(relativePath: String): String = getClass.getResource(relativePath).getFile

  /**
   * Reads dataframe from `nestedFilePath` with `nestedCaseSchema` schema
   * @return
   */

  /**
   * Read a testing DataFrame from a json by relative path with a schema
   * @param schema schema to use
   * @param jsonRelativePath relative path of a json file to load
   * @return df
   */
  def readDfFromJson(schema: StructType, jsonRelativePath: String): DataFrame = spark.read
    .schema(schema)
    .json(relativeToResourcePath(jsonRelativePath))

  /**
   * Same as za.co.absa.spark.partition.sizing.DummyDatasets#readNestedDf(), but makes sure that the data is ready (non-empty)
   *
   * @return
   */
  def readDfFromJsonWhenReady(schema: StructType, jsonRelativePath: String): DataFrame = {
    eventually(timeout(scaled(2.seconds)), interval(scaled(50.millis))) {
      val df = readDfFromJson(schema, jsonRelativePath)
      assert(!df.isEmpty, "input should not be empty") // eventually will retry on this if empty
      df
    }
  }

}
