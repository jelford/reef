import authentication
import restlite
import controlcentre
import commandclient
import vazelsintegration

### Manage starting & stopping the server ###

@restlite.resource
def control_handler():
  def GET(request):
    authentication.login(request)
    return request.response({"control_centre_status": commandclient.vazelsPhase()})
  
  return locals()


@restlite.resource
def start_handler():
### Shouldn't need this at all ###
#  def GET(request):
#    authentication.login(request)
#    raise restlite.Status("400 Invalid GET request to " + request["PATH_INFO"])
  
  def POST(request, entity):
    authentication.login(request)
      
    if controlcentre.startVazels():
      # Need to upadate client to handle 204 as a successful response
      return request.response("")
    else :
      raise restlite.Status("500 Vazels Control Centre failed to start")
    
    vazelsintegration.setupWhenReady()

  return locals()


@restlite.resource
def stop_handler():
  
  def POST(request,entity):
    authentication.login(request)
    
    
    if commandclient.stopVazels():
      return request.response("")
    else :
      raise restlite.Status("500 Cannot Stop Non Running Control Centre")
      
  return locals()
  

@restlite.resource
def getallstatus_handler():
  def GET(request):
    authentication.login(request)
    
    if commandclient.updateStatuses():
      return request.response("")
    else :
      raise restlite.Status, "400 Failed to send getallstatus request - don't call this before starting the Control Center"
      
  return locals()


@restlite.resource
def getalloutput_handler():
  def GET(request):
    authentication.login(request)
    
    if commandclient.getalloutput():
      return request.response("")
    else :
      raise restlite.Status, "400 Failed to get output from Vazels - don't call this before starting the Control Center"
      
  return locals()
  

@restlite.resource
def startexperiment_handler():
  def POST(request, entity):
    authentication.login(request)
    
    if commandclient.startexperiment():
      return request.response("")
    else:
      raise restlite.Status, "400 Failed to start experiment - don't call this before starting the Control Center"
    
  return locals()
