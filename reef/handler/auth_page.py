import restlite
import config
import authentication
import os

### Serves authorised pages from global.pagedir ###

@restlite.resource
def auth_page_handler():
    def GET(request):
        authentication.login(request)

        if '..' in request['PATH_INFO']:
            raise restlite.Status, '400 Invalid Path'

        page_file = os.path.join(
            config.getSettings("global")["basedir"],
            config.getSettings("server")["pagedir"],
            request['PATH_INFO']
        )
        try:
            return request.response(getPage(page_file), getType(page_file))
        except restlite.Status:
            index_page = os.path.join(page_file, 'index.html')
            return request.response(getPage(index_page), 'text/html')

    return locals()


# Tries to get the contents of a page at path
# Otherwise throws a status 404
def getPage(path):
    try:
       with open(path, 'r') as page:
            return page.read()
    except IOError:
       raise restlite.Status, "404 Not Found"


# Try to get the filetype from the file extension
def getType(path):
    extSplit = path.rsplit('.', 1)
    ext = extSplit[len(extSplit)-1]
    try:
        return {
            'html' : 'text/html',
            'htm' : 'text/html',
            'css' : 'text/css',
            'js' : 'text/javascript',
        }[ext]
    except KeyError:
        return 'text/plain'

