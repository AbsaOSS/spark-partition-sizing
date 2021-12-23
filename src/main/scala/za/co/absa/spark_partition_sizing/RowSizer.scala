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

package za.co.absa.spark_partition_sizing

import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.util.SizeEstimator
import za.co.absa.spark_partition_sizing.types.ByteSize


object RowSizer {

  def rowSize(row: Row)(implicit fieldNames: Seq[String]): ByteSize = {
    rowEstimate(row)
  }

  private def rowEstimate(row: Row)(implicit fieldNames: Seq[String]): Long = {
    row match {
      case r: GenericRowWithSchema => {
        val values = r.getValuesMap[AnyRef](fieldNames).values
        values.map {
          case struct: GenericRowWithSchema => rowEstimate(struct)(struct.schema.fields.map(_.name))
          case a => SizeEstimator.estimate(a)
        }.sum
      }
      case _ => SizeEstimator.estimate(row)
    }
  }

}
