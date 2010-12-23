"""
:synopsis: Simple persistent storage for the reef backend.

This is (possibly) maldesigned as it can only be used once in any application.

"""

location = None
settings = {}

def getSettings(type, create=True):
    """
    Grab the settings ``dict`` for the given type.

    :param type: The section (or type) of settings to grab.
    :type type: ``str``
    :param create: If ``type`` does not exist do we create it or explode?
    :type create: ``bool``
    :returns: A dictionary containing the settings for the given section.
    :raises: ``KeyError`` If the type does not exist and is not set to be created.
    """

    global settings
    if type not in settings:
        if create:
            settings[type] = {}
        else:
            return None
    return settings[type]

def hasSettings(type):
    """
    Check if the given section exists in the settings.

    :param type: The settings section.
    :type type: ``str``
    :returns: ``True`` if and only if the section already exists.

    """

    global settings
    return type in settings

def getSections():
    """
    Grab a list of currently available settings sections.

    :returns: A list of settings sections.

    """

    global settings
    return settings.keys()

def setConfig(cfgpath):
    """
    Either load a config from the given path or create a new one there.

    After running this method the path will be saved for other functions
    like :func:`saveConfig()`.

    :param cfgpath: The path to the config file.
    :type cfgpath: ``str``
    """

    global location, settings
    location = cfgpath
    try:
        with open(location, 'r') as cfg:
            import pickle
            settings = pickle.load(cfg)
    except IOError:
        print "Creating new config"
        settings = {}

def saveConfig():
    """
    Save the config file if a location has been set with :func:`setConfig()`.

    """

    global location, settings
    if location:
        try:
            with open(location, 'w') as cfg:
                import pickle
                pickle.dump(settings, cfg)
        except IOError:
            print "Couldn't save config...whoopsy"

def onDisk():
    """
    Check if the config is currently on the disk.

    This is for example if we have used :func:`setConfig()` to set a file
    but do not know whether it was there before or not.

    :returns: ``True`` if and only if a location has been set and a file 
              exists there.

    """

    global location
    try:
        with open(location, 'r') as test : pass
    except IOError:
        return False
    return True
