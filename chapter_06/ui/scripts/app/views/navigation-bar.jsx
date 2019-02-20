import React from 'react';
import { NavLink } from 'react-router-dom';

class NavigationBar extends React.Component {
  render = () => {
    return <div className="view-home-composite__side-menu-panel">
      <ul id="sideMenu" className="nav nav-stacked">
        <li><NavLink to="/tags">Tags</NavLink></li>
        <li><NavLink to="/ask">Ask</NavLink></li>
        <li><NavLink to="/questions">Questions</NavLink></li>
      </ul>
    </div>;
  }
}

export default NavigationBar;