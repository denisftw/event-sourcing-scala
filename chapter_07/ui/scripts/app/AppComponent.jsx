import React from 'react';
import ReactDOM from 'react-dom';
import axios from 'axios';
import TagManager from './views/TagManager.jsx';
import { createStore } from 'redux';
import { Provider } from 'react-redux';
import { BrowserRouter, Route, Switch, Redirect } from 'react-router-dom';
import NavigationBar from './views/NavigationBar.jsx';
import AskQuestionView from './views/AskQuestionView.jsx';
import NotificationService from './util/NotificationService.js';
import QuestionComposite from './views/QuestionComposite.jsx';
import RefreshPanel from './views/RefreshPanel.jsx';

class AppComponent {
  init = () => {
    const reactDiv = document.getElementById('reactDiv');
    if (reactDiv !== null) {
      this.initLoginRedirecting();
      this.connectToWSEndpoint();
      this.initAppState();
      // this.connectToSSEEndpoint();
      this.renderComponent(reactDiv);
    }
  };
  connectToWSEndpoint = () => {
    this.streamWS = new WebSocket("ws://localhost:9000/api/wsStream");
    this.streamWS.onmessage = this.onServerSideEvent;
  };
  connectToSSEEndpoint = () => {
    this.es = new EventSource("/api/sse");
    this.es.addEventListener("message", this.onServerSideEvent);
  };
  onServerSideEvent = (event) => {
    if (event.type === 'message') {
      this.updateReceived(JSON.parse(event.data));
    }
  };
  updateReceived = (data) => {
    if (data['updateType'] === 'tags') {
      this.store.dispatch({
        type: 'tags_updated',
        data: data['updateData']
      });
    }
    else if (data['updateType'] === 'questions') {
      this.store.dispatch({
        type: 'questions_updated',
        data: data['updateData']
      });
    }
    else if (data['updateType'] === 'questionThread') {
      this.store.dispatch({
        type: 'question_thread_updated',
        data: data['updateData']
      });
    }
    else if (data['error'] != null) {
      NotificationService.showMessage({
        messageType: 'error',
        messageText: data['error']
      });
    } else if (data['updateType'] === 'stateRebuilt') {
      this.store.dispatch({
        type: 'state_rebuilt',
        data: true
      });
    }
  };
  useWS = (callback) => {
    if (this.streamWS.readyState === 1) {
      callback();
    } else {
      setTimeout(() => {
        this.useWS(callback);
      }, 1000);
    }
  };
  updateQuestionThreadId = (action) => {
    this.useWS(() => {
      if (action.type === 'question_thread_updated' ||
        action.type === 'question_thread_loaded') {
        const questionThreadId = action.data.question.id;
        this.streamWS.send(`{"questionThreadId": "${questionThreadId}"}`);
      }
    });
  };
  initAppState = () => {
    const initialState = {
      tags: [],
      questions: [],
      questionThread: {}
    };
    const reducer = (state = initialState, action) => {
      const updatedState = {...state};
      const actionType = action.type;

      if (actionType === 'tags_updated') {
        updatedState['tags'] = action.data;
      } else if (actionType === 'questions_updated') {
        updatedState['questions'] = action.data;
      } else if (actionType === 'question_thread_loaded') {
        updatedState['questionThread'] = action.data;
      } else if (actionType === 'question_thread_updated') {
        if (state['questionThread']['id'] === action.data['id']) {
          updatedState['questionThread'] = action.data;
        }
      } else if (actionType === 'state_rebuilt') {
        updatedState['refreshNeeded'] = true;
      }

      this.updateQuestionThreadId(action);

      return updatedState;
    };
    this.store = createStore(reducer);
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
  renderComponent = (reactDiv) => {
    ReactDOM.render(<Provider store={this.store}>
      <BrowserRouter>
        <div>
          <RefreshPanel />
          <div className="view-home-composite__main-panel">
            <NavigationBar />
            <div className="view-home-composite__content-panel">
              <Switch>
                <Route path="/tags" component={TagManager} />
                <Route path="/ask" component={AskQuestionView}  />
                <Route path="/questions" component={QuestionComposite} />
                <Redirect path="/" to="/ask" />
              </Switch>
            </div>
          </div>
        </div>
      </BrowserRouter>
    </Provider>, reactDiv);
  }
}

export default AppComponent;
