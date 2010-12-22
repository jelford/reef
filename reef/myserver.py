"""
:synopsis: Quick and easy implementation of a :mod:`restlite`-based server.

This was implemented so that the server acts as a class but should only be
created with :func:`getServer()` so remains singleton.

"""

import restlite
import config
import permanent_bits as pb

theserver = None

class ReefServer:
    """
    Represents the server.

    This should perform setup and running operations with minimal input from
    elsewhere.
    """
    def __init__(self):
        """
        Create a basic server setup.

        This will set up the most basic possible settings for a working server.
        It also adds enough defaults in the :mod:`config` so that the server
        will run with no additional input.

        """

        self.authmodel = None
        self.routes = []
        config.getSettings("server").setdefault("port", 8000)
        # Paths from base dir
        config.getSettings("server").setdefault("pagedir", "./pages/")

    def setup(self):
        """
        Perform any setup needed to bend the server to our whims.

        This really only grabs the routing table from the :mod:`handlers`
        module.

        """

        import handlers
        self.routes = handlers.getRouting()

    def start(self):
        """
        Build the actual server and set it running.

        This will use the setup performed by any of the other class methods,
        it also takes into account any :mod:`config` values set.

        """

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


def getServer():
    """
    Grab the singleton server instance.

    :returns: A server instance
    :rtype: :class:`ReefServer`

    """

    global theserver
    if not theserver:
        theserver = ReefServer()
    return theserver
