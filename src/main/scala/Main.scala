import asdf.TestCls
import utils.EntityUtils

object Main {

  def main(args: Array[String]): Unit = {
    val entity = EntityUtils.createEntity[List[TestCls]]
    println(s"--->entity: $entity")
  }


}