import React from 'react';
import { Link } from 'react-router';

class QuestionComposite extends React.Component {
  render = () => {
    const questionId = this.props.params['questionId'];
    const subtitle = questionId != null ?
      <li><span>{questionId}</span></li> :
      <li><span>All</span></li>;

    return <div className="question-list-composite-container">
      <div className="question-list-composite">
        <div className="question-list-composite__header">
          <ol className="breadcrumb">
            <li><Link to="/questions" activeClassName="active">
              Questions</Link></li>
            {subtitle}
          </ol>
        </div>
        <div className="question-list-composite__content">
          {this.props.children}
        </div>
      </div>
    </div>
  }
}

export default QuestionComposite;