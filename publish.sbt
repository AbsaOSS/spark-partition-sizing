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

ThisBuild / organizationName := "ABSA Group Limited"
ThisBuild / organizationHomepage := Some(url("https://www.absa.africa"))
ThisBuild / scmInfo := Some(
  ScmInfo(
    browseUrl = url("https://github.com/AbsaOSS/spark-partition-sizing/tree/master"),
    connection = "scm:git:git://github.com/AbsaOSS/spark-partition-sizing.git",
    devConnection = "scm:git:ssh://github.com/AbsaOSS/spark-partition-sizing.git"
  )
)

ThisBuild / developers := List(
  Developer(
    id    = "AdrianOlosutean",
    name  = "Adrian Olosutean",
    email = "adrian.olosutean@absa.africa",
    url   = url("https://github.com/AdrianOlosutean")
  ),
  Developer(
    id    = "benedeki",
    name  = "David Benedeki",
    email = "david.benedeki@absa.africa",
    url   = url("https://github.com/benedeki")
  )
)

ThisBuild / homepage := Some(url("https://github.com/AbsaOSS/spark-partition-sizing"))
ThisBuild / description := "Tool setting partitioning for Spark"
ThisBuild / startYear := Some(2021)
ThisBuild / licenses += "Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0.txt")

ThisBuild / pomIncludeRepository := { _ => false }
ThisBuild / publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value) {
    Some("snapshots" at s"${nexus}content/repositories/snapshots")
  } else {
    Some("releases" at s"${nexus}service/local/staging/deploy/maven2")
  }
}
ThisBuild / publishMavenStyle := true
