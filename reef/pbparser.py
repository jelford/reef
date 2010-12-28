import os
import sys
import vOutput_pb2

def scan_output(path_to_output):
    
    # Main entry point to scanner, this will return a dictionary with the
    # same hierarchy structure as Vazels outputs into the Output_Folder

    # if you do not provide the path to an Output_Folder, bad things might happen.

    #print (path_to_output)
    #if os.path.exists(path_to_output):
    #    result = scan_output_helper(path_to_output)
    #else:
    #    print "Folder provided did not exist"
    #    raise restlite.Status, "404 Output Folder Not Found"

    # Instead of how we did it before, let any exceptions spell failure.
    # This catches any problems with parsing too, although we should
    # probably look for specific failures
    try:
        return scan_output_helper(path_to_output)
    except:
        return None


def scan_output_helper(path):
    # this will recursively scan down through the directories looking for
    # the files at the bottom, continually building dictionaries as it goes
    # to store each layer in. Uses the current folder's name as the key for
    # the dictionary. this ensures keys will not clash

    file_paths = [os.path.join(path, name) for name in os.listdir(path)]

    '''
    If any of the files in this directory are files, then they all are,
    and they contain snapshots
    '''
    if not file_paths:
        return {}
    elif os.path.isfile(file_paths[0]) :
        snapshots_list = [process_file(name) for name in file_paths]
        snapshots_dict = {}
        # Join all the dictionaries together as one
        for snapshot in snapshots_list :
            snapshots_dict.update(snapshot)
        return snapshots_dict
    else:
        folder_dict = {}
        for name in file_paths:
            # Recurse through each folder building up a tree
            folder_dict[os.path.basename(name)] = scan_output_helper(name)
        return folder_dict

def process_file(filepath):

    # takes the path to a Buffered Protocol file and returns a dictionary
    # with usable data stored in the fields to it

    # prepares an object for us to read the protocol buffer
    time_serie = vOutput_pb2.TimeSerie()
    with open(filepath, "rb") as f:
        time_serie.ParseFromString(f.read())

    # protocol buffer is now all in memory.

    time_serie_dict = {}

    for snapshot in time_serie.snapshot:

        snapshot_dict = {}
        snapshot_dict['timestamp'] = snapshot.timestamp
        snapshot_dict['actor'] = snapshot.actor

        # AFAIK the data value has many different types but there can only
        # be one data. i guess i just need one field for this in python

        if snapshot.value.HasField('doubleValue'):
            snapshot_dict['type'] = "Double"
            snapshot_dict['value'] = snapshot.value.doubleValue
        elif snapshot.value.HasField('stringValue'):
            snapshot_dict['type'] = "String"
            snapshot_dict['value'] = snapshot.value.stringValue
        elif snapshot.value.HasField('customValue'):
            snapshot_dict['type'] = "Byte"
            snapshot_dict['value'] = snapshot.value.customValue

        # Index them by timestamp instead of order in file
        time_serie_dict[snapshot_dict['timestamp']] = snapshot_dict

    return time_serie_dict
