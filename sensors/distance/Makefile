SRC_FILES := $(wildcard ./*.py)

reset:
	rshell "repl ~ import machine ~ machine.soft_reset() ~"
upload: $(SRC_FILES)
	rshell "cp ./* /pyboard/"
	rshell "cp ../common/* /pyboard/"
deploy: upload reset
