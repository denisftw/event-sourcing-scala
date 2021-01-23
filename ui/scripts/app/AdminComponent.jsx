import React from 'react';
import ReactDOM from 'react-dom';
import TimeTravelDashboard from './views/TimeTravelDashboard.jsx';

class AdminComponent {
  init = () => {
    const reactDiv = document.getElementById('adminReactDiv');
    if (reactDiv !== null) {
      this.renderComponent(reactDiv);
    }
  };
  renderComponent = (reactDiv) => {
    ReactDOM.render(<TimeTravelDashboard />, reactDiv);
  }
}

export default AdminComponent;