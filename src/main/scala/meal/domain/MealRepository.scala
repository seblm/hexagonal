package meal.domain
import java.time.LocalDate

import meal.domain.MealRepository.{AddError, ListError, RemoveError}

trait MealRepository {

  def list(): Either[Seq[ListError], Seq[Meal]]

  def add(meal: Meal): Either[AddError, Meal]

  def remove(date: LocalDate): Either[RemoveError, Seq[Meal]]
}

object MealRepository {
  sealed trait AddError

  sealed trait ListError

  sealed trait RemoveError

  case class UnderlyingError(throwable: Throwable) extends AddError with ListError with RemoveError

  case class LineIsNotAMeal(line: String) extends ListError

  case class DateIsUnparseable(unparseableDate: String, throwable: Throwable) extends ListError

  case class ListErrorDuringRemove(errors: Seq[ListError]) extends RemoveError
}
