package datamap

import shapeless._
//import shapeless.labelled._

trait Mapper[A] {
  def fromKdb(flipY: List[List[AnyRef]]): HList
}
object Mapper {
  def apply[A](implicit mapper: Mapper[A]): Mapper[A] = mapper
  def instance[A](f: List[List[AnyRef]] => HList): Mapper[A] =
    new Mapper[A] {
      def fromKdb(flipY: List[List[AnyRef]]): HList = f(flipY)
    }

  implicit def hNilMapper: Mapper[HNil] = instance(_ => HNil)


  /*def process(flipY: List[List[AnyRef]], column: Int, build: HList): HList = {
    val head = build.head

  }*/

}
