import sys
msg = sys.stdin.read().strip()
if 'kotlin' in msg.lower():
    print('updated build configuration and java source files')
else:
    print(msg)
