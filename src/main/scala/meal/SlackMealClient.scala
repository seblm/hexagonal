package meal

import com.slack.api.Slack
import com.slack.api.methods.SlackApiResponse
import com.slack.api.methods.request.chat.ChatPostMessageRequest
import com.slack.api.methods.request.users.UsersListRequest
import meal.SlackMealClient.{EnvironmentVariableIsNotDefined, SendError, SlackResponseError, UsernameNotFound}

import scala.jdk.CollectionConverters.ListHasAsScala
import scala.util.{Properties, Try}

class SlackMealClient {

  private val MEAL_SLACK_TOKEN = envOrNone("MEAL_SLACK_TOKEN")
  private val MEAL_SLACK_USER_NAME = envOrNone("MEAL_SLACK_USER_NAME")

  def send(message: String): Either[SendError, Unit] = {
    for {
      token <- MEAL_SLACK_TOKEN
      username <- MEAL_SLACK_USER_NAME
      slack = Slack.getInstance()
      methods = slack.methods(token)
      users <- Try(methods.usersList(UsersListRequest.builder().build())).toSlackEither
      userId <- users.getMembers.asScala.find(_.getName == username).toRight(UsernameNotFound(username)).map(_.getId())
      _ <- Try(methods.chatPostMessage(ChatPostMessageRequest.builder().channel(userId).text(message).build())).toSlackEither
    } yield ()
  }

  private def envOrNone(name: String): Either[EnvironmentVariableIsNotDefined, String] =
    Properties.envOrNone(name).toRight(EnvironmentVariableIsNotDefined(name))

  private implicit class TryToEither[T <: SlackApiResponse](response: Try[T]) {
    def toSlackEither: Either[SendError, T] = response.toEither.left.map(SlackResponseError.apply).flatMap {
      case response if response.isOk => Right(response)
      case errorResponse             => Left(SlackResponseError(errorResponse))
    }
  }

}

object SlackMealClient {

  sealed trait SendError

  final case class EnvironmentVariableIsNotDefined(name: String) extends SendError

  final case class SlackResponseError(message: String) extends SendError

  object SlackResponseError {

    def apply(response: SlackApiResponse): SlackResponseError =
      if (response.isOk)
        SlackResponseError(s"${response.getClass.getName} is not an error")
      else
        SlackResponseError(
          s"error: ${response.getError}, needed: ${response.getNeeded}, provided: ${response.getProvided}, warning: ${response.getWarning}"
        )

    def apply(throwable: Throwable): SlackResponseError =
      SlackResponseError(Option(throwable.getMessage).getOrElse(throwable.getClass.getName))

  }

  final case class UsernameNotFound(username: String) extends SendError

}
