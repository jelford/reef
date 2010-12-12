import authentication
import restlite
import vazelsmanager

### Manage starting & stopping the server ###

@restlite.resource
def control_handler():
  def GET(request):
    authentication.login(request)
    runningState = vazelsmanager.vazelsRunning()
    if runningState is True:
      return request.response({"control_centre_status": "running"})
    elif runningState is False:
      return request.response({"control_centre_status": "ready"})
    else:
      return request.response({"control_centre_status": runningState})
  
  return locals()

@restlite.resource
def start_handler():
  def GET(request):
    authentication.login(request)
    raise restlite.Status("400 Invalid GET request to " + request["PATH_INFO"])
  
  def POST(request, entity):
    authentication.login(request)
      
    os_call_to_vazels = vazelsmanager.runVazels()
      
    if os_call_to_vazels :
      # Need to upadate client to handle 204 as a successful response
      return request.response("")
    else :
      raise restlite.Status("500 Vazels Control Centre failed to start")
    
  return locals()

@restlite.resource
def stop_handler():
  
  def POST(request,entity):
    authentication.login(request)
    
    os_call_to_vazels = vazelsmanager.stopVazels()
    
    if os_call_to_vazels is True :
      return request.response("")
    else :
      raise restlite.Status("500 "+str(os_call_to_vazels))
      
  return locals()
  
@restlite.resource
def getallstatus_handler():
  def POST(request, entity):
    authentication.login(request)
    
    if vazelsmanager.updateStatuses() is True:
      return request.response("")
    else :
      raise restlite.Status("500 Failed to send getallstatus request")
      
  return locals()
