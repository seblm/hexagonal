package meal

import java.nio.file.StandardOpenOption.{APPEND, CREATE}
import java.nio.file.{Files, OpenOption, Path}
import java.time.LocalDate

import meal.FileMealsRepository._

import scala.Array.emptyByteArray
import scala.jdk.CollectionConverters.ListHasAsScala
import scala.util.Try

class FileMealsRepository(path: Path) {

  private val mealRegexp = """(\d+-\d{2}-\d{2})\t(.+)""".r

  def list(): Either[Seq[ListError], Seq[Meal]] = {
    val result = Files.readAllLines(path).asScala.toSeq.map {
      case mealRegexp(date, description) =>
        Try(LocalDate.parse(date)).toEither
          .map(d => Meal(d, description))
          .left
          .map(throwable => DateIsUnparseable(date, throwable))
      case line => Left[ListError, Meal](LineIsNotAMeal(line))
    }

    result.foldLeft[Either[Seq[ListError], Seq[Meal]]](Right(Seq.empty)) {
      case (Left(errors), Left(error))       => Left(errors :+ error)
      case (errors @ Left(_), Right(_))      => errors
      case (Right(_), Left(error))           => Left(Seq(error))
      case (values @ Right(_), Right(value)) => values.map(_ :+ value)
    }
  }

  def add(meal: Meal): Either[AddError, Meal] = write(Seq(meal), CREATE, APPEND).map(_.head)

  def remove(date: LocalDate): Either[RemoveError, Seq[Meal]] =
    list().fold[Either[RemoveError, Seq[Meal]]](
      errors => Left(ListErrorDuringRemove(errors)),
      meals => {
        val (mealsToBeRemoved, mealsNotRemoved) = meals.partition(_.date == date)
        write(mealsNotRemoved).map(_ => mealsToBeRemoved)
      }
    )

  private def write(meals: Seq[Meal], openOptions: OpenOption*): Either[UnderlyingError, Seq[Meal]] = {
    val bytes =
      if (meals.isEmpty) emptyByteArray
      else meals.map(meal => s"${meal.date}\t${meal.description}").mkString("", "\n", "\n").getBytes
    Try(Files.write(path, bytes, openOptions: _*)).map(_ => meals).toEither.left.map(UnderlyingError)
  }

}

object FileMealsRepository {

  sealed trait AddError

  sealed trait ListError

  sealed trait RemoveError

  case class UnderlyingError(throwable: Throwable) extends AddError with RemoveError

  case class LineIsNotAMeal(line: String) extends ListError

  case class DateIsUnparseable(unparseableDate: String, throwable: Throwable) extends ListError

  case class ListErrorDuringRemove(errors: Seq[ListError]) extends RemoveError

}
