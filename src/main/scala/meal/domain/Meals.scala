package meal.domain

import java.time.LocalDate

import meal.domain.MealRepository.{AddError, ListError, RemoveError}
import org.slf4j.{Logger, LoggerFactory}

class Meals(mealsRepository: MealRepository, slackMealClient: Messaging) {

  private val logger: Logger = LoggerFactory.getLogger(getClass)

  def list(): Either[Seq[ListError], Seq[Meal]] = mealsRepository.list()

  def add(date: LocalDate, description: String): Either[AddError, Meal] = {
    val addResult = mealsRepository.add(Meal(date, description))

    val sendResult = addResult match {
      case Left(error) => slackMealClient.send(s"wow impossible d'ajouter le plat _${description}_ le $date: $error")
      case Right(_)    => slackMealClient.send(s"tu as mangé le plat _${description}_ le $date")
    }
    logger.debug(s"add: $sendResult")

    addResult
  }

  def remove(date: LocalDate): Either[RemoveError, Seq[Meal]] = {
    val removeResult = mealsRepository.remove(date)

    val sendResult = removeResult match {
      case Left(error)         => slackMealClient.send(s"wow impossible de supprimer le $date: $error")
      case Right(Nil)          => slackMealClient.send(s"aucun repas à supprimer pour le $date")
      case Right(_ :: Nil)     => slackMealClient.send(s"un repas supprimé pour le $date")
      case Right(removedMeals) => slackMealClient.send(s"${removedMeals.length} repas supprimés pour le $date")
    }
    logger.debug(s"remove: $sendResult")

    removeResult
  }

}

case class Meal(date: LocalDate, description: String)
