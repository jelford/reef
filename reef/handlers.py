"""
:synopsis: Supplies reef server routing table.

This is used by the server when setting up. All it does it provide a
nice place for us to add new handlers without delving through a pile of code
to get there.

"""

import config

from handler.settings import SettingsEditor
from handler.testing import TestHandler
from handler.auth_page import AuthPageHandler
from handler.workloads import WorkloadHandler
from handler.actors import ActorsHandler
from handler.actorassign import ActorAssignHandler
from handler.control_handler import ControlHandler, StartHandler, StopHandler, GetAllStatusHandler, GetAllOutputHandler, StartExperimentHandler
from handler.groups import GroupHandler, GroupBatchHandler
from handler.status import StatusHandler
from handler.output_full import OutputFullHandler
from handler.probe import probe_handler

'''
This module is responsible for routing the different possible paths to their
associated handlers.
'''
def getRouting():
    """
    Get the routing table for the server.

    This will be in the format used in :func:`restlite.router()`.

    :returns: A routing table to be used by the reef backend.
    :rtype: ``list`` of ``tuple`` as in :func:`restlite.router()`.

    """

    return [
        #(r'GET,POST /settings$', 'GET,POST /settings/'),
        #(r'GET,POST /settings/', SettingsEditor().getHandler()),
        (r'GET /status$', StatusHandler().getHandler()),
        (r'GET /probe.tar.gz$', probe_handler),
        (r'GET,POST /actors$', 'GET,POST /actors/'),
        (r'GET,POST /actors/', ActorsHandler().getHandler()),
        (r'POST /actorassign$', 'POST /actorassign/'),
        (r'POST /actorassign/', ActorAssignHandler().getHandler()),
        (r'GET,POST /testing/', TestHandler().getHandler()),
        (r'GET,POST /groups/?$', GroupBatchHandler().getHandler()),
        (r'GET,POST /groups/', GroupHandler().getHandler()),
        (r'POST /control/start/?$', StartHandler().getHandler()),
        (r'POST /control/stop/?$', StopHandler().getHandler()),
        (r'GET /control/?$', ControlHandler().getHandler()),
        (r'GET /control/getallstatus/?$', GetAllStatusHandler().getHandler()),
        (r'GET /control/getalloutput/?$', GetAllOutputHandler().getHandler()),
        (r'GET /control/startexperiment/?$', StartExperimentHandler().getHandler()),
        (r'GET,POST /workloads/?', WorkloadHandler().getHandler()),
        (r'GET /output/$', OutputFullHandler().getHandler()),
        (r'GET,POST /', AuthPageHandler().getHandler()),
    ]


