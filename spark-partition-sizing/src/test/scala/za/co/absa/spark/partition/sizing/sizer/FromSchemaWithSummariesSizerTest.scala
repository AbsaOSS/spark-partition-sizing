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

package za.co.absa.spark.partition.sizing.sizer

import org.scalatest.funsuite.AnyFunSuite
import za.co.absa.spark.partition.sizing.DummyDatasets
import za.co.absa.spark.partition.sizing.types.DataTypeSizes
import za.co.absa.spark.partition.sizing.types.DataTypeSizes.DefaultDataTypeSizes

class FromSchemaWithSummariesSizerTest extends AnyFunSuite with DummyDatasets {

  private implicit val defaultSizes: DataTypeSizes = DefaultDataTypeSizes

  test("test dummy dataframes") {
    assert(new FromSchemaWithSummariesSizer().performRowSizing(simpleDf) < 80)
    assert(new FromSchemaWithSummariesSizer().performRowSizing(simpleMultiDf) < 160)
    assertThrows[IllegalArgumentException](new FromSchemaWithSummariesSizer().performRowSizing(arrayDf))
    assertThrows[IllegalArgumentException](new FromSchemaWithSummariesSizer().performRowSizing(structDf))
  }

  test("test non-empty input has a positive size (non-empty) sample is generated from non-empty input)") {
    val sizer1 = new FromDataframeSampleSizer(1)
    // need to do a few retries because sampling is randomly imprecise in count:
    // sampling 1 out of 2 can lead to 0-sized sample!
    (0 to 10).foreach { _ =>
      assert(sizer1.performRowSizing(simpleDf) > 0)
    }
  }

}
