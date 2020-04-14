package meal

import java.nio.file.Paths
import java.time.LocalDate

import meal.FileMealsRepository.{AddError, ListError, RemoveError}

class Meals {

  private val mealsRepository = new FileMealsRepository(Paths.get("meals.txt"))

  private val slackMealClient = new SlackMealClient()

  def list(): Either[Seq[ListError], Seq[Meal]] = mealsRepository.list()

  def add(date: LocalDate, description: String): Either[AddError, Meal] = {
    val addResult = mealsRepository.add(Meal(date, description))

    addResult match {
      case Left(error) => slackMealClient.send(s"wow impossible d'ajouter le plat _${description}_ le $date: $error")
      case Right(_)    => slackMealClient.send(s"tu as mangé le plat _${description}_ le $date")
    }

    addResult
  }

  def remove(date: LocalDate): Either[RemoveError, Seq[Meal]] = {
    val removeResult = mealsRepository.remove(date)

    removeResult match {
      case Left(error)         => slackMealClient.send(s"wow impossible de supprimer le $date: $error")
      case Right(Nil)          => slackMealClient.send(s"aucun repas à supprimer pour le $date")
      case Right(_ :: Nil)     => slackMealClient.send(s"un repas supprimé pour le $date")
      case Right(removedMeals) => slackMealClient.send(s"${removedMeals.length} repas supprimés pour le $date")
    }

    removeResult
  }

}

case class Meal(date: LocalDate, description: String)
