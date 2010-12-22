"""
:synopsis: Serves probe files.

"""

import restlite
import authentication
import tarfile
import config
import os
import tempfile
import commandclient
import controlcentre

from handler.dochandler import DocHandler

class ProbeHandler(DocHandler):
    """Handles creation and serving of the probe archive."""

    def GET(self, request):
        """
        Get the tgz archive of the probe folder.

        The filename of the resulting file will be taken by the browser
        from the location this handler was attached to.

        .. todo:: Work out why FF cannot open this fil directly (without
                  saving it first)

        .. todo:: Allow this file through the testing proxy.

        :raises: :exc:`restlite.Status` 400 if the experiment is not set up.

                 :exc:`restlite.Status` 500 if the folder could not be served.

        """

        authentication.login(request)
        if commandclient.vazelsPhase() != commandclient.Statuses["STATUS_RUNNING"]:
            raise restlite.Status, "400 Can't get the Probe until the experiment is set up"

        probePath = os.path.join(controlcentre.getExperimentPath(), "Probe_Folder")
    
        with tempfile.SpooledTemporaryFile(1024) as temp:
            tar = tarfile.open(fileobj=temp, mode="w:gz")
            tar.add(name=probePath, arcname="Probe_Folder")
            tar.close()

            temp.seek(0)

            # Don't have this in the return statement
            # It will break EVERYTHING mysteriously
            content = temp.read()

            return request.response(content, "application/x-gzip")

        raise restlite.Status, "500 Could Not Serve Probe File"

