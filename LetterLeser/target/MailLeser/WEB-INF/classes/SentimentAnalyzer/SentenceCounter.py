from nltk.tokenize import sent_tokenize
import sys

email = sys.argv[1]
sentList = sent_tokenize(email, "english")
print("{0}".format(len(sentList)))
