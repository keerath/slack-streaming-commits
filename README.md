# slack-streaming-commits

This repo consists of a spark job which is resposible for processing a stream of messages from slack. It creates a daily 
report as to who did not commit and saves them as text files in the repo directory. It uses a config file which contains
the auth token for slack bot. 
