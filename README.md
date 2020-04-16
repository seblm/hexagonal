### steps

 - create package `meal.domain`
 - extract trait `meal.domain.Messaging` over `SlackMealClient`
   - create trait `meal.domain.Messaging`
   - make `SlackMealClient` extends `meal.domain.Messaging`
   - add override modifier to send function
   - Alt-Enter to pull member up
 - change return type of `Messaging.send` from `Either[SendError, Unit]` to `Either[String, Unit]`
   - on `meal.domain.Messaging.send` ⌘+F6
   - on `SlackMealClient` need to map from `SendError` to `String`
   - extract for comprehension to `result` val
   - `.left.map { ... }`
   - run tests : side effects and no tests are broken
 - extract trait `domain.MealsRepository` over `FileMealsRepository`
   - create trait `meal.domain.MealsRepository`
   - make `FileMealsRepository` extends `meal.domain.MealsRepository`
   - add override modifier to all public functions
   - move `AddError`, `ListError` and `RemoveError` to `MealsRepository` companion object
 - introduce constructor parameters into `Meals`
   - adapt `MealsSuite` by changing `FunFixture[Meals]` to `FunFixture[(MemoryMessaging, Meals)]`
   - modify `MemoryMessaging` to implements `meal.domain.Messaging`
   - test send function

```scala
// add
assertEquals(messaging.messages.length, 1, "only one message should be sent")
assertEquals(messaging.messages.head, "tu as mangé le plat _lasagnes_ le 2020-04-12")

// remove
assert(messaging.messages.nonEmpty, "no message was sent")
assertEquals(messaging.messages.last, "2 repas supprimés pour le 2020-04-12")
```

 - create and dispatch to packages `meal.application`, `meal.domain` and `meal.infrastructure`
