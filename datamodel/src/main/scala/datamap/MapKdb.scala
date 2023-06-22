package datamap

import shapeless._
//import shapeless.labelled._
object mapKdb {
  // MapKdb using TypeClass
  trait MapKdb[T] {
    def mapKdb(t: T): String
  }

  object MapKdb extends LabelledProductTypeClassCompanion[MapKdb] {
    implicit def stringMapKdb: MapKdb[String] = new MapKdb[String] {
      def mapKdb(t: String) = t
    }

    implicit def intMapKdb: MapKdb[Int] = new MapKdb[Int] {
      def mapKdb(n: Int) = n.toString
    }

    object typeClass extends LabelledProductTypeClass[MapKdb] {
      def emptyProduct = new MapKdb[HNil] {
        def mapKdb(t: HNil) = ""
      }

      def product[F, T <: HList](name: String, sh: MapKdb[F], st: MapKdb[T]) = new MapKdb[F :: T] {
        def mapKdb(ft: F :: T) = {
          val head = sh.mapKdb(ft.head)
          val tail = st.mapKdb(ft.tail)
          if (tail.isEmpty)
            s"$name = $head"
          else
            s"$name = $head, $tail"
        }
      }

      def project[F, G](instance: => MapKdb[G], to: F => G, from: G => F) = new MapKdb[F] {
        def mapKdb(f: F) = instance.mapKdb(to(f))
      }
    }
  }

  implicit class MapKdbOps[T](x: T)(implicit mapKdbT: MapKdb[T]) {
    def mapKdb: String = mapKdbT.mapKdb(x)
  }
}