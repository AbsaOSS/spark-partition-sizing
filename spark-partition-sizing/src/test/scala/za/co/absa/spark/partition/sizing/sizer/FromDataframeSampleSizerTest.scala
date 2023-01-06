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

import org.scalatest.concurrent.Eventually
import org.scalatest.funsuite.AnyFunSuite
import za.co.absa.spark.partition.sizing.DummyDatasets

class FromDataframeSampleSizerTest extends AnyFunSuite with DummyDatasets with Eventually {

  private val sizer = new FromDataframeSampleSizer(2)
  test("test dummy dataframes") {
    assert(sizer.performRowSizing(simpleDf) < 80)
    assert(sizer.performRowSizing(simpleDf) > 0)

    assert(sizer.performRowSizing(arrayDf) < 170)
    assert(sizer.performRowSizing(arrayDf) > 0)

    assert(sizer.performRowSizing(structDf) < 140)
    assert(sizer.performRowSizing(structDf) > 0)
  }

  test("test deeper nested dataframe") {
    readDfFromJsonWhenReadyAndThen(nestedCaseSchema, nestedFilePath) { inputDf =>
      assert(sizer.performRowSizing(inputDf) < 3000)
      //the number of samples should be higher than 0
      assert(sizer.performRowSizing(inputDf) > 0)
    }
  }

}
