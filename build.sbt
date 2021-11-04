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
ThisBuild / name         := "spark-partition-sizer"

import Dependencies._

lazy val scala211 = "2.11.12"

ThisBuild / scalaVersion := scala211

lazy val root = (project in file("."))
  .settings(
    name := "spark-partition-sizer",
    libraryDependencies ++= dependencies,
    javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint"),
    assembly / mainClass := Some("za.co.absa.spark_partition_sizer.Application"),
    assembly / test := (Test / test).value,
    mergeStrategy,
    artifact in (Compile, assembly) := {
      val art = (artifact in (Compile, assembly)).value
      art.withClassifier(Some("assembly"))
    },
    addArtifact(artifact in (Compile, assembly), assembly)
  )

val mergeStrategy: Def.SettingsDefinition = assembly / assemblyMergeStrategy  := {
  case PathList("META-INF", _) => MergeStrategy.discard
  case "application.conf"      => MergeStrategy.concat
  case "reference.conf"        => MergeStrategy.concat
  case _                       => MergeStrategy.first
}
