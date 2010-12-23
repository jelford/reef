import restlite
import authentication
import tarfile
import config
import os
from tempfile import mkstemp
from vazelsmanager import vazelsRunning, Statuses

@restlite.resource
def probe_handler():
  def GET(request):
    authentication.login(request)
    if vazelsRunning() != Statuses["STATUS_RUNNING"]:
      raise restlite.Status, "400 Can't get the Probe until the experiment is set up"
    
    tempFile = mkstemp(prefix="probe",suffix=".tgz")[1]
    probePath = os.path.join(config.getSettings("command_centre")["experiment_dir"], "Probe_Folder")
    probeFullPath = os.path.join(os.getcwd(),probePath)
    tar = tarfile.open(tempFile, "w:gz")
    for file in [os.path.join(probeFullPath, x) for x in os.listdir(probeFullPath)]:
      tar.add(file)
      print file
    with open(tempFile, "rb") as file:
      return request.response(file.read(), "application/x-gtar")
  
  return locals()