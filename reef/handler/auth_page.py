"""
:synopsis: Handles basic authenticated pages requests.

"""

import restlite
import config
import authentication
import os

from handler.dochandler import DocHandler

MIMETYPES = {
    'html' : 'text/html',
    'htm' : 'text/html',
    'css' : 'text/css',
    'js' : 'text/javascript',
}
"""A dictionary matching file extensions to mimetypes."""

class AuthPageHandler(DocHandler):
    """
    Handles normal page requests and requires authentication from the
    requester.

    Authentication is dealt with by the :mod:`authentication` module.
    """

    def GET(self, request):
        """
        Get a particular page.

        The handler will take anything after the base address and look for it
        in the pages folder. This folder is stored in the :mod:`config` module
        at::

            config.getSettings("server")["pagedir"]

        If a file is not found the handler will assume the address is a
        directory and try to serve the file index.html from it.

        :returns: The requested file.
        :raises: :exc:`restlite.Status` 400 if the path contains ".."

                 :exc:`restlite.Status` 404 if the file is not found.

        """

        authentication.login(request)

        if '..' in request['PATH_INFO']:
            raise restlite.Status, '400 Invalid Path'

        page_file = os.path.join(
            config.getSettings("global")["basedir"],
            config.getSettings("server")["pagedir"],
            request['PATH_INFO']
        )
        try:
            return request.response(self.getPage(page_file), self.getType(page_file))
        except restlite.Status:
            index_page = os.path.join(page_file, 'index.html')
            return request.response(self.getPage(index_page), 'text/html')


    def getPage(self, path):
        """
        Try to get the contents of a text file at ``path``.

        :param path: The absolute path of the file to read.
        :type path: ``str``
        :returns: The contents of the file.
        :rtype: ``str``
        :raises: :exc:`restlite.Status` 404 if the file is not found.

        """

        try:
            with open(path, 'r') as page:
                return page.read()
        except IOError:
            raise restlite.Status, "404 Not Found"


    def getType(self, path):
        """
        Work out the mimetype of the file given.

        This is looked up in :data:`MIMETYPES` but defaults to ``"text/plain"``

        :param path: The path of the file. (We really only need the filename)
        :type path: ``str``
        :returns: The mimetype of the file.
        :rtype: ``str``

        """

        extSplit = path.rsplit('.', 1)
        ext = extSplit[len(extSplit)-1]
        try:
          return MIMETYPES[ext]
        except KeyError:
            return 'text/plain'

