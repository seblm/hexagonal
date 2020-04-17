package meal.infrastrucutre

import meal.domain.Messaging

import scala.collection.mutable

class MemoryMessaging extends Messaging {

  val messages: mutable.Buffer[String] = mutable.ArrayBuffer()

  def send(message: String): Either[String, Unit] = {
    messages += message
    Right(())
  }

}
