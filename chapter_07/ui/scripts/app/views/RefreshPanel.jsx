import React from 'react';
import { connect } from 'react-redux';

class RefreshPanel extends React.Component {
  refresh = () => {
    window.location.reload();
  };
  render = () => {
    return this.props.refreshNeeded ?
      <div className="view-home-composite__refresh-panel"
           onClick={this.refresh}>
        Server data were updated. Click to refresh the page</div> : null;
  }
}

const mapStateToProps = (state) => {
  return { refreshNeeded: state.refreshNeeded }
};

export default connect(mapStateToProps)(RefreshPanel);