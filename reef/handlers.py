import restlite
import config

authmodel = None

def setAuth(model):
    global authmodel
    authmodel = model

def getRouting():
    return [
        (r'GET,POST /settings$', 'GET,POST /settings/'),
        (r'GET,POST /settings/', settings_editor),
        (r'GET,POST /', auth_page_handler)
    ]


### Serves 404 not found ###

@restlite.resource
def generic_page_handler():
    global authmodel
    def GET(request):
        #if authmodel:
        #    authmodel.login(request)
        #return request.response(('path', request['PATH_INFO'], request['SCRIPT_NAME']))
        raise restlite.Status, "404 Not Found"
    return locals()



### Serves authorised pages from global.pagedir ###

@restlite.resource
def auth_page_handler():
    import os
    def GET(request):
        if authmodel:
            authmodel.login(request)
        if '..' in request['PATH_INFO']:
            raise restlite.Status, '400 Invalid Path'
        page_file = os.path.join(
            config.getSettings("global")["basedir"],
            config.getSettings("server")["pagedir"],
            request['PATH_INFO']
        )
        try:
            return request.response(getPage(page_file), 'text/html')
        except restlite.Status:
            index_page = os.path.join(page_file, 'index.html')
            return request.response(getPage(index_page), 'text/html')

    def getPage(path):
        try:
            with open(path, 'r') as page:
                return page.read()
        except IOError:
            raise restlite.Status, "404 Not Found"
    return locals()



### Allows Editing of config settings through /settings ###
#
# GET
# - /settings/ returns a list of settings types
# - /settings/section returns a json representation of this section of the settings
# POST
# - /settings/ gives error
# - /settings/section?name_s=value&name2_i=value2&...
#   Updates section with the names and values given,
#   Each name is appended with (possibly _ and)
#   a character specifying the type
#   - s : String
#   - i : Integer
#   - f : Float
# - If a name is given with no value, it is deleted
@restlite.resource
def settings_editor():
    def GET(request):
        if authmodel:
            authmodel.login(request)
        section = getSection(request['PATH_INFO'])
        if section:
            if config.hasSettings(section):
                return request.response(config.getSettings(section))
            else:
                raise restlite.Status, "404 Not Found"
        else:
            return request.response(config.getSections())

    def POST(request, entity):
        if authmodel:
            authmodel.login(request)
        section = getSection(request['PATH_INFO'])
        # Uncomment below to disallow editing of new sections
        #if not section:
        #    raise restlite.Status, '405 Method Not Allowed'

        # Grab arguments
        import urlparse
        qs = urlparse.parse_qs(entity, True)
        new_qs = {}
        for key in qs:
            name, func = splitName(key)
            try:
                # Convert all values for this key to correct format
                nq = map(func, qs[key])
            except:
                raise restlite.Status, "405 Invalid Value"
            if len(nq) == 1:
                nq = nq[0]
            if nq == '':
                # If theres a single empty value, delete the key
                del config.getSettings(section)[key]
            else:
                new_qs[name] = nq
        config.getSettings(section).update(new_qs)
        config.saveConfig()
        return request.response(config.getSettings(section))

    def splitName(name):
    # Returns type and conversion func
    # If name ends in _. where . is any character, return name, func (from .)
    # If there is no _ or . is invalid, use string as type
        fs = {
            's' : str,
            'i' : int,
            'f' : float,
        }

        l = len(name)
        if l > 1 and name[l-2] == '_':
            try:
                return name[:(l-2)], fs[name[l-1]]
            except KeyError:
                return name[:(l-2)], str
        else:
            return name, str

    def getSection(path):
        return path.split('/')[0]

    return locals()
