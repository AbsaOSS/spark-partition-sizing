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

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions.{avg, col}
import za.co.absa.spark.partition.sizing.DataframeSizer
import za.co.absa.spark.partition.sizing.types.ByteSize
import za.co.absa.spark.partition.sizing.utils.RowSizer

class FromDataframeSizer() extends DataframeSizer {
  override def totalSize(df: DataFrame): ByteSize = {
    import df.sparkSession.implicits._

    if(df.isEmpty) {
      0L
    } else {
      val dfWithAvg: DataFrame = df.map(RowSizer.rowSize).agg(avg(col("value")))
      dfWithAvg.collect().last.get(0).asInstanceOf[Number].longValue()
    }
  }

  override def performSizing(df: DataFrame): ByteSize = {
    1L
  }
}
