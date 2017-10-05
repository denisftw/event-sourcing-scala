import React from 'react';
import axios from 'axios';
import NotificationService from '../util/notification-service.js';
import DateTime from 'react-datetime';
import moment from 'moment';

class TimeTravelDashboard extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      destination: new Date()
    };
  };
  rewind = () => {
    const destination = this.state.destination;
    const destFmt = moment(destination).toISOString();
    console.log('Formatted date', destFmt);
    axios.post("/api/rewind", { "destination" : destFmt }).then(() => {
      NotificationService.showMessage({
        messageType: "info",
        messageText: "The system was rebuilt up to the specified point"
      })
    })
  };
  destinationChanged = (momentDate) => {
    this.setState({
      destination: momentDate.toDate()
    })
  };
  render = () => {
    return <div className="time-travel-dashboard">
      <div className="time-travel-dashboard__prompt">
        Select the destination date in the field below and click "Rewind".
        The system will be restored up to this point in time.
      </div>
      <div className="time-travel-dashboard__date-selector">
        <div className="time-travel-dashboard__date-selector__input">
          <DateTime dateFormat="YYYY-MM-DD" timeFormat="HH:mm:ss"
                    onChange={this.destinationChanged}
                    value={this.state.destination} />
        </div>
      </div>
      <div className="time-travel-dashboard__button-panel">
        <button className="btn btn-primary"
                onClick={this.rewind}>Rewind</button>
      </div>
    </div>
  }
}

export default TimeTravelDashboard;
