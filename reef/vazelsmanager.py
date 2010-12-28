import config
import os
import asyncPopen as subprocess
from tempfile import mkstemp
import vazelsmonitor
from handler.groups import getGroupFromRank
from threading import Timer
from time import sleep

# When we start processes, we get a handle on them and can use that to
# terminate, check for termination, get return codes, ...

# The command-line client
vazels_command_process = None

# The control centre itself
vazels_control_process = None

# We'll use this to communicate with the vazels control centre; we need
# a read and write handle on a file. Think of it like a buffered IO queue.
vazels_control_stdout_WRITE = None
vazels_control_stdout_READ = None

# Similar to previous, we need to know what's going on when we use the
# commandline client. Most of the time these should be empty, since we
# only need it for a little bit at a time.
vazels_command_stdout_WRITE = None
vazels_command_stdout_READ = None

# Experiment state - use the to know whether the experiment has started.
# Valid values: None, "started"
experiment_running_state = None


''' DEFAULT SETTINGS FOR VAZELS - paths, certificates, ... '''
config.getSettings("command_centre").setdefault("vazels_dir", "vazels")
config.getSettings("command_centre").setdefault("experiment_dir", "experiment")
config.getSettings("command_centre").setdefault("rmi_port", "1099")
config.getSettings("command_centre").setdefault("rmi_host", "localhost")
config.getSettings("command_centre").setdefault("rmi", "-start_rmi")
config.getSettings("command_centre").setdefault("siena", "-start_siena")

''' Some constants for reflecting state info '''
Statuses = {"STATUS_RUNNING": "running",
            "STATUS_READY": "ready",
            "STATUS_EXPERIMENT_STARTED" : "started",
            "STATUS_FINISHED" : "finished"}

def setupFiles():
  global vazels_control_stdout_WRITE
  global vazels_control_stdout_READ

  # Get a new secure temporary file to store the output from the control centre
  tmpFile = mkstemp()[1]

  # Assign these global objects.
  vazels_control_stdout_WRITE = open(tmpFile,"w")
  vazels_control_stdout_READ = open(tmpFile,"r")
   
def shutdownFiles():
  global vazels_control_stdout_WRITE
  global vazels_control_stdout_READ
  # Close the files we opened up for communications with the Control Centre
  # Put them in separate try/catch blocks because it one fails we still
  # want to try the other. Two possible errors: the variables are set to None,
  # or the variables have not yet been assigned at all. In either case it's
  # fine to fail silently.
  try:
    vazels_control_stdout_WRITE.close()
  except AttributeError: pass
  
  try:
    vazels_control_stdout_READ.close()    
  except AttributeError: pass
  
  # In any case we want to set them to empty afterwards.
  vazels_control_stdout_WRITE = None
  vazels_control_stdout_READ = None

def vazelsRunning():
  global vazels_control_process
  global vazels_control_stdout_READ
  global experiment_running_state
  global Statuses
  
  # If we haven't yet started the control centre
  if vazels_control_process is None:
    return Statuses["STATUS_READY"]
  
  # If we have started the control centre, but it has terminated
  vazels_control_process.poll()
  if vazels_control_process.returncode != None or vazels_control_stdout_READ is None:
    vazels_control_process = None # So we don't get problems when we start it again
    experiment_running_state = None # Likewise
    return Statuses["STATUS_READY"]
  
  # Check whether the experiment has already started
  if experiment_running_state == Statuses["STATUS_EXPERIMENT_STARTED"] :
    return Statuses["STATUS_EXPERIMENT_STARTED"]
  
  # Read through what the control centre's told us & get the state from that
  # (if it is not any of the above cases it is either running or starting)
  vazels_control_stdout_READ.seek(0)
  cur_line = None
  state = "starting"
  while cur_line != "": #Read through the whole thing
    cur_line = vazels_control_stdout_READ.readline()
    if cur_line.find("[OK]") != -1:
      state = Statuses["STATUS_RUNNING"]
      break # We don't care about the rest here.

  return state
  
def getVazelsPath():
  return os.path.join(
    config.getSettings("global")['basedir'],
    config.getSettings("command_centre")["vazels_dir"],
  )

def getCommandLineClientPath():
  return os.path.join(getVazelsPath(), 'client')

def getExperimentPath():
  return os.path.join(
    config.getSettings("global")['projdir'],
    config.getSettings("command_centre")["experiment_dir"]
  )
  
  
