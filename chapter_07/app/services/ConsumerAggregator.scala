package services

/**
  * Created by denis on 12/4/16.
  */
class ConsumerAggregator(
    tagEventConsumer: TagEventConsumer,
    userEventConsumer: UserEventConsumer,
    logRecordConsumer: LogRecordConsumer,
    questionEventConsumer: QuestionEventConsumer,
    answerEventConsumer: AnswerEventConsumer)