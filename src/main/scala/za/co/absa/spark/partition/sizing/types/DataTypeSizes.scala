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

package za.co.absa.spark.partition.sizing.types

import org.apache.spark.sql.types.{ByteType, _}
import org.apache.spark.util.SizeEstimator

case class DataTypeSizes(typeSizes: DataType => ByteSize, averageArraySize: Int) {
  def apply(dataType: DataType): ByteSize = {
    typeSizes(dataType)
  }

}

object DataTypeSizes {
  private def dataTypeSizes(provided: DataType): ByteSize = {
    provided match {
      case NullType => 0L
      case ByteType => 1L
      case BooleanType => 1L
      case ShortType => 2L
      case IntegerType => 4L
      case LongType => 8L
      case _ => SizeEstimator.estimate(provided)
    }
  }

  final val DefaultDataTypeSizes = new DataTypeSizes(dataTypeSizes, 12)
}


