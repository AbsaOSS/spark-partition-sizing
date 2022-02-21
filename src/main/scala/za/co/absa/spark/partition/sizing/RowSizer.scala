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

package za.co.absa.spark.partition.sizing

import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.util.SizeEstimator
import za.co.absa.spark.partition.sizing.types.ByteSize


object RowSizer {

  def rowSize(row: Row): ByteSize = {
    rowEstimate(row, 0)
  }

  private def rowEstimate(item: AnyRef, accumulator: ByteSize): ByteSize = {
    item match {
      case row: GenericRowWithSchema =>
        val fieldNames: Seq[String] = row.schema.fields.map(_.name)
        val values = row.getValuesMap[AnyRef](fieldNames).values
        values.foldLeft(accumulator) {(acc, item) => rowEstimate(item, acc)}
      case _ => accumulator + SizeEstimator.estimate(item)
    }
  }

}
