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
lazy val spark3   = "3.2.2"

import SparkVersionAxis._
import com.github.sbt.jacoco.report.JacocoReportSettings

ThisBuild / scalaVersion := scala211
ThisBuild / crossScalaVersions := Seq(scala211, scala212)

ThisBuild / versionScheme := Some("early-semver")

lazy val commonSettings = Seq(
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"/*, "-Xfatal-warnings"*/), //TODO
  javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint"),
  Test / parallelExecution := false
)

releasePublishArtifactsAction := PgpKeys.publishSigned.value

lazy val jacocoReportCommonSettings: JacocoReportSettings = JacocoReportSettings(
  title = s"spark-partition-sizing Jacoco Report",
  formats = Seq(JacocoReportFormats.HTML, JacocoReportFormats.XML)
)

lazy val commonJacocoExcludes: Seq[String] = Seq(
  // exclude example
  //    "za.co.absa.spark.commons.utils.JsonUtils*", // class and related objects
  //    "za.co.absa.spark.commons.utils.ExplodeTools" // class only
)

lazy val `sparkPartitionSizing` = (projectMatrix in file("spark-partition-sizing"))
  .settings(commonSettings: _*)
  .settings(
    jacocoReportSettings := jacocoReportCommonSettings,
    jacocoExcludes := commonJacocoExcludes)
  .sparkRow(SparkVersionAxis(spark2), scalaVersions = Seq(scala211, scala212))
  .sparkRow(SparkVersionAxis(spark3), scalaVersions = Seq(scala212))