package datamap

import shapeless._
import shapeless.labelled._

object show {
  trait ShowEg[T] {
    def show(t: T): String
  }

  object ShowEg {

    implicit val showString: ShowEg[String] = new ShowEg[String] {
      def show(t: String) = t
    }

    implicit val showInt: ShowEg[Int] = new ShowEg[Int] {
      def show(t: Int) = t.toString
    }

    implicit def showList[A](implicit showA: ShowEg[A]): ShowEg[List[A]] = new ShowEg[List[A]] {
      def show(t: List[A]) = t.map(showA.show).mkString("List(", ", ", ")")
    }

    implicit def showGeneric[F, G](implicit gen: LabelledGeneric.Aux[F, G], sg: Lazy[ShowEg[G]]): ShowEg[F] =
      new ShowEg[F] {
        def show(f: F) = sg.value.show(gen.to(f))
      }

    implicit def showHNil: ShowEg[HNil] =
      new ShowEg[HNil] {
        def show(p: HNil): String = ""
      }

    implicit def showHCons[K <: Symbol, V, T <: HList]
    (implicit
     key: Witness.Aux[K],
     sv: Lazy[ShowEg[V]],
     st: Lazy[ShowEg[T]]
    ): ShowEg[FieldType[K, V] :: T] =
      new ShowEg[FieldType[K, V] :: T] {
        def show(p: FieldType[K, V] :: T): String = {
          val head = s"${key.value.name} = ${sv.value.show(p.head)}"
          val tail = st.value.show(p.tail)
          if (tail.isEmpty) head else s"$head, $tail"
        }
      }

    implicit def showCNil: ShowEg[CNil] =
      new ShowEg[CNil] {
        def show(p: CNil): String = ""
      }

    implicit def showCCons[K <: Symbol, V, T <: Coproduct]
    (implicit
     key: Witness.Aux[K],
     sv: Lazy[ShowEg[V]],
     st: Lazy[ShowEg[T]]
    ): ShowEg[FieldType[K, V] :+: T] =
      new ShowEg[FieldType[K, V] :+: T] {
        def show(c: FieldType[K, V] :+: T): String =
          c match {
            case Inl(l) => s"${key.value.name}(${sv.value.show(l)})"
            case Inr(r) => st.value.show(r)
          }
      }
  }

  implicit class ShowOps[T](x: T)(implicit showT: ShowEg[T]) {
    def show: String = showT.show(x)
  }
}

object show2 {
  // Show using TypeClass
  trait Show[T] {
    def show(t: T): String
  }

  object Show extends LabelledTypeClassCompanion[Show] {
    implicit def stringShow: Show[String] = new Show[String] {
      def show(t: String) = t
    }

    implicit def intShow: Show[Int] = new Show[Int] {
      def show(n: Int) = n.toString
    }

    object typeClass extends LabelledTypeClass[Show] {
      def emptyProduct = new Show[HNil] {
        def show(t: HNil) = ""
      }

      def product[F, T <: HList](name: String, sh: Show[F], st: Show[T]) = new Show[F :: T] {
        def show(ft: F :: T) = {
          val head = sh.show(ft.head)
          val tail = st.show(ft.tail)
          if (tail.isEmpty)
            s"$name = $head"
          else
            s"$name = $head, $tail"
        }
      }

      def emptyCoproduct = new Show[CNil] {
        def show(t: CNil) = ""
      }

      def coproduct[L, R <: Coproduct](name: String, sl: => Show[L], sr: => Show[R]) = new Show[L :+: R] {
        def show(lr: L :+: R) = lr match {
          case Inl(l) => s"$name(${sl.show(l)})"
          case Inr(r) => s"${sr.show(r)}"
        }
      }

      def project[F, G](instance: => Show[G], to: F => G, from: G => F) = new Show[F] {
        def show(f: F) = instance.show(to(f))
      }
    }
  }

  implicit class ShowOps[T](x: T)(implicit showT: Show[T]) {
    def show: String = showT.show(x)
  }
}