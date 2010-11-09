import config
import os
import asyncPopen as subprocess
import time

vazels_command_process = None
vazels_control_process = None

''' DEFAULT SETTINGS FOR VAZELS - paths, certificates, ... '''

# TODO: Update this to generate a new ssh certificate at startup!
config.getSettings("global")["certificate"] = os.path.expanduser("~/.ssh/id_rsa")

config.getSettings("command_centre").setdefault("rmi_port", "1099")
config.getSettings("command_centre").setdefault("rmi_host", "localhost")
config.getSettings("command_centre").setdefault("rmi", "-start_rmi")
config.getSettings("command_centre").setdefault("siena", "-start_siena")


def vazelsRunning():
  global vazels_command_process
  vazels_command_process.poll()
  return vazels_command_process.returncode == None
  
def getVazelsPath():
  return os.path.join(config.getSettings("global")['basedir'],"vazels")
  
def runVazels():
  global vazels_command_process
  
  experiment_path = os.path.join(config.getSettings("global")['projdir'], "experiment")
  
  groups_string = "--groups="
  group_data = config.getSettings("groups")
  groups_string += ':'.join([str(group)+','+str(group_data[group]['size']) for group in group_data])
  
  os.chdir(getVazelsPath())
    
  args = ['/bin/sh', 'vazels_control_centre.sh']
  args.append('--root_dir='+experiment_path+'/')
  args.append(groups_string)
  args.append('--ssh_server_identity='+config.getSettings('global')['certificate'])
  args.append(config.getSettings('command_centre')['rmi'])
  args.append(config.getSettings('command_centre')['siena'])
  
  vazels_command_process = subprocess.Popen(args, stdout=subprocess.PIPE, stderr=subprocess.PIPE, cwd=getVazelsPath())
  
  # Oh dear this can only fail if the process dies!
  time.sleep(1)
  vazels_command_process.poll()
  return vazels_command_process.returncode == None
  
def stopVazels():
  global vazels_control_process
  
  experiment_path = os.path.join(config.getSettings("global")['projdir'], "experiment")
    
  os.chdir(os.path.join(getVazelsPath(),"client"))
  args = ['/bin/sh', 'command_line_client.sh']
  args.append('--rmi_host='+config.getSettings('command_centre')['rmi_host'])
  args.append('--rmi_port='+config.getSettings('command_centre')['rmi_port'])
  args.append('stop')
  
  vazels_control_process = subprocess.Popen(args, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
  
  # Oh dear this can't fail!
  vazels_control_process.poll()
  return True


def timeout(func, args=(), kwargs={}, timeout_duration=10, default=None):
    """This function will spawn a thread and run the given function
    using the args, kwargs and return the given default value if the
    timeout_duration is exceeded.
    """ 
    import threading
    class InterruptableThread(threading.Thread):
        def __init__(self):
            threading.Thread.__init__(self)
            self.result = default
        def run(self):
            self.result = func(*args, **kwargs)
    it = InterruptableThread()
    it.start()
    it.join(timeout_duration)
    if it.isAlive():
        return it.result
    else:
        return it.result
