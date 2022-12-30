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

  def majorVersion(fullVersion: String): String = {
    fullVersion.split("\\.", 2).headOption.getOrElse(fullVersion)
  }

  def dependencies(sparkVersion: String): Seq[ModuleID] = Seq(
    "org.apache.spark"    %% "spark-core"         % sparkVersion  % "provided",
    "org.apache.spark"    %% "spark-sql"          % sparkVersion  % "provided",
    "za.co.absa"          %% "spark-commons-test" % "0.2.0"       % Test,
    "org.scalatest"       %% "scalatest"          % "3.2.9"       % Test
  )

}
