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

import org.scalatest.funsuite.AnyFunSuite
import za.co.absa.spark.partition.sizing.types.DataTypeSizes.DefaultDataTypeSizes

class RecordSizerTest extends AnyFunSuite with DummyDatasets {

  test("test fromDataFrame") {
    assert(RecordSizer.fromDataFrame(simpleDf) < 80)
    assert(RecordSizer.fromDataFrame(arrayDf) < 170)
    assert(RecordSizer.fromDataFrame(structDf) < 140)
  }

  test("test deeper nested dataframe") {
    val inputDf = spark.read
      .schema(testCaseSchema)
      .json(getClass.getResource(nestedFilePath).getPath)
    assert(RecordSizer.fromDataFrame(inputDf) < 1500)
    assert(RecordSizer.fromDataFrame(inputDf.drop("array1", "array2")) < 1000)
  }

  test("test fromSchema") {
    assert(RecordSizer.fromSchema(simpleDf.schema)(DefaultDataTypeSizes) < 80)
    assert(RecordSizer.fromSchema(arrayDf.schema)(DefaultDataTypeSizes)  < 170)
    assert(RecordSizer.fromSchema(structDf.schema)(DefaultDataTypeSizes) < 140)
  }

  test("test deeper nested fromSchema") {
    val inputDf = spark.read
      .schema(testCaseSchema)
      .json(getClass.getResource(nestedFilePath).getPath)
    assert(RecordSizer.fromSchema(inputDf.schema)(DefaultDataTypeSizes) < 1500)
    assert(RecordSizer.fromSchema(inputDf.drop("array1", "array2").schema)(DefaultDataTypeSizes) < 1000)
  }


}
