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

import org.apache.spark.sql.functions.{avg, col}
import org.apache.spark.sql.{DataFrame, Row, SaveMode, SparkSession, functions}
import org.apache.spark.sql.types._
import za.co.absa.spark.partition.sizing.types.DataTypeSizes

import scala.util.control.TailCalls.{TailRec, done, tailcall}
import za.co.absa.spark.partition.sizing.types._

/**
  * Estimate an average row size in bytes.
  */
object RecordSizer {
  private val zeroByteSize: ByteSize = 0

  def fromSchema(schema: StructType)(implicit dataTypeSizes: DataTypeSizes): ByteSize = {
    structSize(schema, 1, done(zeroByteSize)).result //nullability not taken into account
  }

  def fromDataFrameSample(df: DataFrame, sampleSize: Int): ByteSize = {
    val (rowsTotalSize, rowCount) = df.head(sampleSize) //TODO head could be skewed
      .foldLeft(zeroByteSize, 0L){case ((size, count), row) =>
        (size + RowSizer.rowSize(row), count + 1)
      }
    ceilDiv(rowsTotalSize,  rowCount)
  }

  def fromDataFrame(df: DataFrame): ByteSize = {
    import df.sparkSession.implicits._

    val dfWithAvg: DataFrame = df.map(RowSizer.rowSize).agg(avg(col("value")))
    dfWithAvg.collect().last.get(0).asInstanceOf[Number].longValue()
  }

  def fromDirectorySize(path: String): ByteSize = {
    ??? //TODO Issue #6
  }

  private def ceilDiv(dividend: ByteSize, divisor: Long): ByteSize = {
    dividend / divisor + (dividend % divisor match {
      case 0          => 0
      case x if x > 0 => 1
      case _          => -1
    })
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