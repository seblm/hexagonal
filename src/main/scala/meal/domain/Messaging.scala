package meal.domain

trait Messaging {

  def send(message: String): Either[String, Unit]

}
