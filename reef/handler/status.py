"""
:synopsis: Allows the client to assess whether or not the server is alive.

"""

import restlite
import authentication

from handler.dochandler import DocHandler

class StatusHandler(DocHandler):
    """Deals with server status requests."""

    def GET(self, request):
        """
        Check if the server is alive.

        :returns: Code \"200 OK\" if alive.

        Obviously if the server is down it will not respond.

        """

        authentication.login(request)
        return restlite.response("")
