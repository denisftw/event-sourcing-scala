import React from 'react';
import ReactDOM from 'react-dom';
import axios from 'axios';
import TagManager from './views/tag-manager.jsx';


class AppComponent {
  init = () => {
    this.initLoginRedirecting();
    this.renderComponent();
  };
  initLoginRedirecting = () => {
    axios.interceptors.response.use((response) => {
      return response;
    }, (error) => {
      if (error.response.status === 401) {
        window.location = '/login';
      }
      return error.response;
    });
  };
  renderComponent = () => {
    const reactDiv = document.getElementById('reactDiv');
    if (!!reactDiv) {
      ReactDOM.render(<TagManager />, reactDiv);
    }
  }
}

export default AppComponent;
