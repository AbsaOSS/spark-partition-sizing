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

class FromDataframeSizerTest extends AnyFunSuite with DummyDatasets {

  test("test dummy dataframes") {
    assert(new FromDataframeSizer().performRowSizing(simpleDf) < 80)
    assert(new FromDataframeSizer().performRowSizing(arrayDf) < 170)
    assert(new FromDataframeSizer().performRowSizing(structDf) < 140)
  }

  test("test deeper nested dataframe") {
    val inputDf = readDfFromJsonWhenReady(nestedCaseSchema, nestedFilePath)
    assert(new FromDataframeSizer().performRowSizing(inputDf) < 1500)
    //the number of samples should be higher than 0
    assert(new FromDataframeSizer().performRowSizing(inputDf) > 0)
  }

}
