"""
:synopsis: Simple authentication management.

This allows any part of the server to add users or authenticate against
them without even thinking about it.

"""

import restlite
import config

config.getSettings("auth").setdefault("users", {})

authModel = None
"""
This is the authentication model used by the server.

We cannot store it with :mod:`config`.
Instead we store the information used to create it and cache
the finished thing here.

"""

def addUser(name, password):
    """
    Add a user to the current authentication model.

    This will also get update the configuration with the information
    needed to recreate itself.

    :param name: The name of the user to add.
    :type name: ``str``
    :param password: The password to use for this user.
    :type password: ``str``
    :raises: ``KeyError`` if the name exists

    """
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

def refresh():
    """
    Refresh the cached authentication model with the information stored
    in :mod:`config`.

    """
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
    """
    Add a user to the cached authentication model.

    .. note:: This function expects the authentication model to exist.
              i.e. it should not be ``None``!

    """

    global authModel
    authModel.register(
        user,
        'localhost',
        password
    )


def login(request):
    """
    Try to authenticate against the current model.

    This is usually called from a restlite handler.

    :param request: The request we are authenticating against.
    :type request: :class:`restlite.Request`

    """

    global authModel
    # If there are not users, don't ask for login
    if authModel is not None:
        authModel.login(request)


def clear():
    """
    Clear all authentication data.

    This removes all saved users and turn authentication off so that
    :func:`login` will do nothing.

    """

    config.getSettings("auth")["users"] = {}
    refresh()


def active():
    """
    Check if authentication is currently active.

    :returns: ``True`` only if authentication is currently active.
    :rtype: ``bool``
    """
    return authModel is not None


# Set up here
refresh()
