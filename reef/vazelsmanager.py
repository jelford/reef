import config
import os


def getVazelsPath():
  return os.path.join(config.getSettings("global")['basedir'],"vazels")
  
def runVazels():
  experiment_path = os.path.join(config.getSettings("global")['projdir'], "experiment")
  groups_string=""
    
    
  os.chdir(getVazelsPath())
  command_line = "vazels_control_centre.sh"
  command_line += " --root_dir="+experiment_path+"/"
  command_line += " --groups="+groups_string
  command_line += " --ssh_server_identity=" + config.getSettings("global")['certificate']
  command_line += " -start_siena -start_rmi"
  
  
  os_return_value = os.system(command_line)
   
  if os_return_value == 0:
    return True
  else:
    return os_return_value