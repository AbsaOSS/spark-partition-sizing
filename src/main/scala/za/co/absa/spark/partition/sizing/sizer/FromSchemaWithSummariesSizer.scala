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

package za.co.absa.spark.partition.sizing.sizer

import jdk.jfr.Experimental
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.types.{ArrayType, StructField, StructType}
import za.co.absa.spark.partition.sizing.RecordSizer
import za.co.absa.spark.partition.sizing.types.{ByteSize, DataTypeSizes}


@Experimental
class FromSchemaWithSummariesSizer(implicit dataTypeSizes: DataTypeSizes) extends RecordSizer {

  override def performRowSizing(df: DataFrame, dfRecordCount: Option[Long] = None): ByteSize = {
    if(df.isEmpty) 0L
    else {
      val totalCounts: ByteSize = dfRecordCount match {
        case Some(x) => x
        case None => df.count()
      }

      val hasComplexTypes = df.schema.fields.exists(f => f.dataType.isInstanceOf[StructType] || f.dataType.isInstanceOf[ArrayType])
      if (hasComplexTypes) throw new IllegalArgumentException("Sizer not working with complex types")

      val summary = df.summary("count")
      val summaries: Map[String, String] = summary.head().getValuesMap(summary.dtypes.map(_._1))
      val existingPercentages: Map[String, Double] = summaries.mapValues(_.toDouble / totalCounts.toDouble)

      df.schema.fields.foldLeft(0.0)((runningTotal: Double, structField: StructField) => {
        val weightedTypes: Double = dataTypeSizes.typeSizes(structField.dataType) * existingPercentages.getOrElse(structField.name, 0.0)
        runningTotal + weightedTypes
      }).toLong
    }

  }
}
