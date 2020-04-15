package meal.infrastrucutre

import scala.collection.mutable

class MemoryMessaging {

  val messages: mutable.Buffer[String] = mutable.ArrayBuffer()

  def send(message: String): Either[String, Unit] = {
    messages += message
    Right(())
  }

}
