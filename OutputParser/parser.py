import string
import os
import sys

# all protocol buffer scripts are stored in the includes folder
# hopefully this should deal with the issue that lab machines don't
# have it installed

''' Oh good, some imperative code! This module provides methods to
	convert Vazles' output dumps into a python dictionary,
	from whence it can be readily converted to e.g. XML or JSON
	TODO: OO-ify this code (wrap it in a parser class)'''

sys.path.append('includes')
import protocolBuffer_pb2

def scan_output(path_to_output):
	# Main entry point to scanner, this will return a dictionary with the
	# same hierarchy structure as Vazels outputs into the Output_Folder
	
	# if you do not provide the path to an Output_Folder, bad things might happen.
	
	if os.path.exists(path_to_output):
		result = scan_output_helper(path_to_output)
	else:
		print "Folder provided did not exist"
		sys.exit(-1)

	return result
	
	
def scan_output_helper(path):
	# this will recursively scan down through the directories looking for
	# the files at the bottom, continually building dictionaries as it goes
	# to store each layer in. Uses the current folder's name as the key for
	# the dictionary. this ensures keys will not clash

	folder_dict = {}

	file_paths = [os.path.join(path, name) for name in os.listdir(path)]
	
	'''If any of the files in this directory are files, then they all are,
		and they contain snapshots'''
	if os.path.isfile(file_paths[0]) :
		snapshots_list = [process_file(name) for name in file_paths]
		snapshots_dict = {}
		# Join all the dictionaries together as one
		map(snapshots_dict.update,snapshots_list)
		return snapshots_dict
	else :
		for directory_path in file_paths:
			# Take the name of the directory from the end of the path
			path_split = directory_path.rsplit('/',1)
			directory_name = path_split[len(path_split)-1]
			# Recurse through each folder building up a tree
			folder_dict[directory_name] = scan_output_helper(directory_path)
		return folder_dict

def process_file(filepath):

	# takes the path to a Buffered Protocol file and returns a dictionary
	# with usable data stored in the fields to it

	# prepares an object for us to read the protocol buffer
	time_serie = protocolBuffer_pb2.TimeSerie()
	f = open(filepath, "rb")
	time_serie.ParseFromString(f.read())
	f.close()
	
	# protocol buffer is now all in memory.
	
	time_serie_dict = {}
	# since the snapshots don't keep track of how many are in the time_serie
	# we shall keep a counter, mainly so there is a key that we can fetch data
	# with, could consider using timestamp?
	
	i = 0
	for snapshot in time_serie.snapshot:
		
		snapshot_dict = {}
		snapshot_dict['timestamp'] = snapshot.timestamp
		snapshot_dict['actor'] = snapshot.actor
		
		# AFAIK the data value has many different types but there can only
		# be one data. i guess i just need one field for this in python
		
		if snapshot.value.HasField('doubleValue'):
			snapshot_dict['value'] = snapshot.value.doubleValue
		elif snapshot.value.HasField('stringValue'):
			snapshot_dict['value'] = snapshot.value.stringValue
		elif snapshot.value.HasField('customValue'):
			snapshot_dict['value'] = snapshot.value.customValue
		
		# Index them by timestamp instead of order in file
		time_serie_dict[snapshot_dict['timestamp']] = snapshot_dict
		# time_serie_dict[i] = snapshot_dict
		i += 1
		
	return time_serie_dict
