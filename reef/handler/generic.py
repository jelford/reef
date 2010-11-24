import restlite

### Serves 404 not found ###

@restlite.resource
def generic_page_handler():
    def GET(request):
        #return request.response(('path', request['PATH_INFO'], request['SCRIPT_NAME']))
        raise restlite.Status, "404 Not Found"
    return locals()
