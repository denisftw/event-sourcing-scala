import React from 'react';
import { NavLink } from 'react-router-dom';
import Modal from 'react-modal';

class NavigationBar extends React.Component {
  componentDidMount = () => {
    Modal.setAppElement('body');
  }
  render = () => {
    return <div className="view-home-composite__side-menu-panel">
      <div className="nav flex-column nav-pills">
        <NavLink className="nav-link" activeClassName="active" to="/tags">Tags</NavLink>
        <NavLink className="nav-link" activeClassName="active" to="/ask">Ask</NavLink>
        <NavLink className="nav-link" activeClassName="active" to="/questions">Questions</NavLink>
      </div>
    </div>;
  }
}

export default NavigationBar;