import React from 'react';
import axios from 'axios';
import Modal from 'react-modal';


const answerEditStyle = {
  content: {
    maxWidth: '600px',
    margin: '0 auto',
    position: 'relative'
  }
};

class EditAnswerForm extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      isOpen: false,
      answerText: '',
    }
  }
  resetState = () => {
    const text = this.props.maybeAnswer ?
      this.props.maybeAnswer.answerText : '';
    this.setState({
      answerText: text
    });
  };
  handleChange = (e) => {
    this.setState({
      answerText: e.target.value
    });
  };
  openForm = () => {
    this.resetState();
    this.setState({
      isOpen: true
    })
  };
  closeForm = () => {
    this.setState({
      isOpen: false
    })
  };
  saveEntity = () => {
    const payload = {
      'questionId': this.props.questionId,
      'answerText' : this.state.answerText
    };
    if (this.props.maybeAnswer != null) {
      payload.answerId = this.props.maybeAnswer.answerId;
      axios.post('/api/updateAnswer', payload).
      then(this.afterAnswerUpdated);
    } else {
      axios.post('/api/createAnswer', payload).
      then(this.afterAnswerUpdated);
    }
  };
  afterAnswerUpdated = (res) => {
    if (res.status === 200) {
      this.resetState();
      this.closeForm();
    }
  };
  render = () => {
    const editButtonDisabled = this.state.answerText.length === 0;
    const saveAnswerButtonText = this.props.maybeAnswer ?
      'Update answer' : 'Create answer';
    return <Modal
      isOpen={this.state.isOpen}
      onRequestClose={this.closeForm}
      style={answerEditStyle}
      contentLabel="Edit answer">
      <div className="answer-edit-form">
        <h2>{saveAnswerButtonText}</h2>
        <div className="form-group">
          <textarea rows="10" name="text" onChange={this.handleChange}
                    placeholder="Enter text of your answer here"
                    value={this.state.answerText} className="form-control" />
        </div>
        <hr />
        <div className="form-group answer-edit-form__button-container">
          <button disabled={editButtonDisabled}
                  className="btn btn-primary save-button"
                  onClick={this.saveEntity}
                  type="button">{saveAnswerButtonText}</button>
        </div>
      </div>
    </Modal>;
  }
}

export default EditAnswerForm;