def runVazels():
  global vazels_control_process
  global vazels_control_stdout
  
  # Set up our FIFO file so we can monitor what's going on with the control centre
  setupFiles();
  
  experiment_path = getExperimentPath()
  
  groups_string = "--groups="
  group_data = config.getSettings("groups")
  
  # Work out the physical group numbers. Can't do this earlier as they can
  # change all the way up to the point when we start the server.
  physical_group_number = 1
  for group in group_data :
    group_data[group]['group_number'] = physical_group_number
    physical_group_number += 1
  
  groups_string += ':'.join([str(group)+','+str(group_data[group]['size']) for group in group_data])
  
  args = ['/bin/sh', 'vazels_control_centre.sh']
  args.append('--root_dir='+experiment_path+'/')
  args.append(groups_string)
  args.append('--ssh_server_identity='+config.getSettings('security')['certificate'])
  args.append(config.getSettings('command_centre')['rmi'])
  args.append(config.getSettings('command_centre')['siena'])
  
  vazels_control_process = subprocess.Popen(args, cwd=getVazelsPath(), stdout=vazels_control_stdout_WRITE)
  
  # Dispatch a thread to monitor the control centre and give it workloads/actors
  # when it's ready.
  vazelsmonitor.applyWorkloads()
  
  # Return false if we know something's gone wrong already
  vazels_control_process.poll()
  return vazels_control_process.returncode == None
  
def stopVazels():
  __issueControlCentreCommand('stop')
  experiment_running_state = None
  
  shutdownFiles()
  
  # Oh dear this can't fail! Simply communicate to the client that we're trying.
  return True

def startexperiment():
  global experiment_running_state
  global Statuses
  experiment_running_state = Statuses["STATUS_EXPERIMENT_STARTED"]
  return __issueControlCentreCommand('start')
  
def getalloutput():
  return __issueControlCentreCommand('getalloutput')
  
def updateStatuses():
  # Doesn't make sense to call this before the control centre is running
  if vazelsRunning() is not True:
    return False
  
  # Need a little more control here than in __issueControlCentreCommand()
  global vazels_command_process
  global vazels_command_stdout_WRITE
  global vazels_command_stdout_READ
  experiment_path = getExperimentPath()
  args = __getCommandLineClientArgs()
  
  args.append('getallstatus')
  
  tmpFile = mkstemp()[1]
  vazels_command_stdout_WRITE = open(tmpFile,"w")
  vazels_command_stdout_READ = open(tmpFile,"r")
  
  vazels_command_process = subprocess.Popen(args, cwd=getCommandLineClientPath(), stdout=vazels_command_stdout_WRITE, stderr=vazels_command_stdout_WRITE)
  
  # Dispatch a new thread to catch the output for us. It could take a
  #   little while for stuff to come back.
  Timer(interval=1, function=__readStatusReturnValue, args=[]).start()
  
  # TODO: Address reasons this might fail?
  return True
  
def __readStatusReturnValue():
  global vazels_command_process
  global vazels_command_stdout_WRITE
  global vazels_command_stdout_READ
  FIRST_GROUP_RANK = 1
  
  outputStarted = False
  while(not outputStarted):
    vazels_command_stdout_READ.seek(0)
    lastLine = vazels_command_stdout_READ.readline()
    if lastLine != "" :
      outputStarted = True
    sleep(2)
  
  # Seek the first line of the output where we talk about group status
  while(lastLine.find("group rank "+str(FIRST_GROUP_RANK)) == -1):
    lastLine = vazels_command_stdout_READ.readline()
  
  # TODO: Tidy this up and use string manipulation instead of brittle counting!
  # for now, easier just to count the group numbers rather than doing string manipulation
  current_group_rank = FIRST_GROUP_RANK
  
  def __host_free(group, host_name):
    pass # TODO: Maybe do something more intelligent with this in the future?
  def __host_evolving(group, host_name):
    group['evolving_hosts'].add(host_name)
  def __host_connected(group, host_name):
    group['online_hosts'].add(host_name)
    
  statusActionSwitch =  {'FREE' : __host_free,
                          'CONNECTED' : __host_connected,
                          'EVOLVING' : __host_evolving}
  
  
  while (lastLine != "") :
    current_group = getGroupFromRank(current_group_rank)
    
    for host in range(current_group['size']):
      status_text = lastLine.split('-->')[-1].strip()
      
      # Statuses are of the form 'STATUS @ hostname', so split this up.
      status, host_name = (lambda l: (l[0], l[-1]))(status_text.split(' '))
      
      try:
        statusActionSwitch[status](current_group, host_name)
      except KeyError:
        raise restlite.Status, "500 Something went seriously wrong trying to find status of hosts!"
      
      lastLine = vazels_command_stdout_READ.readline()
    
    current_group_rank += 1
    
  vazels_command_stdout_READ.close()
  vazels_command_stdout_WRITE.close()
  vazels_command_process = None
  
def __getCommandLineClientArgs():
  return ['/bin/sh',
                 'commandline_client.sh',
                 '--rmi_host='+config.getSettings('command_centre')['rmi_host'],
                 '--rmi_port='+config.getSettings('command_centre')['rmi_port']
                 ]

def __issueControlCentreCommand(command):
  if vazelsRunning() is not True:
    return False
  
  global vazels_command_process
  experiment_path = getExperimentPath()
  args = __getCommandLineClientArgs()
  args.append(command)
  vazels_command_process = subprocess.Popen(args, cwd=getCommandLineClientPath())
  
  return True
  
