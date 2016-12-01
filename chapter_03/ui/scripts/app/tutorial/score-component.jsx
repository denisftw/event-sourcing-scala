import React from 'react';

class ScoreComponent extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      score: 0
    }
  };
  increase = () => {
    this.setState({
      score: this.state.score + 1
    })
  };
  render = () => {
    const score = this.state.score;
    return <h1 onClick={this.increase}>Score: {score}</h1>
  }
}

export default ScoreComponent;