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
import za.co.absa.spark.commons.test.SparkTestBase

import scala.collection.immutable

class RecordSizerTest extends AnyFunSuite with SparkTestBase {

  import spark.implicits._

  test("Simple df") {
    val values = List((1,"sds"), (5, "asfdbfnfgnfgg"))
    implicit val colNames: immutable.Seq[String] = List("a", "b")
    val df = values.toDF(colNames: _*)

    assert(RowSizer.rowSize(df.first()) < 100)
    assert(RowSizer.rowSize(df.take(2).last) < 100)
  }

  test("Array df") {
    val values = List((1,"sds", List()), (5, "asfdbfnfgnfgg", List(4,5,6,7,8)))
    val colNames: immutable.Seq[String] = List("a", "b", "c")
    val df = values.toDF(colNames: _*)

    assert(RowSizer.rowSize(df.first()) < 120)
    assert(RowSizer.rowSize(df.take(2).last) < 250)

  }

  test("struct df") {
    val values = List((1,"sds", (12,"zzzz")), (5, "asfdbfnfgnfgg", (55,"")))
    val colNames: immutable.Seq[String] = List("a", "b", "c")
    val df = values.toDF(colNames: _*)

    assert(RowSizer.rowSize(df.first()) < 150)
    assert(RowSizer.rowSize(df.take(2).last) < 150)

  }
}
