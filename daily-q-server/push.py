import firebase_admin
from firebase_admin import messaging


def send(device_tokens, data=None, notification=None):
    try:
        app = firebase_admin.get_app()
        if not app:
            return

        device_count = len(device_tokens)
        if device_count == 1:
            message = messaging.Message(
                token=device_tokens[0],
                data=data,
                notification=notification
            )
            response = messaging.send(message)
        elif device_count > 1:
            message = messaging.MulticastMessage(
                tokens=device_tokens,
                data=data,
                notification=notification
            )
            response = messaging.send_multicast(message)
    except Exception as e:
        print(e)
