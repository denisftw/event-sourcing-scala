import React from 'react';
import { NavLink, Switch, Route } from 'react-router-dom';
import QuestionListView from './QuestionList.jsx';
import QuestionDetailsView from './QuestionDetailsView.jsx';

class QuestionComposite extends React.Component {
  render = () => {
    const questionId = this.props.match.params['questionId'];
    const subtitle = questionId != null ?
      <li className="breadcrumb-item active"><span>{questionId}</span></li> :
      <li className="breadcrumb-item active"><span>All</span></li>;

    return <div className="question-list-composite-container">
      <div className="question-list-composite">
        <ol className="breadcrumb">
          <li className="breadcrumb-item">
            <NavLink to="/questions">Questions</NavLink>
          </li>
          {subtitle}
        </ol>
        <div className="question-list-composite__content">
          <Switch>
            <Route exact path="/questions" component={QuestionListView} />
            <Route path="/questions/:questionId" component={QuestionDetailsView} />
          </Switch>
        </div>
      </div>
    </div>
  }
}

export default QuestionComposite;