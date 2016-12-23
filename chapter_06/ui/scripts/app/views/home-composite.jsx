import React from 'react';
import { Link } from 'react-router';

class HomeComposite extends React.Component {
  render = () => {
    return <div>
      <div className="view-home-composite__main-panel">
        <div className="view-home-composite__side-menu-panel">
          <ul id="sideMenu" className="nav nav-stacked">
            <li><Link to="/tags" activeClassName="active">Tags</Link></li>
            <li><Link to="/ask" activeClassName="active">Ask</Link></li>
            <li><Link to="/questions" activeClassName="active">Questions</Link></li>
          </ul>
        </div>
        <div className="view-home-composite__content-panel">
          {this.props.children}
        </div>
      </div>
    </div>
  }
}

export default HomeComposite