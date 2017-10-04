import React from 'react';
import { Link } from 'react-router';
import { connect } from 'react-redux';


class HomeComposite extends React.Component {
  refresh = () => {
    window.location.reload();
  };
  render = () => {
    const refreshPanel = this.props.refreshNeeded ?
      <div className="view-home-composite__refresh-panel"
           onClick={this.refresh}>
        Server data was updated. Click to refresh the page</div> : "";
    return <div>
      {refreshPanel}
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

const mapStateToProps = (state) => {
  return { refreshNeeded: state.refreshNeeded }
};

export default connect(mapStateToProps)(HomeComposite);