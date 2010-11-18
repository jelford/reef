import vazelsmanager
import config
import os
import subprocess
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
    if runningState is True:
      # TODO: Actually apply actors and workloads
      print("APPLYING WORKLOADS TO THE CONTROL CENTRE")
      
      #### Apply the workload files themselves ####
      workloads = config.getSettings("workloads")
      workloadDefs = workloads["defs"]
      
      groups = config.getSettings("groups")
      # For each group, scan through its workloads & apply each one in turn
      for group in groups:
        for wkld in groups[group]["workloads"]:
          print ("Adding workload(" + wkld + ") to group("+group+")")
          __applyWorkload(workload_def=workloadDefs[wkld], target_group=groups[group])
          __extractActors(workload_def=workloadDefs[wkld], target_group=groups[group])

    elif runningState == "starting":
      # Go round again, after a quick pause
      print("Control Centre not yet started")
      sleep(interval)
      continue
    elif runningState == "timeout":
      # TODO: Do we really want to terminate it in this case? I'd say so.
      print "Doing ssh stuff..."
      sleep(interval)
      continue
      vazelsmanager.vazels_control_process.terminate()
      print("TERMINATED CONTROL CENTRE BECAUSE OF A TIMEOUT")
    else:
      # Workload application has failed; the CCentre isn't running.
      print("Workload application failed; the control centre isn't running")
      print runningState
      
    break;
  print("Finished monitoring for startup")
  
def __applyWorkload(workload_def, target_group):
  wkld_dir = os.path.join(
        config.getSettings("global")["projdir"],
        config.getSettings("workloads")["dir"]
    )
  
  args = ['/bin/sh', 'commandline_client.sh']
  args.append('--rmi_host='+config.getSettings('command_centre')['rmi_host'])
  args.append('--rmi_port='+config.getSettings('command_centre')['rmi_port'])
  args.append('--group_rank='+str(target_group['group_number']))
  args.append('addworkload')
  args.append(workload_def['file'])
  
  subprocess.Popen(args, cwd=vazelsmanager.getVazelsPath()+'/client')

def __extractActors(workload_def, target_group):
  pass