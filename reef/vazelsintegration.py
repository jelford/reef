"""
:synopsis: Interface between our setup and vazels.

"""

import controlcentre
import commandclient
import config
import os
import tarfile
from threading import Timer

def setupWhenReady(delay=3):
    """
    Wait until the control centre is running and set up all the stored configuration.

    :param delay: Seconds to wait between checking the control centre. (Optional).
    :type delay: ``int``

    """

    state  = controlcentre.runningState()

    # Only do something if it's starting or running.
    if state is True:
        _setupNow()
    elif state is None: # Starting
        Timer(interval=delay, function=setupWhenReady, args=[delay]).start()


def _setupNow():
    """Set up all vazels configuration we have here on the control centre."""

    _applyWorkloadsAndActors()


def _applyWorkloadsAndActors():
    """Apply all workloads to the groups and extract actors in the right places."""

    workloads = config.getSettings("workloads")["defs"]
    groups = config.getSettings("groups")

    # Scan through each group and apply its workloads/actors in turn
    for group in groups:
        grp_num = groups[group]["group_number"]
        
        actors = set()
        for wkld in groups[group]["workloads"]:
            _applyWorkload(workloads[wkld]["file"], grp_num)
            actors.update(workloads[wkld]["actors"])
        _extractActors(grp_num, actors)

        sue_components = set()
        for sueComp in groups[group]["sue_components"]:
            sue_components.update(sueComp)
        _extractSueComponents(grp_num, sue_components)
  
def _applyWorkload(workload, group_number):
    """
    Apply a workload to a specific group.

    :param workload: The absolute file path to the workload.
    :type workload: ``str``
    :param group_number: The group number to add to.
    :type group_number: ``int``

    """

    commandclient.issueControlCentreCommand(
        "addworkload",
        args=[workload],
        extraargs={
            "--group_rank": str(group_number)
        }
    )


def _extractActors(group_number, actors):
    """
    Extract actors into group directories.

    :param group_number: The number of the group to extract into.
    :type group_number: ``int``
    :param actors: The set of actors to extract.
    :type actors: ``set(str)``

    """

    experiment_dir = controlcentre.getExperimentPath()

    for actor_name in actors:
        actor = config.getSettings('actors')['defs'][actor_name]
        type=actor['type'].upper()

        extractpath = os.path.join(
            controlcentre.getExperimentPath(),
            "Group_"+str(group_number)
        )

        actorTGZ = tarfile.open(actor['file'],'r:gz')
        extractpath = os.path.join(extractpath, "Vazels", type+"_launcher")

        actorTGZ.extractall(path=extractpath)

def _extractSueComponents(group_number, sueComps):
    """
    Extract SUE components into the appropriate directories.
    
    :param group_number: The number of the group to extract into.
    :type group_number: ``int``
    :param sueComps: The set of names of Sue Componenets to extract.
    :type sueComps: ``set(str)``
    
    """
    
    experiment_dir = config.getSettings('command_centre')['experiment_dir']
    
    for sueComp_name in sueComps:
        sueCompFile = config.getSettings('SUE')['defs'][sueComp_name]['file']
    
        extractpath = os.path.join(
            controlcentre.getExperimentPath(),
            "Group_"+str(group_number),
            "SUE"
        )
        
        sueCompTGZ = tarfile.open(sueCompFile,'r:gz')
        sueCompTGZ.extractall(path=extractpath)
