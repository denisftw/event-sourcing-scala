import toastr from 'toastr';

export default {
  showMessage: function (message) {
    if (message.messageType == 'error') {
      toastr.error(message.messageText, 'Error occurred');
    } else {
      toastr.info(message.messageText, 'Information');
    }
  }
}