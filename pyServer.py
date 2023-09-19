#!/usr/bin/env python

import sys
import http.server
from http.server import SimpleHTTPRequestHandler
import webbrowser    
class MyHTTPRequestHandler(http.server.SimpleHTTPRequestHandler):
    def end_headers(self):
        self.send_my_headers()
        http.server.SimpleHTTPRequestHandler.end_headers(self)

    def send_my_headers(self):
        self.send_header("Cache-Control", "no-cache, no-store, must-revalidate")
        self.send_header("Pragma", "no-cache")
        self.send_header("Expires", "0")


if __name__ == '__main__':
    http.server.test(HandlerClass=MyHTTPRequestHandler, port=89)


   

   
    
  
    # webbrowser.get('windows-default').open_new(urL)