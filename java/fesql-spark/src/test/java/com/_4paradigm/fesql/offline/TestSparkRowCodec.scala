package com._4paradigm.fesql.offline

import java.nio.ByteBuffer
import java.sql.Timestamp

import com._4paradigm.fesql.codec.{Row => NativeRow}
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
import org.scalatest.FunSuite
import sun.nio.ch.DirectBuffer

import scala.util.Random

class TestSparkRowCodec extends FunSuite {

  FeSqlLibrary.init()

  private val baseSchema = StructType(Seq(
    StructField("short", ShortType),
    StructField("int", IntegerType),
    StructField("long", LongType),
    StructField("float", FloatType),
    StructField("double", DoubleType),
    StructField("bool", BooleanType),
    StructField("timestamp", TimestampType)
  ))

  test("Test encode and decode primitive types") {
    testRow(
      Row(1.toShort, 1, 1L, 1.0f, 1.0, true, new Timestamp(1)),
      baseSchema)

    testRow(
      Row(0.toShort, 0, 0L, 0.0f, 0.0, false, new Timestamp(0)),
      baseSchema)
  }


  test("Test encode and decode with strings") {
    val withStrSchema = baseSchema.add("str1", StringType).add("str2", StringType)
    testRow(
      Row(1.toShort, 1, 1L, 1.0f, 1.0, true, new Timestamp(1), "1", "hello world"),
      withStrSchema)
  }


  test("Test encode and decode empty string") {
    val withStrSchema = baseSchema.add("str1", StringType).add("str2", StringType)
    testRow(
      Row(1.toShort, 1, 1L, 1.0f, 1.0, true, new Timestamp(1), "", " "),
      withStrSchema)
  }


  // TODO: support non-utf strings
  ignore("Test encode and decode non-utf string") {
    val schema = StructType(Seq(StructField("str", StringType)))
    val data1 = Array.fill[Char](512)(Random.nextInt().toChar)
    testRow(Row(String.valueOf(data1)), schema)

    val data2 = Array.fill[Char](2048)(Random.nextInt().toChar)
    testRow(Row(String.valueOf(data2)), schema)

    val data3 = Array.fill[Char](8192)(Random.nextInt().toChar)
    testRow(Row(String.valueOf(data3)), schema)
  }


  test("Test encode and decode special integer values") {
    val schema = StructType(Nil)
      .add("short", ShortType)
      .add("int", IntegerType)
      .add("long", LongType)

    testRow(Row(Short.MinValue, Int.MinValue, Long.MinValue), schema)
    testRow(Row(Short.MaxValue, Int.MaxValue, Long.MaxValue), schema)
  }


  test("Test encode and decode special float values") {
    val schema = StructType(Nil)
      .add("float", FloatType)
      .add("double", DoubleType)

    testRow(Row(Float.MinValue, Double.MinValue), schema)
    testRow(Row(Float.MaxValue, Double.MaxValue), schema)
    testRow(Row(Float.NaN, Double.NaN), schema)
    testRow(Row(Float.MinPositiveValue, Double.MinPositiveValue), schema)
    testRow(Row(Float.NegativeInfinity, Double.NegativeInfinity), schema)
    testRow(Row(Float.PositiveInfinity, Double.PositiveInfinity), schema)
  }


  test("Test encode and decode with schema slices") {
    val schema1 = StructType(Nil)
      .add("float", FloatType)
      .add("double", DoubleType)

    val schema2 = StructType(Nil)
      .add("boolean", BooleanType)
      .add("str", StringType)

    val schema3 = StructType(Nil)
      .add("long", LongType)
      .add("str2", StringType)
      .add("int", IntegerType)

    testRow(Row(3.14f, 2.0, false, "hello", 1024L, "world", 5),
      Array(schema1, schema2, schema3))
  }


  def testRow(row: Row, schemaSlices: Array[StructType]): Unit = {
    val bufferPool = new NativeBufferPool
    val codec = new SparkRowCodec(schemaSlices, bufferPool)

    val nativeRow = codec.encode(row, keepBuffer=false)

    val arr = Array.fill[Any](schemaSlices.map(_.size).sum)(null)
    codec.decode(nativeRow, arr)

    val res = Row.fromSeq(arr)

    codec.delete()
    nativeRow.delete()

    assert(res.equals(row),
      s"\nExpect: ${formatRow(row)}\nGet: ${formatRow(res)}")
  }

  def testRow(row: Row, schema: StructType): Unit = {
    testRow(row, Array(schema))
  }


  def formatRow(row: Row): String = {
    row.toSeq.map(x => {
      if (x == null) {
        "null"
      } else if (x.isInstanceOf[String]) {
        "\"" + x + "\":String:" + x.toString.length
      } else {
        x.toString + ":" + x.getClass.getSimpleName
      }
    }).mkString(", ")
  }
}
