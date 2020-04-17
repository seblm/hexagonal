package meal.domain

import java.nio.file.Files
import java.time.LocalDate

import meal.infrastructure.FileMealsRepository
import meal.infrastrucutre.MemoryMessaging
import munit.FunSuite

import scala.util.Try

class MealsSuite extends FunSuite {

  private lazy val withMeals: FunFixture[(MemoryMessaging, Meals)] = {
    val path = Files.createTempFile("meals", ".txt")
    FunFixture[(MemoryMessaging, Meals)](
      setup = _ => {
        val messaging = new MemoryMessaging()
        (messaging, new Meals(new FileMealsRepository(path), messaging))
      },
      teardown = _ => Try(Files.delete(path))
    )
  }

  withMeals.test("Meals should add a meal") {
    case (messaging, meals) =>
      val date = LocalDate.parse("2020-04-12")
      val description = "lasagnes"
      val expected = Meal(date, description)

      val newMeal = meals.add(date, description)

      assertEquals(newMeal, Right(expected))
      assertEquals(meals.list(), Right(Seq(expected)))
      assertEquals(messaging.messages.length, 1, "only one message should be sent")
      assertEquals(messaging.messages.head, "tu as mangé le plat _lasagnes_ le 2020-04-12")
  }

  withMeals.test("Meals should list meals") {
    case (messaging, meals) =>
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

  withMeals.test("Meals should delete all meals of a given date") {
    case (messaging, meals) =>
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
      assert(messaging.messages.nonEmpty, "no message was sent")
      assertEquals(messaging.messages.last, "2 repas supprimés pour le 2020-04-12")
  }

}
