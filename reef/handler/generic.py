import restlite
import handlers

### Serves 404 not found ###

@restlite.resource
def generic_page_handler():
    def GET(request):
        handlers.login(request)
        #return request.response(('path', request['PATH_INFO'], request['SCRIPT_NAME']))
        raise restlite.Status, "404 Not Found"
    return locals()
