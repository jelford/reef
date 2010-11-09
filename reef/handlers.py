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

def getRouting():
    return [
        #(r'GET,POST /settings$', 'GET,POST /settings/'),
        #(r'GET,POST /settings/', settings_editor),
        (r'GET /status$', status_handler),
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
        (r'GET /control/startexperiment/?$', startexperiment_handler),
        (r'GET,POST /workloads/?', workload_handler),
        (r'GET,POST /', auth_page_handler),
        (r'GET,POST /output/$', output_full_handler),
    ]

def output_full_handler():
	global authmodel
	import parser
	import os
	## requests will return the entire data available
	
	def GET(request):
		path1 = config.getSettings("global")["projdir"]
		path = os.path.join(path1, "Output_Folder")
		return request.response(parser.scan_output(path))
		
	def POST(request,entity):
		pass
		
	return locals()
