import os

totalFile = open("Android.txt", "w")

def countFileCoutn(file):
	srcFile = open(file);
	for line in srcFile.readlines():
		totalFile.write(line)
	srcFile.close()

def countDirFileCount(dirFile):
	dirs = os.listdir(dirFile)
	for name in dirs:
		childDir = dirFile + "/" + name
		if os.path.isdir(childDir):
			countDirFileCount(childDir)
		else:
			ext = os.path.splitext(childDir)
			if ext[1] == ".java":
				print "read "+ dirFile
				countFileCoutn(childDir)

countDirFileCount("edusoho_v3/src")