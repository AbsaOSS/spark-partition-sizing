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
import za.co.absa.spark.partition.sizing.utils.RowSizer

class RowSizerTest extends AnyFunSuite with DummyDatasets {

  test("Simple df") {
    assert(RowSizer.rowSize(simpleDf.first()) < 100)
    assert(RowSizer.rowSize(simpleDf.take(2).last) < 100)
  }

  test("Array df") {
    assert(RowSizer.rowSize(arrayDf.first()) < 120)
    assert(RowSizer.rowSize(arrayDf.take(2).last) < 250)
  }

  test("struct df") {
    assert(RowSizer.rowSize(structDf.first()) < 150)
    assert(RowSizer.rowSize(structDf.take(2).last) < 150)
  }
}
