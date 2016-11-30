import React from 'react';
import ReactDOM from 'react-dom';
import axios from 'axios';


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
      return Promise.reject(error);
    });
  };
  renderComponent = () => {
    const reactDiv = document.getElementById('reactDiv');
    if (!!reactDiv) {
      ReactDOM.render(
        <div className="view-home-composite__react-panel__welcome-text">
          If you can see this message then
          the React-based part of the application was successfully connected
          to the Play-based part.
          <br/>
          Now try logging in/logging out using the navigation links on the top.
          The default user has credentials <code>user@example.com</code> and <code>password123</code>.
          <br/>
          You can also register a new user using the <a href="/signup">Sing Up form</a>.
        </div>, reactDiv);
    }
  }
}

export default AppComponent;
