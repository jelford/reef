"""
:synopsis: Supplies reef server routing table.

This is used by the server when setting up. All it does it provide a
nice place for us to add new handlers without delving through a pile of code
to get there.

"""

import config

from handler.settings import settings_editor
#from handler.testing import test_handler
from handler.auth_page import AuthPageHandler
from handler.workloads import workload_handler
from handler.actors import ActorsHandler
from handler.actorassign import ActorAssignHandler
from handler.control_handler import ControlHandler, StartHandler, StopHandler, GetAllStatusHandler, GetAllOutputHandler, StartExperimentHandler
from handler.groups import GroupHandler, GroupBatchHandler
from handler.status import status_handler
from handler.output_full import output_full_handler

def getRouting():
    """
    Get the routing table for the server.

    This will be in the format used in :func:`restlite.router()`.

    :returns: A routing table to be used by the reef backend.
    :rtype: ``list`` of ``tuple`` as in :func:`restlite.router()`.

    """

    return [
        #(r'GET,POST /settings$', 'GET,POST /settings/'),
        #(r'GET,POST /settings/', settings_editor),
        (r'GET /status$', status_handler),
        (r'GET,POST /actors$', 'GET,POST /actors/'),
        (r'GET,POST /actors/', ActorsHandler().getHandler()),
        (r'POST /actorassign$', 'POST /actorassign/'),
        (r'POST /actorassign/', ActorAssignHandler().getHandler()),
#        (r'GET,POST /testing/', test_handler),
        (r'GET,POST /groups/?$', GroupBatchHandler().getHandler()),
        (r'GET,POST /groups/', GroupHandler().getHandler()),
        (r'POST /control/start/?$', StartHandler().getHandler()),
        (r'POST /control/stop/?$', StopHandler().getHandler()),
        (r'GET /control/?$', ControlHandler().getHandler()),
        (r'GET /control/getallstatus/?$', GetAllStatusHandler().getHandler()),
        (r'GET /control/getalloutput/?$', GetAllOutputHandler().getHandler()),
        (r'GET /control/startexperiment/?$', StartExperimentHandler().getHandler()),
        (r'GET,POST /workloads/?', workload_handler),
        (r'GET /output/$', output_full_handler),
        (r'GET,POST /', AuthPageHandler().getHandler()),
    ]


