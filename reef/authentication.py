import restlite
import config

config.getSettings("auth").setdefault("users", {})

# the model cannot be stored in the config, but cache it here
# This should always match the users dict (None is empty)
authModel = None

# Add user to the config and update the model
def addUser(name, password):
    global authModel
    users = config.getSettings("auth")["users"]

    if name in users:
        raise KeyError, str(name)+" already exists"

    users[name] = password
    # Don't need a complete refresh to add one user
    if authModel is None:
        refresh()
    else:
        addUserToModel(name, password)

# Refresh the model
def refresh():
    global authModel

    users = config.getSettings("auth")["users"]
    if not users.keys():
        authModel = None
    else:
        authModel = restlite.AuthModel()
        for user in users:
            addUserToModel(user, users[user])


# Add user to the current model (assuming it exists)
def addUserToModel(user, password):
    global authModel
    authModel.register(
        user,
        'localhost',
        password
    )


# Login with the given request
def login(request):
    global authModel
    # If there are not users, don't ask for login
    if authModel is not None:
        request.login(model)


# Clear all authentication (removes users and turns auth off)
def clear():
    config.getSettings("auth")["users"] = {}
    refresh()


# Set up here
refresh()
