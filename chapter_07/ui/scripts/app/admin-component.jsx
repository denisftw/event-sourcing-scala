import React from 'react';
import ReactDOM from 'react-dom';
import TimeTravelDashboard from './views/time-travel-dashboard.jsx'

class AdminComponent {
  init = () => {
    const reactDiv = document.getElementById('adminReactDiv');
    if (!!reactDiv) {
      this.renderComponent(reactDiv);
    }
  };
  renderComponent = (reactDiv) => {
    ReactDOM.render(<TimeTravelDashboard />, reactDiv);
  }
}

export default AdminComponent;