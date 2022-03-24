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
ThisBuild / releaseCrossBuild := true

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

lazy val printSparkScalaVersion = taskKey[Unit]("Print Spark and Scala versions spark-commons is being built for.")
ThisBuild / printSparkScalaVersion := {
  val log = streams.value.log
  log.info(s"Building with Spark ${sparkVersion}, Scala ${scalaVersion.value}")
}

libraryDependencies ++= dependencies

releasePublishArtifactsAction := PgpKeys.publishSigned.value
