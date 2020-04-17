package meal.application

import java.nio.file.Paths
import java.time.LocalDate

import meal.domain.Meals
import meal.infrastructure.{FileMealsRepository, SlackMealClient}

import scala.util.Try

object Application extends App {

  private val meals = new Meals(
    new FileMealsRepository(Paths.get("meals.txt")),
    new SlackMealClient()
  )

  args match {
    case Array("list")            => println(meals.list().fold(toString, toString))
    case Array("add", date, meal) => println(extract(date)(meals.add(_, meal)))
    case Array("remove", date)    => println(extract(date)(meals.remove))
    case Array(other)             => println(s"unknown command $other")
    case Array()                  => println("please specify a command")
  }

  private def extract[T](date: String)(f: LocalDate => T) =
    Try(LocalDate.parse(date)).fold(throwable => s"date '$date' is not valid: ${throwable.getMessage}", f)

  private def toString(seq: Seq[_]) = seq.mkString("\n")

}
