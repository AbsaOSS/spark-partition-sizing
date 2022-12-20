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
ThisBuild / name         := "spark-partition-sizing"

lazy val scala211 = "2.11.12"
lazy val scala212 = "2.12.12"

import Dependencies._

ThisBuild / crossScalaVersions := Seq(scala211, scala212)
ThisBuild / scalaVersion := scala211

import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  releaseStepCommand("publishSigned"),
  setNextVersion,
  commitNextVersion,
  pushChanges
)

// mapping 2.11 -> Spark 2.4, 2.12 -> Spark 3.2; but sysProp SPARK_VERSION takes precedence
def sparkVersionForScala(scalaVersion: String): String = sys.props.get("SPARK_VERSION") match {
  case Some(version) => version
  case _ if scalaVersion.startsWith("2.11") => Versions.spark2
  case _ if scalaVersion.startsWith("2.12") => Versions.spark3
  case _ => throw new IllegalArgumentException("Only Scala 2.11 and 2.12 are currently supported.")
}

lazy val printSparkScalaVersion = taskKey[Unit]("Print Spark and Scala versions spark-commons is being built for.")
ThisBuild / printSparkScalaVersion := {
  val log = streams.value.log
  val sparkVersion = sparkVersionForScala(scalaVersion.value)
  log.info(s"Building with Scala ${scalaVersion.value}, Spark $sparkVersion")
}

(Compile / compile) := ((Compile / compile) dependsOn printSparkScalaVersion).value // printSparkScalaVersion is run with compile

libraryDependencies ++= {
  val sparkVersion = sparkVersionForScala(scalaVersion.value)
  dependencies(sparkVersion)
}

releasePublishArtifactsAction := PgpKeys.publishSigned.value

// JaCoCo code coverage
Test / jacocoReportSettings := JacocoReportSettings(
  title = s"spark-partition-sizing Jacoco Report - ${scalaVersion.value}",
  formats = Seq(JacocoReportFormats.HTML, JacocoReportFormats.XML)
)

// exclude example
Test / jacocoExcludes := Seq(
//  "za.co.absa.spark.partition.sizing.types.DataTypeSizes*", // class and related objects
//  "za.co.absa.spark.partition.sizing.DataFramePartitioner" // class only
)
