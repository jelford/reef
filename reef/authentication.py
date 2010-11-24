import restlite
import config

config.getSettings("auth").setdefault("users", {})

# the model cannot be stored in the config, but cache it here
authModel = None

# Add user to the config and update the model
def addUser(name, password):
    global authModel
    users = config.getSettings("auth")["users"]

    if name in users:
        raise KeyError, str(name)+"already exists"

    users[name] = password
    if authModel is None:
        refresh()
    else:
        addUserToModel(name, password)

# Refresh the model
def refresh():
    global authModel
    authModel = restlite.AuthModel()

    users = config.getSettings("auth")["users"]
    for user in users:
        addUserToModel(user, users[user])


# Add user to the current model
def addUserToModel(user, password):
    global authModel
    authModel.register(
        user,
        'localhost',
        password
    )


# Get (and possibly create) the current model
def getModel():
    global authModel
    if authModel is None:
        refresh()
    return authModel


# Login with the given request
def login(request):
    request.login(getModel())
