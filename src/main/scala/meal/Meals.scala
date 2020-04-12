package meal

import java.nio.file.Paths
import java.time.LocalDate

import meal.FileMealsRepository.{AddError, ListError}

class Meals {

  private val mealsRepository = new FileMealsRepository(Paths.get("meals.txt"))

  def list(): Either[Seq[ListError], Seq[Meal]] = mealsRepository.list()

  def add(date: LocalDate, description: String): Either[AddError, Meal] = mealsRepository.add(Meal(date, description))

  def remove(date: LocalDate): Either[FileMealsRepository.RemoveError, Seq[Meal]] = mealsRepository.remove(date)

}

case class Meal(date: LocalDate, description: String)
