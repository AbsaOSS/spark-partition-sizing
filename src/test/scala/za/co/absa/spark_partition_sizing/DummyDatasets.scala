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

package za.co.absa.spark_partition_sizing

import za.co.absa.spark.commons.test.SparkTestBase

import scala.collection.immutable

trait DummyDatasets extends SparkTestBase {
  import spark.implicits._

  val simpleDfNames: immutable.Seq[String] = List("a", "b")
  val nestedDfNames: immutable.Seq[String] = simpleDfNames :+ "c"

  val simpleDf = List((1,"sds"), (5, "asfdbfnfgnfgg")).toDF(simpleDfNames: _*)
  val arrayDf = List((1,"sds", List()), (5, "asfdbfnfgnfgg", List(4,5,6,7,8))).toDF(nestedDfNames: _*)
  val structDf = List((1,"sds", (12,"zzzz")), (5, "asfdbfnfgnfgg", (55,""))).toDF(nestedDfNames: _*)

}
