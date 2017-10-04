import React from 'react';
import { connect } from 'react-redux';
import axios from 'axios';
import Select from 'react-select';

class AskQuestionView extends React.Component {
  constructor(props) {
    super(props);
    this.state = this.getInitState();
  };
  getInitState = () => {
    return {
      tags: [],
      title: '',
      details: ''
    }
  };
  handleCombo = (value) => {
    this.setState({
      tags: value
    });
  };
  handleChange = (key) => {
    return event => {
      const newState = {};
      newState[key] = event.target.value;
      this.setState(newState);
    };
  };
  createQuestion = () => {
    const tagCodes = this.state.tags.map((opt) => {
      return opt.value;
    });
    const newQuestion = {
      title: this.state.title,
      tags: tagCodes,
      details: this.state.details
    };
    axios.post("/api/createQuestion", newQuestion).then((res) => {
      if (res.status == 200) {
        this.setState(this.getInitState());
        this.props.router.push('/questions');
      }
    })
  };
  handleResponse = (response) => {
    if (response.status == 200) {
      this.props.dispatch({
        type: 'tags_updated',
        data: response.data
      });
    } else {
      console.error(response.statusText);
    }
  };
  componentDidMount = () => {
    axios.get("/api/tags").then(this.handleResponse);
  };
  render = () => {
    const tagOptions = this.props.tags.map((tag) => {
      return {
        value: tag.id,
        label: tag.text
      };
    });
    const buttonDisabled = this.state.tags.length == 0 ||
      this.state.title.length == 0;
    return <div className="question-view-form">
      <div className="question-view-form__tag-panel">
        <Select className="question-view-form__tag-panel__select"
                value={this.state.tags} multi={true} clearable={true}
                options={tagOptions} placeholder="Please select tags for the question"
                onChange={this.handleCombo}/>
      </div>
      <div className="question-view-form__body-panel">
        <div className="question-view-form__body-panel__title-panel">
          <div className="question-view-form__body-panel__title-panel__input">
            <input type="text" className="form-control"
                   placeholder="Enter you question"
                   value={this.state.title} onChange={this.handleChange('title')} />
          </div>
          <div className="question-view-form__body-panel__title-panel__button">
            <button className="btn btn-primary" disabled={buttonDisabled}
                    onClick={this.createQuestion}>Submit</button>
          </div>
        </div>
        <div className="question-view-form__body-panel__details-panel">
          <textarea className="form-control" rows="3" value={this.state.details}
                    onChange={this.handleChange('details')}
                    placeholder="Optionally, enter additional details clarifying the question"/>
        </div>
      </div>
    </div>
  };
}

const mapStateToProps = (state) => {
  return {
    tags: state.tags,
  }
};

export default connect(mapStateToProps)(AskQuestionView)
