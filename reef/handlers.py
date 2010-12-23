"""
:synopsis: Supplies reef server routing table.

This is used by the server when setting up. All it does it provide a
nice place for us to add new handlers without delving through a pile of code
to get there.

"""

import config

from handler.settings import settings_editor
from handler.testing import test_handler
from handler.auth_page import auth_page_handler
from handler.workloads import workload_handler
from handler.actors import actors_handler
from handler.actorassign import actorassign_handler
from handler.control_handler import control_handler, start_handler, stop_handler, getallstatus_handler, getalloutput_handler, startexperiment_handler
from handler.groups import group_handler, group_batch_handler
from handler.status import status_handler
from handler.output_full import output_full_handler
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
        #(r'GET,POST /settings/', settings_editor),
        (r'GET /status$', status_handler),
        (r'GET /probe.tar.gz$', probe_handler),
        (r'GET,POST /actors$', 'GET,POST /actors/'),
        (r'GET,POST /actors/', actors_handler),
        (r'POST /actorassign$', 'POST /actorassign/'),
        (r'POST /actorassign/', actorassign_handler),
        (r'GET,POST /testing/', test_handler),
        (r'GET,POST /groups/?$', group_batch_handler),
        (r'GET,POST /groups/', group_handler),
        (r'POST /control/start/?$', start_handler),
        (r'POST /control/stop/?$', stop_handler),
        (r'GET /control/?$', control_handler),
        (r'GET /control/getallstatus/?$', getallstatus_handler),
        (r'GET /control/getalloutput/?$', getalloutput_handler),
        (r'POST /control/startexperiment/?$', startexperiment_handler),
        (r'GET,POST /workloads/?', workload_handler),
        (r'GET /output/$', output_full_handler),
        (r'GET,POST /', auth_page_handler),
    ]


