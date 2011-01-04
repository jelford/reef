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

@restlite.resource
def probe_handler():
  def GET(request):
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
  
  return locals()
