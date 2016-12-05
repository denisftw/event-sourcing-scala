import React from 'react';

class TextComponent extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      phrase: ''
    }
  };
  handleInput = (event) => {
    this.setState({
      phrase: event.target.value
    })
  };
  render = () => {
    return <input type="text" value={this.state.phrase}
                  onChange={this.handleInput} />
  }
}

export default TextComponent;