import React from 'react';
import axios from 'axios';
import { connect } from 'react-redux';
import Modal from 'react-modal';
import EditAnswerForm from './edit-answer-form.jsx';
import ConfirmationService from '../util/confirmation-service.js';
import moment from 'moment';

class QuestionDetailsView extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      editAnswerIsOpen: false
    }
  }
  componentDidMount = () => {
    const questionId = this.props.params['questionId'];
    axios.get(`/api/questionThread/${questionId}`).then(this.handleResponse);
  };
  handleResponse = (response) => {
    if (response.status == 200) {
      this.props.dispatch({
        type: 'question_thread_loaded',
        data: response.data
      });
    }
  };
  render = () => {
    if (this.props.questionThread == null ||
      this.props.questionThread.question == null) {
      return <div className="question-thread-view-form">
        <div className="question-thread-view-form__body">
          <div className="question-thread-view-form__loading">Loading...</div>
        </div>
      </div>
    }
    const question = this.props.questionThread.question;
    console.log('questionThread', this.props.questionThread);

    // User
    const userIdHolder = document.getElementById('data-user-id-holder');
    const maybeUserId =  (userIdHolder != null) ?
      userIdHolder.getAttribute('data-user-id') : null;
    const userNotLoggedIn = maybeUserId == null;

    // Answers
    const answers = this.props.questionThread.answers;
    const answerInd = answers.findIndex((answer) => {
      return answer.authorId == maybeUserId;
    });
    const answerExists = answerInd != -1;
    const maybeAnswer = answerExists ? answers[answerInd] : null;
    const editAnswerText = answerExists ? 'Edit answer' : 'Add answer';

    const answerEditStyle = {
      content: {
        maxWidth: "600px",
        margin: "0 auto",
        height: "400px",
        position: "relative"
      }
    };

    return <div className="question-thread-view-form">
      <div className="question-thread-view-form__body">
        <div className="question-thread-view-form__tags">
          {question.tags.map((tag) => {
            return <span className="label label-default" key={tag.id}>{tag.text}</span>
          })}
        </div>
        <div className="question-thread-view-form__title">
          {question.title}
        </div>
        <div className="question-thread-view-form__details">
          {question.details}
        </div>
        <div className="question-thread-view-form__controls">
          <button className="btn btn-default save-button"
                  disabled={userNotLoggedIn}
                  onClick={this.openEditAnswerForm}
                  type="button">{editAnswerText}</button>
        </div>
      </div>
      <div className="question-thread-view-form__answers">
        {answers.map((answer) => {
          const updatedDate = moment(answer.updated).format('DD/MM/YYYY');
          const upvotes = answer.upvotes;
          const upvoteButtonDisabled = !maybeUserId ||
            answer.authorId == maybeUserId;
          const deleteButtonDisabled = !maybeUserId ||
            answer.authorId != maybeUserId;
          const alreadyUpvoted = !!maybeUserId && upvotes.findIndex((id) => {
              return id == maybeUserId; }) != -1;
          const upvoteButton = alreadyUpvoted ?
            <button type="button" className="btn btn-default"
                onClick={this.downvoteAnswer(question.id, answer.answerId)}
                disabled={upvoteButtonDisabled}>Downvote</button> :
            <button type="button"  className="btn btn-default"
                onClick={this.upvoteAnswer(question.id, answer.answerId)}
                disabled={upvoteButtonDisabled}>Upvote</button>;
          const authorName = answer.authorFullName;
          const answerWritten =
            `Answer written by ${authorName}, last updated on ${updatedDate}`;
          return <div className="question-thread-view-form__answer-one"
                      key={answer.answerId}>
            <div className="question-thread-view-form__answer-one__author-date">
              {answerWritten}
            </div>
            <div className="question-thread-view-form__answer-one__text">
              {answer.answerText}
            </div>
            <div className="question-thread-view-form__answer-one__button">
              <div className="button-container">
                <div className="btn-group btn-group-xs" role="group">
                  {upvoteButton}
                  <button type="button" disabled="disabled"
                          className="btn btn-default button-stat-indicator">
                    {upvotes.length}</button>
                </div>
              </div>
              <div className="button-container">
                <button className="btn btn-default btn-xs"
                        onClick={this.deleteAnswer(question.id, answer.answerId)}
                        disabled={deleteButtonDisabled}>Delete
                </button>
              </div>
            </div>
          </div>
        })}
      </div>
      <Modal
        isOpen={this.state.editAnswerIsOpen}
        onRequestClose={this.closeEditAnswerForm}
        style={answerEditStyle}
        contentLabel="Edit answer">
        <EditAnswerForm maybeAnswer={maybeAnswer} questionId={question.id}
             onAnswerUpdated={this.closeEditAnswerForm}/>
      </Modal>
    </div>
  };
  openEditAnswerForm = () => {
    this.setState({
      editAnswerIsOpen: true
    })
  };
  closeEditAnswerForm = () => {
    this.setState({
      editAnswerIsOpen: false
    })
  };
  deleteAnswer = (questionId, answerId) => {
    return () => {
      ConfirmationService.showConfirmationDialog({
        title: 'Delete answer',
        body: 'Are you sure you want to delete the answer?'
      }, 'warning', () => {
        axios.post("/api/deleteAnswer", {
          "answerId" : answerId,
          "questionId": questionId
        })
      });
    }
  };
  upvoteAnswer = (questionId, answerId) => {
    return () => {
      axios.post("/api/upvoteAnswer", {
        "answerId" : answerId,
        "questionId": questionId
      })
    }
  };
  downvoteAnswer = (questionId, answerId) => {
    return () => {
      axios.post("/api/downvoteAnswer", {
        "answerId" : answerId,
        "questionId": questionId
      })
    }
  };
}

const mapStateToProps = (state) => {
  return {
    questionThread: state.questionThread
  }
};

export default connect(mapStateToProps)(QuestionDetailsView)
