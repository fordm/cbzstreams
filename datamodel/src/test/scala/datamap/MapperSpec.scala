package datamap

//import datamap.Mapper._
import datamap.Data1
import org.scalatest.freespec.AnyFreeSpec
import shapeless._
//import shapeless.labelled._
import shapeless.record._

class MapperSpec extends AnyFreeSpec {

  "a case class" - {
    "with a single string" - {
      "should map to the correct column" in {
        val requested = Data1("one")

        val gen: Generic.Aux[Data1, String :: HNil] = Generic[Data1]

        val lGen = LabelledGeneric[Data1]

        val genTo = lGen.to(requested)

        // val manualMapper: Mapper[Data1] = Mapper.instance((a: Data1) => List(Column("name", 1)))

        val m1: Mapper[HNil] = Mapper.hNilMapper
        val _ = m1

        val data: List[AnyRef] = List("foo".asInstanceOf[AnyRef])

        val dataObject: AnyRef = data.head
        val _ = dataObject
        val dataObjectH = data.head :: HNil
        val _ = dataObjectH

        val oneMap = genTo.toMap
        val _ = oneMap

        // val updated = genTo.updated

        val newDataH = "two" :: HNil

        // val newData = gen.from(newDataH)

        case class User(name: String, age: Int)
        val user = User("John", 25)
        val userHList: String :: Int :: HNil = Generic[User].to(user)
        val _ = assert(userHList == "John" :: 25 :: HNil)

        val newData1: Data1 = gen.from(newDataH)
        val _ = newData1


        //val mapper = Mapper[Data1].getCols(requested)
        //val caseClassColumns: List[Column] = mapper.getCols(requested)
        assert(false)
      }
    }
  }

}
object MapperSpec {

}
