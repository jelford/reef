"""
:synopsis: Replaces the :mod:`restlite` usage message.

"""

import restlite

from handler.dochandler import DocHandler

class GenericPageHandler(DocHandler):
    """
    Used to replace :mod:`restlite`'s usage message when a request is not handled.

    Instead, if inserted to handle pages at "/", this will return 404 for
    **any** GET request.

    """

    def GET(self, request):
        """
        Return a 404 error.

        :raise: :exc:`restlite.Status` 404...always!

        """

        #return request.response(('path', request['PATH_INFO'], request['SCRIPT_NAME']))
        raise restlite.Status, "404 Not Found"
