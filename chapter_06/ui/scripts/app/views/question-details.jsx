import React from 'react';
import axios from 'axios';
import { connect } from 'react-redux';

class QuestionDetailsView extends React.Component {
  componentDidMount = () => {
    axios.get("/api/questions").then(this.handleResponse);
  };
  handleResponse = (response) => {
    const questionId = this.props.params['questionId'];
    if (response.status == 200) {
      const maybeIndex = response.data.findIndex((q) => {
        return q.id == questionId;
      });
      if (maybeIndex != -1) {
        this.props.dispatch({
          type: 'question_thread_loaded',
          data: response.data[maybeIndex]
        });
      }
    }
  };
  render = () => {
    if (this.props.questionThread == null || this.props.questionThread.id == null) {
      return <div className="question-thread-view-form">
        <div className="question-thread-view-form__body">
          <div className="question-thread-view-form__loading">Loading...</div>
        </div>
      </div>
    }
    const question = this.props.questionThread;
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
