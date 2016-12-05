import React from 'react';

class MyComponent extends React.Component {
  render = () => {
    const name = this.props.name;
    return <h1>Hello {name}</h1>
  }
}

export default MyComponent;