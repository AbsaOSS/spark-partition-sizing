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
import sbt.Keys._
import sbt.internal.ProjectMatrix
import Dependencies._
import JacocoSetup._
import com.github.sbt.jacoco.JacocoKeys.{jacocoExcludes, jacocoReportSettings}

case class SparkVersionAxis(sparkVersion: String) extends VirtualAxis.WeakAxis {
  val sparkVersionMinor: String = sparkVersion.split("\\.", 3).take(2).mkString(".")
  override val directorySuffix = s"-spark$sparkVersionMinor"
  override val idSuffix: String = directorySuffix.replaceAll("""\W+""", "_")
}

object SparkVersionAxis {
  private def camelCaseToLowerDashCase(origName: String): String = {
    origName
      .replaceAll("([A-Z])", "-$1")
      .toLowerCase()
  }

  implicit class ProjectExtension(val projectMatrix: ProjectMatrix) extends AnyVal {

    def sparkRow(sparkAxis: SparkVersionAxis, scalaVersions: Seq[String], settings: Def.SettingsDefinition*): ProjectMatrix = {
      val sparkVersion = sparkAxis.sparkVersion
      scalaVersions.foldLeft(projectMatrix)((currentProjectMatrix, scalaVersion) =>
        currentProjectMatrix.customRow(
          scalaVersions = Seq(scalaVersion),
          axisValues = Seq(sparkAxis, VirtualAxis.jvm),
          _.settings(
            moduleName := camelCaseToLowerDashCase(name.value + sparkAxis.directorySuffix),
            libraryDependencies ++= dependencies(sparkAxis.sparkVersion),
            jacocoReportSettings := jacocoSettings(sparkVersion, scalaVersion),
            jacocoExcludes := jacocoProjectExcludes(sparkVersion, scalaVersion)
          ).settings(settings: _*)
        )
      )
    }
  }
}
