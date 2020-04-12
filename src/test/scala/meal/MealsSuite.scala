package meal

import java.nio.file.{Files, Paths}
import java.time.LocalDate

import munit.FunSuite

import scala.util.Try

class MealsSuite extends FunSuite {

  private lazy val withMeals: FunFixture[Meals] = {
    val path = Paths.get("meals.txt")
    FunFixture[Meals](
      setup = _ => { Try(Files.delete(path)); new Meals() },
      teardown = _ => Try(Files.delete(path))
    )
  }

  withMeals.test("Meals should add a meal") { meals =>
    val date = LocalDate.parse("2020-04-12")
    val description = "lasagnes"
    val expected = Meal(date, description)

    val newMeal = meals.add(date, description)

    assertEquals(newMeal, Right(expected))
    assertEquals(meals.list(), Right(Seq(expected)))
  }

  withMeals.test("Meals should list meals") { meals =>
    val date1 = LocalDate.parse("2020-04-12")
    val description1 = "lasagnes"
    val Right(meal1) = meals.add(date1, description1)
    val date2 = LocalDate.parse("2020-04-10")
    val description2 = "jambon frites"
    val Right(meal2) = meals.add(date2, description2)
    val Right(meal3) = meals.add(date1, description1)

    val mealsList = meals.list()

    assertEquals(mealsList, Right(Seq(meal1, meal2, meal3)))
  }

  withMeals.test("Meals should delete all meals of a given date") { meals =>
    val date1 = LocalDate.parse("2020-04-12")
    val description1 = "lasagnes"
    val Right(meal1) = meals.add(date1, description1)
    val date2 = LocalDate.parse("2020-04-10")
    val description2 = "jambon frites"
    val Right(meal2) = meals.add(date2, description2)
    val Right(meal3) = meals.add(date1, description2)

    val mealsRemoved = meals.remove(date1)

    assertEquals(mealsRemoved, Right(Seq(meal1, meal3)))
    assertEquals(meals.list(), Right(Seq(meal2)))
  }

}
