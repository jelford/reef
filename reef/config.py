location = None
settings = {}

def getSettings(type):
    global settings
    if type not in settings:
        settings[type] = {}
    return settings[type]

def hasSettings(type):
    global settings
    return type in settings

def getSections():
    global settings
    return settings.keys()

def setConfig(cfgpath):
# Create new config here or load existing
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
    global location, settings
    if location:
        try:
            with open(location, 'w') as cfg:
                import pickle
                pickle.dump(settings, cfg)
        except IOError:
            print "Couldn't save config...whoopsy"

def onDisk():
    global location
    try:
        with open(location, 'r') as test : pass
    except IOError:
        return False
    return True
