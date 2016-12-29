import swal from 'sweetalert';

export default {
  showConfirmationDialog: function(message, confirmationType, yesCallback) {
    swal({
      title: message.title,
      text: message.body,
      animation: false,
      type: confirmationType,
      showCancelButton: true,
      confirmButtonColor: "#DD6B55",
      confirmButtonText: 'OK',
      cancelButtonText: 'Cancel',
      closeOnConfirm: true,
      closeOnCancel: true
    }, function(isConfirm){
      if (isConfirm) {
        yesCallback();
      }
    });
  }
}