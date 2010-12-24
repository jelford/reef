"""
:synopsis: Provides parsed experiment data through Rest.

"""

import restlite
import config
import authentication
import pbparser
import os
import vazelsmanager

from handler.dochandler import DocHandler

class OutputFullHandler(DocHandler):
    """Handles requests for experiment output data."""

    def GET(self, request):
        """
        Grab the experiment output data.

        :returns: JSON representation of the experiment output data.
        :rtype: ``json``
        :raises: :exc:`restlite.Status` 500 if the output could not be parsed.

        """

        authentication.login(request)

        # We want the proper experiment path that vazelsmanager tells us
        exp_dir = vazelsmanager.getExperimentPath()
        path = os.path.join(exp_dir,"Output_Folder")
        parsed = pbparser.scan_output(path)
        # If the parsing broke in any way we get back None
        if parsed is None:
            raise restlite.Status, "500 Could Not Read Output Data"
        return request.response(parsed)

