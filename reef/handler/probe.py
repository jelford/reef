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

@restlite.resource
def probe_handler():
  def GET(request):
    authentication.login(request)
    if commandclient.vazelsPhase() != commandclient.Statuses["STATUS_RUNNING"]:
      raise restlite.Status, "400 Can't get the Probe until the experiment is set up"
    
    tempFile = tempfile.SpooledTemporaryFile(1024)
    probePath = os.path.join(config.getSettings("command_centre")["experiment_dir"], "Probe_Folder")
    tar = tarfile.open(tempFile, "w:gz")
    tar.add(name=probePath, arcname="Probe_Folder")
    tar.close()
    tempFile.seek(0)
    with open(tempFile, "rb") as file:
      return request.response(file.read(), "application/x-gzip")

    raise restlite.Status, "500 Could Not Serve Probe File"
  
  return locals()
