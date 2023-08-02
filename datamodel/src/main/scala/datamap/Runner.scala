package datamap

import com.typesafe.scalalogging.Logger
import datamap.mapKdb._
import shapeless.Generic.Aux
import shapeless._

import scala.annotation.nowarn
import scala.reflect.runtime.universe._
//import shapeless.labelled.FieldType
/*
object csvMapper extends Poly1 {
  implicit def caseEncode[K <: Symbol, V](
                                           implicit e: CsvEncoder[V]
                                         ): Case.Aux[FieldType[K, V], FieldType[K, String]] =
    at[FieldType[K, V]](s => field[K](e.encode(s)))
}*/
object flattenPoly extends Poly1 {
  //implicit def genericCase[T]: Case.Aux[(T :: HNil), T] =
  //  at(position => position.head)
  implicit val tupleStringCase: Case.Aux[(String :: HNil), String] =
    at(position => position.head)
  implicit val tupleIntCase: Case.Aux[(Int :: HNil), Int] =
    at(position => position.head)
  implicit val defaultCase: Case.Aux[(_ :: HNil), HNil] =
    at(_ => HNil)
}
object Runner extends App {
  val log = Logger(this.getClass)
  log.info("start")


  val a = "one" :: HNil
  val b = 2 :: HNil
  val c = a ::: b
  log.info(s"c = $c")

  //val x = a.foldLeft()
  val data: List[AnyRef] = List("foo".asInstanceOf[AnyRef])

  @nowarn
  def getType[T: TypeTag](obj: T) = typeOf[T]

  log.info(s"data is $data")
  log.info(s"data type is ${getType(data)}")
  log.info(s"data element type is ${getType(data.head)}")

  val data2 = Data2("data b", 2)
  val genData2: Aux[Data2, String :: Int :: HNil] = Generic[Data2]
  val x: String :: Int :: HNil = genData2.to(data2)
  val mapKdb2: HList = data2.mapKdb

  log.info(s"mapKdb2 is $mapKdb2")

  // val flatMapKdb2 = mapKdb2.map(flattenPoly)

  //val p2 = genData2.
  //val revData2 = genData2.from(mapKdb2)



  log.info("complete")
}
