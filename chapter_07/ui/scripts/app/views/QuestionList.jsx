import React from 'react';
import { connect } from 'react-redux';
import axios from 'axios';
import ConfirmationService from '../util/ConfirmationService.js';
import { Link } from 'react-router-dom';


class QuestionList extends React.Component {
  deleteQuestion = (id) => {
    return () => {
      ConfirmationService.showConfirmationDialog({
        title: 'Delete question',
        body: 'Are you sure you want to delete the question?'
      }, 'warning', () => {
        axios.post('/api/deleteQuestion', { id })
      });
    }
  };
  handleResponse = (response) => {
    if (response.status === 200) {
      this.props.dispatch({
        type: 'questions_updated',
        data: response.data
      });
    }
  };
  componentDidMount = () => {
    axios.get('/api/questions').then(this.handleResponse);
  };
  render = () => {
    const questions = this.props.questions;
    return <div className="question-list-container">
      <div className="question-list">
        {questions.map((question) => {
          return <div key={question.id}
                      className="question-list__one-question">
            <div className="question-list__one-question__tags">
              {question.tags.map((tag) => {
                return <span className="badge badge-primary"
                             key={tag.id}>{tag.text}</span>
              })}
            </div>
            <div className="question-list__one-question__title">
              <Link to={"/questions/" + question.id}>{question.title}</Link>
              <a href="#" onClick={this.deleteQuestion(question.id)}>
                <svg viewBox="0 0 24 24" fill="currentColor" height="1em" width="1em">
                  <path d="M20 6.91L17.09 4 12 9.09 6.91 4 4 6.91 9.09 12 4 17.09 6.91 20 12 14.91 17.09 20 20 17.09 14.91 12 20 6.91z" />
                </svg>
              </a>
            </div>
            <div className="question-list__one-question__author">
              {"by " + question.authorFullName}
            </div>
          </div>
        })}
      </div>
    </div>
  };
}

const mapStateToProps = (state) => {
  return {
    questions: state.questions
  }
};

export default connect(mapStateToProps)(QuestionList)
