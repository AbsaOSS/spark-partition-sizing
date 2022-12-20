/*
 * Copyright 2021 ABSA Group Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import sbt._

object Dependencies {

  object Versions {
    val spark2 = "2.4.8"
    val spark3 = "3.2.3"

    val sparkCommonsTest = "0.2.0"
    val scalaTest = "3.2.9"
  }

  def sparkCore(sparkVersion: String) = "org.apache.spark"  %% "spark-core"         % sparkVersion  % Provided
  def sparkSql(sparkVersion: String)  = "org.apache.spark"  %% "spark-sql"          % sparkVersion  % Provided
  lazy val sparkCommon                = "za.co.absa"        %% "spark-commons-test" % Versions.sparkCommonsTest % Test
  lazy val scalaTest                  = "org.scalatest"     %% "scalatest"          % Versions.scalaTest % Test

  def dependencies(sparkVersion: String): Seq[ModuleID] = Seq(
    sparkCore(sparkVersion),
    sparkSql(sparkVersion),
    sparkCommon,
    scalaTest
  )

}
