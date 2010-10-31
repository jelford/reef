import restlite
import config
import authentication

### Collects data from OutputFolder and returns JSON object with data in ###
#
# GET to fetch data
#

@restlite.resource
def output_full_handler():
	global authmodel
	import parser
	import os
	import restlite
	## requests will return the entire data available
	
	def GET(request):
		path1 = config.getSettings("global")["projdir"]
		path = os.path.join(path1, "experiment/Output_Folder")
		return request.response(parser.scan_output(path))
		
	def POST(request,entity):
		pass
		
	return locals()
