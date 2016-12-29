import $ from 'jquery';
import AppComponent from './app-component.jsx';
import AdminComponent from './admin-component.jsx';

$(() => {
  const appComponent = new AppComponent();
  appComponent.init();
  const adminComponent = new AdminComponent();
  adminComponent.init();
});
