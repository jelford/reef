import config
import os


def getVazelsPath():
  return os.path.join(config.getSettings("global")['basedir'],"vazels")
  
def runVazels():
  experiment_path = os.path.join(config.getSettings("global")['projdir'], "experiment")
  
  groups_string = "--groups="
  group_data = config.getSettings("groups")
  groups_string += ':'.join([str(group)+','+str(group_data[group]['size']) for group in group_data])
  
  os.chdir(getVazelsPath())
  command_line = "vazels_control_centre.sh"
  command_line += " --root_dir="+experiment_path+"/"
  command_line += " "+groups_string
  command_line += " --ssh_server_identity=" + config.getSettings("global")['certificate']
  command_line += " " + config.getSettings("command_centre")["rmi"]
  command_line += " " + config.getSettings("command_centre")["siena"]
  
  # TODO: Change this so that it pipes its output to some handler!
  os_return_value = os.system("sh " + command_line + " &")
   
  if os_return_value == 0:
    return True
  else:
    return os_return_value
  
def stopVazels():
  experiment_path = os.path.join(config.getSettings("global")['projdir'], "experiment")
    
  os.chdir(os.path.join(getVazelsPath(),"client"))
  command_line =  "commandline_client.sh"
  command_line += " --rmi_host=" + config.getSettings("command_centre")['rmi_host']
  command_line += " --rmi_port=" + config.getSettings("command_centre")['rmi_port']
  command_line += " stop"
  
  # TODO: Change this so that it pipes its output to some handler!
  os_return_value = os.system("sh " + command_line)
   
  if os_return_value == 0:
    return True
  else:
    return os_return_value