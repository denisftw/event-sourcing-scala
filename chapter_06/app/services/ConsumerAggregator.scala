package services

/**
  * Created by denis on 12/4/16.
  */
class ConsumerAggregator(
    tagEventConsumer: TagEventConsumer,
    logRecordConsumer: LogRecordConsumer,
    userEventConsumer: UserEventConsumer,
    questionEventConsumer: QuestionEventConsumer,
    answerEventConsumer: AnswerEventConsumer)