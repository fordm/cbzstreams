package datamap

case class Data1(name: String)

object Data1 {
  // todo mf add Validation
  def apply(data: List[List[AnyRef]]): Data1 = new Data1(data.head(0).asInstanceOf[String])
}
