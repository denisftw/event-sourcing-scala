import swal from 'sweetalert';

export default {
  showConfirmationDialog: function(message, confirmationType, yesCallback) {
    swal({
      title: message.title,
      text: message.body,
      icon: confirmationType,
      buttons: ['Cancel', true],
    }).then(isConfirm => {
      if (isConfirm) {
        yesCallback();
      }
    });
  }
}