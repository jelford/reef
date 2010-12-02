import vazelsmanager
import config
import os
import subprocess
import tarfile
from threading import Timer
from time import sleep

# Outside modules can call this to apply workloads to the control centre
def applyWorkloads(pause=3) :
  # Should be sure this isn't getting called out of turn!
  # Workloads must at least be initialized.
  #assert "defs" in workloads and "dir" in workloads
  
  # Dispatch it in a new thread with a slight delay as the control centre
  # probably isn't finished initializing yet.
  Timer(interval=1, function=__applyWorkloadsToControlCentre, args=[pause]).start()
  
def __applyWorkloadsToControlCentre(interval) :
  while(True): # Keep checking until we don't have to anymore
    runningState = vazelsmanager.vazelsRunning()
    
    #Don't use a dict/switch structure because we want to control flow
    if runningState is True:
      
      #### Apply the workload files themselves ####
      workloads = config.getSettings("workloads")
      workloadDefs = workloads["defs"]
      
      groups = config.getSettings("groups")
      sue_components = config.getSettings("SUE")
      # For each group, scan through its workloads & apply each one in turn
      for group in groups:
        print("Managing group: " + group)
        for wkld in groups[group]["workloads"]:
          __applyWorkload(workload_def=workloadDefs[wkld], target_group=groups[group])
          __extractActors(workload_def=workloadDefs[wkld], target_group=groups[group])
        for component in groups[group]["sue_components"]:
          __extractSueComponents(sue_component_def=sue_components[component],
                                 target_group=groups[group])

    elif runningState == "starting":
      # Go round again, after a quick pause
      print("Control Centre not yet started")
      sleep(interval)
      continue
    elif runningState == "timeout":
      # TODO: Do we really want to terminate it in this case? I'd say so.
      print("Doing ssh stuff...")
      sleep(interval)
      continue
    else:
      # Workload application has failed; the CCentre isn't running.
      print("Workload application failed; the control centre isn't running")
      print runningState
      
    break;
  print("Finished monitoring for startup")
  
def __applyWorkload(workload_def, target_group):
  
  args = ['/bin/sh', 'commandline_client.sh']
  args.append('--rmi_host='+config.getSettings('command_centre')['rmi_host'])
  args.append('--rmi_port='+config.getSettings('command_centre')['rmi_port'])
  args.append('--group_rank='+str(target_group['group_number']))
  args.append('addworkload')
  args.append(workload_def['file'])
  
  subprocess.Popen(args, cwd=vazelsmanager.getVazelsPath()+'/client')
  return
  

def __extractActors(workload_def, target_group):
  group_number = target_group['group_number']
  experiment_dir = config.getSettings("global")['expdir']
  
  for actor_name in workload_def['actors']:
    actor = config.getSettings('actors')['defs'][actor_name]
    type=actor['type'].upper()
    actorTGZ = tarfile.open(actor['file'],'r:gz')
    actorTGZ.extractall(path=experiment_dir+'/Group_'+str(group_number)+"/Vazels/"+type+"_launcher/")
  return

def __extractSueComponents(sue_component_def, target_group):
  print "Extracting SUE component"
  group_number = target_group['group_number']
  experiment_dir = config.getSettings("global")['expdir']
  
  sueTGZ = tarfile.open(sue_component_def['file'],'r:gz')
  sueTGZ.extractall(path=experiment_dir+'/Group_'+str(group_number)+'/SUE/')
  return