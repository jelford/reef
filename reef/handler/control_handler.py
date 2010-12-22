"""
:synopsis: Creates an interface to the vazels system.

"""

import authentication
import restlite
import controlcentre
import commandclient
import vazelsintegration

from handler.dochandler import DocHandler

class ControlHandler(DocHandler):
    """Handles status requests for the vazels control centre."""

    def GET(self, request):
        """
        Get the status of the vazels control centre.

        :returns: The current control centre status, either

                  * ``"{'control_centre_status': 'running'}"``
                  * ``"{'control_centre_status': 'ready'}"``
                  * ``"{'control_centre_status': <somethingelse>}"``

                  where ``<somethingelse>`` is a return value of
                  :func:`vazelsmanager.vazelsRunning()`

        """

        authentication.login(request)
        return request.response({"control_centre_status": commandclient.vazelsPhase()})


class StartHandler(DocHandler):
    """Handles requests to start the vazels control centre."""

### Shouldn't need this at all ###
#    def GET(self, request):
#        authentication.login(request)
#        raise restlite.Status("400 Invalid GET request to " + request["PATH_INFO"])

    def POST(self, request, entity):
        """
        Start the control centre.

        :raises: :exc:`restlite.Status` 500 if something went immediately
                 wrong while starting the control centre.

        .. todo:: Update client to handle 204 as a successful response.
                  Then we dont have to send 200 with a blank response.

        """

        authentication.login(request)

        if not controlcentre.startVazels():
            raise restlite.Status("500 Vazels Control Centre failed to start")
    
        vazelsintegration.setupWhenReady()
        return request.response("")


class StopHandler(DocHandler):
    """Handles requests to stop the vazels control centre."""

    def POST(self, request, entity):
        """
        Stop the control centre.

        :raises: :exc:`restlite.Status` 500 if something went immediately
                 wrong while stopping the control centre.

        .. todo:: Update client to handle 204 as a successful response.
                  Then we dont have to send 200 with a blank response.

        """

        authentication.login(request)

        if commandclient.stopVazels():
            return request.response("")
        else :
            raise restlite.Status("500 Cannot Stop Non Running Control Centre")


class GetAllStatusHandler(DocHandler):
    """Handles status requests for hosts."""

    def GET(self, request):
        """
        Update the groups with host statuses.

        .. todo:: Make this POST!
        .. todo:: Allow 204 as success in the client.

        :raises: :exc:`restlite.Status` 400 if we cannot initiate an attempt
                 to update.

        """

        authentication.login(request)

        if commandclient.updateStatuses():
            return request.response("")
        else:
            raise restlite.Status, "400 Failed to send getallstatus request - don't call this before starting the Control Center"


class GetAllOutputHandler(DocHandler):
    """Handles requests for vazels output."""

    def GET(self, request):
        """
        Grab the latest output from the vazels experiment.

        .. todo:: Make this POST!
        .. todo:: Allow 204 as success in the client.

        :raises: :exc:`restlite.Status` 400 if we could not start to
                 retrieve data.

        """

        authentication.login(request)

        if commandclient.getalloutput():
            return request.response("")
        else:
            raise restlite.Status, "400 Failed to get output from Vazels - don't call this before starting the Control Center"


class StartExperimentHandler(DocHandler):
    """Handles requests to start a vazels experiment."""

    def POST(self, request, entity):
        """
        Try to start a vazels experiment.

        .. todo:: Make this POST!
        .. todo:: Allow 204 as success in the client.

        :raises: :exc:`restlite.Status` 400 if the attempt to start an
                 experiment failed immediately.

        """

        authentication.login(request)

        if commandclient.startexperiment():
            return request.response("")
        else:
            raise restlite.Status, "400 Failed to start experiment - don't call this before starting the Control Center"


