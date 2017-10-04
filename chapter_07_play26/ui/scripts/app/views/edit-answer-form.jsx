import React from 'react';
import axios from 'axios';


class EditAnswerForm extends React.Component {
  constructor(props) {
    super(props);
    this.resetState();
  }
  resetState = () => {
    const text = this.props.maybeAnswer != null ?
      this.props.maybeAnswer.answerText : '';
    this.state = {
      answerText: text
    }
  };
  handleChange = (e) => {
    this.setState({
      answerText: e.target.value
    });
  };
  saveEntity = () => {
    const payload = {
      "questionId": this.props.questionId,
      "answerText" : this.state.answerText
    };
    console.log('payload: ', payload);
    if (this.props.maybeAnswer != null) {
      payload['answerId'] = this.props['maybeAnswer'].answerId;
      axios.post("/api/updateAnswer", payload).
      then(this.afterAnswerUpdated);
    } else {
      axios.post("/api/createAnswer", payload).
      then(this.afterAnswerUpdated);
    }
  };
  afterAnswerUpdated = (res) => {
    if (res.status == 200) {
      this.resetState();
      this.props.onAnswerUpdated();
    }
  };
  render = () => {
    const addButtonDisabled = this.state.answerText.length == 0;
    const saveAnswerButtonText = this.props.maybeAnswer != null ?
      'Update answer' : 'Create answer';
    return <div className="answer-edit-form">
      <h2>{saveAnswerButtonText}</h2>
      <div className="form-group">
        <textarea rows="10" name="text" onChange={this.handleChange}
                  placeholder="Enter text of your answer here"
                  value={this.state.answerText} className="form-control" />
      </div>
      <hr />
      <div className="form-group answer-edit-form__button-container">
        <button disabled={addButtonDisabled}
                className="btn btn-primary save-button"
                onClick={this.saveEntity}
                type="button">{saveAnswerButtonText}</button>
      </div>
    </div>
  }
}

export default EditAnswerForm;