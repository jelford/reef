import restlite
import code

### Brings up a console on the server to inspect the request ###

@restlite.resource
def test_handler():
    def GET(request):
        return openConsole(request)
    def POST(request, entity):
        return openConsole(request)
    return locals()

def openConsole(request):
    code.InteractiveConsole(locals()).interact()
    return request.response('', 'text/plain')
    
