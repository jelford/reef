"""
:synopsis: Request handlers for testing the system.

"""

import code

from handler.dochandler import DocHandler

### Brings up a console on the server to inspect the request ###

class TestHandler(DocHandler):
    """Catches requests and allows us to dissect them easily."""

    def GET(self, request):
        """See :func:`openConsole`"""

        return self.openConsole(request)

    def POST(self, request, entity):
        """See :func:`openConsole`"""

        return self.openConsole(request, entity)

    def openConsole(self, request, entity=None):
        """
        Catches a request and opens up a console on the server to inspect it.

        Once the console is open, the request data (including the query string)
        will be stored in the variable ``request``. If this was a POST request
        then the entity will be stored in a variable called ``entity``.

        Beware, a response will not be sent until the console is closed (use
        CTRL+D). More worryingly, the server cannot process any other request
        until this happens. This would make for an awfully simple DoS attack so
        **don't** leave this available when deploying.

        """

        loc = {"request": request}
        if entity is not None:
            loc["entity"] = entity
        code.InteractiveConsole(loc).interact()
        return request.response('', 'text/plain')
    
