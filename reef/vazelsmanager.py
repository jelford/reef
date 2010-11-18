import config
import os
import asyncPopen as subprocess
from tempfile import mkstemp
import vazelsmonitor

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


''' DEFAULT SETTINGS FOR VAZELS - paths, certificates, ... '''
config.getSettings("command_centre").setdefault("rmi_port", "1099")
config.getSettings("command_centre").setdefault("rmi_host", "localhost")
config.getSettings("command_centre").setdefault("rmi", "-start_rmi")
config.getSettings("command_centre").setdefault("siena", "-start_siena")

def setupFifo():
  global vazels_control_stdout_WRITE
  global vazels_control_stdout_READ

  # Get a new secure temporary file to store the output from the control centre
  tmpFile = mkstemp()[1]

  # Assign these global objects.
  vazels_control_stdout_WRITE = open(tmpFile,"w")
  vazels_control_stdout_READ = open(tmpFile,"r")
  
def shutdownFifo():
  # Close the files we opened up for communications with the Control Centre
  # Put them in separate try/catch blocks because it one fails we still
  # want to try the other.
  try:
    vazels_control_stdout_WRITE.close()
  except AttributeError: # In the case that we haven't opened the file
    pass
  
  try:
    vazels_control_stdout_READ.close()
  except AttributeError:
    pass

def vazelsRunning():
  global vazels_control_process
  global vazels_control_stdout_READ
  
  # If we haven't yet started the control centre
  if vazels_control_process is None:
    print "Control centre not yet started"
    return False
  
  # If we have started the control centre, but it has terminated
  vazels_control_process.poll()
  if vazels_control_process.returncode != None :
    vazels_control_process = None # So we don't get problems when we start it again
    return False
  
  # If the control centre is not yet ready to receive commands. The only
  # way we have to tell is to look at the 13th line of output and compare it
  # to a string literal (in this case, just check it's there)
  cur_line = None
  vazels_control_stdout_READ.seek(0)
  for i in range(13):
    new_line = vazels_control_stdout_READ.readline()
    if new_line == "": break
    cur_line = new_line
    if cur_line.find("[OK]") != -1:
      return True
  
  if cur_line.find("SSH") != -1:
    return "timeout"
  
  return "starting"
  
def getVazelsPath():
  return os.path.join(config.getSettings("global")['basedir'],"vazels")
  
def runVazels():
  global vazels_control_process
  global vazels_control_stdout
  
  # Set up our FIFO file so we can monitor what's going on with the control centre
  setupFifo();
  
  experiment_path = os.path.join(config.getSettings("global")['projdir'], "experiment")
  
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
  
  # Return false if we know something's gone wrong already
  vazels_control_process.poll()
  return vazels_control_process.returncode == None
  
def stopVazels():
  global vazels_control_process
  
  experiment_path = os.path.join(config.getSettings("global")['projdir'], "experiment")

  args = ['/bin/sh', 'commandline_client.sh']
  args.append('--rmi_host='+config.getSettings('command_centre')['rmi_host'])
  args.append('--rmi_port='+config.getSettings('command_centre')['rmi_port'])
  args.append('stop')
  
  vazels_control_process = subprocess.Popen(args, cwd=os.path.join(getVazelsPath(),'client'))#, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
  
  shutdownFifo()
  
  # Oh dear this can't fail!
  vazels_control_process.poll()
  return True