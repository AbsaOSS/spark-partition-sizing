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

  override def performSizing(df: DataFrame): ByteSize = {
    if(df.isEmpty) 0L
    else {
      val schemaNames = df.schema.map(_.name)

      val hasComplexTypes = df.schema.fields.exists(f => f.dataType.isInstanceOf[StructType] || f.dataType.isInstanceOf[ArrayType])
      if (hasComplexTypes) throw new IllegalArgumentException("Sizer not working with complex types")

      val summaries: Map[String, String] = df.summary("count").head().getValuesMap(schemaNames)
      val totalCounts: ByteSize = df.count()
      val existingPercentages: Map[String, Double] = summaries.mapValues(_.toDouble / totalCounts.toDouble)

      df.schema.fields.foldLeft(0.0)((runningTotal: Double, structField: StructField) => {
        val weightedTypes: Double = dataTypeSizes.typeSizes(structField.dataType) * existingPercentages(structField.name)
        runningTotal + weightedTypes
      }).toLong
    }

  }
}
