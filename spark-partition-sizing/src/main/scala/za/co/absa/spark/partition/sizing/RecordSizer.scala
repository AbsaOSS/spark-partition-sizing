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

import org.apache.spark.sql.DataFrame
import za.co.absa.spark.partition.sizing.types._

/**
  * Estimate an average row size in bytes.
  */
trait RecordSizer {
  def performRowSizing(df: DataFrame, dfRecordCount: Option[Long] = None): ByteSize
}

trait DataframeSizer extends RecordSizer {
  def totalSize(df: DataFrame): ByteSize
}
