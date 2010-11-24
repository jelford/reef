import restlite
import config
import permanent_bits as pb

theserver = None

class ReefServer:
    def __init__(self):
        self.authmodel = None
        self.routes = []
        config.getSettings("server").setdefault("port", 8000)
        # Paths from base dir
        config.getSettings("server").setdefault("pagedir", "./pages/")

    def setup(self):
        import handlers
        self.routes = handlers.getRouting()

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


def getServer():
    global theserver
    if not theserver:
        theserver = ReefServer()
    return theserver
