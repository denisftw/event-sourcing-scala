import React from 'react';
import axios from 'axios';
import { connect } from 'react-redux';

class QuestionDetailsView extends React.Component {
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
      </div>
    </div>
  };
}

const mapStateToProps = (state) => {
  return {
    questionThread: state.questionThread
  }
};

export default connect(mapStateToProps)(QuestionDetailsView)
