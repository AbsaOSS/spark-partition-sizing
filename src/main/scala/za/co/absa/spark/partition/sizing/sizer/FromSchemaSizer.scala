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
import org.apache.spark.sql.types.{ArrayType, DataType, StructType}
import za.co.absa.spark.partition.sizing.RecordSizer
import za.co.absa.spark.partition.sizing.types.{ByteSize, DataTypeSizes}

import scala.util.control.TailCalls.{TailRec, done, tailcall}

class FromSchemaSizer(implicit dataTypeSizes: DataTypeSizes) extends RecordSizer {
  private val zeroByteSize: ByteSize = 0

  def performRowSizing(df: DataFrame): ByteSize = {
    val schema = df.schema
    structSize(schema, 1, done(zeroByteSize)).result
  }

  private def structSize(struct: StructType, itemCount: Int, totalSoFar: TailRec[ByteSize])
                        (implicit dataTypeSizes: DataTypeSizes): TailRec[ByteSize] = {
    struct.fields.foldLeft(totalSoFar)((runningTotal, structField) =>
      tailcall(dataTypeSize(structField.dataType, itemCount, runningTotal)))
  }

  private def dataTypeSize(dataType: DataType, itemCount: Int, totalSoFar: TailRec[ByteSize])
                          (implicit dataTypeSizes: DataTypeSizes): TailRec[ByteSize] = {
    dataType match {
      case subStruct: StructType => tailcall(structSize(subStruct, itemCount, totalSoFar))
      case array: ArrayType => tailcall(dataTypeSize(array.elementType, dataTypeSizes.averageArraySize * itemCount, totalSoFar))
      case dataType => done(itemCount * dataTypeSizes(dataType) + totalSoFar.result)
    }
  }
}
