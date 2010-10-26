import restlite
import config
import permanent_bits as pb

theserver = None

class ReefServer:
    def __init__(self):
        self.authmodel = restlite.AuthModel()
        self.routes = []
        config.getSettings("server").setdefault("user", pb.appname_file)
        config.getSettings("server").setdefault("passkey", "")
        config.getSettings("server").setdefault("port", 8000)
        # Paths from base dir
        config.getSettings("server").setdefault("pagedir", "./pages/")

    def setup(self):
        self.authmodel.register(
            config.getSettings("server")["user"],
            'localhost',
            config.getSettings("server")["passkey"]
        )

        self.getRoutes()

    def start(self):
        from wsgiref.simple_server import make_server
        httpd = make_server(
            '',
            config.getSettings("server")["port"],
            restlite.router(self.routes)
        )

        try:
            httpd.serve_forever()
        except KeyboardInterrupt:
            pass

    def getRoutes(self):
    # Set up request handling and routing
        @restlite.resource
        def generic_page_handler():
        # Serves 404 not found 
            def GET(request):
                #self.authmodel.login(request)
                #return request.response(('path', request['PATH_INFO'], request['SCRIPT_NAME']))
                raise restlite.Status, "404 Not Found"
            return locals()

        @restlite.resource
        def auth_page_handler():
        # Serves authorised pages from global.pagedir
            import os
            def GET(request):
                self.authmodel.login(request)
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

        @restlite.resource
        def settings_editor():
        # Edit config settings through /settings
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
            def GET(request):
                self.authmodel.login(request)
                section = getSection(request['PATH_INFO'])
                if section:
                    if config.hasSettings(section):
                        return request.response(config.getSettings(section))
                    else:
                        raise restlite.Status, "404 Not Found"
                else:
                    return request.response(config.getSections())

            def POST(request, entity):
                self.authmodel.login(request)
                section = getSection(request['PATH_INFO'])
                #if not section:
                #    raise restlite.Status, '405 Method Not Allowed'
                # Grab arguments
                import urlparse
                qs = urlparse.parse_qs(entity, True)
                new_qs = {}
                for key in qs:
                # qs is a dict
                    name, func = splitName(key)
                    try:
                        nq = map(func, qs[key])
                    except:
                        raise restlite.Status, "405 Invalid Value"
                    if len(nq) == 1:
                        nq = nq[0]
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


        self.routes = [
            (r'GET,POST /settings$', 'GET,POST /settings/'),
            (r'GET,POST /settings/', settings_editor),
            (r'GET,POST /', auth_page_handler)
        ]

def getServer():
    global theserver
    if not theserver:
        theserver = ReefServer()
    return theserver
