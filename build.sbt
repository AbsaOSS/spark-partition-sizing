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

ThisBuild / organization := "za.co.absa"

lazy val scala211 = "2.11.12"
lazy val scala212 = "2.12.12"
lazy val spark2   = "2.4.7"
lazy val spark3_2 = "3.2.3"
lazy val spark3_3 = "3.3.1"

import SparkVersionAxis._

ThisBuild / scalaVersion := scala211
ThisBuild / crossScalaVersions := Seq(scala211, scala212)

ThisBuild / versionScheme := Some("early-semver")

lazy val commonSettings = Seq(
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"/*, "-Xfatal-warnings"*/),
  javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint"),
  Test / parallelExecution := false
)

releasePublishArtifactsAction := PgpKeys.publishSigned.value

lazy val parent = (project in file("."))
  .aggregate(sparkPartitionSizing.projectRefs: _*)
  .settings(
    name := "spark-partition-sizing",
    publish / skip := true
  )

lazy val `sparkPartitionSizing` = (projectMatrix in file("spark-partition-sizing"))
  .settings(commonSettings: _*)
  .sparkRow(SparkVersionAxis(spark2), scalaVersions = Seq(scala211, scala212))
  .sparkRow(SparkVersionAxis(spark3_2), scalaVersions = Seq(scala212))
  .sparkRow(SparkVersionAxis(spark3_3), scalaVersions = Seq(scala212))