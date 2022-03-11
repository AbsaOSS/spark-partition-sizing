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
import za.co.absa.spark.partition.sizing.RecordSizer
import za.co.absa.spark.partition.sizing.types.ByteSize
import za.co.absa.spark.partition.sizing.utils.RowSizer

@Experimental
class FromDataframeSampleSizer(sampleSize: Int = 1) extends RecordSizer {

  override def performSizing(df: DataFrame): ByteSize = {
    if(df.isEmpty) {
      0L
    } else {
      val count = df.count()
      val howManyToTake: ByteSize = if (count > sampleSize) sampleSize else count

      val sampleSizes: Array[ByteSize] = df.sample(1.0 * howManyToTake / count)
        .limit(sampleSize)
        .collect()
        .map(RowSizer.rowSize)

      if(sampleSizes.isEmpty) {
        0L
      } else ceilDiv(sampleSizes.sum, sampleSizes.length)
    }
  }

  private def ceilDiv(dividend: ByteSize, divisor: Long): ByteSize = {
    dividend / divisor + (dividend % divisor match {
      case 0          => 0
      case x if x > 0 => 1
      case _          => -1
    })
  }
}
